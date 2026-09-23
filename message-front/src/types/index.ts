// ──────────────────────────────────────────────
// Domain Models
// ──────────────────────────────────────────────

export interface Channel {
  channelId: number;
  title: string;
  headCount: number;
}

export interface ChatMessage {
  channelId: number;
  messageSeqId: number;
  username: string;
  content: string;
}

export interface Connection {
  userId: number;
  username: string;
  status: 'ACCEPTED' | 'PENDING' | 'NONE' | 'REJECTED' | 'DISCONNECTED';
}

// ──────────────────────────────────────────────
// Auth
// ──────────────────────────────────────────────

export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignUpRequest {
  username: string;
  password: string;
}

// ──────────────────────────────────────────────
// WebSocket Outbound Requests
// ──────────────────────────────────────────────

export interface BaseRequest {
  type: string;
  [key: string]: unknown;
}

export interface WriteMessageRequest extends BaseRequest {
  type: 'WRITE_MESSAGE';
  channelId: number;
  content: string;
  serial: number;
}

export interface ReadMessageAckRequest extends BaseRequest {
  type: 'READ_MESSAGE_ACK';
  channelId: number;
  messageSeqId: number;
}

export interface FetchMessagesRequest extends BaseRequest {
  type: 'FETCH_MESSAGES_REQUEST';
  channelId: number;
  startMessageSeqId: number;
  endMessageSeqId: number;
}

export interface KeepAliveRequest extends BaseRequest {
  type: 'KEEP_ALIVE';
}

// ──────────────────────────────────────────────
// WebSocket Inbound Messages (Server → Client)
// ──────────────────────────────────────────────

export interface BaseMessage {
  type: string;
}

export interface WriteMessageAck extends BaseMessage {
  type: 'WRITE_MESSAGE_ACK';
  messageSeqId: number;
  serial: number;
}

export interface MessageNotification extends BaseMessage {
  type: 'NOTIFY_MESSAGE';
  channelId: number;
  messageSeqId: number;
  username: string;
  content: string;
}

export interface FetchMessagesResponse extends BaseMessage {
  type: 'FETCH_MESSAGES_RESPONSE';
  channelId: number;
  messages: ChatMessage[];
}

export interface InviteNotification extends BaseMessage {
  type: 'ASK_INVITE';
  username: string;
}

export interface AcceptNotification extends BaseMessage {
  type: 'NOTIFY_ACCEPT';
  username: string;
}

export interface JoinNotification extends BaseMessage {
  type: 'NOTIFY_JOIN';
  channelId: number;
  title: string;
}

export interface ErrorResponse extends BaseMessage {
  type: 'ERROR';
  message: string;
  messageType: string;
}

export type InboundMessage =
  | WriteMessageAck
  | MessageNotification
  | FetchMessagesResponse
  | InviteNotification
  | AcceptNotification
  | JoinNotification
  | ErrorResponse;

// ──────────────────────────────────────────────
// UI State Types
// ──────────────────────────────────────────────

export type WsStatus = 'disconnected' | 'connecting' | 'connected' | 'error';

export interface PendingMessage {
  serial: number;
  content: string;
  resolve: (seqId: number) => void;
  reject: (reason?: string) => void;
  timeoutId: ReturnType<typeof setTimeout>;
}
