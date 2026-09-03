import { describe, it, expect } from 'vitest';
import { chatReducer, initialState } from '@/contexts/ChatContext';
import type { ChatState, ChatAction } from '@/contexts/ChatContext';
import type { Channel, ChatMessage, Connection } from '@/types';

const makeChannel = (id: number, title = `채널${id}`): Channel => ({
  channelId: id,
  title,
  headCount: 0,
});

const makeMessage = (channelId: number, seqId: number, username = 'user'): ChatMessage => ({
  channelId,
  messageSeqId: seqId,
  username,
  content: `메시지 ${seqId}`,
});

const makeConnection = (username: string, status: Connection['status'] = 'ACCEPTED'): Connection => ({
  username,
  status,
});

describe('chatReducer', () => {
  // ── SET_CHANNELS ──────────────────────────────
  describe('SET_CHANNELS', () => {
    it('채널 목록을 설정한다', () => {
      const channels = [makeChannel(1), makeChannel(2)];
      const action: ChatAction = { type: 'SET_CHANNELS', channels };
      const next = chatReducer(initialState, action);
      expect(next.channels).toEqual(channels);
    });

    it('빈 배열로 설정할 수 있다', () => {
      const stateWithChannels: ChatState = { ...initialState, channels: [makeChannel(1)] };
      const next = chatReducer(stateWithChannels, { type: 'SET_CHANNELS', channels: [] });
      expect(next.channels).toHaveLength(0);
    });
  });

  // ── ADD_CHANNEL ───────────────────────────────
  describe('ADD_CHANNEL', () => {
    it('신규 채널을 추가한다', () => {
      const channel = makeChannel(1);
      const next = chatReducer(initialState, { type: 'ADD_CHANNEL', channel });
      expect(next.channels).toHaveLength(1);
      expect(next.channels[0]).toEqual(channel);
    });

    it('이미 존재하는 채널은 중복 추가하지 않는다', () => {
      const channel = makeChannel(1);
      const state: ChatState = { ...initialState, channels: [channel] };
      const next = chatReducer(state, { type: 'ADD_CHANNEL', channel: makeChannel(1, '다른제목') });
      expect(next.channels).toHaveLength(1);
      expect(next.channels[0].title).toBe('채널1');
    });
  });

  // ── REMOVE_CHANNEL ────────────────────────────
  describe('REMOVE_CHANNEL', () => {
    it('채널을 제거한다', () => {
      const state: ChatState = { ...initialState, channels: [makeChannel(1), makeChannel(2)] };
      const next = chatReducer(state, { type: 'REMOVE_CHANNEL', channelId: 1 });
      expect(next.channels).toHaveLength(1);
      expect(next.channels[0].channelId).toBe(2);
    });

    it('currentChannel이 제거 대상이면 null로 초기화한다', () => {
      const channel = makeChannel(1);
      const state: ChatState = { ...initialState, channels: [channel], currentChannel: channel };
      const next = chatReducer(state, { type: 'REMOVE_CHANNEL', channelId: 1 });
      expect(next.currentChannel).toBeNull();
    });

    it('currentChannel이 다른 채널이면 유지한다', () => {
      const ch1 = makeChannel(1);
      const ch2 = makeChannel(2);
      const state: ChatState = { ...initialState, channels: [ch1, ch2], currentChannel: ch2 };
      const next = chatReducer(state, { type: 'REMOVE_CHANNEL', channelId: 1 });
      expect(next.currentChannel).toEqual(ch2);
    });
  });

  // ── SET_CURRENT_CHANNEL ───────────────────────
  describe('SET_CURRENT_CHANNEL', () => {
    it('현재 채널을 설정한다', () => {
      const channel = makeChannel(1);
      const next = chatReducer(initialState, { type: 'SET_CURRENT_CHANNEL', channel });
      expect(next.currentChannel).toEqual(channel);
    });

    it('null로 설정할 수 있다', () => {
      const state: ChatState = { ...initialState, currentChannel: makeChannel(1) };
      const next = chatReducer(state, { type: 'SET_CURRENT_CHANNEL', channel: null });
      expect(next.currentChannel).toBeNull();
    });
  });

  // ── ADD_MESSAGE ───────────────────────────────
  describe('ADD_MESSAGE', () => {
    it('메시지를 추가한다', () => {
      const message = makeMessage(1, 1);
      const next = chatReducer(initialState, { type: 'ADD_MESSAGE', message });
      expect(next.messages[1]).toHaveLength(1);
      expect(next.messages[1][0]).toEqual(message);
    });

    it('중복 messageSeqId는 무시한다', () => {
      const message = makeMessage(1, 1);
      const state: ChatState = {
        ...initialState,
        messages: { 1: [message] },
        lastSeqId: { 1: 1 },
      };
      const next = chatReducer(state, { type: 'ADD_MESSAGE', message: { ...message, content: '다른내용' } });
      expect(next.messages[1]).toHaveLength(1);
      expect(next.messages[1][0].content).toBe('메시지 1');
    });

    it('seqId 기준으로 정렬된다', () => {
      const msg3 = makeMessage(1, 3);
      const state: ChatState = { ...initialState, messages: { 1: [msg3] }, lastSeqId: { 1: 3 } };
      const msg1 = makeMessage(1, 1);
      const next = chatReducer(state, { type: 'ADD_MESSAGE', message: msg1 });
      expect(next.messages[1][0].messageSeqId).toBe(1);
      expect(next.messages[1][1].messageSeqId).toBe(3);
    });

    it('lastSeqId를 업데이트한다', () => {
      const message = makeMessage(1, 5);
      const next = chatReducer(initialState, { type: 'ADD_MESSAGE', message });
      expect(next.lastSeqId[1]).toBe(5);
    });

    it('현재 lastSeqId보다 작은 seqId가 와도 기존 값을 유지한다', () => {
      const state: ChatState = { ...initialState, messages: { 1: [makeMessage(1, 5)] }, lastSeqId: { 1: 5 } };
      const next = chatReducer(state, { type: 'ADD_MESSAGE', message: makeMessage(1, 3) });
      expect(next.lastSeqId[1]).toBe(5);
    });
  });

  // ── PREPEND_MESSAGES ──────────────────────────
  describe('PREPEND_MESSAGES', () => {
    it('기존 메시지 앞에 이전 메시지를 추가한다', () => {
      const existing = [makeMessage(1, 5), makeMessage(1, 6)];
      const state: ChatState = { ...initialState, messages: { 1: existing } };
      const older = [makeMessage(1, 3), makeMessage(1, 4)];
      const next = chatReducer(state, { type: 'PREPEND_MESSAGES', channelId: 1, messages: older });
      expect(next.messages[1]).toHaveLength(4);
      expect(next.messages[1].map((m) => m.messageSeqId)).toEqual([3, 4, 5, 6]);
    });

    it('중복 메시지를 제거한다', () => {
      const existing = [makeMessage(1, 4), makeMessage(1, 5)];
      const state: ChatState = { ...initialState, messages: { 1: existing } };
      const withDup = [makeMessage(1, 3), makeMessage(1, 4)];
      const next = chatReducer(state, { type: 'PREPEND_MESSAGES', channelId: 1, messages: withDup });
      expect(next.messages[1]).toHaveLength(3);
      expect(next.messages[1].map((m) => m.messageSeqId)).toEqual([3, 4, 5]);
    });
  });

  // ── SET_LAST_SEQ_ID ───────────────────────────
  describe('SET_LAST_SEQ_ID', () => {
    it('lastSeqId를 업데이트한다', () => {
      const next = chatReducer(initialState, { type: 'SET_LAST_SEQ_ID', channelId: 1, seqId: 10 });
      expect(next.lastSeqId[1]).toBe(10);
    });

    it('여러 채널의 lastSeqId를 독립적으로 관리한다', () => {
      const s1 = chatReducer(initialState, { type: 'SET_LAST_SEQ_ID', channelId: 1, seqId: 5 });
      const s2 = chatReducer(s1, { type: 'SET_LAST_SEQ_ID', channelId: 2, seqId: 10 });
      expect(s2.lastSeqId[1]).toBe(5);
      expect(s2.lastSeqId[2]).toBe(10);
    });
  });

  // ── SET_ACCEPTED_CONNECTIONS ──────────────────
  describe('SET_ACCEPTED_CONNECTIONS', () => {
    it('수락된 연결 목록을 설정한다', () => {
      const connections = [makeConnection('alice'), makeConnection('bob')];
      const next = chatReducer(initialState, { type: 'SET_ACCEPTED_CONNECTIONS', connections });
      expect(next.acceptedConnections).toEqual(connections);
    });
  });

  // ── SET_PENDING_CONNECTIONS ───────────────────
  describe('SET_PENDING_CONNECTIONS', () => {
    it('대기 연결 목록을 설정한다', () => {
      const connections = [makeConnection('charlie', 'PENDING')];
      const next = chatReducer(initialState, { type: 'SET_PENDING_CONNECTIONS', connections });
      expect(next.pendingConnections).toEqual(connections);
    });
  });

  // ── REMOVE_ACCEPTED_CONNECTION ────────────────
  describe('REMOVE_ACCEPTED_CONNECTION', () => {
    it('수락된 연결을 제거한다', () => {
      const state: ChatState = {
        ...initialState,
        acceptedConnections: [makeConnection('alice'), makeConnection('bob')],
      };
      const next = chatReducer(state, { type: 'REMOVE_ACCEPTED_CONNECTION', username: 'alice' });
      expect(next.acceptedConnections).toHaveLength(1);
      expect(next.acceptedConnections[0].username).toBe('bob');
    });

    it('존재하지 않는 username은 변화 없음', () => {
      const state: ChatState = {
        ...initialState,
        acceptedConnections: [makeConnection('alice')],
      };
      const next = chatReducer(state, { type: 'REMOVE_ACCEPTED_CONNECTION', username: 'unknown' });
      expect(next.acceptedConnections).toHaveLength(1);
    });
  });

  // ── REMOVE_PENDING_CONNECTION ─────────────────
  describe('REMOVE_PENDING_CONNECTION', () => {
    it('대기 연결을 제거한다', () => {
      const state: ChatState = {
        ...initialState,
        pendingConnections: [makeConnection('dave', 'PENDING'), makeConnection('eve', 'PENDING')],
      };
      const next = chatReducer(state, { type: 'REMOVE_PENDING_CONNECTION', username: 'dave' });
      expect(next.pendingConnections).toHaveLength(1);
      expect(next.pendingConnections[0].username).toBe('eve');
    });
  });

  // ── SET_USER_INVITE_CODE ──────────────────────
  describe('SET_USER_INVITE_CODE', () => {
    it('사용자 초대 코드를 설정한다', () => {
      const next = chatReducer(initialState, { type: 'SET_USER_INVITE_CODE', code: 'ABC123' });
      expect(next.userInviteCode).toBe('ABC123');
    });
  });

  // ── SET_CHANNEL_INVITE_CODE ───────────────────
  describe('SET_CHANNEL_INVITE_CODE', () => {
    it('채널 초대 코드를 설정한다', () => {
      const next = chatReducer(initialState, {
        type: 'SET_CHANNEL_INVITE_CODE',
        channelId: 42,
        code: 'XYZ789',
      });
      expect(next.channelInviteCodes[42]).toBe('XYZ789');
    });

    it('여러 채널 코드를 독립적으로 관리한다', () => {
      const s1 = chatReducer(initialState, { type: 'SET_CHANNEL_INVITE_CODE', channelId: 1, code: 'AAA' });
      const s2 = chatReducer(s1, { type: 'SET_CHANNEL_INVITE_CODE', channelId: 2, code: 'BBB' });
      expect(s2.channelInviteCodes[1]).toBe('AAA');
      expect(s2.channelInviteCodes[2]).toBe('BBB');
    });
  });

  // ── ADD_NOTIFICATION ──────────────────────────
  describe('ADD_NOTIFICATION', () => {
    it('알림을 추가한다', () => {
      const next = chatReducer(initialState, { type: 'ADD_NOTIFICATION', message: '알림1' });
      expect(next.notifications).toContain('알림1');
    });

    it('최대 50개까지만 유지한다 (51번째 추가 시 오래된 것 제거)', () => {
      const manyNotifications = Array.from({ length: 50 }, (_, i) => `알림${i}`);
      const state: ChatState = { ...initialState, notifications: manyNotifications };
      const next = chatReducer(state, { type: 'ADD_NOTIFICATION', message: '새알림' });
      expect(next.notifications).toHaveLength(50);
      expect(next.notifications[49]).toBe('새알림');
      expect(next.notifications[0]).toBe('알림1');
    });
  });

  // ── CLEAR_NOTIFICATIONS ───────────────────────
  describe('CLEAR_NOTIFICATIONS', () => {
    it('알림을 모두 비운다', () => {
      const state: ChatState = { ...initialState, notifications: ['알림1', '알림2'] };
      const next = chatReducer(state, { type: 'CLEAR_NOTIFICATIONS' });
      expect(next.notifications).toHaveLength(0);
    });
  });

  // ── SET_ERROR ─────────────────────────────────
  describe('SET_ERROR', () => {
    it('에러 메시지를 설정한다', () => {
      const next = chatReducer(initialState, { type: 'SET_ERROR', message: '에러 발생' });
      expect(next.error).toBe('에러 발생');
    });

    it('null로 에러를 해제한다', () => {
      const state: ChatState = { ...initialState, error: '기존 에러' };
      const next = chatReducer(state, { type: 'SET_ERROR', message: null });
      expect(next.error).toBeNull();
    });
  });
});
