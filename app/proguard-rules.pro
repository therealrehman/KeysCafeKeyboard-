# KeysCafe Keyboard ProGuard Rules

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }

# Keep model classes for Gson serialization
-keep class com.keys.cafe.keyboard.model.** { *; }

# Keep Parcelize
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
