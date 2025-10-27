# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep data classes used with Compose and domain models
-keep class com.aircalc.converter.domain.model.** { *; }
-keep class com.aircalc.converter.presentation.state.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclassmembers class * {
    @dagger.hilt.* <methods>;
}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class androidx.compose.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# Keep Navigation arguments
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# Keep enum classes
-keepclassmembers enum * { *; }

# Keep Application class
-keep class com.aircalc.converter.AirCalcApplication { *; }

# Keep BroadcastReceiver for timer alarms
-keep class com.aircalc.converter.presentation.service.TimerAlarmReceiver { *; }

# DataStore
-keep class androidx.datastore.*.** { *; }

# Keep Kotlin metadata for reflection
-keepattributes *Annotation*, Signature, Exception

# Keep all classes in the converter package from being removed
-keep class com.aircalc.converter.** { *; }
-keepclassmembers class com.aircalc.converter.** { *; }