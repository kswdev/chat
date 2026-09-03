import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import NotificationBar from '@/components/shared/NotificationBar';
import { useChat } from '@/contexts/ChatContext';

vi.mock('@/contexts/ChatContext', () => ({
  useChat: vi.fn(),
}));

const mockClearError = vi.fn();

function setupUseChat(overrides: { notifications?: string[]; error?: string | null }) {
  vi.mocked(useChat).mockReturnValue({
    notifications: overrides.notifications ?? [],
    error: overrides.error ?? null,
    clearError: mockClearError,
    // 나머지 ChatContextValue 필드 (사용 안 함)
    channels: [],
    messages: {},
    lastSeqId: {},
    currentChannel: null,
    acceptedConnections: [],
    pendingConnections: [],
    userInviteCode: null,
    channelInviteCodes: {},
    fetchChannels: vi.fn(),
    createChannel: vi.fn(),
    joinChannel: vi.fn(),
    enterChannel: vi.fn(),
    leaveChannel: vi.fn(),
    quitChannel: vi.fn(),
    fetchChannelInviteCode: vi.fn(),
    sendMessage: vi.fn(),
    fetchMessages: vi.fn(),
    fetchAcceptedConnections: vi.fn(),
    fetchPendingConnections: vi.fn(),
    inviteUser: vi.fn(),
    acceptUser: vi.fn(),
    rejectUser: vi.fn(),
    disconnectUser: vi.fn(),
    fetchUserInviteCode: vi.fn(),
  } as ReturnType<typeof useChat>);
}

describe('NotificationBar', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    mockClearError.mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('notifications가 비어있으면 아무것도 렌더링하지 않는다', () => {
    setupUseChat({ notifications: [] });
    const { container } = render(<NotificationBar />);
    expect(container.firstChild).toBeNull();
  });

  it('notifications가 있으면 마지막 항목을 표시한다', () => {
    setupUseChat({ notifications: ['알림1', '알림2', '마지막알림'] });
    render(<NotificationBar />);
    expect(screen.getByText('마지막알림')).toBeInTheDocument();
  });

  it('3.5초 후 자동으로 사라진다', () => {
    setupUseChat({ notifications: ['자동소멸알림'] });
    render(<NotificationBar />);
    expect(screen.getByText('자동소멸알림')).toBeInTheDocument();

    act(() => {
      vi.advanceTimersByTime(3500);
    });

    expect(screen.queryByText('자동소멸알림')).not.toBeInTheDocument();
  });
});
