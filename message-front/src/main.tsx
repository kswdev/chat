import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { AuthProvider } from './contexts/AuthContext';
import { WebSocketProvider } from './contexts/WebSocketContext';
import { VideoCallProvider } from './contexts/VideoCallContext';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <WebSocketProvider>
        <VideoCallProvider>
          <App />
        </VideoCallProvider>
      </WebSocketProvider>
    </AuthProvider>
  </React.StrictMode>,
);
