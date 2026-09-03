import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import InviteCodeDisplay from '@/components/shared/InviteCodeDisplay';

// clipboard mock - writable: true로 설정해야 userEvent와 충돌하지 않음
const writeTextMock = vi.fn();
beforeEach(() => {
  writeTextMock.mockReset();
  Object.defineProperty(navigator, 'clipboard', {
    value: { writeText: writeTextMock },
    writable: true,
    configurable: true,
  });
});

describe('InviteCodeDisplay', () => {
  it('초대 코드가 렌더링된다', () => {
    render(<InviteCodeDisplay code="INVITE-CODE-XYZ" />);
    expect(screen.getByText('INVITE-CODE-XYZ')).toBeInTheDocument();
  });

  it('복사 버튼 클릭 시 navigator.clipboard.writeText가 호출된다', async () => {
    writeTextMock.mockResolvedValue(undefined);
    render(<InviteCodeDisplay code="MY-CODE-123" />);
    fireEvent.click(screen.getByRole('button', { name: '복사' }));
    await waitFor(() => expect(writeTextMock).toHaveBeenCalledWith('MY-CODE-123'));
  });

  it('복사 후 "복사됨!" 피드백이 표시된다', async () => {
    writeTextMock.mockResolvedValue(undefined);
    render(<InviteCodeDisplay code="MY-CODE-456" />);
    fireEvent.click(screen.getByRole('button', { name: '복사' }));
    await waitFor(() => expect(screen.getByRole('button', { name: '복사됨!' })).toBeInTheDocument());
  });
});
