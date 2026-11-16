import React, {
  createContext,
  useContext,
  useRef,
  useState,
  useCallback,
  useEffect,
} from 'react';
import * as videoCallApi from '../api/videoCallApi';
import type { RoomSummary, Signal, SignalCommand, SignalType } from '../types';

const WS_BASE_URL = 'ws://localhost:8080/ws/v1/video-call';
const ICE_SERVERS: RTCConfiguration = {
  iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],
};

type CallStatus = 'idle' | 'connecting' | 'connected' | 'error';

interface ActiveCall {
  roomId: string;
  /** 늦게 들어온 사람(참가자)이 offer를 보낸다 — 방 만든 사람은 대기한다 */
  isInitiator: boolean;
}

interface VideoCallContextValue {
  rooms: RoomSummary[];
  isLobbyOpen: boolean;
  activeCall: ActiveCall | null;
  callStatus: CallStatus;
  localStream: MediaStream | null;
  remoteStream: MediaStream | null;
  micEnabled: boolean;
  cameraEnabled: boolean;
  openLobby: () => void;
  closeLobby: () => void;
  refreshRooms: () => Promise<void>;
  createRoom: (name: string) => Promise<void>;
  joinRoom: (roomId: string) => Promise<void>;
  leaveCall: () => void;
  toggleMic: () => void;
  toggleCamera: () => void;
}

const VideoCallContext = createContext<VideoCallContextValue | null>(null);

export function VideoCallProvider({ children }: { children: React.ReactNode }) {
  const [rooms, setRooms] = useState<RoomSummary[]>([]);
  const [isLobbyOpen, setIsLobbyOpen] = useState(false);
  const [activeCall, setActiveCall] = useState<ActiveCall | null>(null);
  const [callStatus, setCallStatus] = useState<CallStatus>('idle');
  const [localStream, setLocalStream] = useState<MediaStream | null>(null);
  const [remoteStream, setRemoteStream] = useState<MediaStream | null>(null);
  const [micEnabled, setMicEnabled] = useState(true);
  const [cameraEnabled, setCameraEnabled] = useState(true);

  const wsRef = useRef<WebSocket | null>(null);
  const pcRef = useRef<RTCPeerConnection | null>(null);
  const pendingCandidatesRef = useRef<RTCIceCandidateInit[]>([]);
  const remoteDescSetRef = useRef(false);

  const refreshRooms = useCallback(async () => {
    try {
      const list = await videoCallApi.listRooms();
      setRooms(list);
    } catch (e) {
      console.error('[VideoCall] 방 목록 조회 실패', e);
    }
  }, []);

  const openLobby = useCallback(() => {
    setIsLobbyOpen(true);
    refreshRooms();
  }, [refreshRooms]);

  const closeLobby = useCallback(() => setIsLobbyOpen(false), []);

  const sendSignal = useCallback((type: SignalType, payload: unknown) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      const command: SignalCommand = { type, payload };
      wsRef.current.send(JSON.stringify(command));
    }
  }, []);

  const flushPendingCandidates = useCallback(async (pc: RTCPeerConnection) => {
    const queued = pendingCandidatesRef.current;
    pendingCandidatesRef.current = [];
    for (const candidate of queued) {
      await pc.addIceCandidate(candidate);
    }
  }, []);

  const handleSignal = useCallback(
    async (pc: RTCPeerConnection, signal: Signal) => {
      switch (signal.type) {
        case 'OFFER': {
          await pc.setRemoteDescription(
            new RTCSessionDescription(signal.payload as RTCSessionDescriptionInit),
          );
          remoteDescSetRef.current = true;
          await flushPendingCandidates(pc);
          const answer = await pc.createAnswer();
          await pc.setLocalDescription(answer);
          sendSignal('ANSWER', answer);
          break;
        }
        case 'ANSWER': {
          await pc.setRemoteDescription(
            new RTCSessionDescription(signal.payload as RTCSessionDescriptionInit),
          );
          remoteDescSetRef.current = true;
          await flushPendingCandidates(pc);
          break;
        }
        case 'CANDIDATE': {
          const candidate = signal.payload as RTCIceCandidateInit;
          if (remoteDescSetRef.current) {
            await pc.addIceCandidate(candidate);
          } else {
            pendingCandidatesRef.current.push(candidate);
          }
          break;
        }
        case 'MEDIA_STATE':
          // 상대방 mute/카메라 상태 표시는 추후 확장
          break;
      }
    },
    [flushPendingCandidates, sendSignal],
  );

  const connectCall = useCallback(
    async (roomId: string, isInitiator: boolean) => {
      setCallStatus('connecting');
      pendingCandidatesRef.current = [];
      remoteDescSetRef.current = false;

      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      setLocalStream(stream);

      const pc = new RTCPeerConnection(ICE_SERVERS);
      pcRef.current = pc;
      stream.getTracks().forEach((track) => pc.addTrack(track, stream));

      const remote = new MediaStream();
      setRemoteStream(remote);
      pc.ontrack = (event) => {
        event.streams[0]?.getTracks().forEach((track) => remote.addTrack(track));
      };

      pc.onicecandidate = (event) => {
        if (event.candidate) {
          sendSignal('CANDIDATE', event.candidate.toJSON());
        }
      };

      pc.onconnectionstatechange = () => {
        if (pc.connectionState === 'connected') setCallStatus('connected');
        if (pc.connectionState === 'failed' || pc.connectionState === 'disconnected') {
          setCallStatus('error');
        }
      };

      const token = sessionStorage.getItem('token') ?? '';
      const ws = new WebSocket(`${WS_BASE_URL}/${roomId}?token=${encodeURIComponent(token)}`);
      wsRef.current = ws;

      ws.onopen = () => {
        if (isInitiator) {
          pc.createOffer()
            .then((offer) => pc.setLocalDescription(offer).then(() => offer))
            .then((offer) => sendSignal('OFFER', offer))
            .catch((e) => console.error('[VideoCall] offer 생성 실패', e));
        }
      };

      ws.onmessage = (event: MessageEvent) => {
        try {
          const signal = JSON.parse(event.data as string) as Signal;
          handleSignal(pc, signal);
        } catch (e) {
          console.error('[VideoCall] 시그널 파싱 실패', e);
        }
      };

      ws.onerror = () => setCallStatus('error');
    },
    [handleSignal, sendSignal],
  );

  const createRoom = useCallback(
    async (name: string) => {
      const roomId = await videoCallApi.createRoom(name);
      setActiveCall({ roomId, isInitiator: false });
      setIsLobbyOpen(false);
      await connectCall(roomId, false);
    },
    [connectCall],
  );

  const joinRoom = useCallback(
    async (roomId: string) => {
      setActiveCall({ roomId, isInitiator: true });
      setIsLobbyOpen(false);
      await connectCall(roomId, true);
    },
    [connectCall],
  );

  const leaveCall = useCallback(() => {
    wsRef.current?.close();
    wsRef.current = null;
    pcRef.current?.close();
    pcRef.current = null;
    localStream?.getTracks().forEach((track) => track.stop());
    setLocalStream(null);
    setRemoteStream(null);
    setActiveCall(null);
    setCallStatus('idle');
    setMicEnabled(true);
    setCameraEnabled(true);
    refreshRooms();
  }, [localStream, refreshRooms]);

  const toggleMic = useCallback(() => {
    if (!localStream) return;
    localStream.getAudioTracks().forEach((track) => (track.enabled = !track.enabled));
    setMicEnabled((prev) => !prev);
  }, [localStream]);

  const toggleCamera = useCallback(() => {
    if (!localStream) return;
    localStream.getVideoTracks().forEach((track) => (track.enabled = !track.enabled));
    setCameraEnabled((prev) => !prev);
  }, [localStream]);

  // 언마운트 시 정리
  useEffect(() => {
    return () => {
      wsRef.current?.close();
      pcRef.current?.close();
    };
  }, []);

  return (
    <VideoCallContext.Provider
      value={{
        rooms,
        isLobbyOpen,
        activeCall,
        callStatus,
        localStream,
        remoteStream,
        micEnabled,
        cameraEnabled,
        openLobby,
        closeLobby,
        refreshRooms,
        createRoom,
        joinRoom,
        leaveCall,
        toggleMic,
        toggleCamera,
      }}
    >
      {children}
    </VideoCallContext.Provider>
  );
}

export function useVideoCall() {
  const ctx = useContext(VideoCallContext);
  if (!ctx) throw new Error('useVideoCall must be used within VideoCallProvider');
  return ctx;
}
