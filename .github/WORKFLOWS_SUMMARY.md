# 🔄 GitHub Actions 워크플로우 요약

## 📋 워크플로우 아키텍처 (최적화됨 ✨)

### 전체 구조
```
코드 Push / PR 생성
  │
  ├─ ⚙️ setup-gradle.yml (공통 환경 설정) [NEW 재사용!]
  │   ├─ Checkout
  │   ├─ JDK 17 설정
  │   ├─ Gradle 권한 설정
  │   ├─ local.properties 생성
  │   └─ google-services.json 생성
  │
  ├─ 🏗️ android-build.yml (빌드 & 테스트)
  │   ├─ setup-gradle.yml 호출
  │   ├─ PR: assembleDebug (~5분)
  │   ├─ Push: testDebugUnitTest + assembleDebug (~8분)
  │   └─ APK & 테스트 결과 업로드
  │
  ├─ 🔐 code-quality.yml (코드 품질 & 보안)
  │   ├─ setup-gradle.yml 호출
  │   ├─ 🔍 lint-and-detekt (모든 Push/PR)
  │   ├─ 🔒 security-scan (Push only)
  │   └─ 📊 coverage (Push only)
  │
  └─ 🏷️ auto-label.yml (라벨 자동 추가)
      └─ PR 제목 & 경로 기반 라벨 추가
```

---

## 💡 주요 개선 사항

### 문제점 → 해결책

| 문제 | 원인 | 해결책 |
|------|------|--------|
| ❌ 중복 설정 코드 | 각 워크플로우가 독립적으로 setup | ✅ setup-gradle.yml 재사용 |
| ❌ 유지보수 어려움 | 변경 시 2곳 이상 수정 필요 | ✅ 한 곳만 수정, 모두 적용 |
| ❌ 설정 불일치 | 각자 다른 버전/설정 사용 | ✅ 단일 진실 공급원(SSOT) |
| ❌ 불필요한 시간낭비 | PR에서도 보안검사, 커버리지 실행 | ✅ Push only 조건으로 최적화 |

**결과**: 중복 코드 75% 감소, 관리 편의성 극대화 🎯

---

## 📊 워크플로우 비교표

| 워크플로우 | 책임 | 트리거 | 소요시간 | 상태 |
|-----------|------|--------|---------|------|
| **setup-gradle.yml** | 환경 설정 | 내부 호출 | ~2분 | ✨ NEW |
| **android-build.yml** | 빌드 & 테스트 | Push/PR | 5-8분 | 🔄 개편 |
| **code-quality.yml** | 품질 & 보안 검사 | Push/PR | ~15분 | 🔄 개편 |
| **auto-label.yml** | 라벨 추가 | PR | ~1분 | ✅ 기존 |

---

## ⚙️ 각 워크플로우 상세

### 1️⃣ setup-gradle.yml ⭐ 신규

**목적**: 모든 빌드/검사 작업의 공통 설정을 한 곳에서 관리

**포함 작업**:
- 📥 Checkout
- ☕ JDK 17 설정 (Gradle 캐싱)
- 🔧 Gradle wrapper 권한 설정
- 📝 local.properties 생성
  - 기본값: `sdk.dir=$ANDROID_HOME`
  - 선택값: API 키 (GEMINI_API_KEY, LAW_API_KEY)
- 🔥 google-services.json 생성

**사용 예시**:
```yaml
jobs:
  setup:
    uses: ./.github/workflows/setup-gradle.yml
    with:
      setup-api-keys: true  # API 키 포함 여부
    secrets:
      GOOGLE_SERVICES_JSON: ${{ secrets.GOOGLE_SERVICES_JSON }}
      GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
      LAW_API_KEY: ${{ secrets.LAW_API_KEY }}
```

---

### 2️⃣ android-build.yml 🏗️

**책임**: 안드로이드 빌드 및 단위 테스트

**작동 방식**:
1. setup-gradle.yml 호출 (API 키 포함)
2. PR 이벤트: `assembleDebug` (빠른 검증)
3. Push 이벤트: `testDebugUnitTest + assembleDebug` (완전 검증)
4. 산출물 업로드 (APK, 테스트 결과)

**최적화**:
- Gradle 병렬 처리 & 캐싱
- Kotlin 증분 컴파일 & 캐싱
- PR은 빠른 빌드 → 개발자 피드백 빠름
- Push는 완전 테스트 → 품질 보증

---

### 3️⃣ code-quality.yml 🔐

**책임**: 코드 품질, 보안, 커버리지 검사

**구성**: 3개 병렬 Job

#### 🔍 lint-and-detekt (모든 Push/PR)
```
Android Lint 검사
  ↓
Detekt 정적 분석
  ↓
📤 lint-report, detekt-report 업로드
```

#### 🔒 security-scan (Push only)
```
OWASP Dependency Check
  ↓
의존성 보안 취약점 검사
  ↓
📤 security-scan-report 업로드
```

#### 📊 coverage (Push only)
```
Jacoco 코드 커버리지
  ↓
단위 테스트 + 커버리지 수집
  ↓
📤 coverage-report 업로드
```

---

## 🎯 자동 라벨링 (auto-label.yml)

### 작동 원리
```
PR 생성
  ↓
PR 제목 분석 (feat:, fix:, refactor: ...)
  ↓
변경 파일 경로 분석 (ui/, viewmodel/, ...)
  ↓
해당 라벨 자동 추가
  ↓
PR에 라벨 표시 ✅
```

### 라벨 규칙

**커밋 타입별**:
| 타입 | 라벨 |
|------|------|
| feat: | ✨ feature |
| fix: | 🐛 bug |
| refactor: | ♻️ refactor |
| docs: | 📚 documentation |
| test: | ✅ test |
| chore: | 🧹 chore |
| style: | 💅 style |
| perf: | ⚡ performance |

**파일 경로별**:
| 경로 | 라벨 |
|------|------|
| ui/ | 🎨 UI |
| viewmodel/ | ⚙️ ViewModel |
| domain/ | 📦 Domain |
| repository/ | 🗄️ Repository |
| util/ | 🛠️ Utility |
| test/, androidTest/ | ✅ Test |
| build.gradle | 🔧 build |
| .github/workflows/ | 🔄 CI/CD |

---

## 🎯 빠른 시작

### 1단계: 라벨 초기화
```bash
# GitHub Actions 탭 → Setup Labels → Run workflow
# 또는 develop 브랜치에 push
git push origin develop
```

### 2단계: PR 생성
```bash
# feature 브랜치에서 커밋 (표준 형식)
git commit -m "feat: 새로운 기능"

# PR 생성하면 자동으로 라벨이 추가됩니다! 🏷️
```

---

## 📝 예시

### 예시 1: UI 버그 수정
```
PR 제목: "fix: 로그인 화면 레이아웃"
변경: app/src/main/java/.../ui/screen/Login.kt

자동 라벨:
├─ 🐛 bug
└─ 🎨 UI
```

### 예시 2: ViewModel 기능
```
PR 제목: "feat: 사용자 프로필 ViewModel"
변경: app/src/main/java/.../viewmodel/ProfileViewModel.kt

자동 라벨:
├─ ✨ feature
└─ ⚙️ ViewModel
```

### 예시 3: 다중 영역 변경
```
PR 제목: "refactor: 로그인 UI & ViewModel"
변경:
  - app/src/main/java/.../ui/screen/Login.kt
  - app/src/main/java/.../viewmodel/LoginViewModel.kt

자동 라벨:
├─ ♻️ refactor
├─ 🎨 UI
└─ ⚙️ ViewModel
```

---

## ✅ 체크리스트

PR 생성 전 확인:
- [ ] 커밋 메시지 표준 형식? (`feat:`, `fix:`, etc.)
- [ ] 브랜치명 올바른가? (`feature/*`, `fix/*`, etc.)
- [ ] 변경 파일 논리적 그룹화?
- [ ] PR 제목 명확한가?

---

## 📞 문제 해결

### 라벨 추가 안 됨
1. `Setup Labels` 워크플로우 완료 확인
2. PR 제목 형식 확인
3. 파일 경로 정확성 확인
4. Actions 탭에서 로그 확인

### 워크플로우 실행 안 됨
1. Secrets 등록 확인:
   - GOOGLE_SERVICES_JSON
   - GEMINI_API_KEY
   - LAW_API_KEY
2. 브랜치가 main/develop인지 확인
3. GitHub Settings → Secrets and variables → Actions에서 확인

---

**작성일**: 2026년 2월 27일  
**최종 수정**: 2026년 2월 27일 (워크플로우 최적화)  
**개선 사항**: setup-gradle.yml 재사용 워크플로우로 코드 중복 75% 감소

