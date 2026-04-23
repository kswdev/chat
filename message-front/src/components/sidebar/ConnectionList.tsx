import { useState } from 'react';
import { useChat } from '../../contexts/ChatContext';
import Modal from '../shared/Modal';
import InviteCodeDisplay from '../shared/InviteCodeDisplay';

export default function ConnectionList() {
  const {
    acceptedConnections,
    userInviteCode,
    fetchUserInviteCode,
    inviteUser,
    disconnectUser,
  } = useChat();

  const [showInvite, setShowInvite] = useState(false);
  const [showMyCode, setShowMyCode] = useState(false);
  const [inviteCode, setInviteCode] = useState('');

  const handleShowMyCode = () => {
    fetchUserInviteCode();
    setShowMyCode(true);
  };

  const handleInvite = () => {
    if (inviteCode.trim()) {
      inviteUser(inviteCode.trim());
      setInviteCode('');
      setShowInvite(false);
    }
  };

  return (
    <div>
      {/* 섹션 헤더 */}
      <div className="flex items-center justify-between px-3 py-2">
        <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
          연결된 사용자
        </span>
        <div className="flex gap-1">
          <button
            onClick={() => setShowInvite(true)}
            title="사용자 초대"
            className="text-gray-400 hover:text-white text-lg leading-none px-1"
          >
            +
          </button>
          <button
            onClick={handleShowMyCode}
            title="내 초대 코드"
            className="text-gray-400 hover:text-white text-xs px-1"
          >
            코드
          </button>
        </div>
      </div>

      {/* 연결 목록 */}
      <ul className="space-y-0.5">
        {acceptedConnections.length === 0 && (
          <li className="px-3 py-1 text-xs text-gray-500 italic">
            연결된 사용자가 없습니다
          </li>
        )}
        {acceptedConnections.map((conn) => (
          <li key={conn.username} className="group relative px-3 py-1.5 flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-green-500 flex-shrink-0" />
            <span className="text-sm text-gray-300 flex-1">{conn.username}</span>
            <button
              onClick={() => disconnectUser(conn.username)}
              title="연결 끊기"
              className="hidden group-hover:block text-red-400 hover:text-red-300 text-xs"
            >
              끊기
            </button>
          </li>
        ))}
      </ul>

      {/* 사용자 초대 모달 */}
      {showInvite && (
        <Modal title="사용자 초대" onClose={() => setShowInvite(false)}>
          <div className="space-y-4">
            <p className="text-sm text-gray-400">
              상대방의 초대 코드를 입력해서 연결 요청을 보내세요.
            </p>
            <div>
              <label className="block text-sm text-gray-400 mb-1">초대 코드</label>
              <input
                type="text"
                value={inviteCode}
                onChange={(e) => setInviteCode(e.target.value)}
                placeholder="초대 코드 입력"
                className="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                autoFocus
              />
            </div>
            <button
              onClick={handleInvite}
              className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 rounded transition-colors"
            >
              초대 요청
            </button>
          </div>
        </Modal>
      )}

      {/* 내 초대 코드 모달 */}
      {showMyCode && (
        <Modal title="내 초대 코드" onClose={() => setShowMyCode(false)}>
          <div>
            <p className="text-sm text-gray-400 mb-2">
              이 코드를 공유해서 친구를 초대하세요.
            </p>
            {userInviteCode ? (
              <InviteCodeDisplay code={userInviteCode} />
            ) : (
              <p className="text-gray-500 text-sm">코드를 불러오는 중...</p>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
}
