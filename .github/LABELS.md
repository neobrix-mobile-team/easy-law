# 🏷️ GitHub Actions 자동 라벨 설정 가이드

이 문서는 **EasyLaw** 프로젝트의 자동 라벨 설정 워크플로우에 대해 설명합니다.

## 📋 목차

1. [개요](#개요)
2. [워크플로우](#워크플로우)
3. [지원되는 라벨](#지원되는-라벨)
4. [사용 방법](#사용-방법)
5. [커스터마이징](#커스터마이징)

---

## 개요

GitHub Actions를 활용하여 Pull Request가 생성되면 자동으로 적절한 라벨을 추가합니다. 이를 통해:

- ✅ **수동 라벨 추가 작업 제거**
- ✅ **일관된 라벨 관리**
- ✅ **PR 필터링 및 자동화 단순화**
- ✅ **팀의 워크플로우 표준화**

---

## 워크플로우

### 1. 🏷️ Setup Labels (`setup-labels.yml`)

**목적**: 저장소에 필요한 모든 라벨을 자동으로 생성합니다.

**트리거**:
- `workflow_dispatch`: 수동 실행
- `push to main/develop`: 자동 실행
- 파일 변경: `.github/workflows/setup-labels.yml`

**실행 주기**: 초기 1회 실행 후, 필요시 수동 실행

**권한 필요**: `issues: write`

### 2. 🎯 Auto Label (`auto-label.yml`)

**목적**: PR 생성 시 변경 사항을 분석하여 라벨을 자동 추가합니다.

**트리거**:
- `pull_request`: opened, synchronize, reopened

**분석 항목**:
1. **PR 제목** (커밋 타입 감지)
   - `feat:` → `✨ feature`
   - `fix:` → `🐛 bug`
   - `refactor:` → `♻️ refactor`
   - `docs:` → `📚 Documentation`
   - `chore:` → `🧹 chore`
   - `style:` → `💅 style`
   - `perf:` → `⚡ performance`

2. **변경된 파일 경로** (모듈 감지)
   - `app/src/main/java/com/easylaw/app/ui/` → `🎨 UI`
   - `app/src/main/java/com/easylaw/app/viewmodel/` → `⚙️ ViewModel`
   - `app/src/main/java/com/easylaw/app/domain/` → `📦 Domain`
   - `app/src/main/java/com/easylaw/app/repository/` → `🗄️ Repository`
   - `app/src/main/java/com/easylaw/app/util/` → `🛠️ Utility`
   - `app/src/test/` 또는 `app/src/androidTest/` → `✅ Test`
   - `build.gradle` → `🔧 build`
   - `.github/workflows/` → `🔄 CI/CD`
   - `README`, `CONTRIBUTING`, `DEVELOPMENT` → `📚 Documentation`

**권한 필요**: `pull-requests: write`, `contents: read`

---

## 지원되는 라벨

| 라벨 | 색상 | 설명 |
|-----|------|------|
| 🐛 bug | `d73a49` (빨강) | 버그 수정 |
| ✨ feature | `2da44e` (초록) | 새로운 기능 |
| ♻️ refactor | `0366d6` (파랑) | 코드 리팩토링 |
| 📚 Documentation | `6f42c1` (보라) | 문서 수정 |
| 🧹 chore | `ffc107` (노랑) | 일상적인 유지보수 |
| 💅 style | `fbca04` (밝은노랑) | 코드 스타일 및 포매팅 |
| ⚡ performance | `ff6600` (주황) | 성능 개선 |
| 🎨 UI | `e00ff7` (분홍) | UI/UX 관련 |
| ⚙️ ViewModel | `1f6feb` (파랑) | ViewModel 관련 |
| 📦 Domain | `1f6feb` (파랑) | Domain 레이어 관련 |
| 🗄️ Repository | `1f6feb` (파랑) | Repository 관련 |
| 🛠️ Utility | `1f6feb` (파랑) | Utility 함수/클래스 |
| ✅ Test | `0075ca` (진파랑) | 테스트 관련 |
| 🔧 build | `a2eeef` (하늘) | 빌드 설정 |
| 🔄 CI/CD | `a2eeef` (하늘) | CI/CD 파이프라인 |
| 🚀 ready | `7cb342` (녹색) | 병합 준비 완료 |
| ⏸️ on-hold | `d4a5a5` (회색) | 진행 보류 중 |

---

## 사용 방법

### Step 1: 초기 라벨 생성

첫 배포 시 라벨을 생성해야 합니다:

**방법 A: 자동 실행**
```bash
git push origin develop
# 또는 main 브랜치에 push하면 자동 실행됨
```

**방법 B: 수동 실행**
1. GitHub 저장소 → `Actions` 탭
2. `🏷️ Setup Labels` 워크플로우 선택
3. `Run workflow` 클릭
4. `develop` 브랜치 선택 후 실행

### Step 2: PR 생성 시 자동 라벨 추가

```bash
# 1. 새 브랜치에서 작업
git checkout -b feature/새기능

# 2. 변경사항 커밋 (커밋 타입 명시)
git commit -m "feat: 새로운 로그인 기능"
git commit -m "fix: 로그인 버그 수정"

# 3. 푸시 및 PR 생성
git push origin feature/새기능

# GitHub에서 PR 생성하면 자동으로 라벨이 추가됩니다!
```

### Step 3: 라벨 확인

PR 페이지에서 `Labels` 섹션을 확인하면 자동 추가된 라벨을 볼 수 있습니다.

---

## 커스터마이징

### 새로운 라벨 추가

`.github/workflows/setup-labels.yml` 파일 수정:

```yaml
const labels = [
  // 기존 라벨들...
  
  // 새로운 라벨 추가
  { 
    name: '🎯 새라벨', 
    color: 'YOUR_HEX_COLOR', 
    description: '라벨 설명' 
  },
];
```

### 자동 라벨 규칙 수정

`.github/workflows/auto-label.yml` 파일 수정:

```javascript
// PR 제목 기반 라벨 추가 규칙
if (title.includes('새로운키워드')) {
  labelsToAdd.push('🎯 새라벨');
}

// 파일 경로 기반 라벨 추가 규칙
if (changedFiles.some(f => f.includes('새로운경로'))) {
  labelsToAdd.push('🎯 새라벨');
}
```

---

## 트러블슈팅

### Q: 라벨이 자동 추가되지 않습니다

**A**: 다음을 확인하세요:
1. `setup-labels.yml`이 실행되었는지 확인
2. GitHub 저장소의 `Actions` 탭에서 워크플로우 실행 기록 확인
3. 저장소 설정 → `Actions` → `Permissions` 확인 (필요한 권한 부여)

### Q: 원하는 라벨이 추가되지 않습니다

**A**: 다음을 확인하세요:
1. PR 제목이 올바른 커밋 타입으로 시작하는지 확인
2. 변경된 파일 경로가 규칙과 일치하는지 확인
3. `auto-label.yml` 워크플로우의 로그 확인

### Q: 라벨을 수동으로 추가해야 합니다

**A**: 다음 경우 수동으로 추가하세요:
1. 복합적인 작업 (예: UI + ViewModel + 테스트)
2. 중요한 상태 라벨 (예: 🚀 ready, ⏸️ on-hold)

---

## 참고 문서

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [github-script 액션](https://github.com/actions/github-script)
- [EasyLaw CONTRIBUTING.md](../CONTRIBUTING.md)

---

**Happy Labeling! 🏷️**

