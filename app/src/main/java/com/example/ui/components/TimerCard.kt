package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AutomationSettings
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordGreen
import com.example.ui.theme.DiscordRed

@Composable
fun TimerCard(
    isAutomationRunning: Boolean,
    countdownRemainingSeconds: Long,
    totalNextDelaySeconds: Long,
    settings: AutomationSettings,
    lastSentMessage: String?,
    totalSentSessionCount: Int,
    onToggleAutomation: (Boolean) -> Unit,
    onSendSingleNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalNextDelaySeconds > 0) {
        (totalNextDelaySeconds - countdownRemainingSeconds).toFloat() / totalNextDelaySeconds.toFloat()
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")

    val cardBorderColor by animateColorAsState(
        targetValue = if (isAutomationRunning) DiscordGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        label = "card_border"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
            .padding(18.dp)
            .testTag("timer_card")
    ) {
        // Header: Title & Master Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isAutomationRunning) DiscordGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAutomationRunning) Icons.Default.Bolt else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (isAutomationRunning) DiscordGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Automation Engine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isAutomationRunning) "RUNNING (Random Timer)" else "STOPPED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isAutomationRunning) DiscordGreen else DiscordRed
                    )
                }
            }

            // Master Toggle Switch
            Switch(
                checked = isAutomationRunning,
                onCheckedChange = onToggleAutomation,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = DiscordGreen,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("automation_master_switch")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Countdown & Interval Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Timer Range",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${settings.minTime}s ~ ${settings.maxTime}s (${settings.timeUnit.label})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sent this session: $totalSentSessionCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // Circular Countdown Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(68.dp)
            ) {
                CircularProgressIndicator(
                    progress = { if (isAutomationRunning) animatedProgress else 0f },
                    modifier = Modifier.size(68.dp),
                    color = if (isAutomationRunning) DiscordGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    strokeWidth = 5.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isAutomationRunning) {
                        Text(
                            text = "${countdownRemainingSeconds}s",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "OFF",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Last Sent Message Preview
        AnimatedVisibility(visible = lastSentMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Last Sent Message:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "\"$lastSentMessage\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onToggleAutomation(!isAutomationRunning) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAutomationRunning) DiscordRed else DiscordGreen
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("toggle_automation_button")
            ) {
                Icon(
                    imageVector = if (isAutomationRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAutomationRunning) "Stop Automation" else "Start Automation",
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onSendSingleNow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("send_single_now_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send Now")
            }
        }
    }
}
