package com.example.lockedsecure.feature.powerblocker.domain

/**
 * Decision produced by [PowerMenuActionPolicy] for an incoming [PowerMenuEvent].
 */
enum class PowerMenuDecision {
    /** Dismiss the dialog immediately because the device is locked. */
    BLOCK_AND_DISMISS,

    /** Leave the window untouched (either not a power menu or device is unlocked). */
    ALLOW,
}

/**
 * Combines [PowerMenuDetector] and [LockStateProvider] to decide whether a window event must be dismissed.
 */
interface PowerMenuActionPolicy {

    /**
     * Evaluates [event] against the current lock state and returns the required [PowerMenuDecision].
     */
    fun evaluate(event: PowerMenuEvent): PowerMenuDecision
}

/**
 * Default policy: dismisses detected power menus strictly while the keyguard is locked.
 *
 * Lock state is queried lazily only after [detector] matches so ordinary window transitions
 * avoid an unnecessary IPC call to `KeyguardManager`.
 */
internal class DefaultPowerMenuActionPolicy(
    private val detector: PowerMenuDetector,
    private val lockStateProvider: LockStateProvider,
) : PowerMenuActionPolicy {

    override fun evaluate(event: PowerMenuEvent): PowerMenuDecision {
        val shouldBlock = detector.isPowerMenu(event) && lockStateProvider.isDeviceLocked()
        return if (shouldBlock) PowerMenuDecision.BLOCK_AND_DISMISS else PowerMenuDecision.ALLOW
    }
}

/**
 * Factory creating the default [PowerMenuActionPolicy] without exposing internal implementations.
 */
object PowerBlockerFactory {

    /**
     * Creates a [PowerMenuActionPolicy] wired with the default cross-OEM [PowerMenuDetector].
     */
    fun createPolicy(
        lockStateProvider: LockStateProvider,
        detector: PowerMenuDetector = DefaultPowerMenuDetector(),
    ): PowerMenuActionPolicy = DefaultPowerMenuActionPolicy(detector, lockStateProvider)
}
