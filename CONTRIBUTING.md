# EasyLaw에 기여하기 👋

모든 레벨의 개발자를 환영합니다! 함께 더 나은 법률 정보 플랫폼을 만들어가요.

---

## 📋 시작하기

### 1️⃣ 포크 & 클론
```bash
# GitHub에서 리포를 포크한 후
git clone https://github.com/YOUR-USERNAME/easy-law.git
cd easy-law
git remote add upstream https://github.com/neobrix-mobile-team/easy-law.git
```

### 2️⃣ 개발 환경 설정
```bash
# Frontend 설정
cd frontend
npm install
npm start

# Backend 설정 (새로운 터미널)
cd backend
npm install
npm run dev
```

---

## 🔄 개발 워크플로우

### 1. 이슈 선택
- [GitHub Issues](https://github.com/neobrix-mobile-team/easy-law/issues)에서 관심 있는 이슈를 선택
- `good first issue` 라벨은 초보자 친화적입니다

### 2. 브랜치 생성
```bash
git checkout -b feature/기능명
# 또는
git checkout -b fix/버그명
```

**브랜치 네이밍 규칙**:
- `feature/description` - 새로운 기능
- `fix/description` - 버그 수정
- `docs/description` - 문서 작성
- `refactor/description` - 코드 정리

### 3. 코드 작성 및 커밋
```bash
git add .
git commit -m "feat: 기능 설명"
```

**커밋 메시지 규칙** (Conventional Commits):
```
feat: 기능 추가
fix: 버그 수정
docs: 문서 작성
style: 코드 포맷팅 (기능 변화 없음)
refactor: 코드 정리 (기능 변화 없음)
test: 테스트 추가
chore: 빌드, 의존성 관리
ci: CI/CD 설정 변경
```

**예시**:
```
feat: 구글 소셜 로그인 기능 추가 (#123)

- Firebase Auth 연동
- 사용자 프로필 자동 생성
- 다국어 지원 (한글, 영어)

Closes #123
```

### 4. 변경 사항 푸시
```bash
git push origin feature/기능명
```

### 5. Pull Request 생성
- GitHub에서 `Compare & pull request` 클릭
- PR 템플릿을 따라 작성
- 관련 이슈를 링크 (`fixes #123`)
- 최소 1명의 리뷰어 지정

### 6. 리뷰 & 병합
- 팀원의 리뷰를 받습니다
- 요청받은 변경 사항을 적용합니다
- 리뷰 승인 후 병합됩니다

---

## ✅ 코드 스타일

### Frontend (React Native / React)
```javascript
// ✅ 좋은 예
const UserProfile = ({ userId, name }) => {
  const [user, setUser] = useState(null);
  
  useEffect(() => {
    fetchUser(userId);
  }, [userId]);
  
  return <View className="user-profile">{name}</View>;
};

// ❌ 나쁜 예
const UserProfile = (props) => {
  let user;
  // 중요한 로직이 빠짐
  return <div>{props.name}</div>;
};
```

### Backend (Node.js)
```javascript
// ✅ 좋은 예
router.post('/api/auth/login', authenticate, async (req, res) => {
  try {
    const user = await loginUser(req.body);
    res.json({ success: true, data: user });
  } catch (error) {
    res.status(400).json({ error: error.message });
  }
});

// ❌ 나쁜 예
router.post('/login', (req, res) => {
  let user;
  // 에러 핸들링 없음
  res.send(user);
});
```

---

## 🧪 테스트

### Frontend 테스트
```bash
cd frontend
npm test
```

### Backend 테스트
```bash
cd backend
npm test
```

**테스트를 작성해주세요!** PR에 테스트 코드가 포함되면 리뷰가 빨라집니다.

---

## 📚 문서 작성

- API 문서는 `docs/API.md`에 추가
- 구조 변경은 `docs/ARCHITECTURE.md` 업데이트
- README에 새 기능이 추가되면 반영

---

## 🚫 하지 말아야 할 것

- ❌ 직접 `main` 브랜치에 푸시
- ❌ 커밋 메시지 없이 병합
- ❌ 테스트 없이 PR 제출
- ❌ 무관한 파일 변경 (`.DS_Store`, `node_modules` 등)
- ❌ 민감한 정보 커밋 (API 키, 비밀번호 등)

---

## 🐛 버그 신고

[Bug Report 이슈 템플릿](https://github.com/neobrix-mobile-team/easy-law/issues/new?template=bug_report.md)을 사용해주세요.

필수 정보:
- 재현 방법
- 예상 동작
- 실제 동작
- 환경 정보 (OS, 버전, 기기 등)

---

## 💡 기능 제안

[Feature Request 이슈 템플릿](https://github.com/neobrix-mobile-team/easy-law/issues/new?template=feature_request.md)을 사용해주세요.

---

## 🤝 도움이 필요하신가요?

- GitHub Discussions에서 질문 가능
- 팀 채팅: [Slack/Discord 링크]
- 이메일: team@neobrix.dev

---

## 🏆 기여자 인정

모든 기여자를 `CONTRIBUTORS.md`에 기록합니다!

---

**감사합니다! 🙏 Happy Coding!**
