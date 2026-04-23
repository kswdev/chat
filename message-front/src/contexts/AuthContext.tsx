import React, { createContext, useContext, useState, useCallback } from 'react';
import * as authApi from '../api/authApi';
import type { LoginRequest, SignUpRequest } from '../types';

interface AuthContextValue {
  username: string | null;
  token: string | null;
  isLoggedIn: boolean;
  login: (data: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  register: (data: SignUpRequest) => Promise<void>;
  unregister: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [username, setUsername] = useState<string | null>(
    sessionStorage.getItem('username'),
  );
  const [token, setToken] = useState<string | null>(
    sessionStorage.getItem('token'),
  );

  const login = useCallback(async (data: LoginRequest) => {
    const receivedToken = await authApi.login(data);
    sessionStorage.setItem('token', receivedToken);
    sessionStorage.setItem('username', data.username);
    setToken(receivedToken);
    setUsername(data.username);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } finally {
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('username');
      setToken(null);
      setUsername(null);
    }
  }, []);

  const register = useCallback(async (data: SignUpRequest) => {
    await authApi.register(data);
  }, []);

  const unregister = useCallback(async () => {
    await authApi.unregister();
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('username');
    setToken(null);
    setUsername(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        username,
        token,
        isLoggedIn: !!token,
        login,
        logout,
        register,
        unregister,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
