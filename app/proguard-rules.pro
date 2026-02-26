# 기본 규칙
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Kotlin
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Compose
-keep class androidx.compose.** { *; }

# Hilt
-keep @dagger.hilt.android.HiltAndroidApp class * {
    <init>();
}

# Retrofit
-keepclasseswithmembers class * {
    @retrofit2.http.<Method> <methods>;
}

# Gson
-keep class com.google.gson.stream.** { *; }
-keepclassmembers class * {
    <fields>;
}
