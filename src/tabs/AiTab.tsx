import React, { useState, useRef, useEffect } from 'react';
import {
  Send,
  Bot,
  User,
  Lightbulb,
  Search,
  Globe,
  Radio,
  Mic,
  Loader2,
  Sparkles,
  ExternalLink,
  ChevronDown
} from 'lucide-react';
import { ChatMessage, Transaction, Wallet } from '../types';
import {
  ChatModelTier,
  GroundingSource,
  sendGeminiChatMessage,
  buildSystemInstruction
} from '../utils/geminiChat';
import { AudioRecorder, transcribeAudioWithGemini } from '../utils/geminiAudio';

interface AiTabProps {
  messages: ChatMessage[];
  wallets: Wallet[];
  transactions: Transaction[];
  onSendMessage: (msg: ChatMessage) => void;
  onOpenLiveVoice: () => void;
}

interface EnrichedChatMessage extends ChatMessage {
  model?: string;
  sources?: GroundingSource[];
  searchQueries?: string[];
}

export const AiTab: React.FC<AiTabProps> = ({
  messages,
  wallets,
  transactions,
  onSendMessage,
  onOpenLiveVoice
}) => {
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [selectedModel, setSelectedModel] = useState<ChatModelTier>('gemini-3.5-flash');
  const [searchGroundingEnabled, setSearchGroundingEnabled] = useState(false);
  const [selectedRole, setSelectedRole] = useState<'Financial Advisor' | 'Smart Budget Auditor' | 'Tax & Wealth Strategist'>('Financial Advisor');
  const [isRecording, setIsRecording] = useState(false);
  const [recordingStatus, setRecordingStatus] = useState<string | null>(null);

  const [chatLog, setChatLog] = useState<EnrichedChatMessage[]>(() =>
    messages.map(m => ({ ...m }))
  );

  const bottomRef = useRef<HTMLDivElement>(null);
  const recorderRef = useRef<AudioRecorder | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatLog, isTyping]);

  const quickPrompts = [
    "How can I save more money?",
    "What is my daily burn rate?",
    "Where did I spend the most?",
    "Can I afford a ₹3,000 weekend trip?",
    "Current RBI repo rate and fixed deposit returns",
    "Predict my month-end balance"
  ];

  const handleStartTranscribe = async () => {
    try {
      setRecordingStatus("Listening...");
      recorderRef.current = new AudioRecorder();
      await recorderRef.current.start();
      setIsRecording(true);
    } catch (e) {
      console.warn("Could not access mic:", e);
      setRecordingStatus("Mic not available");
      setTimeout(() => setRecordingStatus(null), 2000);
    }
  };

  const handleStopTranscribe = async () => {
    if (!recorderRef.current || !isRecording) return;
    setIsRecording(false);
    setRecordingStatus("Transcribing with gemini-3.5-transcribe...");

    try {
      const { base64, mimeType } = await recorderRef.current.stop();
      const text = await transcribeAudioWithGemini(base64, mimeType);
      if (text.trim()) {
        setInput(prev => (prev ? `${prev} ${text}` : text));
        setRecordingStatus(null);
      } else {
        setRecordingStatus("No speech detected");
        setTimeout(() => setRecordingStatus(null), 2000);
      }
    } catch (e) {
      setRecordingStatus("Transcription failed");
      setTimeout(() => setRecordingStatus(null), 2000);
    }
  };

  const handleSend = async (text: string) => {
    if (!text.trim()) return;

    const userMsg: EnrichedChatMessage = {
      id: `msg_${Date.now()}`,
      text: text.trim(),
      isUser: true,
      timestamp: Date.now()
    };

    setChatLog(prev => [...prev, userMsg]);
    onSendMessage(userMsg);
    setInput('');
    setIsTyping(true);

    try {
      // Build multi-turn history from current chatLog
      const history = chatLog.map(m => ({
        role: m.isUser ? ('user' as const) : ('model' as const),
        text: m.text
      }));

      const systemInstruction = buildSystemInstruction(selectedRole, wallets, transactions);

      const result = await sendGeminiChatMessage({
        model: selectedModel,
        history,
        newMessage: text.trim(),
        systemInstruction,
        enableSearchGrounding: searchGroundingEnabled
      });

      const aiMsg: EnrichedChatMessage = {
        id: `msg_${Date.now() + 1}`,
        text: result.text,
        isUser: false,
        timestamp: Date.now(),
        model: searchGroundingEnabled ? 'gemini-3.5-flash (Google Search)' : selectedModel,
        sources: result.sources,
        searchQueries: result.searchQueries
      };

      setChatLog(prev => [...prev, aiMsg]);
      onSendMessage(aiMsg);
    } catch (err: any) {
      const errorMsg: EnrichedChatMessage = {
        id: `msg_${Date.now() + 1}`,
        text: `Error: ${err.message || 'Failed to generate response'}`,
        isUser: false,
        timestamp: Date.now()
      };
      setChatLog(prev => [...prev, errorMsg]);
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-140px)] pb-16">
      {/* Model Selection & Live Voice Header */}
      <div className="p-3 bg-[#0F172A] border border-slate-800 rounded-2xl mb-2.5 space-y-2.5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-xl bg-blue-500/20 text-blue-400 flex items-center justify-center shrink-0">
              <Bot className="w-4 h-4" />
            </div>
            <div>
              <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                Gemini Financial Copilot
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              </h4>
              <p className="text-[10px] text-slate-400">
                Multi-turn conversation with financial context
              </p>
            </div>
          </div>

          {/* Real-time Live Voice conversation trigger */}
          <button
            onClick={onOpenLiveVoice}
            className="px-2.5 py-1.5 rounded-xl bg-gradient-to-r from-purple-600 to-blue-600 hover:opacity-90 active:scale-95 text-white text-[11px] font-bold flex items-center gap-1.5 shadow-md transition"
            title="Start live voice conversation with gemini-3.8-live"
          >
            <Radio className="w-3.5 h-3.5 animate-pulse text-amber-300" />
            <span>Live Voice</span>
          </button>
        </div>

        {/* Controls: Model Tiers & Search Grounding */}
        <div className="pt-2 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-2">
          {/* Model Selector Tabs */}
          <div className="flex bg-slate-900 p-0.5 rounded-xl border border-slate-800 text-[10px] font-semibold">
            <button
              onClick={() => setSelectedModel('gemini-3.1-flash-lite')}
              className={`px-2 py-1 rounded-lg transition ${
                selectedModel === 'gemini-3.1-flash-lite' && !searchGroundingEnabled
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
              title="Fastest model for quick tasks"
            >
              ⚡ Fast (Flash-Lite)
            </button>
            <button
              onClick={() => setSelectedModel('gemini-3.5-flash')}
              className={`px-2 py-1 rounded-lg transition ${
                selectedModel === 'gemini-3.5-flash' && !searchGroundingEnabled
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
              title="Balanced general tasks"
            >
              ⚖️ General (3.5 Flash)
            </button>
            <button
              onClick={() => setSelectedModel('gemini-3.1-pro-preview')}
              className={`px-2 py-1 rounded-lg transition ${
                selectedModel === 'gemini-3.1-pro-preview' && !searchGroundingEnabled
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
              title="High intelligence for complex tasks"
            >
              🧠 Complex (3.1 Pro)
            </button>
          </div>

          {/* Search Grounding Toggle */}
          <button
            onClick={() => setSearchGroundingEnabled(!searchGroundingEnabled)}
            className={`px-2.5 py-1 rounded-xl text-[10px] font-bold flex items-center gap-1.5 transition border ${
              searchGroundingEnabled
                ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40 shadow-sm'
                : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-slate-200'
            }`}
            title="Search Grounding with gemini-3.5-flash and googleSearch tool"
          >
            <Globe className="w-3 h-3 text-emerald-400" />
            <span>Search Grounding</span>
            <span className={`w-1.5 h-1.5 rounded-full ${searchGroundingEnabled ? 'bg-emerald-400' : 'bg-slate-600'}`} />
          </button>
        </div>

        {/* Persona Selector */}
        <div className="flex items-center gap-1.5 text-[10px] text-slate-400 pt-1">
          <span className="font-semibold text-slate-500">Role:</span>
          {(['Financial Advisor', 'Smart Budget Auditor', 'Tax & Wealth Strategist'] as const).map(role => (
            <button
              key={role}
              onClick={() => setSelectedRole(role)}
              className={`px-2 py-0.5 rounded-md transition ${
                selectedRole === role
                  ? 'bg-blue-500/20 text-blue-300 font-bold border border-blue-500/30'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {role.split(' ')[0]}
            </button>
          ))}
        </div>
      </div>

      {/* Chat Messages Thread */}
      <div className="flex-1 overflow-y-auto space-y-3 pr-1">
        {chatLog.map(msg => (
          <div
            key={msg.id}
            className={`flex items-start gap-2.5 ${msg.isUser ? 'flex-row-reverse' : ''}`}
          >
            <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 text-xs ${
              msg.isUser ? 'bg-blue-600 text-white' : 'bg-slate-800 text-blue-400 border border-slate-700'
            }`}>
              {msg.isUser ? <User className="w-3.5 h-3.5" /> : <Bot className="w-3.5 h-3.5" />}
            </div>

            <div className={`max-w-[85%] rounded-2xl p-3 text-xs leading-relaxed ${
              msg.isUser
                ? 'bg-blue-600 text-white rounded-tr-none'
                : 'bg-slate-800/95 text-slate-200 border border-slate-700/80 rounded-tl-none whitespace-pre-line'
            }`}>
              {msg.text}

              {/* Model Tag */}
              {!msg.isUser && msg.model && (
                <div className="mt-2 pt-1.5 border-t border-slate-700/60 flex items-center justify-between text-[10px] text-slate-400">
                  <span className="flex items-center gap-1 text-blue-300">
                    <Sparkles className="w-3 h-3" />
                    {msg.model}
                  </span>
                  <span>{new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                </div>
              )}

              {/* Grounding Sources (Search Grounding) */}
              {!msg.isUser && msg.sources && msg.sources.length > 0 && (
                <div className="mt-2 pt-2 border-t border-slate-700/60 space-y-1">
                  <span className="text-[10px] font-bold text-emerald-400 flex items-center gap-1">
                    <Globe className="w-3 h-3" />
                    Grounded with Google Search Sources:
                  </span>
                  <div className="flex flex-wrap gap-1">
                    {msg.sources.map((src, idx) => (
                      <a
                        key={idx}
                        href={src.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-[10px] px-2 py-0.5 rounded bg-slate-900/80 hover:bg-slate-900 text-slate-300 hover:text-white border border-slate-700/80 flex items-center gap-1 transition"
                      >
                        <span className="truncate max-w-[140px]">{src.title || src.url}</span>
                        <ExternalLink className="w-2.5 h-2.5 opacity-60" />
                      </a>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        ))}

        {isTyping && (
          <div className="flex items-center gap-2 text-slate-400 text-xs italic py-1 pl-1">
            <Loader2 className="w-4 h-4 text-blue-400 animate-spin" />
            <span>
              {searchGroundingEnabled
                ? 'Searching Google & analyzing market data with gemini-3.5-flash...'
                : `Analyzing finances with ${selectedModel}...`}
            </span>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Quick Prompts */}
      <div className="py-2">
        <div className="flex items-center gap-1 text-[11px] font-bold text-slate-400 mb-1.5 px-1">
          <Lightbulb className="w-3 h-3 text-amber-400" />
          <span>Tap prompt:</span>
        </div>
        <div className="flex gap-1.5 overflow-x-auto pb-1 no-scrollbar">
          {quickPrompts.map(prompt => (
            <button
              key={prompt}
              onClick={() => handleSend(prompt)}
              className="text-[11px] px-2.5 py-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 text-slate-300 border border-slate-700 whitespace-nowrap transition"
            >
              {prompt}
            </button>
          ))}
        </div>
      </div>

      {recordingStatus && (
        <div className="text-[11px] text-blue-400 italic px-2 pb-1 flex items-center gap-1.5">
          <span className="w-2 h-2 rounded-full bg-rose-500 animate-pulse" />
          {recordingStatus}
        </div>
      )}

      {/* Input Bar with Audio Transcription (gemini-3.5-transcribe) */}
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSend(input);
        }}
        className="flex items-center gap-2 pt-1"
      >
        <button
          type="button"
          onClick={isRecording ? handleStopRecording : handleStartTranscribe}
          className={`w-10 h-10 rounded-xl flex items-center justify-center transition shrink-0 ${
            isRecording
              ? 'bg-rose-600 text-white animate-pulse'
              : 'bg-slate-800 hover:bg-slate-700 text-blue-400 border border-slate-700'
          }`}
          title="Audio Transcription (gemini-3.5-transcribe)"
        >
          <Mic className="w-4 h-4" />
        </button>

        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder={`Ask ${selectedRole} (using ${searchGroundingEnabled ? 'Google Search Grounding' : selectedModel.split('-')[1]})...`}
          className="flex-1 bg-slate-800/90 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        <button
          type="submit"
          disabled={!input.trim() || isTyping}
          className="w-10 h-10 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:opacity-40 text-white flex items-center justify-center transition shadow-md shrink-0"
        >
          <Send className="w-4 h-4" />
        </button>
      </form>
    </div>
  );
};
