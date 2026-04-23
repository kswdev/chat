import type { ChatMessage } from '../../types';
import { useAuth } from '../../contexts/AuthContext';

interface MessageItemProps {
  message: ChatMessage;
  showUsername: boolean;
}

export default function MessageItem({ message, showUsername }: MessageItemProps) {
  const { username } = useAuth();
  const isMe = message.username === username;

  return (
    <div className={`flex flex-col ${isMe ? 'items-end' : 'items-start'} mb-1`}>
      {showUsername && !isMe && (
        <span className="text-xs text-gray-400 ml-1 mb-0.5">{message.username}</span>
      )}
      <div
        className={`max-w-xs lg:max-w-md xl:max-w-lg px-3 py-2 rounded-2xl text-sm break-words ${
          isMe
            ? 'bg-indigo-600 text-white rounded-br-sm'
            : 'bg-gray-700 text-gray-100 rounded-bl-sm'
        }`}
      >
        {message.content}
      </div>
      <span className="text-xs text-gray-600 mx-1 mt-0.5">
        #{message.messageSeqId}
      </span>
    </div>
  );
}
