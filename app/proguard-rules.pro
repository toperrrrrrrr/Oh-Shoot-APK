# Hilt / Dagger
-keep class androidx.hilt.** { *; }
-keep class com.ohshootstudio.resibooth.**_HiltModules { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# DataStore
-keep class androidx.datastore.** { *; }

# Serialization (if you use Gson/Moshi for the custom template)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.ohshootstudio.resibooth.domain.** { *; }

# UVCCamera (Native libraries)
-keep class com.serenegiant.** { *; }
-keep class com.herohan.** { *; }
-dontwarn com.serenegiant.**
-dontwarn com.herohan.**
