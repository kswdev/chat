import { useChat } from '../../contexts/ChatContext';
import MessageList from './MessageList';
import MessageInput from './MessageInput';
import Modal from '../shared/Modal';
import InviteCodeDisplay from '../shared/InviteCodeDisplay';
import { useState } from 'react';

export default function ChatView() {
  const {
    currentChannel,
    messages,
    channelInviteCodes,
    sendMessage,
    leaveChannel,
    fetchMessages,
    fetchChannelInviteCode,
  } = useChat();

  const [showInviteCode, setShowInviteCode] = useState(false);

  if (!currentChannel) {
    return (
      <div className="flex-1 flex items-center justify-center bg-gray-900">
        <div className="text-center text-gray-500">
          <p className="text-4xl mb-4">💬</p>
          <p className="text-lg font-medium text-gray-400">채널을 선택해주세요</p>
          <p className="text-sm mt-1">왼쪽 사이드바에서 채널을 선택하거나 새 채널을 만드세요</p>
        </div>
      </div>
    );
  }

  const channelMessages = messages[currentChannel.channelId] ?? [];

  const handleLoadMore = (start: number, end: number) => {
    fetchMessages(currentChannel.channelId, start, end);
  };

  const handleInviteCode = () => {
    fetchChannelInviteCode(currentChannel.channelId);
    setShowInviteCode(true);
  };

  return (
    <div className="flex-1 flex flex-col bg-gray-900 min-w-0">
      {/* 채널 헤더 */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-gray-700 bg-gray-800 flex-shrink-0">
        <div className="flex items-center gap-2">
          <span className="text-gray-400 text-lg">#</span>
          <h2 className="font-semibold text-white">{currentChannel.title}</h2>
          <span className="text-xs text-gray-500">
            ({currentChannel.headCount}명)
          </span>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={handleInviteCode}
            className="text-sm text-gray-400 hover:text-white px-2 py-1 rounded hover:bg-gray-700 transition-colors"
          >
            초대 코드
          </button>
          <button
            onClick={leaveChannel}
            className="text-sm text-gray-400 hover:text-white px-2 py-1 rounded hover:bg-gray-700 transition-colors"
          >
            ← 나가기
          </button>
        </div>
      </div>

      {/* 메시지 목록 */}
      <MessageList
        messages={channelMessages}
        channelId={currentChannel.channelId}
        onLoadMore={handleLoadMore}
      />

      {/* 메시지 입력 */}
      <MessageInput onSend={sendMessage} channelTitle={currentChannel.title} />

      {/* 채널 초대 코드 모달 */}
      {showInviteCode && (
        <Modal title="채널 초대 코드" onClose={() => setShowInviteCode(false)}>
          <p className="text-sm text-gray-400 mb-2">
            이 코드를 공유해서 채널에 초대하세요.
          </p>
          {channelInviteCodes[currentChannel.channelId] ? (
            <InviteCodeDisplay code={channelInviteCodes[currentChannel.channelId]} />
          ) : (
            <p className="text-gray-500 text-sm">코드를 불러오는 중...</p>
          )}
        </Modal>
      )}
    </div>
  );
}
