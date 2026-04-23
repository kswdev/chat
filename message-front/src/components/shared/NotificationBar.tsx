import { useEffect, useState } from 'react';
import { useChat } from '../../contexts/ChatContext';

export default function NotificationBar() {
  const { notifications, error, clearError } = useChat();
  const [visible, setVisible] = useState<string | null>(null);
  const [fade, setFade] = useState(false);

  const latestNotification = notifications[notifications.length - 1];

  useEffect(() => {
    if (error) {
      setVisible(`오류: ${error}`);
      setFade(false);
      const t1 = setTimeout(() => setFade(true), 3000);
      const t2 = setTimeout(() => {
        setVisible(null);
        clearError();
      }, 3500);
      return () => {
        clearTimeout(t1);
        clearTimeout(t2);
      };
    }
  }, [error, clearError]);

  useEffect(() => {
    if (latestNotification) {
      setVisible(latestNotification);
      setFade(false);
      const t1 = setTimeout(() => setFade(true), 3000);
      const t2 = setTimeout(() => setVisible(null), 3500);
      return () => {
        clearTimeout(t1);
        clearTimeout(t2);
      };
    }
  }, [latestNotification]);

  if (!visible) return null;

  return (
    <div
      className={`fixed bottom-6 right-6 z-50 max-w-sm transition-opacity duration-500 ${
        fade ? 'opacity-0' : 'opacity-100'
      }`}
    >
      <div
        className={`px-4 py-3 rounded-lg shadow-lg text-sm text-white ${
          error ? 'bg-red-600' : 'bg-gray-700'
        }`}
      >
        {visible}
      </div>
    </div>
  );
}
