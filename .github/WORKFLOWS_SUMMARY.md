# 🔄 GitHub Actions 워크플로우 요약

## 📌 핵심 기능

### 자동 라벨링 시스템

```
PR 생성
  ↓
auto-label.yml 워크플로우 실행
  ↓
  ├─ PR 제목 분석 (feat:, fix:, refactor: 등)
  ├─ 변경된 파일 경로 분석
  └─ 해당 라벨 자동 추가
  ↓
PR에 라벨 자동 표시 ✅
```

---

## 🎯 빠른 시작

### 1단계: 라벨 초기화 (처음 한 번만)

```bash
# GitHub Actions 탭 → Setup Labels → Run workflow
# 또는 develop 브랜치에 push
git push origin develop
```

### 2단계: PR 생성 시 자동 라벨 추가

```bash
# feature 브랜치에서 커밋 (커밋 타입 명시)
git commit -m "feat: 새로운 기능"

# PR 생성하면 자동으로 라벨이 추가됩니다!
```

---

## 📊 라벨 자동 추가 규칙

### 커밋 타입별 라벨

| 커밋 타입 | 예시 | 추가되는 라벨 |
|----------|------|--------------|
| `feat:` | `feat: 로그인 기능` | ✨ feature |
| `fix:` | `fix: 버그 수정` | 🐛 bug |
| `refactor:` | `refactor: 코드 정리` | ♻️ refactor |
| `docs:` | `docs: README 수정` | 📚 Documentation |
| `chore:` | `chore: 의존성 업데이트` | 🧹 chore |
| `style:` | `style: 포매팅` | 💅 style |
| `perf:` | `perf: 성능 개선` | ⚡ performance |

### 파일 경로별 라벨

| 변경 경로 | 추가되는 라벨 |
|----------|--------------|
| `app/src/main/java/.../ui/` | 🎨 UI |
| `app/src/main/java/.../viewmodel/` | ⚙️ ViewModel |
| `app/src/main/java/.../domain/` | 📦 Domain |
| `app/src/main/java/.../repository/` | 🗄️ Repository |
| `app/src/main/java/.../util/` | 🛠️ Utility |
| `app/src/test/` 또는 `androidTest/` | ✅ Test |
| `build.gradle` | 🔧 build |
| `.github/workflows/` | 🔄 CI/CD |
| `README.md`, `CONTRIBUTING.md` | 📚 Documentation |

---

## 📝 예시 시나리오

### 예시 1: UI 버그 수정

```bash
# PR 제목: "fix: 로그인 화면 레이아웃 버그"
# 변경 파일: app/src/main/java/com/easylaw/app/ui/screen/Login/LoginView.kt

# 자동 추가 라벨:
# 🐛 bug (제목에서 "fix:" 감지)
# 🎨 UI (ui/ 경로 감지)
```

### 예시 2: ViewModel 새 기능

```bash
# PR 제목: "feat: 사용자 프로필 ViewModel 추가"
# 변경 파일: app/src/main/java/com/easylaw/app/viewmodel/UserProfileViewModel.kt

# 자동 추가 라벨:
# ✨ feature (제목에서 "feat:" 감지)
# ⚙️ ViewModel (viewmodel/ 경로 감지)
```

### 예시 3: 테스트 추가

```bash
# PR 제목: "test: 로그인 ViewModel 유닛 테스트 추가"
# 변경 파일: app/src/test/java/com/easylaw/app/viewmodel/LoginViewModelTest.kt

# 자동 추가 라벨:
# ✅ Test (test/ 경로 감지)
```

---

## 🚀 추가 팁

### 1. 여러 라벨을 동시에 추가하려면

변경 사항이 여러 영역에 걸쳐 있으면 자동으로 여러 라벨이 추가됩니다:

```bash
# PR 제목: "refactor: 로그인 UI 및 ViewModel 리팩토링"
# 변경 파일: 
#   - app/src/main/java/com/easylaw/app/ui/screen/Login/LoginView.kt
#   - app/src/main/java/com/easylaw/app/viewmodel/LoginViewModel.kt

# 자동 추가 라벨:
# ♻️ refactor
# 🎨 UI
# ⚙️ ViewModel
```

### 2. 라벨 수동 추가

자동으로 추가되지 않은 라벨은 PR 페이지에서 수동 추가:
1. PR 페이지 오른쪽 → `Labels` 클릭
2. 원하는 라벨 선택

### 3. 라벨로 PR 필터링

PR 목록에서:
```
github.com/yourrepo/pulls?labels=🐛%20bug
github.com/yourrepo/pulls?labels=✨%20feature
github.com/yourrepo/pulls?labels=🎨%20UI
```

---

## ✅ 체크리스트

PR을 생성하기 전에 확인하세요:

- [ ] 커밋 메시지가 표준 형식인가? (`feat:`, `fix:`, etc.)
- [ ] 브랜치명이 올바른가? (`feature/*`, `fix/*`, etc.)
- [ ] 변경 파일이 논리적으로 그룹화되어 있나?
- [ ] PR 제목이 변경 사항을 명확히 설명하는가?

---

## 📞 문제 해결

### 라벨이 추가되지 않음

**확인 사항:**
1. `Setup Labels` 워크플로우가 완료되었는가?
2. PR 제목이 올바른 형식인가?
3. 변경된 파일 경로가 정확한가?

**해결:**
```bash
# 1. Actions 탭에서 워크플로우 로그 확인
# 2. 필요시 라벨을 수동으로 추가
# 3. 레포지토리 권한 설정 확인
```

---

**작성일**: 2026년 2월 27일  
**유지보수**: neobrix-mobile-team

