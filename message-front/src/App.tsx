import { useEffect } from 'react';
import { useAuth } from './contexts/AuthContext';
import { useWebSocket } from './contexts/WebSocketContext';
import { ChatProvider } from './contexts/ChatContext';
import { useVideoCall } from './contexts/VideoCallContext';
import AuthPage from './components/auth/AuthPage';
import Sidebar from './components/sidebar/Sidebar';
import ChatView from './components/chat/ChatView';
import NotificationBar from './components/shared/NotificationBar';
import RoomListView from './components/videoCall/RoomListView';
import CallView from './components/videoCall/CallView';

function ChatApp() {
  const { token } = useAuth();
  const { connect, disconnect } = useWebSocket();
  const { isLobbyOpen, activeCall } = useVideoCall();

  // 로그인 시 WebSocket 연결, 로그아웃 시 해제
  useEffect(() => {
    if (token) {
      connect(token);
    } else {
      disconnect();
    }
    return () => {
      disconnect();
    };
  }, [token, connect, disconnect]);

  return (
    <ChatProvider>
      <div className="flex h-screen overflow-hidden">
        <Sidebar />
        {isLobbyOpen ? <RoomListView /> : <ChatView />}
      </div>
      <NotificationBar />
      {activeCall && <CallView />}
    </ChatProvider>
  );
}

export default function App() {
  const { isLoggedIn } = useAuth();

  if (!isLoggedIn) {
    return <AuthPage />;
  }

  return <ChatApp />;
}
