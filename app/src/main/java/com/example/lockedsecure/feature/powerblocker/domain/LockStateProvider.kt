package com.example.lockedsecure.feature.powerblocker.domain

/**
 * Reports whether the device lock screen (keyguard) is currently engaged.
 */
fun interface LockStateProvider {
    /**
     * Returns true when the keyguard is locked and unauthorized shutdown must be prevented.
     */
    fun isDeviceLocked(): Boolean
}
