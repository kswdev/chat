import { useState } from 'react';

interface InviteCodeDisplayProps {
  code: string;
}

export default function InviteCodeDisplay({ code }: InviteCodeDisplayProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    });
  };

  return (
    <div className="flex items-center gap-2 mt-2">
      <div className="flex-1 bg-gray-900 rounded px-3 py-2 font-mono text-sm text-indigo-300 break-all">
        {code}
      </div>
      <button
        onClick={handleCopy}
        className="px-3 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm rounded transition-colors whitespace-nowrap"
      >
        {copied ? '복사됨!' : '복사'}
      </button>
    </div>
  );
}
