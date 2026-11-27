package com.example.lockedsecure.feature.dashboard.domain

/**
 * Reads the current [ProtectionStatus] from the host platform.
 */
fun interface ProtectionStatusReader {

    /**
     * Queries the current protection state.
     */
    fun readStatus(): ProtectionStatus
}
