# EasyLaw - 이해하기 쉬운 법률 가이드 서비스

> 내/외국인, 노인, 취약계층을 위한 **맞춤형 법률 정보 플랫폼**

## 📌 배경 및 목적

### 🔓 법률 장벽을 허무는 기술
전문가가 아니면 이해하기 힘든 **난해한 법률 용어**, **판례**, **법규** 등을 일반인도 쉽게 이해할 수 있도록 **자동으로 변환**하고 설명해주는 환경 구축

### 🎯 LLM 기반 개인화된 법률 가이드
- 방대한 법률 데이터를 **의미 있게 요약**
- "나와 비슷한 판례가 있을까?" → **개인화된 법률 가이드 제공**
- **LLM 기술**을 적극 활용하여 사용자 맞춤형 솔루션 제공

---

## 👥 주요 타겟

| 타겟 | 특징 |
|------|------|
| 🇰🇷 **일반 국민** | 법률 용어가 낯선 사용자, 법률 전문 지식 부족 |
| 🌏 **국내 체류 외국인** | 다국어 지원 필요, 한국 법률에 대한 정보 부족 |
| 👴 **노인층** | 음성 입력, 큰 폰트, TTS 등 접근성 우선 |
| 🆘 **취약계층** | 개인정보 보호 강화, 맞춤형 UI/UX |

---

## 🎯 프로젝트 개요

**EasyLaw**는 복잡한 법률 정보를 누구나 이해할 수 있도록 제공하는 모바일 애플리케이션입니다.
- 🌍 **다국어 지원**: 내/외국인 타겟팅
- 👴 **노인 친화형**: 음성 입력, 큰 폰트, TTS
- 🤝 **커뮤니티 기반**: 포인트 시스템으로 활동 장려
- 📍 **위치 기반**: 근처 법률 기관 지도 제공
- 📄 **OCR & AI**: 문서 자동 인식 및 쉬운 풀이

---

## 📋 주요 기능 (2주 MVP)

### 1️⃣ 인증 및 사용자 관리 (Auth & User)
**구글 로그인 + Firebase 기반 내/외국인 관리**

- [ ] 구글 소셜 로그인 연동 (Firebase Auth)
- [ ] 사용자 프로필 확장 (Firestore)
  - `isForeigner` (내/외국인)
  - `language` (기본 언어)
  - `userType` (노인/일반/취약계층)
- [ ] 국가별 로컬라이징 (UI 언어 즉시 변경)
- [ ] 약관 동의 (개인정보 처리방침)

**예상 일정**: Week 1-2 (Backend 우선)

---

### 2️⃣ 시나리오 기반 상황 진단 (UX/UI)
**Decision Tree 기반 대화형 인터페이스**

- [ ] 대화형 인터페이스 (Decision Tree)
  - 예: "전세사기를 당하셨나요?" → "계약서를 쓰셨나요?" (단계별 질문)
- [ ] 카테고리 퀵 메뉴 (템플릿화)
  - 전세사기, 가정폭력, 임금체불 등
- [ ] 입력 보조 도구 검토
  - 음성 입력(STT) 옵션

**예상 일정**: Week 1-2 (Frontend 우선)

---

### 3️⃣ 커뮤니티 및 보상 체계 (Engagement)
**포인트 + 신뢰도 시스템으로 활동 장려**

- [ ] 포인트 시스템 (정보 공유, 도움되는 댓글)
- [ ] 연계 콘텐츠 (법률 전문가 칼럼 + 판례 요약)
- [ ] 신뢰도 시스템 (배지 부여)

**예상 일정**: Week 2 (MVP 최소 기능만)

---

### 4️⃣ 위치 기반 기관 정보 (Map API)
**네이버 지도 기반 법률 기관 노출**

- [ ] 네이버 지도 API 연동 (현재 위치 권한)
- [ ] 마커 커스텀 (법원, 로펌, 국선변호사 등)
- [ ] 상세 정보 카드 (운영 시간, 연락처, 길 찾기)

**예상 일정**: Week 2 (Bonus)

---

### 5️⃣ 접근성 도구 (OCR & TTS)
**문서 인식 + AI 기반 쉬운 풀이**

- [ ] OCR 기능 (Google Vision API 또는 CLOVA OCR)
  - 등기부등본, 판례문서 촬영 시 텍스트 추출
- [ ] 쉬운 용어 풀이 (LLM 기반 변환)
- [ ] TTS 연동 (모든 텍스트 음성 읽어주기)

**예상 일정**: Week 2 (MVP 코어 기능)

---

### 6️⃣ 디자인 시스템 (Theming)
**전역 테마 설정 및 접근성**

- [ ] 컬러 변수 정의
  - Primary: Deep Blue (신뢰)
  - Secondary: Soft Grey (보조)
  - Accent: Amber/Orange (경고)
- [ ] 다크/라이트 모드 (CSS 변수 또는 ThemeProvider)
- [ ] 동적 폰트 크기 (노인 사용자 고려)

**예상 일정**: Week 1 (지속적 개선)

---

## 🛠️ 기술 스택

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (권장) 또는 XML Layouts
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM + Clean Architecture
- **State Management**: ViewModel + StateFlow / LiveData
- **DI**: Hilt (Dependency Injection)
- **Navigation**: Jetpack Navigation Component
- **UI Components**: Material Design 3

### Backend & Services
- **Database**: Firebase Firestore (NoSQL)
- **인증**: Firebase Authentication
- **실시간**: Firebase Realtime Database (댓글, 포인트)
- **AI/ML**: Google Vision API (OCR), Google Gemini (용어 풀이)
- **배포**: Firebase Hosting
- **구글 로그인**: Firebase Auth + Google Sign-In SDK

### 위치 기반 & 지도
- 🗺️ **Naver Maps SDK for Android**: 기관 위치 표시
- 📍 **Location Services**: GPS 기반 현재 위치

### 접근성 & 입력
- 🎙️ **Google Speech-to-Text**: 음성 입력
- 🔊 **Google Text-to-Speech**: 음성 읽기
- 📱 **Jetpack Accessibility**: 스크린리더 지원

### 테스팅
- **Unit Tests**: JUnit 4, Mockito, Mockk
- **Instrumented Tests**: Espresso, AndroidX Test
- **Code Coverage**: Jacoco
- **Static Analysis**: ktlint, Detekt, Android Lint

---

## 📂 프로젝트 구조

```
easy-law/
├── app/                         # Main Android App Module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/neobrix/easylaw/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── EasyLawApplication.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/         # 화면들 (로그인, 홈, 상세 등)
│   │   │   │   │   ├── components/      # 재사용 컴포넌트
│   │   │   │   │   └── theme/           # 테마 (Color, Typography)
│   │   │   │   ├── data/
│   │   │   │   │   ├── repository/      # Repository 패턴
│   │   │   │   │   ├── datasource/      # 원격/로컬 데이터
│   │   │   │   │   └── models/          # 데이터 모델
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/           # 도메인 모델
│   │   │   │   │   └── usecase/         # 비즈니스 로직
│   │   │   │   ├── viewmodel/           # ViewModel들
│   │   │   │   ├── navigation/          # Navigation Graph
│   │   │   │   ├── di/                  # Hilt DI (AppModule 등)
│   │   │   │   └── util/                # 유틸리티 함수
│   │   │   └── res/
│   │   │       ├── drawable/            # 이미지, 벡터
│   │   │       ├── values/              # 문자열, 색상, 테마
│   │   │       └── values-night/        # 다크 모드
│   │   ├── test/                        # 단위 테스트
│   │   │   └── java/com/neobrix/easylaw/
│   │   │       ├── viewmodel/
│   │   │       └── repository/
│   │   └── androidTest/                 # 통합 테스트 (Espresso)
│   │       └── java/com/neobrix/easylaw/
│   └── build.gradle
│
├── docs/                        # 문서
│   ├── API.md                  # Firebase & API 문서
│   ├── ARCHITECTURE.md         # MVVM & Clean Architecture
│   └── ANDROID_SETUP.md        # Android 개발 환경 설정
│
├── .github/
│   ├── ISSUE_TEMPLATE/         # 이슈 템플릿
│   ├── PULL_REQUEST_TEMPLATE/  # PR 템플릿
│   └── workflows/              # CI/CD (GitHub Actions)
│
├── build.gradle                # 프로젝트 레벨 Gradle
├── settings.gradle             # 모듈 설정
├── gradle.properties           # Gradle 속성
├── detekt.yml                  # Detekt 설정
├── LICENSE                     # MIT License
├── README.md                   # 프로젝트 개요
├── CONTRIBUTING.md            # 기여 가이드
├── DEVELOPMENT.md             # 개발 규칙
└── .gitignore                 # Git 무시 파일
```

---

## 🚀 설치 및 실행 (2주 MVP)

### 필수 환경
```
- Android Studio (2024.1.0 이상 권장)
- JDK 17+
- Android SDK API 34
- Gradle 8.0+
- Git
```

### 1️⃣ 리포지토리 클론

```bash
# 리포 클론
git clone https://github.com/neobrix-mobile-team/easy-law.git
cd easy-law

# develop 브랜치로 이동
git checkout develop
```

### 2️⃣ Android Studio에서 열기

```
File → Open → 프로젝트 폴더 선택
```

Gradle이 자동으로 의존성을 다운로드합니다. (2-3분 소요)

### 3️⃣ 로컬 환경 설정

**`local.properties` 생성** (Git 커밋 X):
```properties
sdk.dir=/Users/[username]/Library/Android/sdk
ndk.dir=/Users/[username]/Library/Android/sdk/ndk/25.1.8937393
```

### 4️⃣ Gradle 빌드

**터미널에서**:
```bash
# 전체 빌드
./gradlew clean build

# Debug APK만 빌드
./gradlew assembleDebug

# 린트 & 정적 분석
./gradlew ktlint detekt lint
```

### 5️⃣ 앱 실행

**방법 1: Android Studio에서**
- Run 버튼 클릭 → 에뮬레이터 또는 기기 선택

**방법 2: 터미널에서**
```bash
./gradlew installDebugAndroidTest
```

### 6️⃣ Firebase 설정

```bash
# Firebase Console에서 프로젝트 생성
# https://console.firebase.google.com

# 1. 새 Android 앱 추가
# 2. google-services.json 다운로드
# 3. app/ 폴더에 복사

# 4. Firebase CLI 설정 (선택사항)
firebase login
firebase init
```

### 7️⃣ 에뮬레이터 설정

```bash
# Android Studio: Tools → Device Manager
# 또는 CLI로

# 에뮬레이터 목록 확인
$ANDROID_HOME/emulator/emulator -list-avds

# 에뮬레이터 실행
$ANDROID_HOME/emulator/emulator -avd EmulatorName
```

### 🧪 테스트 실행

```bash
# 단위 테스트
./gradlew testDebugUnitTest

# 통합 테스트 (에뮬레이터 필요)
./gradlew connectedAndroidTest

# 코드 커버리지
./gradlew jacocoTestDebugUnitTestReport
```

### 📊 코드 품질 검사

```bash
# Kotlin 스타일 검사
./gradlew ktlint

# 자동 포맷팅
./gradlew ktlintFormat

# 정적 분석
./gradlew detekt

# Android Lint
./gradlew lint

# 보안 취약점
./gradlew dependencyCheckAnalyze
```

---

## 📅 개발 일정 (2주 MVP)

### Week 1: 기초 구축
| 날짜 | 목표 | 담당 |
|------|------|------|
| Mon-Wed | 🔐 Auth (구글 로그인) + Firebase 연동 | Backend/Frontend |
| Wed-Fri | 👤 사용자 프로필 (Firestore) + 다국어 지원 | Frontend |
| Fri | 🎨 디자인 시스템 & 테마 완성 | Frontend |

### Week 2: 핵심 기능 개발
| 날짜 | 목표 | 담당 |
|------|------|------|
| Mon-Tue | 🌳 Decision Tree UI 구현 | Frontend |
| Wed-Thu | 📄 OCR 기능 (간단한 버전) + TTS | Frontend |
| Fri | 💬 커뮤니티 기본 구조 + 배포 테스트 | Full Team |

### 우선순위 (MoSCoW)

**Must** (필수):
- ✅ 프로젝트 기본 구조 (완료)
- 🔐 구글 로그인 + Firebase Auth
- 👤 사용자 프로필 관리
- 🎨 디자인 시스템 (완료)
- 🌳 기본 Decision Tree UX

**Should** (중요):
- 📄 OCR 기능 (간단한 버전)
- 🔊 TTS 연동
- 🌍 다국어 지원

**Could** (선택):
- 💬 커뮤니티 포인트 시스템
- 📍 위치 기반 기관 지도
- 🏆 신뢰도 배지

**Won't** (보류):
- 🤖 고급 AI 기능 (v2에서)
- 🎯 신뢰도 검증 시스템 (v2에서)

---

## 🎓 기여 가이드

자세한 내용은 [CONTRIBUTING.md](./CONTRIBUTING.md) 참조

### 빠른 시작

```bash
# 1. develop에서 새 브랜치 생성
git checkout develop
git pull
git checkout -b feature/기능명

# 2. 코드 작성 & 테스트
./gradlew ktlintFormat
./gradlew testDebugUnitTest
./gradlew detekt

# 3. 커밋
git commit -m "feat: 기능 설명"

# 4. PR 생성
git push origin feature/기능명
# GitHub에서 develop으로 PR 생성
```

### 커밋 메시지 규칙

```
feat: 기능 추가
fix: 버그 수정
docs: 문서 작성
style: 코드 포맷팅 (ktlint)
refactor: 코드 정리
test: 테스트 추가
chore: 의존성, Gradle 관리
```

**예시**:
```
feat: 구글 로그인 & Firebase Auth 연동 (#5)

- Firebase Auth 설정
- Google Sign-In SDK 통합
- 사용자 프로필 자동 생성

Closes #5
```

### 🏷️ 자동 라벨 설정

PR이 생성되면 **GitHub Actions**가 자동으로 적절한 라벨을 추가합니다.

#### 지원되는 라벨

| 라벨 | 설명 | 트리거 |
|-----|------|--------|
| 🐛 bug | 버그 수정 | `fix:` 또는 `hotfix` 커밋 |
| ✨ feature | 새로운 기능 | `feat:` 또는 `feature` 커밋 |
| ♻️ refactor | 코드 리팩토링 | `refactor:` 커밋 |
| 📚 Documentation | 문서 수정 | `docs:` 커밋 또는 README/CONTRIBUTING 파일 변경 |
| 🧹 chore | 일상적인 유지보수 | `chore:` 또는 `maintenance` 커밋 |
| 💅 style | 코드 스타일 및 포매팅 | `style:` 또는 `format` 커밋 |
| ⚡ performance | 성능 개선 | `perf:` 또는 `performance` 커밋 |
| 🎨 UI | UI/UX 관련 | `app/src/main/java/com/easylaw/app/ui/` 경로 변경 |
| ⚙️ ViewModel | ViewModel 관련 | `app/src/main/java/com/easylaw/app/viewmodel/` 경로 변경 |
| 📦 Domain | Domain 레이어 | `app/src/main/java/com/easylaw/app/domain/` 경로 변경 |
| 🗄️ Repository | Repository 관련 | `app/src/main/java/com/easylaw/app/repository/` 경로 변경 |
| 🛠️ Utility | Utility 함수/클래스 | `app/src/main/java/com/easylaw/app/util/` 경로 변경 |
| ✅ Test | 테스트 관련 | 테스트 파일 변경 |
| 🔧 build | 빌드 설정 | `build.gradle` 파일 변경 |
| 🔄 CI/CD | CI/CD 파이프라인 | `.github/workflows` 경로 변경 |

#### 라벨 자동 설정 워크플로우

1. **초기 라벨 생성**: `setup-labels` 워크플로우가 필요한 모든 라벨을 생성합니다
   ```bash
   # 수동 실행 (필요시)
   # GitHub Actions > Setup Labels > Run workflow
   ```

2. **자동 라벨 추가**: PR 생성 시 `auto-label` 워크플로우가 다음을 기반으로 자동 분석:
   - PR 제목의 커밋 타입 (feat:, fix:, etc.)
   - 변경된 파일 경로
   - 변경 유형

#### 예시

```bash
# UI 변경 + 버그 수정 PR
feat: 로그인 화면 버그 수정 및 UI 개선

→ 자동으로 추가되는 라벨:
  🐛 bug, ✨ feature, 🎨 UI
```

### 브랜치 규칙

- `main` → 배포 버전 (PR 필수, 1명 리뷰, 자동 라벨 적용)
- `develop` → 개발 버전 (PR 필수, 1명 리뷰, 자동 라벨 적용)
- `feature/*` → 새 기능 (develop으로 PR, 자동으로 `✨ feature` 라벨 추가)
- `fix/*` → 버그 수정 (develop으로 PR, 자동으로 `🐛 bug` 라벨 추가)
- `docs/*` → 문서 (develop으로 PR, 자동으로 `📚 Documentation` 라벨 추가)

---

## 📞 연락처 및 지원

- **팀**: neobrix-mobile-team
- **문의**: GitHub Issues로 제출
- **라이센스**: MIT License

---

## 라이센스

이 프로젝트는 **MIT License** 하에 배포됩니다.
[LICENSE](./LICENSE) 파일 참조

---

**Happy Coding! 🎉**
