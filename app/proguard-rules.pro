# LockedSecure R8 / ProGuard Rules (R8 full mode)
#
# AndroidManifest entry points (LockedSecureApp, MainActivity, PowerMenuBlockerService,
# AdminReceiver) are kept automatically via aapt2-generated rules. No blanket -keep rules exist.

# Strip any accidental verbose/debug/info logcat calls from release bytecode
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
