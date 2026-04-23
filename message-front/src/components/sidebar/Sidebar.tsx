import { useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useWebSocket } from '../../contexts/WebSocketContext';
import { useChat } from '../../contexts/ChatContext';
import ChannelList from './ChannelList';
import ConnectionList from './ConnectionList';
import PendingInviteList from './PendingInviteList';

export default function Sidebar() {
  const { username, logout, unregister } = useAuth();
  const { status } = useWebSocket();
  const { fetchChannels, fetchAcceptedConnections, fetchPendingConnections } = useChat();

  // 연결 직후 초기 데이터 로드
  useEffect(() => {
    if (status === 'connected') {
      fetchChannels();
      fetchAcceptedConnections();
      fetchPendingConnections();
    }
  }, [status, fetchChannels, fetchAcceptedConnections, fetchPendingConnections]);

  const statusColor = {
    connected: 'bg-green-500',
    connecting: 'bg-yellow-500',
    disconnected: 'bg-gray-500',
    error: 'bg-red-500',
  }[status];

  const statusLabel = {
    connected: '연결됨',
    connecting: '연결 중',
    disconnected: '연결 끊김',
    error: '오류',
  }[status];

  return (
    <aside className="w-60 flex-shrink-0 bg-gray-800 flex flex-col h-full border-r border-gray-700">
      {/* 앱 헤더 */}
      <div className="px-4 py-3 border-b border-gray-700 flex items-center gap-2">
        <span className="text-lg font-bold text-white">💬 Message</span>
      </div>

      {/* 스크롤 영역 */}
      <div className="flex-1 overflow-y-auto scrollbar-thin py-3 space-y-4">
        {/* 채널 섹션 */}
        <ChannelList />

        <div className="border-t border-gray-700" />

        {/* 연결된 사용자 섹션 */}
        <ConnectionList />

        {/* 대기 초대 섹션 */}
        <PendingInviteList />
      </div>

      {/* 사용자 정보 푸터 */}
      <div className="border-t border-gray-700 p-3">
        <div className="flex items-center gap-2 mb-2">
          <div className={`w-2 h-2 rounded-full flex-shrink-0 ${statusColor}`} />
          <span className="text-xs text-gray-400">{statusLabel}</span>
        </div>
        <div className="flex items-center justify-between">
          <div className="min-w-0">
            <p className="text-sm font-medium text-white truncate">{username}</p>
          </div>
          <div className="flex gap-1 flex-shrink-0">
            <button
              onClick={logout}
              className="text-xs text-gray-400 hover:text-white px-2 py-1 rounded hover:bg-gray-700 transition-colors"
            >
              로그아웃
            </button>
            <button
              onClick={() => {
                if (confirm('정말 탈퇴하시겠습니까?')) {
                  unregister();
                }
              }}
              className="text-xs text-red-400 hover:text-red-300 px-2 py-1 rounded hover:bg-gray-700 transition-colors"
            >
              탈퇴
            </button>
          </div>
        </div>
      </div>
    </aside>
  );
}
