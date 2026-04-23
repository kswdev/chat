import { useState, type KeyboardEvent, useRef } from 'react';

interface MessageInputProps {
  onSend: (content: string) => Promise<void>;
  channelTitle: string;
}

export default function MessageInput({ onSend, channelTitle }: MessageInputProps) {
  const [content, setContent] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const handleSend = async () => {
    const trimmed = content.trim();
    if (!trimmed || sending) return;

    setError('');
    setSending(true);
    try {
      await onSend(trimmed);
      setContent('');
      textareaRef.current?.focus();
    } catch (e) {
      setError(typeof e === 'string' ? e : '메시지 전송에 실패했습니다.');
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="px-4 py-3 border-t border-gray-700">
      {error && (
        <p className="text-red-400 text-xs mb-2">{error}</p>
      )}
      <div className="flex items-end gap-2 bg-gray-700 rounded-lg px-3 py-2">
        <textarea
          ref={textareaRef}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={`#${channelTitle}에 메시지 보내기`}
          rows={1}
          className="flex-1 bg-transparent text-white resize-none focus:outline-none text-sm max-h-32"
          style={{ minHeight: '24px' }}
        />
        <button
          onClick={handleSend}
          disabled={!content.trim() || sending}
          className="flex-shrink-0 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white rounded px-3 py-1 text-sm transition-colors"
        >
          {sending ? '...' : '전송'}
        </button>
      </div>
      <p className="text-xs text-gray-600 mt-1">Enter로 전송, Shift+Enter로 줄바꿈</p>
    </div>
  );
}
