# ===========================================================================
# AllAtOnce ProGuard / R8 Rules
# ===========================================================================
# This file contains keep rules tailored for the AllAtOnce sensor dashboard.
# The app uses 27 hardware modules that interact with Android sensor APIs,
# CameraX, Play Services, Biometric, NFC, and Bluetooth — all of which
# require careful ProGuard configuration.
# ===========================================================================

# ---------------------------------------------------------------------------
# General Android
# ---------------------------------------------------------------------------
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Keep the BuildConfig class (useful for version checks)
-keep class **.BuildConfig { *; }

# ---------------------------------------------------------------------------
# Kotlin
# ---------------------------------------------------------------------------
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ---------------------------------------------------------------------------
# Jetpack Compose
# ---------------------------------------------------------------------------
# Compose uses reflection internally; keep Composable-related metadata
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---------------------------------------------------------------------------
# CameraX
# ---------------------------------------------------------------------------
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
-keep class androidx.camera.core.impl.** { *; }
-keep class androidx.camera.camera2.** { *; }

# ---------------------------------------------------------------------------
# Google Play Services (Location)
# ---------------------------------------------------------------------------
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ---------------------------------------------------------------------------
# Biometric
# ---------------------------------------------------------------------------
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ---------------------------------------------------------------------------
# NFC
# ---------------------------------------------------------------------------
-keep class android.nfc.** { *; }

# ---------------------------------------------------------------------------
# Bluetooth
# ---------------------------------------------------------------------------
-keep class android.bluetooth.** { *; }

# ---------------------------------------------------------------------------
# Sensor API (hardware modules)
# ---------------------------------------------------------------------------
# Keep all sensor-related classes to prevent stripping of reflection-accessed
# sensor types (Accelerometer, Gyroscope, Barometer, etc.)
-keep class android.hardware.** { *; }

# ---------------------------------------------------------------------------
# AllAtOnce Modules
# ---------------------------------------------------------------------------
# Keep all module classes — they may be referenced dynamically
-keep class com.example.allatonce.modules.** { *; }
-keep class com.example.allatonce.state.** { *; }
-keep class com.example.allatonce.data.** { *; }

# ---------------------------------------------------------------------------
# Accompanist
# ---------------------------------------------------------------------------
-dontwarn com.google.accompanist.**

# ---------------------------------------------------------------------------
# Nothing Phone Glyph SDK
# ---------------------------------------------------------------------------
-keep class com.nothing.** { *; }
-keep class com.nothinglondon.** { *; }
-dontwarn com.nothing.**
-dontwarn com.nothinglondon.**

# ---------------------------------------------------------------------------
# Coroutines
# ---------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---------------------------------------------------------------------------
# Remove logging in release builds
# ---------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
