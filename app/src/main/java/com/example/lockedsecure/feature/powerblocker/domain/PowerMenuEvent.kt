package com.example.lockedsecure.feature.powerblocker.domain

/**
 * Platform-independent representation of a window transition event inspected by the power-menu blocker.
 *
 * Kept free of `android.*` types so detection rules remain pure Kotlin and fast to unit test.
 *
 * @property isWindowStateChanged True when the originating accessibility event is a window-state transition.
 * @property packageName Package name of the window source, or null when unavailable.
 * @property className Window or dialog class name, or null when unavailable.
 */
data class PowerMenuEvent(
    val isWindowStateChanged: Boolean,
    val packageName: String?,
    val className: String?,
)
