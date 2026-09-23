import axiosInstance from './axiosInstance';
import type { Channel } from '../types';

interface ChannelApiResponse {
  channelId: number;
  title: string;
}

interface ChannelEntryApiResponse {
  channelId: number;
  title: string;
  lastReadMessageSeqId: number;
  lastChannelMessageSeqId: number;
}

interface ChannelsApiResponse {
  channels: Channel[];
}

interface ChannelInviteCodeApiResponse {
  channelId: number;
  inviteCode: string;
}

/** 내가 속한 채널 목록 조회 */
export const fetchChannels = async (): Promise<Channel[]> => {
  const response = await axiosInstance.get<ChannelsApiResponse>('/api/v1/channel');
  return response.data.channels;
};

/** 채널 생성 */
export const createChannel = async (
  title: string,
  participantUsernames: string[],
): Promise<ChannelApiResponse> => {
  const response = await axiosInstance.post<ChannelApiResponse>('/api/v1/channel', {
    title,
    participantUsernames,
  });
  return response.data;
};

/** 초대 코드로 채널 참가 */
export const joinChannel = async (inviteCode: string): Promise<ChannelApiResponse> => {
  const response = await axiosInstance.post<ChannelApiResponse>(
    `/api/v1/channel/join/${encodeURIComponent(inviteCode)}`,
  );
  return response.data;
};

/** 채널 나가기(멤버십 해제) */
export const quitChannel = async (channelId: number): Promise<void> => {
  await axiosInstance.post(`/api/v1/channel/${channelId}/quit`);
};

/** 채널 입장(대화 화면 진입) */
export const enterChannel = async (channelId: number): Promise<ChannelEntryApiResponse> => {
  const response = await axiosInstance.post<ChannelEntryApiResponse>(
    `/api/v1/channel/${channelId}/enter`,
  );
  return response.data;
};

/** 현재 입장 중인 채널 화면에서 나가기(멤버십은 유지) */
export const leaveChannel = async (): Promise<void> => {
  await axiosInstance.post('/api/v1/channel/leave');
};

/** 채널 초대 코드 조회 */
export const fetchChannelInviteCode = async (channelId: number): Promise<string> => {
  const response = await axiosInstance.get<ChannelInviteCodeApiResponse>(
    `/api/v1/channel/${channelId}/invite-code`,
  );
  return response.data.inviteCode;
};
