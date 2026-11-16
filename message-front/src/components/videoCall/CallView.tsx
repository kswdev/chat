import { useEffect, useRef } from 'react';
import { useVideoCall } from '../../contexts/VideoCallContext';

export default function CallView() {
  const { callStatus, localStream, remoteStream, micEnabled, cameraEnabled, leaveCall, toggleMic, toggleCamera } =
    useVideoCall();

  const localVideoRef = useRef<HTMLVideoElement | null>(null);
  const remoteVideoRef = useRef<HTMLVideoElement | null>(null);

  useEffect(() => {
    if (localVideoRef.current) {
      localVideoRef.current.srcObject = localStream;
    }
  }, [localStream]);

  useEffect(() => {
    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = remoteStream;
    }
  }, [remoteStream]);

  const statusLabel = {
    idle: '',
    connecting: '연결 중...',
    connected: '연결됨',
    error: '연결 오류',
  }[callStatus];

  return (
    <div className="fixed inset-0 z-40 bg-gray-950 flex flex-col">
      {/* 상태 표시 */}
      <div className="px-6 py-3 text-sm text-gray-400">{statusLabel}</div>

      {/* 비디오 영역 */}
      <div className="flex-1 relative flex items-center justify-center bg-black">
        <video
          ref={remoteVideoRef}
          autoPlay
          playsInline
          className="max-w-full max-h-full"
        />
        <video
          ref={localVideoRef}
          autoPlay
          playsInline
          muted
          className="absolute bottom-6 right-6 w-48 rounded-lg shadow-xl border border-gray-700"
        />
      </div>

      {/* 컨트롤 바 */}
      <div className="px-6 py-4 flex items-center justify-center gap-4 border-t border-gray-800">
        <button
          onClick={toggleMic}
          className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
            micEnabled ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-red-600 text-white hover:bg-red-700'
          }`}
        >
          {micEnabled ? '마이크 끄기' : '마이크 켜기'}
        </button>
        <button
          onClick={toggleCamera}
          className={`px-4 py-2 rounded-full text-sm font-medium transition-colors ${
            cameraEnabled ? 'bg-gray-700 text-white hover:bg-gray-600' : 'bg-red-600 text-white hover:bg-red-700'
          }`}
        >
          {cameraEnabled ? '카메라 끄기' : '카메라 켜기'}
        </button>
        <button
          onClick={leaveCall}
          className="px-4 py-2 rounded-full text-sm font-medium bg-red-600 hover:bg-red-700 text-white transition-colors"
        >
          나가기
        </button>
      </div>
    </div>
  );
}
