# LockedSecure ProGuard Rules
# Keep Compose and Accessibility service entries

-keepclassmembers class * extends android.accessibilityservice.AccessibilityService { *; }
-keepclassmembers class * extends android.app.admin.DeviceAdminReceiver { *; }

# Keep Compose runtime
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
