package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.DiscordAutomationService
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordYellow

@Composable
fun AccessibilityStatusBanner(
    isServiceConnected: Boolean,
    isSettingsEnabled: Boolean,
    onRefreshStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (isServiceConnected) {
        // Connected & Active State
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = DiscordGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = DiscordGreen.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp)
                .testTag("accessibility_status_banner_connected"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Connected",
                tint = DiscordGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Accessibility Engine Active",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = DiscordGreen
                )
                Text(
                    text = "Ready to detect Discord chat input and simulate Send",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            IconButton(
                onClick = onRefreshStatus,
                modifier = Modifier.size(36.dp).testTag("refresh_status_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh status",
                    tint = DiscordGreen
                )
            }
        }
    } else {
        // Disabled / Setup Required State
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = DiscordYellow.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(16.dp)
                .testTag("accessibility_status_banner_disabled")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Accessibility Required",
                    tint = DiscordYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Accessibility Service Required",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Android requires Accessibility permission so AutoSender can locate the Discord message bar and simulate the Enter/Send action automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        try {
                            context.startActivity(DiscordAutomationService.openAccessibilitySettingsIntent())
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("enable_accessibility_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enable in Settings", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onRefreshStatus,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("verify_service_status_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check", fontSize = 13.sp)
                }
            }
        }
    }
}
