import { useEffect, useRef } from 'react';
import type { ChatMessage } from '../../types';
import MessageItem from './MessageItem';

interface MessageListProps {
  messages: ChatMessage[];
  channelId: number;
  onLoadMore: (start: number, end: number) => void;
}

export default function MessageList({ messages, channelId, onLoadMore }: MessageListProps) {
  const bottomRef = useRef<HTMLDivElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // 새 메시지 수신 시 스크롤
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length]);

  // 맨 위 스크롤 시 이전 메시지 불러오기
  const handleScroll = () => {
    const el = containerRef.current;
    if (!el) return;
    if (el.scrollTop < 5 && messages.length > 0) {
      const oldestSeqId = messages[0].messageSeqId;
      if (oldestSeqId > 1) {
        const start = Math.max(1, oldestSeqId - 20);
        const end = oldestSeqId - 1;
        onLoadMore(start, end);
      }
    }
  };

  return (
    <div
      ref={containerRef}
      onScroll={handleScroll}
      className="flex-1 overflow-y-auto scrollbar-thin px-4 py-4 space-y-0.5"
    >
      {messages.length === 0 && (
        <p className="text-center text-gray-500 text-sm mt-8">
          메시지가 없습니다. 첫 메시지를 보내보세요!
        </p>
      )}

      {messages.map((msg, idx) => {
        const prevMsg = messages[idx - 1];
        const showUsername = !prevMsg || prevMsg.username !== msg.username;
        return (
          <MessageItem key={`${channelId}-${msg.messageSeqId}`} message={msg} showUsername={showUsername} />
        );
      })}

      <div ref={bottomRef} />
    </div>
  );
}
