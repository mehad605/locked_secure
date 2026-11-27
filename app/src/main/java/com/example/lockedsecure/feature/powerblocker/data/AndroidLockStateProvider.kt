package com.example.lockedsecure.feature.powerblocker.data

import android.app.KeyguardManager
import android.content.Context
import com.example.lockedsecure.feature.powerblocker.domain.LockStateProvider

/**
 * Android implementation of [LockStateProvider] backed by [KeyguardManager].
 *
 * Uses [Context.getApplicationContext] and resolves [KeyguardManager] lazily so service startup
 * performs zero unnecessary system-service lookups until a power menu candidate is seen.
 */
class AndroidLockStateProvider(
    context: Context,
) : LockStateProvider {

    private val appContext: Context = context.applicationContext

    override fun isDeviceLocked(): Boolean {
        val manager = appContext.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return manager?.isKeyguardLocked == true
    }
}
