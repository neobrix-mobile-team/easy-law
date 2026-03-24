# ---------------------------------------------------------
# 1. 기본 시스템 및 Kotlin 규칙
# ---------------------------------------------------------
-keepattributes SourceFile, LineNumberTable, Signature, *Annotation*, EnclosingMethod, InnerClasses
-renamesourcefileattribute SourceFile

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ---------------------------------------------------------
# 2. Android 컴포넌트 및 UI 프레임워크 (Compose, Firebase)
# ---------------------------------------------------------
-keep class androidx.compose.** { *; }

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ---------------------------------------------------------
# 3. 의존성 주입 (Hilt / Dagger)
# ---------------------------------------------------------
-keep @dagger.hilt.android.HiltAndroidApp class * { <init>(); }
-keep class dagger.hilt.** { *; }

# ---------------------------------------------------------
# 4. 네트워크 및 데이터 파싱 (Retrofit, Gson, Supabase)
# ---------------------------------------------------------
# Retrofit
-keep @retrofit2.http.* class * { *; }
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Gson (필드 이름 보존이 핵심)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

# Supabase & Ktor (실행 시 에러 방지)
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ---------------------------------------------------------
# 5. 카카오 SDK (공유하기 및 로그인)
# ---------------------------------------------------------
-keep class com.kakao.sdk.** { *; }
-keep interface com.kakao.sdk.** { *; }
-keep class com.kakao.sdk.template.model.** { *; }
-keepclassmembers class com.kakao.sdk.template.model.** { *; }
-dontwarn com.kakao.sdk.**

# ---------------------------------------------------------
# 6. 사용자 정의 데이터 모델 (가장 중요: 이름 변경 방지)
# ---------------------------------------------------------
# CommunityWriteModel, CommunityCommentModel 등이 포함된 경로
-keep class com.easylaw.app.data.models.** { *; }
-keepclassmembers class com.easylaw.app.data.models.** { *; }

# TopCommenter, UserSession 등이 포함된 경로
-keep class com.easylaw.app.domain.model.** { *; }
-keepclassmembers class com.easylaw.app.domain.model.** { *; }

# ---------------------------------------------------------
# 7. 기타 범용 규칙
# ---------------------------------------------------------
-keep public class * implements java.io.Serializable {*;}
-dontnote com.google.v8.**