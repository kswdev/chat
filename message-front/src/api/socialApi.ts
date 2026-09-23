import axiosInstance from './axiosInstance';
import type { Connection } from '../types';

interface InviteApiResponse {
  inviterId: number;
  inviteeId: number;
  status: string;
}

interface ConnectionSummaryApiResponse {
  userId: number;
  username: string;
  status: string;
}

interface ConnectionsApiResponse {
  connections: ConnectionSummaryApiResponse[];
}

interface InviteCodeApiResponse {
  inviteCode: string;
}

/** 친구 초대 */
export const invite = async (inviteCode: string): Promise<InviteApiResponse> => {
  const response = await axiosInstance.post<InviteApiResponse>(
    `/api/v1/social/friends/invite/${encodeURIComponent(inviteCode)}`,
  );
  return response.data;
};

/** 초대 수락 */
export const accept = async (username: string): Promise<InviteApiResponse> => {
  const response = await axiosInstance.post<InviteApiResponse>(
    `/api/v1/social/friends/accept/${encodeURIComponent(username)}`,
  );
  return response.data;
};

/** 초대 거절 */
export const reject = async (username: string): Promise<InviteApiResponse> => {
  const response = await axiosInstance.post<InviteApiResponse>(
    `/api/v1/social/friends/reject/${encodeURIComponent(username)}`,
  );
  return response.data;
};

/** 연결 끊기 */
export const disconnect = async (username: string): Promise<InviteApiResponse> => {
  const response = await axiosInstance.post<InviteApiResponse>(
    `/api/v1/social/friends/disconnect/${encodeURIComponent(username)}`,
  );
  return response.data;
};

/** 연결 목록 조회 */
export const fetchConnections = async (
  status: 'ACCEPTED' | 'PENDING',
): Promise<Connection[]> => {
  const response = await axiosInstance.get<ConnectionsApiResponse>(
    '/api/v1/social/friends/connections',
    { params: { status } },
  );
  return response.data.connections.map((c) => ({
    userId: c.userId,
    username: c.username,
    status: c.status as Connection['status'],
  }));
};

/** 내 초대 코드 조회 */
export const fetchInviteCode = async (): Promise<string> => {
  const response = await axiosInstance.get<InviteCodeApiResponse>(
    '/api/v1/social/friends/invite-code',
  );
  return response.data.inviteCode;
};
