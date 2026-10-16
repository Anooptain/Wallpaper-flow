package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    data class OnboardingStep(
        val title: String,
        val subtitle: String,
        val description: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val tint: Color
    )

    val steps = listOf(
        OnboardingStep(
            title = "Wallpaper Flow AI",
            subtitle = "Automatic Native Wallpaper Automation",
            description = "Welcome! Wallpaper Flow AI automatically discovers, downloads, generates, and sets fresh high-resolution wallpapers to your Home and Lock screens without needing you to open the app every time.",
            icon = Icons.Default.Wallpaper,
            tint = Color(0xFF38BDF8)
        ),
        OnboardingStep(
            title = "WorkManager Scheduling",
            subtitle = "Battery-Optimized Background Execution",
            description = "Android restricts background work to preserve battery. We use standard Android WorkManager (minimum 15-minute intervals). Execution windows are battery-optimized and inexact, ensuring your phone stays cool and responsive.",
            icon = Icons.Default.BatterySaver,
            tint = Color(0xFF10B981)
        ),
        OnboardingStep(
            title = "Curated & AI Generation",
            subtitle = "Three-Tier Priority Architecture",
            description = "The app prioritizes local offline wallpapers first, legitimate free online photography second, and AI generation third. AI generation runs within your configured daily quotas, ensuring zero unexpected fees.",
            icon = Icons.Default.AutoAwesome,
            tint = Color(0xFFA855F7)
        ),
        OnboardingStep(
            title = "Respecting Your Privacy",
            subtitle = "Zero Invasive Permissions",
            description = "We never request contacts, camera, or broad media access. Wallpapers are kept safely in sandbox storage. You can configure active hours, Wi-Fi constraints, and target screens anytime in Settings.",
            icon = Icons.Default.Explore,
            tint = Color(0xFFF59E0B)
        )
    )

    val current = steps[step]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("onboarding_screen")
    ) {
        // Skip Button top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Content Center
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = current.tint.copy(alpha = 0.15f),
                modifier = Modifier.size(110.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = current.icon,
                        contentDescription = null,
                        tint = current.tint,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = current.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = current.subtitle,
                style = MaterialTheme.typography.titleSmall,
                color = current.tint,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = current.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Bottom Navigation Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                steps.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == step) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (step < steps.size - 1) {
                        step++
                    } else {
                        onFinish()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (step < steps.size - 1) "Continue" else "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
