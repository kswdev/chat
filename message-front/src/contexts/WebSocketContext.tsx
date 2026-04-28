import React, {
  createContext,
  useContext,
  useRef,
  useState,
  useCallback,
  useEffect,
} from 'react';
import type { InboundMessage, WsStatus, BaseRequest } from '../types';

const WS_URL = 'ws://localhost:8080/ws/v1/message';
const KEEP_ALIVE_INTERVAL_MS = 60_000;

type MessageHandler = (msg: InboundMessage) => void;

interface WebSocketContextValue {
  status: WsStatus;
  connect: (token: string) => void;
  disconnect: () => void;
  send: (request: BaseRequest) => void;
  /** 특정 type의 메시지를 처리할 핸들러 등록 */
  addHandler: (type: string, handler: MessageHandler) => void;
  removeHandler: (type: string) => void;
}

const WebSocketContext = createContext<WebSocketContextValue | null>(null);

export function WebSocketProvider({ children }: { children: React.ReactNode }) {
  const wsRef = useRef<WebSocket | null>(null);
  const handlersRef = useRef<Map<string, MessageHandler>>(new Map());
  const keepAliveTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const [status, setStatus] = useState<WsStatus>('disconnected');

  const stopKeepAlive = useCallback(() => {
    if (keepAliveTimerRef.current) {
      clearInterval(keepAliveTimerRef.current);
      keepAliveTimerRef.current = null;
    }
  }, []);

  const send = useCallback((request: BaseRequest) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(request));
    }
  }, []);

  const startKeepAlive = useCallback(() => {
    stopKeepAlive();
    keepAliveTimerRef.current = setInterval(() => {
      send({ type: 'KEEP_ALIVE' });
    }, KEEP_ALIVE_INTERVAL_MS);
  }, [send, stopKeepAlive]);

  /**
   * 브라우저 WebSocket은 커스텀 헤더를 지원하지 않으므로
   * 토큰을 쿼리 파라미터로 전달합니다.
   * 서버의 AuthorizationHeaderFilter에서 쿼리 파라미터 토큰도
   * 허용하도록 수정이 필요합니다.
   */
  const closeWs = useCallback((ws: WebSocket) => {
    ws.onopen = null;
    ws.onmessage = null;
    ws.onerror = null;
    ws.onclose = null;
    ws.close();
  }, []);

  const connect = useCallback(
    (token: string) => {
      if (wsRef.current) {
        closeWs(wsRef.current);
        wsRef.current = null;
      }

      setStatus('connecting');
      const url = `${WS_URL}?token=${encodeURIComponent(token)}`;
      const ws = new WebSocket(url);
      wsRef.current = ws;

      ws.onopen = () => {
        setStatus('connected');
        startKeepAlive();
      };

      ws.onmessage = (event: MessageEvent) => {
        try {
          const msg = JSON.parse(event.data as string) as InboundMessage;
          const handler = handlersRef.current.get(msg.type);
          handler?.(msg);
        } catch (e) {
          console.error('[WS] Failed to parse message', e);
        }
      };

      ws.onerror = () => {
        setStatus('error');
      };

      ws.onclose = () => {
        setStatus('disconnected');
        stopKeepAlive();
        wsRef.current = null;
      };
    },
    [closeWs, startKeepAlive, stopKeepAlive],
  );

  const disconnect = useCallback(() => {
    stopKeepAlive();
    if (wsRef.current) {
      closeWs(wsRef.current);
      wsRef.current = null;
    }
    setStatus('disconnected');
  }, [closeWs, stopKeepAlive]);

  const addHandler = useCallback((type: string, handler: MessageHandler) => {
    handlersRef.current.set(type, handler);
  }, []);

  const removeHandler = useCallback((type: string) => {
    handlersRef.current.delete(type);
  }, []);

  // 컴포넌트 언마운트 시 정리
  useEffect(() => {
    return () => {
      stopKeepAlive();
      if (wsRef.current) {
        closeWs(wsRef.current);
        wsRef.current = null;
      }
    };
  }, [closeWs, stopKeepAlive]);

  return (
    <WebSocketContext.Provider
      value={{ status, connect, disconnect, send, addHandler, removeHandler }}
    >
      {children}
    </WebSocketContext.Provider>
  );
}

export function useWebSocket() {
  const ctx = useContext(WebSocketContext);
  if (!ctx) throw new Error('useWebSocket must be used within WebSocketProvider');
  return ctx;
}
