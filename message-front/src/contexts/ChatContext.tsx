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
import * as socialApi from '../api/socialApi';
import * as channelApi from '../api/channelApi';
import type {
  Channel,
  ChatMessage,
  Connection,
  InboundMessage,
  WriteMessageAck,
  MessageNotification,
  FetchMessagesResponse,
  InviteNotification,
  AcceptNotification,
  JoinNotification,
  ErrorResponse,
} from '../types';

// ──────────────────────────────────────────────
// State & Reducer
// ──────────────────────────────────────────────

export interface ChatState {
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

export type ChatAction =
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

export function chatReducer(state: ChatState, action: ChatAction): ChatState {
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

export const initialState: ChatState = {
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
  // 연결 액션 (message-social REST API)
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
  const { username } = useAuth();
  const stateRef = useRef(state);
  const pendingSerials = useRef<
    Map<
      number,
      {
        channelId: number;
        content: string;
        resolve: () => void;
        reject: (r: string) => void;
      }
    >
  >(new Map());
  const serialCounter = useRef(0);

  // stateRef를 항상 최신 state로 유지
  useEffect(() => {
    stateRef.current = state;
  }, [state]);

  // ── 연결(친구) 액션 함수들 (message-social REST API) ──────

  const fetchAcceptedConnections = useCallback(() => {
    socialApi
      .fetchConnections('ACCEPTED')
      .then((connections) =>
        dispatch({ type: 'SET_ACCEPTED_CONNECTIONS', connections }),
      )
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '연결 목록을 불러오지 못했습니다.' }),
      );
  }, []);

  const fetchPendingConnections = useCallback(() => {
    socialApi
      .fetchConnections('PENDING')
      .then((connections) =>
        dispatch({ type: 'SET_PENDING_CONNECTIONS', connections }),
      )
      .catch(() =>
        dispatch({
          type: 'SET_ERROR',
          message: '대기 중인 초대를 불러오지 못했습니다.',
        }),
      );
  }, []);

  const inviteUser = useCallback((inviteCode: string) => {
    socialApi
      .invite(inviteCode)
      .then(() => {
        dispatch({ type: 'ADD_NOTIFICATION', message: '초대 요청을 보냈습니다.' });
      })
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '초대 요청에 실패했습니다.' }),
      );
  }, []);

  const acceptUser = useCallback(
    (username: string) => {
      socialApi
        .accept(username)
        .then(() => {
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `${username} 님과 연결되었습니다.`,
          });
          dispatch({ type: 'REMOVE_PENDING_CONNECTION', username });
          fetchAcceptedConnections();
        })
        .catch(() =>
          dispatch({ type: 'SET_ERROR', message: '초대 수락에 실패했습니다.' }),
        );
    },
    [fetchAcceptedConnections],
  );

  const rejectUser = useCallback((username: string) => {
    socialApi
      .reject(username)
      .then(() => {
        dispatch({
          type: 'ADD_NOTIFICATION',
          message: `${username} 님의 초대를 거절했습니다.`,
        });
        dispatch({ type: 'REMOVE_PENDING_CONNECTION', username });
      })
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '초대 거절에 실패했습니다.' }),
      );
  }, []);

  const disconnectUser = useCallback((username: string) => {
    socialApi
      .disconnect(username)
      .then(() => {
        dispatch({
          type: 'ADD_NOTIFICATION',
          message: `${username} 님과 연결이 끊어졌습니다.`,
        });
        dispatch({ type: 'REMOVE_ACCEPTED_CONNECTION', username });
      })
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '연결 끊기에 실패했습니다.' }),
      );
  }, []);

  const fetchUserInviteCode = useCallback(() => {
    socialApi
      .fetchInviteCode()
      .then((code) => dispatch({ type: 'SET_USER_INVITE_CODE', code }))
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '초대 코드를 불러오지 못했습니다.' }),
      );
  }, []);

  // ── WebSocket 핸들러 등록 ──────────────────────

  useEffect(() => {
    const handlers: Array<[string, (msg: InboundMessage) => void]> = [
      // 채널 관련 요청/응답(create/join/quit/enter/leave/fetchChannels/fetchInviteCode)은
      // message-system REST API로 이전됨 — 아래 channelApi 기반 액션 함수 참고.
      // NOTIFY_JOIN(다른 참여자에게 보내는 실시간 알림)만 계속 WS로 수신한다.
      // 메시지
      [
        'WRITE_MESSAGE_ACK',
        (msg) => {
          const m = msg as WriteMessageAck;
          const entry = pendingSerials.current.get(m.serial);
          if (entry) {
            dispatch({
              type: 'ADD_MESSAGE',
              message: {
                channelId: entry.channelId,
                messageSeqId: m.messageSeqId,
                username: username ?? '',
                content: entry.content,
              },
            });
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
      // 연결 관련 알림 (invite/accept/reject/disconnect/connections/invite-code 자체는
      // message-social REST API로 이전됨 — 위 socialApi 기반 액션 함수 참고.
      // 상대방에게 보내는 실시간 알림 2종만 계속 WS로 수신한다.)
      [
        'ASK_INVITE',
        (msg) => {
          const m = msg as InviteNotification;
          dispatch({
            type: 'ADD_NOTIFICATION',
            message: `${m.username} 님이 연결 초대를 보냈습니다.`,
          });
          // 대기 초대 갱신
          fetchPendingConnections();
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
          fetchAcceptedConnections();
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
  }, [
    addHandler,
    removeHandler,
    send,
    fetchPendingConnections,
    fetchAcceptedConnections,
  ]);

  // ── 채널 액션 함수들 (message-system REST API) ──────

  const fetchChannels = useCallback(() => {
    channelApi
      .fetchChannels()
      .then((channels) => dispatch({ type: 'SET_CHANNELS', channels }))
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '채널 목록을 불러오지 못했습니다.' }),
      );
  }, []);

  const createChannel = useCallback((title: string, usernames: string[]) => {
    channelApi
      .createChannel(title, usernames)
      .then((channel) => {
        dispatch({
          type: 'ADD_CHANNEL',
          channel: { channelId: channel.channelId, title: channel.title, headCount: 1 },
        });
        dispatch({
          type: 'ADD_NOTIFICATION',
          message: `채널 '${channel.title}'이 생성되었습니다.`,
        });
      })
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '채널 생성에 실패했습니다.' }),
      );
  }, []);

  const joinChannel = useCallback((inviteCode: string) => {
    channelApi
      .joinChannel(inviteCode)
      .then((channel) => {
        dispatch({
          type: 'ADD_CHANNEL',
          channel: { channelId: channel.channelId, title: channel.title, headCount: 0 },
        });
        dispatch({
          type: 'ADD_NOTIFICATION',
          message: `채널 '${channel.title}'에 참가했습니다.`,
        });
      })
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '채널 참가에 실패했습니다.' }),
      );
  }, []);

  const enterChannel = useCallback(
    (channelId: number) => {
      channelApi
        .enterChannel(channelId)
        .then((entry) => {
          const channel = stateRef.current.channels.find(
            (c) => c.channelId === channelId,
          ) ?? { channelId, title: entry.title, headCount: 0 };
          dispatch({ type: 'SET_CURRENT_CHANNEL', channel });

          const hasLocalHistory =
            (stateRef.current.messages[channelId] ?? []).length > 0;

          if (entry.lastChannelMessageSeqId > 0) {
            if (!hasLocalHistory) {
              // 새로고침 등으로 로컬에 메시지 이력이 없는 상태 → 최근 이력을 다시 조회
              const start = Math.max(1, entry.lastChannelMessageSeqId - 29);
              const end = entry.lastChannelMessageSeqId;
              send({
                type: 'FETCH_MESSAGES_REQUEST',
                channelId,
                startMessageSeqId: start,
                endMessageSeqId: end,
              });
            } else if (entry.lastChannelMessageSeqId > entry.lastReadMessageSeqId) {
              // 읽지 않은 메시지만 가져오기
              const start = entry.lastReadMessageSeqId + 1;
              const end = entry.lastChannelMessageSeqId;
              send({
                type: 'FETCH_MESSAGES_REQUEST',
                channelId,
                startMessageSeqId: start,
                endMessageSeqId: end,
              });
            }
          }
          dispatch({
            type: 'SET_LAST_SEQ_ID',
            channelId,
            seqId: entry.lastReadMessageSeqId,
          });
        })
        .catch(() =>
          dispatch({ type: 'SET_ERROR', message: '채널 입장에 실패했습니다.' }),
        );
    },
    [send],
  );

  const leaveChannel = useCallback(() => {
    channelApi
      .leaveChannel()
      .then(() => dispatch({ type: 'SET_CURRENT_CHANNEL', channel: null }))
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '채널 화면 나가기에 실패했습니다.' }),
      );
  }, []);

  const quitChannel = useCallback((channelId: number) => {
    channelApi
      .quitChannel(channelId)
      .then(() => {
        dispatch({ type: 'REMOVE_CHANNEL', channelId });
        dispatch({ type: 'ADD_NOTIFICATION', message: `채널을 나갔습니다.` });
      })
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '채널 나가기에 실패했습니다.' }),
      );
  }, []);

  const fetchChannelInviteCode = useCallback((channelId: number) => {
    channelApi
      .fetchChannelInviteCode(channelId)
      .then((code) =>
        dispatch({ type: 'SET_CHANNEL_INVITE_CODE', channelId, code }),
      )
      .catch(() =>
        dispatch({ type: 'SET_ERROR', message: '초대 코드를 불러오지 못했습니다.' }),
      );
  }, []);

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
          channelId,
          content,
          resolve: () => {
            clearTimeout(timeoutId);
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
