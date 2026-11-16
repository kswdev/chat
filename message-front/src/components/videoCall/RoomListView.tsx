import { useEffect, useState } from 'react';
import { useVideoCall } from '../../contexts/VideoCallContext';
import Modal from '../shared/Modal';

const MAX_PEER = 2;

export default function RoomListView() {
  const { rooms, refreshRooms, createRoom, joinRoom, closeLobby } = useVideoCall();

  const [showCreate, setShowCreate] = useState(false);
  const [roomName, setRoomName] = useState('');

  useEffect(() => {
    refreshRooms();
  }, [refreshRooms]);

  const handleCreate = () => {
    if (roomName.trim()) {
      createRoom(roomName.trim());
      setRoomName('');
      setShowCreate(false);
    }
  };

  return (
    <div className="flex-1 flex flex-col h-full bg-gray-900">
      {/* 헤더 */}
      <div className="px-6 py-4 border-b border-gray-700 flex items-center justify-between">
        <h1 className="text-lg font-semibold text-white">화상통화</h1>
        <div className="flex gap-2">
          <button
            onClick={refreshRooms}
            className="text-sm text-gray-400 hover:text-white px-3 py-1.5 rounded hover:bg-gray-700 transition-colors"
          >
            새로고침
          </button>
          <button
            onClick={() => setShowCreate(true)}
            className="text-sm bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-1.5 rounded transition-colors"
          >
            방 만들기
          </button>
          <button
            onClick={closeLobby}
            className="text-sm text-gray-400 hover:text-white px-3 py-1.5 rounded hover:bg-gray-700 transition-colors"
          >
            닫기
          </button>
        </div>
      </div>

      {/* 방 목록 */}
      <div className="flex-1 overflow-y-auto scrollbar-thin p-6">
        {rooms.length === 0 ? (
          <p className="text-gray-500 text-sm italic">열려있는 방이 없습니다.</p>
        ) : (
          <ul className="space-y-2">
            {rooms.map((room) => {
              const full = room.peerCount >= MAX_PEER;
              return (
                <li
                  key={room.roomId}
                  className="flex items-center justify-between bg-gray-800 rounded px-4 py-3"
                >
                  <div>
                    <p className="text-white font-medium">{room.name}</p>
                    <p className="text-xs text-gray-400">
                      {room.peerCount}/{MAX_PEER}명 참여 중
                    </p>
                  </div>
                  <button
                    onClick={() => joinRoom(room.roomId)}
                    disabled={full}
                    className={`text-sm font-medium px-3 py-1.5 rounded transition-colors ${
                      full
                        ? 'bg-gray-700 text-gray-500 cursor-not-allowed'
                        : 'bg-indigo-600 hover:bg-indigo-700 text-white'
                    }`}
                  >
                    {full ? '가득 찼음' : '입장'}
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {/* 방 생성 모달 */}
      {showCreate && (
        <Modal title="방 만들기" onClose={() => setShowCreate(false)}>
          <div className="space-y-4">
            <div>
              <label className="block text-sm text-gray-400 mb-1">방 이름</label>
              <input
                type="text"
                value={roomName}
                onChange={(e) => setRoomName(e.target.value)}
                placeholder="방 이름 입력"
                className="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                autoFocus
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
    </div>
  );
}
