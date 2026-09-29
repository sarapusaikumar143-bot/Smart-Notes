import React, { useState } from 'react';
import { Lock, Delete } from 'lucide-react';

interface AppLockScreenProps {
  correctPin: string;
  onUnlock: () => void;
}

export const AppLockScreen: React.FC<AppLockScreenProps> = ({ correctPin, onUnlock }) => {
  const [enteredPin, setEnteredPin] = useState('');
  const [error, setError] = useState(false);

  const handleDigit = (digit: string) => {
    if (enteredPin.length < 4) {
      const nextPin = enteredPin + digit;
      setEnteredPin(nextPin);
      setError(false);

      if (nextPin.length === 4) {
        if (nextPin === correctPin) {
          onUnlock();
        } else {
          setError(true);
          setTimeout(() => {
            setEnteredPin('');
            setError(false);
          }, 600);
        }
      }
    }
  };

  const handleDelete = () => {
    setEnteredPin(prev => prev.slice(0, -1));
    setError(false);
  };

  const keypad = [
    ['1', '2', '3'],
    ['4', '5', '6'],
    ['7', '8', '9'],
    ['', '0', 'DEL']
  ];

  return (
    <div className="fixed inset-0 z-50 bg-[#0F172A] flex flex-col items-center justify-center p-6 text-center select-none">
      <div className="w-16 h-16 rounded-full bg-blue-500/15 border border-blue-500/30 flex items-center justify-center text-blue-400 mb-6">
        <Lock className="w-8 h-8" />
      </div>

      <h2 className="text-2xl font-extrabold text-white tracking-tight">AI Money Security</h2>
      <p className="text-xs text-slate-400 mt-1 mb-8">Enter your 4-digit PIN to access your finance vault</p>

      {/* PIN Indicators */}
      <div className="flex items-center gap-4 mb-4">
        {[0, 1, 2, 3].map(idx => {
          const isFilled = idx < enteredPin.length;
          return (
            <div
              key={idx}
              className={`w-4 h-4 rounded-full transition-all duration-200 border-2 ${
                isFilled
                  ? 'bg-blue-500 border-blue-400 scale-110 shadow-lg shadow-blue-500/50'
                  : error
                  ? 'border-rose-500 bg-rose-500/20 animate-shake'
                  : 'border-slate-600 bg-transparent'
              }`}
            />
          );
        })}
      </div>

      <div className="h-5 mb-8">
        {error && (
          <span className="text-xs font-semibold text-rose-400 animate-pulse">
            Incorrect PIN. Try again.
          </span>
        )}
      </div>

      {/* Keypad */}
      <div className="grid grid-cols-3 gap-5 max-w-[280px]">
        {keypad.flat().map((key, i) => {
          if (!key) {
            return <div key={i} className="w-16 h-16" />;
          }

          if (key === 'DEL') {
            return (
              <button
                key={i}
                onClick={handleDelete}
                className="w-16 h-16 rounded-full bg-slate-800/80 hover:bg-slate-700 active:scale-95 transition flex items-center justify-center text-slate-300 font-bold"
              >
                <Delete className="w-6 h-6" />
              </button>
            );
          }

          return (
            <button
              key={i}
              onClick={() => handleDigit(key)}
              className="w-16 h-16 rounded-full bg-slate-800 hover:bg-slate-700 active:scale-95 transition flex items-center justify-center text-xl font-bold text-white shadow-md border border-slate-700/50"
            >
              {key}
            </button>
          );
        })}
      </div>

      <p className="text-[11px] text-slate-500 mt-8">Default demo PIN is 1234</p>
    </div>
  );
};
