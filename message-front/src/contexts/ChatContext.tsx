import React, {
  createContext,
  useContext,
  useReducer,
  useCallback,
  useEffect,
  useRef,
} from 'react';
import { useWebSocket } from './WebSocketContext';
import { useAuth } from './AuthContext';
import type {
  Channel,
  ChatMessage,
  Connection,
  InboundMessage,
  WriteMessageAck,
  MessageNotification,
  CreateResponse,
  EnterResponse,
  LeaveResponse,
  JoinResponse,
  QuitResponse,
  FetchChannelsResponse,
  FetchMessagesResponse,
  FetchChannelInviteCodeResponse,
  InviteResponse,
  AcceptResponse,
  RejectResponse,
  DisconnectResponse,
  FetchUserConnectionsResponse,
  FetchUserInviteCodeResponse,
  InviteNotification,
  AcceptNotification,
  JoinNotification,
  ErrorResponse,
} from '../types';

// ──────────────────────────────────────────────
// State & Reducer
// ──────────────────────────────────────────────

interface ChatState {
  channels: Channel[];
  /** channelId → messages (sorted by messageSeqId) */
  messages: Record<number, ChatMessage[]>;
  /** channelId → lastReceivedSeqId */
  lastSeqId: Record<number, number>;
  currentChannel: Channel | null;
  acceptedConnections: Connection[];
  pendingConnections: Connection[];
  userInviteCode: string | null;
  channelInviteCodes: Record<number, string>;
  notifications: string[];
  error: string | null;
}

type ChatAction =
  | { type: 'SET_CHANNELS'; channels: Channel[] }
  | { type: 'ADD_CHANNEL'; channel: Channel }
  | { type: 'REMOVE_CHANNEL'; channelId: number }
  | { type: 'SET_CURRENT_CHANNEL'; channel: Channel | null }
  | { type: 'ADD_MESSAGE'; message: ChatMessage }
  | { type: 'PREPEND_MESSAGES'; channelId: number; messages: ChatMessage[] }
  | { type: 'SET_LAST_SEQ_ID'; channelId: number; seqId: number }
  | { type: 'SET_ACCEPTED_CONNECTIONS'; connections: Connection[] }
  | { type: 'SET_PENDING_CONNECTIONS'; connections: Connection[] }
  | { type: 'REMOVE_ACCEPTED_CONNECTION'; username: string }
  | { type: 'REMOVE_PENDING_CONNECTION'; username: string }
  | { type: 'SET_USER_INVITE_CODE'; code: string }
  | { type: 'SET_CHANNEL_INVITE_CODE'; channelId: number; code: string }
  | { type: 'ADD_NOTIFICATION'; message: string }
  | { type: 'CLEAR_NOTIFICATIONS' }
  | { type: 'SET_ERROR'; message: string | null };

function chatReducer(state: ChatState, action: ChatAction): ChatState {
  switch (action.type) {
    case 'SET_CHANNELS':
      return { ...state, channels: action.channels };
    case 'ADD_CHANNEL':
      return {
        ...state,
        channels: state.channels.find(
          (c) => c.channelId === action.channel.channelId,
        )
          ? state.channels
          : [...state.channels, action.channel],
      };
    case 'REMOVE_CHANNEL':
      return {
        ...state,
        channels: state.channels.filter(
          (c) => c.channelId !== action.channelId,
        ),
        currentChannel:
          state.currentChannel?.channelId === action.channelId
            ? null
            : state.currentChannel,
      };
    case 'SET_CURRENT_CHANNEL':
      return { ...state, currentChannel: action.channel };
    case 'ADD_MESSAGE': {
      const channelId = action.message.channelId;
      const existing = state.messages[channelId] ?? [];
      // 중복 메시지 무시
      if (existing.some((m) => m.messageSeqId === action.message.messageSeqId)) {
        return state;
      }
      const updated = [...existing, action.message].sort(
        (a, b) => a.messageSeqId - b.messageSeqId,
      );
      return {
        ...state,
        messages: { ...state.messages, [channelId]: updated },
        lastSeqId: {
          ...state.lastSeqId,
          [channelId]: Math.max(
            state.lastSeqId[channelId] ?? 0,
            action.message.messageSeqId,
          ),
        },
      };
    }
    case 'PREPEND_MESSAGES': {
      const { channelId, messages } = action;
      const existing = state.messages[channelId] ?? [];
      const existingIds = new Set(existing.map((m) => m.messageSeqId));
      const newMessages = messages.filter(
        (m) => !existingIds.has(m.messageSeqId),
      );
      const merged = [...newMessages, ...existing].sort(
        (a, b) => a.messageSeqId - b.messageSeqId,
      );
      return {
        ...state,
        messages: { ...state.messages, [channelId]: merged },
      };
    }
    case 'SET_LAST_SEQ_ID':
      return {
        ...state,
        lastSeqId: { ...state.lastSeqId, [action.channelId]: action.seqId },
      };
    case 'SET_ACCEPTED_CONNECTIONS':
      return { ...state, acceptedConnections: action.connections };
    case 'SET_PENDING_CONNECTIONS':
      return { ...state, pendingConnections: action.connections };
    case 'REMOVE_ACCEPTED_CONNECTION':
      return {
        ...state,
        acceptedConnections: state.acceptedConnections.filter(
          (c) => c.username !== action.username,
        ),
      };
    case 'REMOVE_PENDING_CONNECTION':
      return {
        ...state,
        pendingConnections: state.pendingConnections.filter(
          (c) => c.username !== action.username,
        ),
      };
    case 'SET_USER_INVITE_CODE':
      return { ...state, userInviteCode: action.code };
    case 'SET_CHANNEL_INVITE_CODE':
      return {
        ...state,
        channelInviteCodes: {
          ...state.channelInviteCodes,
          [action.channelId]: action.code,
        },
      };
    case 'ADD_NOTIFICATION':
      return {
        ...state,
        notifications: [...state.notifications.slice(-49), action.message],
      };
    case 'CLEAR_NOTIFICATIONS':
      return { ...state, notifications: [] };
    case 'SET_ERROR':
      return { ...state, error: action.message };
    default:
      return state;
  }
}

const initialState: ChatState = {
  channels: [],
  messages: {},
  lastSeqId: {},
  currentChannel: null,
  acceptedConnections: [],
  pendingConnections: [],
  userInviteCode: null,
  channelInviteCodes: {},
  notifications: [],
  error: null,
};

// ──────────────────────────────────────────────
// Context
// ──────────────────────────────────────────────

interface ChatContextValue extends ChatState {
  // 채널 액션
  fetchChannels: () => void;
  createChannel: (title: string, usernames: string[]) => void;
  joinChannel: (inviteCode: string) => void;
  enterChannel: (channelId: number) => void;
  leaveChannel: () => void;
  quitChannel: (channelId: number) => void;
  fetchChannelInviteCode: (channelId: number) => void;
  // 메시지 액션
  sendMessage: (content: string) => Promise<void>;
  fetchMessages: (channelId: number, start: number, end: number) => void;
  // 연결 액션
  fetchAcceptedConnections: () => void;
  fetchPendingConnections: () => void;
  inviteUser: (inviteCode: string) => void;
  acceptUser: (username: string) => void;
  rejectUser: (username: string) => void;
  disconnectUser: (username: string) => void;
  fetchUserInviteCode: () => void;
  // 에러 클리어
  clearError: () => void;
}

const ChatContext = createContext<ChatContextValue | null>(null);

// ──────────────────────────────────────────────
// Provider
// ──────────────────────────────────────────────

const MAX_SERIAL_WAIT_MS = 3000;

export function ChatProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(chatReducer, initialState);
  const { send, addHandler, removeHandler } = useWebSocket();
  useAuth();
  const stateRef = useRef(state);
  const pendingSerials = useRef<
    Map<number, { resolve: () => void; reject: (r: string) => void }>
  >(new Map());
  const serialCounter = useRef(0);

  // stateRef를 항상 최신 state로 유지
  useEffect(() => {
    stateRef.current = state;
  }, [state]);

  // ── WebSocket 핸들러 등록 ──────────────────────

  useEffect(() => {
    const handlers: Array<[string, (msg: InboundMessage) => void]> = [
      // 채널 응답
      [
        'CREATE_RESPONSE',
        (msg) => {
          const m = msg as CreateResponse;
          dispatch({
            type: 'ADD_CHANNEL',
            channel: { channelId: m.channelId, title: m.title, headCount: 1 },
          });
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `채널 '${m.title}'이 생성되었습니다.`,
          });
        },
      ],
      [
        'JOIN_RESPONSE',
        (msg) => {
          const m = msg as JoinResponse;
          dispatch({
            type: 'ADD_CHANNEL',
            channel: { channelId: m.channelId, title: m.title, headCount: 0 },
          });
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `채널 '${m.title}'에 참가했습니다.`,
          });
        },
      ],
      [
        'ENTER_RESPONSE',
        (msg) => {
          const m = msg as EnterResponse;
          const channel = stateRef.current.channels.find((c) => c.channelId === m.channelId) ?? {
            channelId: m.channelId,
            title: m.title,
            headCount: 0,
          };
          dispatch({ type: 'SET_CURRENT_CHANNEL', channel });
          // 읽지 않은 메시지가 있으면 가져오기
          if (
            m.lastChannelMessageSeqId > 0 &&
            m.lastChannelMessageSeqId > m.lastReadMessageSeqId
          ) {
            const start = m.lastReadMessageSeqId + 1;
            const end = m.lastChannelMessageSeqId;
            send({
              type: 'FETCH_MESSAGES_REQUEST',
              channelId: m.channelId,
              startMessageSeqId: start,
              endMessageSeqId: end,
            });
          }
          dispatch({
            type: 'SET_LAST_SEQ_ID',
            channelId: m.channelId,
            seqId: m.lastReadMessageSeqId,
          });
        },
      ],
      [
        'LEAVE_RESPONSE',
        (_msg) => {
          void (_msg as LeaveResponse);
          dispatch({ type: 'SET_CURRENT_CHANNEL', channel: null });
        },
      ],
      [
        'QUIT_RESPONSE',
        (msg) => {
          const m = msg as QuitResponse;
          dispatch({ type: 'REMOVE_CHANNEL', channelId: m.channelId });
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `채널을 나갔습니다.`,
          });
        },
      ],
      [
        'FETCH_CHANNELS_RESPONSE',
        (msg) => {
          const m = msg as FetchChannelsResponse;
          dispatch({ type: 'SET_CHANNELS', channels: m.channels });
        },
      ],
      [
        'FETCH_CHANNEL_INVITE_CODE_RESPONSE',
        (msg) => {
          const m = msg as FetchChannelInviteCodeResponse;
          dispatch({
            type: 'SET_CHANNEL_INVITE_CODE',
            channelId: m.channelId,
            code: m.inviteCode,
          });
        },
      ],
      // 메시지
      [
        'WRITE_MESSAGE_ACK',
        (msg) => {
          const m = msg as WriteMessageAck;
          const entry = pendingSerials.current.get(m.serial);
          if (entry) {
            entry.resolve();
            pendingSerials.current.delete(m.serial);
          }
        },
      ],
      [
        'NOTIFY_MESSAGE',
        (msg) => {
          const m = msg as MessageNotification;
          const lastSeq = stateRef.current.lastSeqId[m.channelId] ?? 0;

          // 갭 감지: 순서가 맞지 않으면 누락 메시지 조회
          if (m.messageSeqId > lastSeq + 1 && lastSeq > 0) {
            send({
              type: 'FETCH_MESSAGES_REQUEST',
              channelId: m.channelId,
              startMessageSeqId: lastSeq + 1,
              endMessageSeqId: m.messageSeqId - 1,
            });
          }

          dispatch({
            type: 'ADD_MESSAGE',
            message: {
              channelId: m.channelId,
              messageSeqId: m.messageSeqId,
              username: m.username,
              content: m.content,
            },
          });

          // ReadMessageAck 전송
          send({
            type: 'READ_MESSAGE_ACK',
            channelId: m.channelId,
            messageSeqId: m.messageSeqId,
          });
        },
      ],
      [
        'FETCH_MESSAGES_RESPONSE',
        (msg) => {
          const m = msg as FetchMessagesResponse;
          dispatch({
            type: 'PREPEND_MESSAGES',
            channelId: m.channelId,
            messages: m.messages,
          });
        },
      ],
      // 연결 관련
      [
        'INVITE_RESPONSE',
        (msg) => {
          const m = msg as InviteResponse;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `초대 요청 전송됨 (상태: ${m.status})`,
          });
        },
      ],
      [
        'ACCEPT_RESPONSE',
        (msg) => {
          const m = msg as AcceptResponse;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `${m.username} 님과 연결되었습니다.`,
          });
          dispatch({ type: 'REMOVE_PENDING_CONNECTION', username: m.username });
          // 수락 후 연결 목록 갱신
          send({ type: 'FETCH_USER_CONNECTIONS_REQUEST', status: 'ACCEPTED' });
        },
      ],
      [
        'REJECT_RESPONSE',
        (msg) => {
          const m = msg as RejectResponse;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `${m.username} 님의 초대를 거절했습니다.`,
          });
          dispatch({ type: 'REMOVE_PENDING_CONNECTION', username: m.username });
        },
      ],
      [
        'DISCONNECT_RESPONSE',
        (msg) => {
          const m = msg as DisconnectResponse;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `${m.username} 님과 연결이 끊어졌습니다.`,
          });
          dispatch({ type: 'REMOVE_ACCEPTED_CONNECTION', username: m.username });
        },
      ],
      [
        'FETCH_USER_CONNECTIONS_RESPONSE',
        (msg) => {
          const m = msg as FetchUserConnectionsResponse;
          const accepted = m.connections.filter((c) => c.status === 'ACCEPTED');
          const pending = m.connections.filter((c) => c.status === 'PENDING');
          if (accepted.length > 0) {
            dispatch({ type: 'SET_ACCEPTED_CONNECTIONS', connections: accepted });
          }
          if (pending.length > 0) {
            dispatch({ type: 'SET_PENDING_CONNECTIONS', connections: pending });
          }
        },
      ],
      [
        'FETCH_USER_INVITE_CODE_RESPONSE',
        (msg) => {
          const m = msg as FetchUserInviteCodeResponse;
          dispatch({ type: 'SET_USER_INVITE_CODE', code: m.inviteCode });
        },
      ],
      // 알림
      [
        'ASK_INVITE',
        (msg) => {
          const m = msg as InviteNotification;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `${m.username} 님이 연결 초대를 보냈습니다.`,
          });
          // 대기 초대 갱신
          send({ type: 'FETCH_USER_CONNECTIONS_REQUEST', status: 'PENDING' });
        },
      ],
      [
        'NOTIFY_ACCEPT',
        (msg) => {
          const m = msg as AcceptNotification;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `${m.username} 님이 연결 초대를 수락했습니다.`,
          });
          send({ type: 'FETCH_USER_CONNECTIONS_REQUEST', status: 'ACCEPTED' });
        },
      ],
      [
        'NOTIFY_JOIN',
        (msg) => {
          const m = msg as JoinNotification;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `채널 '${m.title}'에 누군가 참가했습니다.`,
          });
        },
      ],
      // 에러
      [
        'ERROR',
        (msg) => {
          const m = msg as ErrorResponse;
          dispatch({ type: 'SET_ERROR', message: m.message });
        },
      ],
    ];

    handlers.forEach(([type, handler]) => addHandler(type, handler));
    return () => handlers.forEach(([type]) => removeHandler(type));
  }, [addHandler, removeHandler, send]);

  // ── 액션 함수들 ──────────────────────────────

  const fetchChannels = useCallback(() => {
    send({ type: 'FETCH_CHANNELS_REQUEST' });
  }, [send]);

  const createChannel = useCallback(
    (title: string, usernames: string[]) => {
      send({ type: 'CREATE_REQUEST', title, participantUsernames: usernames });
    },
    [send],
  );

  const joinChannel = useCallback(
    (inviteCode: string) => {
      send({ type: 'JOIN_REQUEST', inviteCode });
    },
    [send],
  );

  const enterChannel = useCallback(
    (channelId: number) => {
      send({ type: 'ENTER_REQUEST', channelId });
    },
    [send],
  );

  const leaveChannel = useCallback(() => {
    send({ type: 'LEAVE_REQUEST' });
  }, [send]);

  const quitChannel = useCallback(
    (channelId: number) => {
      send({ type: 'QUIT_REQUEST', channelId });
    },
    [send],
  );

  const fetchChannelInviteCode = useCallback(
    (channelId: number) => {
      send({ type: 'FETCH_CHANNEL_INVITE_CODE_REQUEST', channelId });
    },
    [send],
  );

  const sendMessage = useCallback(
    (content: string): Promise<void> => {
      if (!state.currentChannel) return Promise.reject('No channel selected');

      const serial = ++serialCounter.current;
      const channelId = state.currentChannel.channelId;

      return new Promise<void>((resolve, reject) => {
        const timeoutId = setTimeout(() => {
          pendingSerials.current.delete(serial);
          reject('메시지 전송 타임아웃');
        }, MAX_SERIAL_WAIT_MS);

        pendingSerials.current.set(serial, {
          resolve: () => {
            clearTimeout(timeoutId);
            // 내가 보낸 메시지는 NOTIFY_MESSAGE로 돌아오지 않을 수 있으므로
            // ACK 시점에 로컬에 추가 (username 포함)
            resolve();
          },
          reject: (r) => {
            clearTimeout(timeoutId);
            reject(r);
          },
        });

        send({
          type: 'WRITE_MESSAGE',
          channelId,
          content,
          serial,
        });
      });
    },
    [send, state.currentChannel],
  );

  const fetchMessages = useCallback(
    (channelId: number, start: number, end: number) => {
      send({
        type: 'FETCH_MESSAGES_REQUEST',
        channelId,
        startMessageSeqId: start,
        endMessageSeqId: end,
      });
    },
    [send],
  );

  const fetchAcceptedConnections = useCallback(() => {
    send({ type: 'FETCH_USER_CONNECTIONS_REQUEST', status: 'ACCEPTED' });
  }, [send]);

  const fetchPendingConnections = useCallback(() => {
    send({ type: 'FETCH_USER_CONNECTIONS_REQUEST', status: 'PENDING' });
  }, [send]);

  const inviteUser = useCallback(
    (inviteCode: string) => {
      send({ type: 'INVITE_REQUEST', inviteCode });
    },
    [send],
  );

  const acceptUser = useCallback(
    (username: string) => {
      send({ type: 'ACCEPT_REQUEST', username });
    },
    [send],
  );

  const rejectUser = useCallback(
    (username: string) => {
      send({ type: 'REJECT_REQUEST', username });
    },
    [send],
  );

  const disconnectUser = useCallback(
    (username: string) => {
      send({ type: 'DISCONNECT_REQUEST', username });
    },
    [send],
  );

  const fetchUserInviteCode = useCallback(() => {
    send({ type: 'FETCH_USER_INVITE_CODE_REQUEST' });
  }, [send]);

  const clearError = useCallback(() => {
    dispatch({ type: 'SET_ERROR', message: null });
  }, []);

  return (
    <ChatContext.Provider
      value={{
        ...state,
        fetchChannels,
        createChannel,
        joinChannel,
        enterChannel,
        leaveChannel,
        quitChannel,
        fetchChannelInviteCode,
        sendMessage,
        fetchMessages,
        fetchAcceptedConnections,
        fetchPendingConnections,
        inviteUser,
        acceptUser,
        rejectUser,
        disconnectUser,
        fetchUserInviteCode,
        clearError,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
}

export function useChat() {
  const ctx = useContext(ChatContext);
  if (!ctx) throw new Error('useChat must be used within ChatProvider');
  return ctx;
}
