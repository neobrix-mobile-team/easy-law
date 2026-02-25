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

### Frontend
- **Framework**: React Native / Flutter (모바일 우선)
- **상태 관리**: Redux / Zustand
- **UI**: Material Design 3 + 커스텀 테마
- **지도**: Naver Map SDK
- **음성**: React Native Voice

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js / NestJS
- **Database**: Firebase Firestore (NoSQL)
- **인증**: Firebase Authentication
- **AI/ML**: Google Vision API, Google Cloud NLP, OpenAI LLM
- **배포**: Firebase Hosting / Google Cloud Run

### External APIs
- 🔐 **Firebase**: Auth, Firestore, Hosting
- 🗺️ **Naver Maps API**: 위치 기반 기관 정보
- 👁️ **Google Vision API**: OCR
- 🧠 **LLM**: OpenAI / Google Gemini (용어 풀이)
- 🔊 **TTS**: Google Cloud TTS / Azure Speech Services

---

## 📂 프로젝트 구조

```
easy-law/
├── frontend/                    # React Native / Flutter 프로젝트
│   ├── src/
│   │   ├── screens/            # 화면 컴포넌트
│   │   ├── components/         # 재사용 가능한 컴포넌트
│   │   ├── navigation/         # 네비게이션 구조
│   │   ├── services/           # API 호출 로직
│   │   ├── store/              # 상태 관리 (Redux/Zustand)
│   │   ├── theme/              # 테마 설정 (Color, Font)
│   │   └── utils/              # 유틸리티 함수
│   └── package.json
│
├── backend/                     # Node.js Express 백엔드
│   ├── src/
│   │   ├── routes/             # API 라우트
│   │   ├── controllers/        # 비즈니스 로직
│   │   ├── services/           # 서비스 계층
│   │   ├── models/             # 데이터 모델 (Firestore)
│   │   ├── middleware/         # 인증, 에러 핸들링
│   │   ├── config/             # 환경 설정 (Firebase, API keys)
│   │   └── utils/              # 유틸리티
│   └── package.json
│
├── docs/                        # 설명서
│   ├── API.md                  # API 문서
│   ├── ARCHITECTURE.md         # 아키텍처
│   └── SETUP.md                # 개발 환경 설정
│
├── .github/
│   ├── ISSUE_TEMPLATE/         # 이슈 템플릿
│   ├── PULL_REQUEST_TEMPLATE/  # PR 템플릿
│   └── workflows/              # CI/CD (GitHub Actions)
│
├── LICENSE                      # MIT License
├── README.md                    # 프로젝트 개요
├── CONTRIBUTING.md             # 기여 가이드
└── .gitignore                  # Git 무시 파일

```

---

## 🚀 설치 및 실행 (2주 MVP)

### Frontend 시작
```bash
cd frontend
npm install
npm start                # 또는 npm run android / npm run ios (React Native)
```

### Backend 시작
```bash
cd backend
npm install
firebase init           # Firebase 설정
npm run dev            # 개발 서버 실행
```

### Firebase 설정
```bash
firebase login
firebase init firestore
firebase init functions
```

---

## 📅 개발 일정 (2주)

| 주차 | 목표 | 담당 |
|------|------|------|
| **Week 1** | Auth (구글 로그인), 사용자 관리, 기본 UI 구성, 디자인 시스템 | 전체 팀 |
| **Week 2** | Decision Tree UX, OCR/TTS (간단한 버전), 커뮤니티 기본 기능, 배포 | 전체 팀 |

### 우선순위 (MoSCoW)
- **Must**: Auth, 기본 UX, 디자인 시스템
- **Should**: OCR, TTS, Decision Tree
- **Could**: 커뮤니티, 포인트 시스템, 지도 (주차별 여유 시 진행)
- **Won't**: 고급 AI 기능, 신뢰도 배지 (v2 이후)

---

## 🎓 기여 가이드

자세한 내용은 [CONTRIBUTING.md](./CONTRIBUTING.md) 참조

### 커밋 메시지 규칙
```
feat: 기능 추가
fix: 버그 수정
docs: 문서 작성
style: 코드 포맷팅
refactor: 코드 정리
test: 테스트 추가
chore: 빌드, 의존성 관리
```

### 브랜치 규칙
- `main`: 배포 가능한 안정 버전
- `develop`: 개발 중인 버전
- `feature/기능명`: 새로운 기능
- `fix/버그명`: 버그 수정

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
