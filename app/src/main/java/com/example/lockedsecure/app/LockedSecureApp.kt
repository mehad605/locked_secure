package com.example.lockedsecure.app

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.StrictMode

/**
 * Minimal [Application] entry point.
 *
 * Performs zero work in release builds: [appGraph] is created lazily on first access.
 * In debuggable builds only, enables [StrictMode] thread and VM checks to catch accidental
 * main-thread I/O or resource leaks early during development.
 */
class LockedSecureApp : Application() {

    /** Lazily initialized manual dependency graph shared across app components. */
    val appGraph: AppGraph by lazy { AppGraph(this) }

    override fun onCreate() {
        super.onCreate()
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            enableDebugStrictMode()
        }
    }

    private fun enableDebugStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build(),
        )
    }

    companion object {
        /**
         * Resolves the application's [AppGraph] from any [Context].
         */
        fun graph(context: Context): AppGraph {
            val app = context.applicationContext
            return if (app is LockedSecureApp) app.appGraph else AppGraph(app)
        }
    }
}
