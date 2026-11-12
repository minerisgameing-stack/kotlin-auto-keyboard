package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class InjectionResult(
    val success: Boolean,
    val message: String
)

class DiscordAutomationService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        AutomationLogManager.success("AutoSender Accessibility Service connected and active")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Optional tracking of window changes
    }

    override fun onInterrupt() {
        AutomationLogManager.warning("AutoSender Accessibility Service was interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
        _isServiceActive.value = false
        AutomationLogManager.warning("AutoSender Accessibility Service stopped")
    }

    /**
     * Injects text into the Discord text input area and triggers Enter / Send.
     */
    fun injectMessageAndSend(
        text: String,
        targetPackage: String = "com.discord",
        sendEnter: Boolean = true
    ): InjectionResult {
        val rootNode = rootInActiveWindow
            ?: return InjectionResult(
                success = false,
                message = "Cannot access active window. Ensure Discord is open on screen."
            )

        // Check if package matches (if active window is available)
        val currentPackage = rootNode.packageName?.toString() ?: ""
        if (targetPackage.isNotBlank() && currentPackage.isNotBlank() && !currentPackage.contains(targetPackage, ignoreCase = true)) {
            // Also allow generic package or warn
            AutomationLogManager.info("Active window package is '$currentPackage'. Searching for input field...")
        }

        // 1. Locate editable text input node
        val inputNode = findInputNode(rootNode)
        if (inputNode == null) {
            return InjectionResult(
                success = false,
                message = "Could not find text input area in Discord. Ensure the chat channel is open."
            )
        }

        AutomationLogManager.info("Found input field (${inputNode.className}). Injecting message...")

        // 2. Set text into the input node
        var textSet = false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }

        inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        if (inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
            textSet = true
        } else {
            // Fallback: Copy to clipboard and paste
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AutoSender", text)
                clipboard.setPrimaryClip(clip)
                inputNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                textSet = true
            } catch (e: Exception) {
                AutomationLogManager.error("Clipboard paste fallback failed: ${e.message}")
            }
        }

        if (!textSet) {
            return InjectionResult(
                success = false,
                message = "Failed to inject text into input field"
            )
        }

        // Small wait before sending (let UI update and send button appear)
        Thread.sleep(150)

        // 3. Simulate immediate Enter / Send button click
        if (sendEnter) {
            // Refresh hierarchy to locate the newly activated Send button
            val refreshedRoot = rootInActiveWindow ?: rootNode
            val sendClicked = findAndClickSendButton(refreshedRoot)

            if (sendClicked) {
                AutomationLogManager.success("Message injected and Send button clicked successfully!")
                return InjectionResult(success = true, message = "Message dispatched via Send button")
            } else {
                // Try IME Enter or Enter action on the input node
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val imeResult = inputNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
                    if (imeResult) {
                        AutomationLogManager.success("Message dispatched via IME Enter action!")
                        return InjectionResult(success = true, message = "Dispatched via IME Enter")
                    }
                }

                // If send button wasn't found by standard labels, search clickable siblings
                val parentNode = inputNode.parent
                if (parentNode != null) {
                    val siblingClicked = clickFirstClickableSibling(parentNode, inputNode)
                    if (siblingClicked) {
                        AutomationLogManager.success("Message dispatched via adjacent action button!")
                        return InjectionResult(success = true, message = "Dispatched via send button")
                    }
                }

                AutomationLogManager.warning("Text placed in input, but send button was not detected automatically.")
                return InjectionResult(
                    success = true,
                    message = "Text inserted into chat input (Send button manual press may be required)"
                )
            }
        }

        return InjectionResult(success = true, message = "Text injected into input field")
    }

    /**
     * Recursively traverses accessibility node tree to find an editable input.
     */
    private fun findInputNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            val isEditText = current.className?.toString()?.contains("EditText", ignoreCase = true) == true
            val isEditable = current.isEditable
            val viewId = current.viewIdResourceName?.lowercase() ?: ""

            if (isEditText || isEditable || viewId.contains("chat_input") || viewId.contains("text_input") || viewId.contains("input_edit_text")) {
                return current
            }

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }
        return null
    }

    /**
     * Recursively locates and clicks the Discord Send button.
     */
    private fun findAndClickSendButton(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            val contentDesc = current.contentDescription?.toString()?.trim() ?: ""
            val text = current.text?.toString()?.trim() ?: ""
            val viewId = current.viewIdResourceName?.lowercase() ?: ""

            val isSendDescription = contentDesc.equals("Send", ignoreCase = true) ||
                    contentDesc.contains("send message", ignoreCase = true)
            val isSendText = text.equals("Send", ignoreCase = true)
            val isSendId = viewId.contains("send_button") ||
                    viewId.contains("chat_input_send_button") ||
                    viewId.contains("button_send") ||
                    viewId.endsWith("send")

            if ((isSendDescription || isSendText || isSendId) && (current.isClickable || current.isEnabled)) {
                if (current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
                // Try parent if button wrapper is clickable
                current.parent?.let { parent ->
                    if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                }
            }

            for (i in 0 until current.childCount) {
                current.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    private fun clickFirstClickableSibling(parent: AccessibilityNodeInfo, target: AccessibilityNodeInfo): Boolean {
        for (i in 0 until parent.childCount) {
            val child = parent.getChild(i) ?: continue
            if (child != target && child.isClickable) {
                val desc = child.contentDescription?.toString() ?: ""
                val id = child.viewIdResourceName ?: ""
                if (!desc.contains("emoji", ignoreCase = true) && !desc.contains("gift", ignoreCase = true) && !id.contains("attachment")) {
                    if (child.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    companion object {
        var instance: DiscordAutomationService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        /**
         * Checks if our Accessibility Service is enabled in Android System Settings.
         */
        fun isAccessibilitySettingsEnabled(context: Context): Boolean {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return false

            val expectedServiceName = "${context.packageName}/${DiscordAutomationService::class.java.name}"
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

            for (service in enabledServices) {
                val serviceId = service.id
                if (serviceId.contains(context.packageName) && serviceId.contains(DiscordAutomationService::class.java.simpleName)) {
                    return true
                }
            }

            // Fallback: check secure settings string
            try {
                val settingValue = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: ""
                return settingValue.contains(expectedServiceName) || settingValue.contains(DiscordAutomationService::class.java.simpleName)
            } catch (_: Exception) {
                return false
            }
        }

        fun openAccessibilitySettingsIntent(): Intent {
            return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
