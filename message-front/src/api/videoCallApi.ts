import axiosInstance from './axiosInstance';
import type { RoomSummary } from '../types';

interface CreatedApiResponse {
  roomId: string;
}

/** 현재 열려있는 모든 화상통화 방 목록 조회 */
export const listRooms = async (): Promise<RoomSummary[]> => {
  const response = await axiosInstance.get<RoomSummary[]>('/api/v1/video-call/room');
  return response.data;
};

/** 화상통화 방 생성 */
export const createRoom = async (name: string): Promise<string> => {
  const response = await axiosInstance.post<CreatedApiResponse>('/api/v1/video-call/room', {
    name,
  });
  return response.data.roomId;
};
