import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, act, screen } from '@testing-library/react';
import { AuthProvider, useAuth } from '@/contexts/AuthContext';
import * as authApi from '@/api/authApi';

// authApi mock
vi.mock('@/api/authApi', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  register: vi.fn(),
  unregister: vi.fn(),
}));

// sessionStorage mock
const sessionStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] ?? null,
    setItem: (key: string, val: string) => {
      store[key] = val;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();
Object.defineProperty(window, 'sessionStorage', { value: sessionStorageMock, writable: false });

// AuthContext 값을 컴포넌트 외부로 노출하는 헬퍼
let capturedAuth: ReturnType<typeof useAuth>;

function AuthConsumer() {
  capturedAuth = useAuth();
  return (
    <div>
      <span data-testid="username">{capturedAuth.username ?? 'null'}</span>
      <span data-testid="token">{capturedAuth.token ?? 'null'}</span>
      <span data-testid="isLoggedIn">{capturedAuth.isLoggedIn ? 'true' : 'false'}</span>
    </div>
  );
}

function renderAuth() {
  return render(
    <AuthProvider>
      <AuthConsumer />
    </AuthProvider>,
  );
}

describe('AuthContext', () => {
  beforeEach(() => {
    sessionStorageMock.clear();
    vi.clearAllMocks();
  });

  // ── 초기 상태 ──────────────────────────────────
  describe('초기 상태', () => {
    it('token과 username이 null이고 isLoggedIn이 false이다', () => {
      renderAuth();
      expect(screen.getByTestId('token').textContent).toBe('null');
      expect(screen.getByTestId('username').textContent).toBe('null');
      expect(screen.getByTestId('isLoggedIn').textContent).toBe('false');
    });
  });

  // ── login ──────────────────────────────────────
  describe('login', () => {
    it('성공 시 token/username 상태가 업데이트되고 sessionStorage에 저장된다', async () => {
      vi.mocked(authApi.login).mockResolvedValue('test-token-123');
      renderAuth();

      await act(async () => {
        await capturedAuth.login({ username: 'testuser', password: 'pass123' });
      });

      expect(screen.getByTestId('token').textContent).toBe('test-token-123');
      expect(screen.getByTestId('username').textContent).toBe('testuser');
      expect(screen.getByTestId('isLoggedIn').textContent).toBe('true');
      expect(sessionStorageMock.getItem('token')).toBe('test-token-123');
      expect(sessionStorageMock.getItem('username')).toBe('testuser');
    });

    it('실패 시 에러를 throw하고 상태가 변하지 않는다', async () => {
      vi.mocked(authApi.login).mockRejectedValue(new Error('Unauthorized'));
      renderAuth();

      await expect(
        act(async () => {
          await capturedAuth.login({ username: 'testuser', password: 'wrong' });
        }),
      ).rejects.toThrow('Unauthorized');

      expect(screen.getByTestId('token').textContent).toBe('null');
      expect(screen.getByTestId('username').textContent).toBe('null');
      expect(screen.getByTestId('isLoggedIn').textContent).toBe('false');
    });
  });

  // ── logout ─────────────────────────────────────
  describe('logout', () => {
    it('sessionStorage를 삭제하고 상태를 null로 초기화한다', async () => {
      vi.mocked(authApi.login).mockResolvedValue('token-abc');
      vi.mocked(authApi.logout).mockResolvedValue(undefined);
      renderAuth();

      await act(async () => {
        await capturedAuth.login({ username: 'user1', password: 'pw' });
      });
      expect(screen.getByTestId('token').textContent).toBe('token-abc');

      await act(async () => {
        await capturedAuth.logout();
      });

      expect(screen.getByTestId('token').textContent).toBe('null');
      expect(screen.getByTestId('username').textContent).toBe('null');
      expect(screen.getByTestId('isLoggedIn').textContent).toBe('false');
      expect(sessionStorageMock.getItem('token')).toBeNull();
      expect(sessionStorageMock.getItem('username')).toBeNull();
    });

    it('API가 실패해도 sessionStorage와 상태를 정리한다', async () => {
      vi.mocked(authApi.login).mockResolvedValue('token-xyz');
      vi.mocked(authApi.logout).mockRejectedValue(new Error('Server error'));
      renderAuth();

      await act(async () => {
        await capturedAuth.login({ username: 'user1', password: 'pw' });
      });

      // logout은 finally 블록에서 항상 상태를 정리하므로, API 실패여도 정상 완료됨
      await act(async () => {
        await capturedAuth.logout().catch(() => {/* expected */});
      });

      expect(screen.getByTestId('token').textContent).toBe('null');
      expect(sessionStorageMock.getItem('token')).toBeNull();
    });
  });

  // ── register ───────────────────────────────────
  describe('register', () => {
    it('authApi.register를 호출한다', async () => {
      vi.mocked(authApi.register).mockResolvedValue(undefined);
      renderAuth();

      await act(async () => {
        await capturedAuth.register({ username: 'newuser', password: 'newpass' });
      });

      expect(authApi.register).toHaveBeenCalledWith({ username: 'newuser', password: 'newpass' });
    });
  });

  // ── unregister ─────────────────────────────────
  describe('unregister', () => {
    it('sessionStorage를 삭제하고 상태를 null로 초기화한다', async () => {
      vi.mocked(authApi.login).mockResolvedValue('token-del');
      vi.mocked(authApi.unregister).mockResolvedValue(undefined);
      renderAuth();

      await act(async () => {
        await capturedAuth.login({ username: 'deleteme', password: 'pw' });
      });
      expect(screen.getByTestId('token').textContent).toBe('token-del');

      await act(async () => {
        await capturedAuth.unregister();
      });

      expect(screen.getByTestId('token').textContent).toBe('null');
      expect(screen.getByTestId('username').textContent).toBe('null');
      expect(sessionStorageMock.getItem('token')).toBeNull();
      expect(sessionStorageMock.getItem('username')).toBeNull();
    });
  });
});
