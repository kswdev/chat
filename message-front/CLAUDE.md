# CLAUDE.md

This file provides guidance to Claude Code when working with the `message-front` project.

## Project Overview

React + TypeScript 브라우저 클라이언트. `message-client` Java CLI 클라이언트와 동일한 기능을 웹 UI로 제공한다.
백엔드와 REST API(Axios) 및 WebSocket으로 통신하며, 실시간 채팅·채널 관리·사용자 연결 기능을 지원한다.

## Tech Stack

| 항목 | 내용 |
|---|---|
| 빌드 도구 | Vite 5 |
| UI 라이브러리 | React 18 |
| 언어 | TypeScript 5 (strict) |
| 스타일 | Tailwind CSS 3 |
| HTTP 클라이언트 | Axios |
| 실시간 통신 | 네이티브 WebSocket API |
| 패키지 매니저 | npm |

## API Server Addresses

| 대상 | 주소 |
|---|---|
| REST API (web-gateway) | `http://localhost:8080` |
| WebSocket | `ws://localhost:8080/ws/v1/message` |

설정 위치:
- REST base URL: `src/api/axiosInstance.ts` → `BASE_URL`
- WebSocket URL: `src/contexts/WebSocketContext.tsx` → `WS_URL`

### REST Endpoints

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/v1/auth/login` | 로그인 → 토큰(JWT) 반환 |
| POST | `/api/v1/auth/logout` | 로그아웃 |
| POST | `/api/v1/user/register` | 회원가입 |
| POST | `/api/v1/user/unregister` | 회원 탈퇴 |

### WebSocket 인증

브라우저 WebSocket은 커스텀 헤더를 지원하지 않으므로 토큰을 쿼리 파라미터로 전달한다.

```
ws://localhost:8080/ws/v1/message?token={JWT}
```

## Build & Run

```bash
# 의존성 설치
npm install

# 개발 서버 (http://localhost:3000)
npm run dev

# 프로덕션 빌드
npm run build

# 빌드 결과 미리보기
npm run preview
```

## Directory Structure

```
message-front/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
├── postcss.config.js
└── src/
    ├── main.tsx                  # 엔트리포인트 (Provider 트리 구성)
    ├── App.tsx                   # 로그인 여부에 따라 AuthPage / ChatApp 렌더링
    ├── index.css                 # Tailwind 베이스 + 스크롤바 커스텀
    │
    ├── types/
    │   └── index.ts              # 모든 도메인 타입 및 WebSocket 메시지 타입 정의
    │
    ├── api/
    │   ├── axiosInstance.ts      # Axios 인스턴스 (BASE_URL, 토큰 자동 첨부 인터셉터)
    │   └── authApi.ts            # login / logout / register / unregister
    │
    ├── contexts/
    │   ├── AuthContext.tsx       # 로그인 상태, 토큰·username (sessionStorage 저장)
    │   ├── WebSocketContext.tsx  # WS 연결 생명주기, Keep-Alive(60초), 메시지 핸들러 등록·디스패치
    │   └── ChatContext.tsx       # 채널·메시지·연결 상태 관리, WS 핸들러 등록, 액션 함수 제공
    │
    └── components/
        ├── auth/
        │   ├── AuthPage.tsx      # 로그인/회원가입 전환 래퍼
        │   ├── LoginForm.tsx     # 로그인 폼
        │   └── RegisterForm.tsx  # 회원가입 폼
        │
        ├── sidebar/
        │   ├── Sidebar.tsx       # 사이드바 레이아웃 (WS 연결 후 초기 데이터 로드)
        │   ├── ChannelList.tsx   # 채널 목록, 생성·참가·입장·나가기·초대코드 조회
        │   ├── ConnectionList.tsx # 연결된 사용자 목록, 초대·연결끊기·내 초대코드
        │   └── PendingInviteList.tsx # 대기 중인 초대 (수락·거절)
        │
        ├── chat/
        │   ├── ChatView.tsx      # 채널 헤더 + 메시지 영역 + 입력창
        │   ├── MessageList.tsx   # 스크롤 메시지 목록, 상단 도달 시 이전 메시지 로드
        │   ├── MessageItem.tsx   # 개별 메시지 (내 메시지 / 상대 메시지 구분)
        │   └── MessageInput.tsx  # 텍스트 입력, Enter 전송 / Shift+Enter 줄바꿈
        │
        └── shared/
            ├── Modal.tsx              # 재사용 모달 (ESC 닫기, 바깥 클릭 닫기)
            ├── InviteCodeDisplay.tsx  # 초대 코드 표시 + 클립보드 복사
            └── NotificationBar.tsx   # 우하단 토스트 알림 (3.5초 자동 사라짐)
```

## WebSocket Message Types

`src/types/index.ts`에 서버와 공유하는 모든 메시지 타입이 정의되어 있다.
실제 문자열 값은 `message-common` 모듈의 `MessageType` 상수와 일치해야 한다.

| 방향 | 주요 타입 |
|---|---|
| 클라이언트 → 서버 | `WRITE_MESSAGE`, `CREATE_REQUEST`, `ENTER_REQUEST`, `LEAVE_REQUEST`, `JOIN_REQUEST`, `QUIT_REQUEST`, `FETCH_CHANNELS_REQUEST`, `INVITE_REQUEST`, `ACCEPT_REQUEST`, `REJECT_REQUEST`, `DISCONNECT_REQUEST`, `FETCH_USER_CONNECTIONS_REQUEST`, `FETCH_USER_INVITE_CODE_REQUEST`, `FETCH_CHANNEL_INVITE_CODE_REQUEST`, `FETCH_MESSAGES_REQUEST`, `READ_MESSAGE_ACK`, `KEEP_ALIVE` |
| 서버 → 클라이언트 (응답) | `*_RESPONSE` 형태 |
| 서버 → 클라이언트 (알림) | `NOTIFY_MESSAGE`, `ASK_INVITE`, `NOTIFY_ACCEPT`, `NOTIFY_JOIN` |

## State Management

별도 상태 관리 라이브러리 없이 React Context + useReducer 조합을 사용한다.

| Context | 역할 |
|---|---|
| `AuthContext` | username, token, 인증 액션 |
| `WebSocketContext` | WS 연결 상태, send 함수, 핸들러 등록 |
| `ChatContext` | 채널·메시지·연결 상태 전체, WS 메시지 핸들러, 모든 채팅 액션 |

Provider 중첩 순서 (`main.tsx`):
```
AuthProvider > WebSocketProvider > ChatProvider
```
