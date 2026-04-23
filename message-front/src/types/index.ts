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

export interface CreateChannelRequest extends BaseRequest {
  type: 'CREATE_REQUEST';
  title: string;
  participantUsernames: string[];
}

export interface EnterChannelRequest extends BaseRequest {
  type: 'ENTER_REQUEST';
  channelId: number;
}

export interface LeaveChannelRequest extends BaseRequest {
  type: 'LEAVE_REQUEST';
}

export interface JoinChannelRequest extends BaseRequest {
  type: 'JOIN_REQUEST';
  inviteCode: string;
}

export interface QuitChannelRequest extends BaseRequest {
  type: 'QUIT_REQUEST';
  channelId: number;
}

export interface FetchChannelsRequest extends BaseRequest {
  type: 'FETCH_CHANNELS_REQUEST';
}

export interface FetchChannelInviteCodeRequest extends BaseRequest {
  type: 'FETCH_CHANNEL_INVITE_CODE_REQUEST';
  channelId: number;
}

export interface InviteUserRequest extends BaseRequest {
  type: 'INVITE_REQUEST';
  inviteCode: string;
}

export interface AcceptUserRequest extends BaseRequest {
  type: 'ACCEPT_REQUEST';
  username: string;
}

export interface RejectUserRequest extends BaseRequest {
  type: 'REJECT_REQUEST';
  username: string;
}

export interface DisconnectUserRequest extends BaseRequest {
  type: 'DISCONNECT_REQUEST';
  username: string;
}

export interface FetchUserConnectionsRequest extends BaseRequest {
  type: 'FETCH_USER_CONNECTIONS_REQUEST';
  status: 'ACCEPTED' | 'PENDING';
}

export interface FetchUserInviteCodeRequest extends BaseRequest {
  type: 'FETCH_USER_INVITE_CODE_REQUEST';
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

export interface CreateResponse extends BaseMessage {
  type: 'CREATE_RESPONSE';
  channelId: number;
  title: string;
}

export interface EnterResponse extends BaseMessage {
  type: 'ENTER_RESPONSE';
  channelId: number;
  title: string;
  lastReadMessageSeqId: number;
  lastChannelMessageSeqId: number;
}

export interface LeaveResponse extends BaseMessage {
  type: 'LEAVE_RESPONSE';
}

export interface JoinResponse extends BaseMessage {
  type: 'JOIN_RESPONSE';
  channelId: number;
  title: string;
}

export interface QuitResponse extends BaseMessage {
  type: 'QUIT_RESPONSE';
  channelId: number;
}

export interface FetchChannelsResponse extends BaseMessage {
  type: 'FETCH_CHANNELS_RESPONSE';
  channels: Channel[];
}

export interface FetchMessagesResponse extends BaseMessage {
  type: 'FETCH_MESSAGES_RESPONSE';
  channelId: number;
  messages: ChatMessage[];
}

export interface FetchChannelInviteCodeResponse extends BaseMessage {
  type: 'FETCH_CHANNEL_INVITE_CODE_RESPONSE';
  channelId: number;
  inviteCode: string;
}

export interface InviteResponse extends BaseMessage {
  type: 'INVITE_RESPONSE';
  inviteCode: string;
  status: string;
}

export interface AcceptResponse extends BaseMessage {
  type: 'ACCEPT_RESPONSE';
  username: string;
}

export interface RejectResponse extends BaseMessage {
  type: 'REJECT_RESPONSE';
  username: string;
  status: string;
}

export interface DisconnectResponse extends BaseMessage {
  type: 'DISCONNECT_RESPONSE';
  username: string;
  status: string;
}

export interface FetchUserConnectionsResponse extends BaseMessage {
  type: 'FETCH_USER_CONNECTIONS_RESPONSE';
  connections: Connection[];
}

export interface FetchUserInviteCodeResponse extends BaseMessage {
  type: 'FETCH_USER_INVITE_CODE_RESPONSE';
  inviteCode: string;
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
  | CreateResponse
  | EnterResponse
  | LeaveResponse
  | JoinResponse
  | QuitResponse
  | FetchChannelsResponse
  | FetchMessagesResponse
  | FetchChannelInviteCodeResponse
  | InviteResponse
  | AcceptResponse
  | RejectResponse
  | DisconnectResponse
  | FetchUserConnectionsResponse
  | FetchUserInviteCodeResponse
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
