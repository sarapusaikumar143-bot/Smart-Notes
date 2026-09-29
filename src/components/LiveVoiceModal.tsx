import React, { useState, useEffect, useRef } from 'react';
import { X, Mic, MicOff, PhoneOff, Radio, Sparkles, Volume2 } from 'lucide-react';
import { GeminiLiveSession, LiveMessage } from '../utils/geminiLive';
import { Transaction, Wallet } from '../types';

interface LiveVoiceModalProps {
  wallets: Wallet[];
  transactions: Transaction[];
  onClose: () => void;
}

export const LiveVoiceModal: React.FC<LiveVoiceModalProps> = ({ wallets, transactions, onClose }) => {
  const [messages, setMessages] = useState<LiveMessage[]>([
    {
      id: 'init_live',
      sender: 'agent',
      text: "👋 I'm listening. Ask me anything about your money, expenses, or savings goals.",
      timestamp: Date.now()
    }
  ]);
  const [status, setStatus] = useState<'connecting' | 'connected' | 'speaking' | 'disconnected' | 'error'>('connecting');
  const [isMuted, setIsMuted] = useState(false);
  const sessionRef = useRef<GeminiLiveSession | null>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const totalBalance = wallets.reduce((acc, w) => acc + w.balance, 0);
    const summary = `Total Balance: ₹${Math.round(totalBalance)}. Wallets: ${wallets.length}. Transactions: ${transactions.length}.`;

    const session = new GeminiLiveSession(
      (msg) => {
        setMessages(prev => [...prev, msg]);
      },
      (newStatus) => {
        setStatus(newStatus);
      }
    );

    sessionRef.current = session;
    session.start(summary).catch(e => {
      console.error('Session start error:', e);
    });

    return () => {
      session.stop();
    };
  }, []);

  useEffect(() => {
    scrollRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, status]);

  const handleEndCall = () => {
    sessionRef.current?.stop();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-sm bg-[#0F172A] border border-blue-500/40 rounded-3xl p-6 shadow-2xl flex flex-col h-[520px]">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <div className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping" />
            <h3 className="text-sm font-bold text-white flex items-center gap-1.5">
              Live Voice Advisor
            </h3>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-purple-500/20 text-purple-300 border border-purple-500/30">
              gemini-3.8-live
            </span>
          </div>

          <button onClick={handleEndCall} className="p-1 rounded-full text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Center Live Waveform Visualizer */}
        <div className="py-6 flex flex-col items-center justify-center text-center">
          <div className="relative flex items-center justify-center">
            {/* Concentric pulse rings */}
            <div className={`w-28 h-28 rounded-full bg-gradient-to-r from-blue-500/20 to-purple-500/20 absolute transition-all duration-500 ${
              status === 'speaking' ? 'scale-150 animate-pulse' : 'scale-100'
            }`} />
            <div className={`w-20 h-20 rounded-full bg-gradient-to-r from-blue-600 to-purple-600 flex items-center justify-center text-white shadow-xl z-10 ${
              status === 'speaking' ? 'animate-bounce' : ''
            }`}>
              <Radio className="w-9 h-9" />
            </div>
          </div>

          <div className="mt-4 flex items-center gap-2">
            <span className="text-xs font-semibold text-slate-300">
              {status === 'connecting' && 'Connecting to Live API...'}
              {status === 'connected' && 'Listening... Speak freely'}
              {status === 'speaking' && 'Live AI is speaking...'}
              {status === 'disconnected' && 'Call ended'}
              {status === 'error' && 'Reconnecting live session...'}
            </span>
            {status === 'speaking' && <Volume2 className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />}
          </div>

          <div className="flex gap-1 items-center mt-2 h-4">
            {[40, 70, 95, 60, 85, 45, 90, 60].map((h, i) => (
              <span
                key={i}
                className="w-1 bg-gradient-to-t from-blue-500 to-emerald-400 rounded-full transition-all duration-150"
                style={{
                  height: status === 'speaking' ? `${(h * Math.random() + 20).toFixed(0)}%` : '20%'
                }}
              />
            ))}
          </div>
        </div>

        {/* Real-time Rolling Transcript */}
        <div className="flex-1 overflow-y-auto bg-slate-900/60 rounded-2xl p-3 border border-slate-800 space-y-2 text-xs">
          {messages.map(m => (
            <div
              key={m.id}
              className={`p-2.5 rounded-xl ${
                m.sender === 'user'
                  ? 'bg-blue-600/30 text-blue-100 border border-blue-500/30 ml-4'
                  : 'bg-slate-800/80 text-slate-200 border border-slate-700/60 mr-4'
              }`}
            >
              <span className="text-[10px] font-bold block opacity-60 mb-0.5">
                {m.sender === 'user' ? 'You' : 'Gemini 3.8 Live'}
              </span>
              <p className="leading-relaxed">{m.text}</p>
            </div>
          ))}
          <div ref={scrollRef} />
        </div>

        {/* Action Controls */}
        <div className="pt-4 flex items-center justify-center gap-4">
          <button
            onClick={() => setIsMuted(!isMuted)}
            className={`w-12 h-12 rounded-full flex items-center justify-center border transition ${
              isMuted
                ? 'bg-amber-500/20 border-amber-500/40 text-amber-400'
                : 'bg-slate-800 border-slate-700 text-slate-300 hover:text-white'
            }`}
            title={isMuted ? 'Unmute' : 'Mute'}
          >
            {isMuted ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
          </button>

          <button
            onClick={handleEndCall}
            className="w-14 h-14 rounded-full bg-rose-600 hover:bg-rose-500 active:scale-95 text-white flex items-center justify-center shadow-lg transition"
            title="End Conversation"
          >
            <PhoneOff className="w-6 h-6" />
          </button>
        </div>
      </div>
    </div>
  );
};
