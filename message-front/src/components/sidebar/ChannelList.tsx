import { useState } from 'react';
import { useChat } from '../../contexts/ChatContext';
import Modal from '../shared/Modal';
import InviteCodeDisplay from '../shared/InviteCodeDisplay';

export default function ChannelList() {
  const {
    channels,
    currentChannel,
    channelInviteCodes,
    enterChannel,
    quitChannel,
    fetchChannelInviteCode,
    createChannel,
    joinChannel,
  } = useChat();

  const [showCreate, setShowCreate] = useState(false);
  const [showJoin, setShowJoin] = useState(false);
  const [showInviteCode, setShowInviteCode] = useState<number | null>(null);

  // 채널 생성 폼
  const [createTitle, setCreateTitle] = useState('');
  const [createUsers, setCreateUsers] = useState('');

  // 채널 참가 폼
  const [joinCode, setJoinCode] = useState('');

  const handleCreate = () => {
    const usernames = createUsers
      .split(',')
      .map((u) => u.trim())
      .filter(Boolean);
    if (createTitle.trim()) {
      createChannel(createTitle.trim(), usernames);
      setCreateTitle('');
      setCreateUsers('');
      setShowCreate(false);
    }
  };

  const handleJoin = () => {
    if (joinCode.trim()) {
      joinChannel(joinCode.trim());
      setJoinCode('');
      setShowJoin(false);
    }
  };

  const handleInviteCode = (channelId: number) => {
    fetchChannelInviteCode(channelId);
    setShowInviteCode(channelId);
  };

  return (
    <div>
      {/* 섹션 헤더 */}
      <div className="flex items-center justify-between px-3 py-2">
        <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
          채널
        </span>
        <div className="flex gap-1">
          <button
            onClick={() => setShowCreate(true)}
            title="채널 생성"
            className="text-gray-400 hover:text-white text-lg leading-none px-1"
          >
            +
          </button>
          <button
            onClick={() => setShowJoin(true)}
            title="채널 참가"
            className="text-gray-400 hover:text-white text-sm px-1"
          >
            #
          </button>
        </div>
      </div>

      {/* 채널 목록 */}
      <ul className="space-y-0.5">
        {channels.length === 0 && (
          <li className="px-3 py-1 text-xs text-gray-500 italic">
            채널이 없습니다
          </li>
        )}
        {channels.map((ch) => (
          <li key={ch.channelId} className="group relative">
            <button
              onClick={() => enterChannel(ch.channelId)}
              className={`w-full text-left px-3 py-1.5 rounded text-sm transition-colors flex items-center gap-2 ${
                currentChannel?.channelId === ch.channelId
                  ? 'bg-gray-600 text-white'
                  : 'text-gray-400 hover:bg-gray-700 hover:text-white'
              }`}
            >
              <span className="text-gray-500">#</span>
              <span className="flex-1 truncate">{ch.title}</span>
              <span className="text-xs text-gray-500">{ch.headCount}</span>
            </button>
            {/* 호버 시 액션 버튼 */}
            <div className="absolute right-1 top-1/2 -translate-y-1/2 hidden group-hover:flex gap-1">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleInviteCode(ch.channelId);
                }}
                title="초대 코드"
                className="text-gray-400 hover:text-white text-xs bg-gray-700 rounded px-1 py-0.5"
              >
                코드
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  quitChannel(ch.channelId);
                }}
                title="채널 나가기"
                className="text-red-400 hover:text-red-300 text-xs bg-gray-700 rounded px-1 py-0.5"
              >
                나가기
              </button>
            </div>
          </li>
        ))}
      </ul>

      {/* 채널 생성 모달 */}
      {showCreate && (
        <Modal title="채널 생성" onClose={() => setShowCreate(false)}>
          <div className="space-y-4">
            <div>
              <label className="block text-sm text-gray-400 mb-1">채널 이름</label>
              <input
                type="text"
                value={createTitle}
                onChange={(e) => setCreateTitle(e.target.value)}
                placeholder="채널 이름 입력"
                className="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                autoFocus
              />
            </div>
            <div>
              <label className="block text-sm text-gray-400 mb-1">
                참가자 (쉼표로 구분, 최대 99명)
              </label>
              <input
                type="text"
                value={createUsers}
                onChange={(e) => setCreateUsers(e.target.value)}
                placeholder="user1, user2, ..."
                className="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <button
              onClick={handleCreate}
              className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 rounded transition-colors"
            >
              생성
            </button>
          </div>
        </Modal>
      )}

      {/* 채널 참가 모달 */}
      {showJoin && (
        <Modal title="채널 참가" onClose={() => setShowJoin(false)}>
          <div className="space-y-4">
            <div>
              <label className="block text-sm text-gray-400 mb-1">초대 코드</label>
              <input
                type="text"
                value={joinCode}
                onChange={(e) => setJoinCode(e.target.value)}
                placeholder="초대 코드를 입력하세요"
                className="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                autoFocus
              />
            </div>
            <button
              onClick={handleJoin}
              className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 rounded transition-colors"
            >
              참가
            </button>
          </div>
        </Modal>
      )}

      {/* 채널 초대 코드 모달 */}
      {showInviteCode !== null && (
        <Modal
          title="채널 초대 코드"
          onClose={() => setShowInviteCode(null)}
        >
          <div>
            <p className="text-sm text-gray-400 mb-2">
              이 코드를 공유해서 채널에 초대하세요.
            </p>
            {channelInviteCodes[showInviteCode] ? (
              <InviteCodeDisplay code={channelInviteCodes[showInviteCode]} />
            ) : (
              <p className="text-gray-500 text-sm">코드를 불러오는 중...</p>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
}
