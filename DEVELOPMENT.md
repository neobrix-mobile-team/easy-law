# 🛠️ EasyLaw 개발 규칙

> 모든 개발자가 따라야 할 기본 규칙입니다. 일관성 있는 개발 환경을 만들기 위해 이 문서를 주의 깊게 읽어주세요.

---

## 📋 목차
1. [브랜치 전략](#1-브랜치-전략-git-flow)
2. [브랜치 명명 규칙](#2-브랜치-명명-규칙)
3. [PR 프로세스](#3-pr-프로세스)
4. [커밋 메시지](#4-커밋-메시지-규칙)
5. [코딩 스타일](#5-코딩-스타일)
6. [테스트](#6-테스트)
7. [배포](#7-배포-프로세스)

---

## 1. 브랜치 전략 (Git Flow)

### 📊 브랜치 구조

```
main (배포 버전 - 항상 안정적)
  ↑
  └─ develop (개발 버전 - PR로만 업데이트)
      ↑
      ├─ feature/* (새 기능)
      ├─ fix/*     (버그 수정)
      ├─ docs/*    (문서)
      └─ chore/*   (기타: Gradle, 의존성)
```

### 🎯 각 브랜치의 목적

| 브랜치 | 용도 | 보호 규칙 |
|--------|------|---------|
| `main` | 배포 가능한 안정 버전 | ✅ PR 필수, 1명 리뷰, CI/CD 통과 |
| `develop` | 개발 중인 버전 | ✅ PR 필수, 1명 리뷰, CI/CD 통과 |
| `feature/*` | 새 기능 개발 | ❌ 보호 없음 |
| `fix/*` | 버그 수정 | ❌ 보호 없음 |
| `docs/*` | 문서 작성 | ❌ 보호 없음 |

### ⚠️ 주의 사항

- ❌ **main에 직접 푸시 금지**
- ❌ **develop에 직접 푸시 금지**
- ✅ **항상 feature/fix/docs 브랜치에서 작업 후 PR**
- ✅ **develop에서만 새 브랜치 시작**

---

## 2. 브랜치 명명 규칙

### 📝 포맷

```
<type>/<description>
```

- **type**: `feature`, `fix`, `docs`, `chore`, `refactor`
- **description**: 소문자, 하이픈으로 단어 구분

### ✅ 좋은 예

```
feature/auth-google-login        # 구글 로그인 기능
feature/decision-tree-ui         # Decision Tree UI
feature/ocr-document-scan        # OCR 문서 스캔
fix/crash-on-home-screen         # 홈 화면 크래시 수정
fix/language-selection-bug       # 언어 선택 버그
docs/android-setup-guide         # 안드로이드 설정 가이드
chore/update-gradle-version      # Gradle 버전 업데이트
refactor/extract-auth-service    # Auth 서비스 분리
```

### ❌ 나쁜 예

```
feature/새기능                    # 한글 사용 금지
feature_auth                      # 언더스코어 사용 금지
FeatureAuthGoogleLogin           # 대문자 사용 금지
feature/very-very-long-and-complicated-description  # 너무 길음
```

---

## 3. PR 프로세스

### 📝 Step 1: 브랜치 생성

```bash
# develop이 최신 상태인지 확인
git checkout develop
git pull origin develop

# 새 브랜치 생성
git checkout -b feature/기능명
```

### 💻 Step 2: 개발 & 커밋

```bash
# 코드 작성...

# ktlint 자동 수정
./gradlew ktlintFormat

# 변경사항 추가
git add .

# 커밋
git commit -m "feat: 기능 설명"
```

### 🔄 Step 3: PR 생성

```bash
# 리모트에 푸시
git push origin feature/기능명
```

GitHub에서 자동으로 "Compare & pull request" 버튼이 나타남 → 클릭

### 📋 Step 4: PR 템플릿 작성

PR 템플릿이 자동으로 나타나면:

```markdown
## 🎯 PR 목표
이 PR이 해결하는 문제 설명

## 📝 변경 사항
- 구글 로그인 Firebase 연동
- 사용자 프로필 저장 (Firestore)
- 다국어 지원 (한글/영어)

## ✅ 체크리스트
- [x] 커밋 메시지 읽기 쉽게 작성
- [x] 관련 이슈 링크 (fixes #5)
- [x] 로컬에서 테스트 완료
- [x] ktlint/detekt 통과

## 🔗 관련 이슈
fixes #5
```

### ✅ Step 5: CI/CD 통과 확인

PR이 생성되면 자동으로 실행:
- ✅ `android-build` (ktlint, detekt, build, unit tests)
- ✅ `android-instrumented-tests` (에뮬레이터 테스트)
- ✅ `code-quality` (lint, security, coverage)

**모든 체크가 통과해야 merge 가능**

### 👥 Step 6: 리뷰 & 승인

1. 팀원이 코드 리뷰
2. 변경 요청 시 수정
3. 승인 후 merge

### 🔗 Step 7: Merge & 정리

```bash
# PR이 merge된 후
git checkout develop
git pull origin develop

# 로컬 브랜치 삭제
git branch -d feature/기능명
```

---

## 4. 커밋 메시지 규칙

### 📝 포맷 (Conventional Commits)

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 🎯 Type

| Type | 설명 | 예시 |
|------|------|------|
| **feat** | 새 기능 추가 | `feat: 구글 로그인 추가` |
| **fix** | 버그 수정 | `fix: OCR 타임아웃 버그` |
| **docs** | 문서 작성 | `docs: API 문서 추가` |
| **style** | 코드 포맷팅 (ktlint) | `style: 들여쓰기 정렬` |
| **refactor** | 코드 정리 (기능 변화 X) | `refactor: Auth 서비스 분리` |
| **test** | 테스트 추가 | `test: 로그인 테스트 추가` |
| **chore** | 빌드, 의존성 | `chore: Gradle 버전 업데이트` |
| **ci** | CI/CD 설정 | `ci: GitHub Actions 수정` |

### 🎯 Scope (선택)

기능이나 모듈 범위:
```
feat(auth): 구글 로그인 추가
fix(ui): 버튼 레이아웃 수정
docs(api): API 문서 작성
```

### 📝 Subject

- 소문자로 시작
- 명령형 사용 ("추가하다" X, "추가" O)
- 마침표 없음
- 50자 이내

### 📖 Body (선택)

상세 설명이 필요할 때:
```
feat: 구글 로그인 기능 추가 (#5)

- Firebase Authentication 설정
- Google OAuth 2.0 연동
- 사용자 프로필 자동 생성
- isForeigner, language 필드 추가

Closes #5
```

### 🔗 Footer

관련 이슈 링크:
```
Closes #5          # 이슈 자동 종료
Fixes #10          # 버그 자동 종료
Related to #15     # 관련 이슈 (자동 종료 X)
```

### ✅ 좋은 예

```
feat(auth): 구글 소셜 로그인 연동 (#5)

- Firebase Auth 설정
- Google OAuth 2.0 Client ID 관리
- 사용자 프로필 Firestore 저장
- isForeigner/language/userType 필드 추가

Closes #5
```

```
fix(ui): Decision Tree 버튼 레이아웃 깨짐 (#12)

모바일 작은 화면에서 버튼이 겹치는 버그 수정
- FlexBox 대신 ConstraintLayout 사용
- 버튼 높이 동적 조정

Fixes #12
```

### ❌ 나쁜 예

```
fixed stuff
새로운 기능 추가했음
random commit
Updated (너무 모호함)
```

---

## 5. 코딩 스타일

### 🎨 Kotlin (Android)

#### ktlint 규칙 자동 적용

```bash
./gradlew ktlintFormat
```

#### 핵심 규칙

```kotlin
// ✅ 좋은 예
class UserViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = authService.login(email, password)
                _state.value = UiState.Success(user)
            } catch (error: Exception) {
                _state.value = UiState.Error(error.message)
            }
        }
    }
}

// ❌ 나쁜 예
class UserViewModel : ViewModel(){
    val state = MutableStateFlow<UiState>(UiState.Loading)
    fun login(email:String,password:String){
        viewModelScope.launch{
            try{
                val user = authService.login(email,password)
                state.value = UiState.Success(user)
            }catch(e:Exception){
                state.value = UiState.Error(e.message)
            }
        }
    }
}
```

#### 네이밍 규칙

```kotlin
// 클래스
class UserViewModel { }
class AuthRepository { }

// 함수
fun getUserProfile() { }
fun fetchDataFromApi() { }

// 변수
val userName: String = ""
var isLoading: Boolean = false
val _state = MutableStateFlow<UiState>()

// 상수
companion object {
    const val DEFAULT_TIMEOUT = 30000L
    const val API_BASE_URL = "https://api.example.com"
}
```

### 🔍 Detekt 규칙

```bash
./gradlew detekt
```

자동으로 확인:
- 복잡한 함수 (Cyclomatic Complexity)
- 긴 라인 (Line Length)
- 사용하지 않는 변수
- 과도한 중첩

---

## 6. 테스트

### 🧪 Unit 테스트 (필수)

```bash
./gradlew testDebugUnitTest
```

**최소 요구사항:**
- 모든 `ViewModel` 테스트
- 모든 `Repository` 테스트
- 복잡한 로직 테스트

**예시:**
```kotlin
@Test
fun loginWithValidCredentials_shouldReturnUser() {
    // Arrange
    val email = "test@example.com"
    val password = "password123"
    
    // Act
    val result = authService.login(email, password)
    
    // Assert
    assertNotNull(result)
    assertEquals(email, result.email)
}
```

### 📱 Instrumented 테스트 (선택)

```bash
./gradlew connectedAndroidTest
```

에뮬레이터에서 실행:
- UI 인터랙션
- Firebase 통합
- 권한 요청

### 📊 코드 커버리지

```bash
./gradlew jacocoTestDebugUnitTestReport
```

목표: **최소 70% 커버리지**

---

## 7. 배포 프로세스

### 🎯 MVP 배포 (2주 후)

#### Step 1: develop 최종 점검

```bash
git checkout develop
git pull

# 모든 테스트 & 린트 통과 확인
./gradlew clean build
./gradlew ktlint
./gradlew detekt
```

#### Step 2: 버전 & 태그

```
version: 0.1.0-MVP
buildVersion: 1
```

#### Step 3: Release PR 생성

```
develop → main (PR)

제목: "release: v0.1.0-MVP"
설명:
- 모든 기능 구현 완료
- CI/CD 통과
- 테스트 커버리지 70%+
```

#### Step 4: 최종 리뷰 & Merge

```bash
# PR 승인 후
git checkout main
git merge develop
git tag v0.1.0-MVP
git push origin main --tags
```

#### Step 5: 배포 (Firebase Hosting)

```bash
firebase deploy
```

---

## ✅ 체크리스트

### 코드 제출 전

- [ ] `git pull origin develop` (최신 상태)
- [ ] `./gradlew ktlintFormat` (자동 포맷)
- [ ] `./gradlew testDebugUnitTest` (테스트)
- [ ] `./gradlew detekt` (정적 분석)
- [ ] `./gradlew lint` (Android Lint)

### PR 제출 전

- [ ] PR 템플릿 작성
- [ ] 관련 이슈 링크 (fixes #123)
- [ ] 스크린샷/비디오 첨부 (UI 변경 시)
- [ ] 최소 1명 리뷰어 지정

### Merge 전

- [ ] CI/CD 모두 통과 ✅
- [ ] 최소 1명 승인 ✅
- [ ] 커밋 히스토리 깔끔함 ✅

---

## 📚 참고 자료

- [Conventional Commits](https://www.conventionalcommits.org/)
- [ktlint 공식 문서](https://ktlint.github.io/)
- [Detekt 공식 문서](https://detekt.dev/)
- [Android Testing Guide](https://developer.android.com/training/testing)
- [Git Flow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)

---

## 🤝 도움이 필요한가요?

- **Git 문제**: [CONTRIBUTING.md](./CONTRIBUTING.md) 참고
- **안드로이드 설정**: [ANDROID_SETUP.md](./docs/ANDROID_SETUP.md) 참고
- **GitHub Issues**: 질문은 Discussions에서!

---

**Happy Coding! 🚀**
