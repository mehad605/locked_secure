package com.example.lockedsecure.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// ponytail: locked to dark mode to ensure a consistent, premium matte black aesthetic
private val DarkColorScheme = darkColorScheme(
    primary = AccentActive,
    background = MattDarkBackground,
    surface = MattCardBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun LockedSecureTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
