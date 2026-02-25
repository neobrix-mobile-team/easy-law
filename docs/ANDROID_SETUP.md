# Android 프로젝트 셋업 가이드

## 📋 필수 도구 설정

### build.gradle (Project level)

```gradle
plugins {
    id 'com.android.application' version '8.0.0' apply false
    id 'com.android.library' version '8.0.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.0' apply false
    id 'org.jlleitschuh.gradle.ktlint' version '12.0.0'
    id 'io.gitlab.arturbosch.detekt' version '1.23.0'
    id 'org.owasp.dependencycheck' version '8.4.0'
}

// ktlint 설정
subprojects {
    apply plugin: 'org.jlleitschuh.gradle.ktlint'
    
    ktlint {
        version = '1.0.1'
        verbose = true
        outputToConsole = true
        coloredOutput = true
        android = true
        reporters {
            reporter 'plain'
            reporter 'sarif'
        }
    }
}

// detekt 설정
detekt {
    toolVersion = '1.23.0'
    config = files('detekt.yml')
    reports {
        html.enabled = true
        sarif.enabled = true
    }
}

// OWASP Dependency Check
dependencyCheck {
    format = 'ALL'
}
```

---

### build.gradle (App level)

```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'kotlin-kapt'
    id 'jacoco'
    id 'io.gitlab.arturbosch.detekt'
}

android {
    namespace 'com.neobrix.easylaw'
    compileSdk 34

    defaultConfig {
        applicationId 'com.neobrix.easylaw'
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName '0.1.0-MVP'
        
        testInstrumentationRunner 'androidx.test.runner.AndroidJUnitRunner'
    }

    buildTypes {
        debug {
            debuggable true
            minifyEnabled false
        }
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = '1.5.0'
    }
}

dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // Compose
    implementation platform('androidx.compose:compose-bom:2023.10.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    
    // Kotlin Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // Networking
    implementation 'com.squareup.retrofit2:retrofit:2.10.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.10.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    
    // State Management
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:5.6.0'
    testImplementation 'org.mockito.kotlin:mockito-kotlin:5.1.0'
    testImplementation 'io.mockk:mockk:1.13.8'
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
    
    // Instrumented Testing
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
    
    // Debugging
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}

// Jacoco 설정 (코드 커버리지)
jacoco {
    toolVersion = '0.8.10'
}

task jacocoTestDebugUnitTestReport(type: JacocoReport) {
    dependsOn testDebugUnitTest
    
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    
    def fileFilter = ['**/R.class', '**/R$*.class', '**/BuildConfig.*', '**/Manifest*.*', '**/*Test*.*', '**/*$Lambda$*.*']
    def debugTree = fileTree(dir: "$buildDir/intermediates/classes/debug", excludes: fileFilter)
    def mainSrc = "$project.projectDir/src/main/java"
    
    sourceDirectories.setFrom(files([mainSrc]))
    classDirectories.setFrom(files([debugTree]))
    executionData.setFrom(fileTree(dir: buildDir, includes: ['jacoco/testDebugUnitTest.exec']))
}

// detekt 설정
detekt {
    config = rootProject.file('detekt.yml')
    reports {
        html.enabled = true
        sarif.enabled = true
    }
}
```

---

## 🚀 로컬 개발 환경

### 1. 환경 변수 설정

**local.properties** (Git에 커밋하지 마세요!):
```properties
sdk.dir=/Users/admin/Library/Android/sdk
ndk.dir=/Users/admin/Library/Android/sdk/ndk/25.1.8937393
```

### 2. 초기 빌드

```bash
./gradlew clean build
```

### 3. Lint 검사

```bash
# ktlint 검사
./gradlew ktlint

# ktlint 자동 수정
./gradlew ktlintFormat

# Android Lint 검사
./gradlew lint

# Detekt 분석
./gradlew detekt
```

### 4. 테스트

```bash
# Unit 테스트
./gradlew testDebugUnitTest

# Instrumented 테스트 (에뮬레이터 필요)
./gradlew connectedAndroidTest

# 코드 커버리지
./gradlew testDebugUnitTest jacocoTestDebugUnitTestReport
```

### 5. 빌드 및 설치

```bash
# Debug APK 생성
./gradlew assembleDebug

# Debug APK 설치 및 실행
./gradlew installDebugAndroidTest
./gradlew connectedAndroidTest
```

---

## 📁 프로젝트 구조

```
easy-law/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/neobrix/easylaw/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   └── components/
│   │   │   │   ├── data/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── source/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   └── usecase/
│   │   │   │   └── di/
│   │   │   └── res/
│   │   ├── test/                    # Unit Tests
│   │   └── androidTest/             # Instrumented Tests
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
├── detekt.yml                       # Detekt 설정
├── gradle.properties
└── local.properties (Git 무시)
```

---

## ✅ Gradle 명령어 치트시트

| 명령어 | 설명 |
|--------|------|
| `./gradlew assemble` | 모든 빌드 variant 컴파일 |
| `./gradlew assembleDebug` | Debug APK 생성 |
| `./gradlew assembleRelease` | Release APK 생성 (서명 필요) |
| `./gradlew installDebug` | Debug APK 설치 |
| `./gradlew clean` | 빌드 캐시 삭제 |
| `./gradlew test` | Unit 테스트 실행 |
| `./gradlew connectedAndroidTest` | Instrumented 테스트 (에뮬레이터 필요) |
| `./gradlew lint` | Android Lint 검사 |
| `./gradlew ktlint` | Kotlin Lint 검사 |
| `./gradlew ktlintFormat` | Kotlin 자동 포맷팅 |
| `./gradlew detekt` | 정적 분석 |
| `./gradlew build` | 전체 빌드 (test + lint + assemble) |

---

## 🔗 참고 자료

- [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin)
- [ktlint 공식 문서](https://ktlint.github.io/)
- [Detekt 공식 문서](https://detekt.dev/)
- [Firebase for Android](https://firebase.google.com/docs/android/setup)
