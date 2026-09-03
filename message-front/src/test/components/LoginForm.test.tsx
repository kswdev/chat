import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import LoginForm from '@/components/auth/LoginForm';
import { useAuth } from '@/contexts/AuthContext';

vi.mock('@/contexts/AuthContext', () => ({
  useAuth: vi.fn(),
}));

const mockLogin = vi.fn();

beforeEach(() => {
  vi.mocked(useAuth).mockReturnValue({
    username: null,
    token: null,
    isLoggedIn: false,
    login: mockLogin,
    logout: vi.fn(),
    register: vi.fn(),
    unregister: vi.fn(),
  });
  mockLogin.mockReset();
});

describe('LoginForm', () => {
  it('아이디/비밀번호 input과 로그인 버튼이 렌더링된다', () => {
    render(<LoginForm onSwitchToRegister={vi.fn()} />);
    expect(screen.getByPlaceholderText('아이디를 입력하세요')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('비밀번호를 입력하세요')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '로그인' })).toBeInTheDocument();
  });

  it('빈 필드로 submit 시 login이 호출되지 않는다', async () => {
    const user = userEvent.setup();
    render(<LoginForm onSwitchToRegister={vi.fn()} />);
    await user.click(screen.getByRole('button', { name: '로그인' }));
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('아이디만 입력하고 submit 시 login이 호출되지 않는다', async () => {
    const user = userEvent.setup();
    render(<LoginForm onSwitchToRegister={vi.fn()} />);
    await user.type(screen.getByPlaceholderText('아이디를 입력하세요'), 'testuser');
    await user.click(screen.getByRole('button', { name: '로그인' }));
    expect(mockLogin).not.toHaveBeenCalled();
  });

  it('아이디/비밀번호 입력 후 submit 시 login(username, password)을 호출한다', async () => {
    mockLogin.mockResolvedValue(undefined);
    const user = userEvent.setup();
    render(<LoginForm onSwitchToRegister={vi.fn()} />);
    await user.type(screen.getByPlaceholderText('아이디를 입력하세요'), 'testuser');
    await user.type(screen.getByPlaceholderText('비밀번호를 입력하세요'), 'mypassword');
    await user.click(screen.getByRole('button', { name: '로그인' }));
    expect(mockLogin).toHaveBeenCalledWith({ username: 'testuser', password: 'mypassword' });
  });

  it('로그인 중 버튼 텍스트가 "로그인 중..."으로 바뀌고 disabled 상태가 된다', async () => {
    let resolveLogin!: () => void;
    mockLogin.mockReturnValue(
      new Promise<void>((res) => {
        resolveLogin = res;
      }),
    );
    const user = userEvent.setup();
    render(<LoginForm onSwitchToRegister={vi.fn()} />);
    await user.type(screen.getByPlaceholderText('아이디를 입력하세요'), 'user');
    await user.type(screen.getByPlaceholderText('비밀번호를 입력하세요'), 'pass');
    await user.click(screen.getByRole('button', { name: '로그인' }));

    expect(screen.getByRole('button', { name: '로그인 중...' })).toBeDisabled();
    resolveLogin();
    await waitFor(() => expect(screen.getByRole('button', { name: '로그인' })).not.toBeDisabled());
  });

  it('login 실패 시 에러 메시지가 표시된다', async () => {
    mockLogin.mockRejectedValue(new Error('Unauthorized'));
    const user = userEvent.setup();
    render(<LoginForm onSwitchToRegister={vi.fn()} />);
    await user.type(screen.getByPlaceholderText('아이디를 입력하세요'), 'baduser');
    await user.type(screen.getByPlaceholderText('비밀번호를 입력하세요'), 'wrongpw');
    await user.click(screen.getByRole('button', { name: '로그인' }));
    await waitFor(() =>
      expect(screen.getByText('로그인에 실패했습니다. 아이디/비밀번호를 확인하세요.')).toBeInTheDocument(),
    );
  });

  it('"회원가입" 버튼 클릭 시 onSwitchToRegister가 호출된다', async () => {
    const onSwitchToRegister = vi.fn();
    const user = userEvent.setup();
    render(<LoginForm onSwitchToRegister={onSwitchToRegister} />);
    await user.click(screen.getByRole('button', { name: '회원가입' }));
    expect(onSwitchToRegister).toHaveBeenCalledTimes(1);
  });
});
