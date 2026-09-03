import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Modal from '@/components/shared/Modal';

describe('Modal', () => {
  it('제목과 children이 렌더링된다', () => {
    render(
      <Modal title="테스트 모달" onClose={vi.fn()}>
        <p>모달 내용</p>
      </Modal>,
    );
    expect(screen.getByText('테스트 모달')).toBeInTheDocument();
    expect(screen.getByText('모달 내용')).toBeInTheDocument();
  });

  it('닫기 버튼 클릭 시 onClose가 호출된다', async () => {
    const onClose = vi.fn();
    const user = userEvent.setup();
    render(
      <Modal title="모달" onClose={onClose}>
        <p>내용</p>
      </Modal>,
    );
    await user.click(screen.getByRole('button'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('오버레이(backdrop) 클릭 시 onClose가 호출된다', async () => {
    const onClose = vi.fn();
    const user = userEvent.setup();
    const { container } = render(
      <Modal title="모달" onClose={onClose}>
        <p>내용</p>
      </Modal>,
    );
    // 최상단 backdrop div (fixed inset-0)
    const backdrop = container.firstChild as HTMLElement;
    await user.click(backdrop);
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('모달 내부 클릭 시 onClose가 호출되지 않는다 (stopPropagation)', async () => {
    const onClose = vi.fn();
    const user = userEvent.setup();
    render(
      <Modal title="모달" onClose={onClose}>
        <p>내용</p>
      </Modal>,
    );
    await user.click(screen.getByText('내용'));
    expect(onClose).not.toHaveBeenCalled();
  });

  it('ESC 키 입력 시 onClose가 호출된다', () => {
    const onClose = vi.fn();
    render(
      <Modal title="모달" onClose={onClose}>
        <p>내용</p>
      </Modal>,
    );
    fireEvent.keyDown(window, { key: 'Escape' });
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
