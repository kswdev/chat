import { useChat } from '../../contexts/ChatContext';

export default function PendingInviteList() {
  const { pendingConnections, acceptUser, rejectUser } = useChat();

  if (pendingConnections.length === 0) return null;

  return (
    <div>
      {/* 섹션 헤더 */}
      <div className="px-3 py-2">
        <span className="text-xs font-semibold text-yellow-500 uppercase tracking-wider">
          대기 중인 초대 ({pendingConnections.length})
        </span>
      </div>

      <ul className="space-y-1 px-2">
        {pendingConnections.map((conn) => (
          <li
            key={conn.username}
            className="bg-gray-700 rounded p-2 flex items-center justify-between gap-2"
          >
            <span className="text-sm text-gray-200 truncate">{conn.username}</span>
            <div className="flex gap-1 flex-shrink-0">
              <button
                onClick={() => acceptUser(conn.username)}
                className="text-xs bg-green-600 hover:bg-green-700 text-white px-2 py-0.5 rounded transition-colors"
              >
                수락
              </button>
              <button
                onClick={() => rejectUser(conn.username)}
                className="text-xs bg-red-600 hover:bg-red-700 text-white px-2 py-0.5 rounded transition-colors"
              >
                거절
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
