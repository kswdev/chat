import axiosInstance from './axiosInstance';
import type { LoginRequest, SignUpRequest } from '../types';

/** 로그인 → 토큰(세션 ID) 반환 */
export const login = async (data: LoginRequest): Promise<string> => {
  const response = await axiosInstance.post<string>('/api/v1/auth/login', data);
  return response.data;
};

/** 로그아웃 */
export const logout = async (): Promise<void> => {
  await axiosInstance.post('/api/v1/auth/logout');
};

/** 회원가입 */
export const register = async (data: SignUpRequest): Promise<void> => {
  await axiosInstance.post('/api/v1/user/register', data);
};

/** 회원 탈퇴 */
export const unregister = async (): Promise<void> => {
  await axiosInstance.post('/api/v1/user/unregister');
};
