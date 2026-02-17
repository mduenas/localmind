# LocalMind ProGuard Rules

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.markduenas.localmind.**$$serializer { *; }
-keepclassmembers class com.markduenas.localmind.** {
    *** Companion;
}
-keepclasseswithmembers class com.markduenas.localmind.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Koin
-keep class org.koin.** { *; }
-keep class com.markduenas.localmind.di.** { *; }

# Keep SQLDelight generated code
-keep class com.markduenas.localmind.data.local.** { *; }

# Keep Cactus SDK
-keep class com.cactuscompute.** { *; }
-dontwarn com.cactuscompute.**

# Keep domain models (used in serialization/reflection)
-keep class com.markduenas.localmind.domain.model.** { *; }
-keep class com.markduenas.localmind.ai.** { *; }

# Compose
-dontwarn androidx.compose.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
