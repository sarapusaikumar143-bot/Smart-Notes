import React, { useState, useRef } from 'react';
import { X, Mic, Check, Volume2, Loader2, Sparkles } from 'lucide-react';
import { parseNaturalExpense } from '../utils/ai';
import { TransactionType } from '../types';
import { AudioRecorder, transcribeAudioWithGemini } from '../utils/geminiAudio';

interface VoiceModalProps {
  onClose: () => void;
  onConfirm: (entry: {
    title: string;
    amount: number;
    type: TransactionType;
    category: string;
  }) => void;
}

export const VoiceModal: React.FC<VoiceModalProps> = ({ onClose, onConfirm }) => {
  const [transcript, setTranscript] = useState('Add petrol 500');
  const [isRecording, setIsRecording] = useState(false);
  const [isTranscribing, setIsTranscribing] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [parsed, setParsed] = useState(parseNaturalExpense('Add petrol 500'));

  const recorderRef = useRef<AudioRecorder | null>(null);

  const sampleCommands = [
    "Add petrol 500",
    "Biryani 250",
    "Uber cab 320",
    "Salary credited 55000",
    "Coffee and snack 180",
    "Supermarket groceries 1450"
  ];

  const handleStartRecording = async () => {
    try {
      setStatusMessage("Listening... Speak your transaction clearly");
      recorderRef.current = new AudioRecorder();
      await recorderRef.current.start();
      setIsRecording(true);
    } catch (err: any) {
      console.warn("Could not access microphone, attempting browser recognition:", err);
      fallbackSpeechRecognition();
    }
  };

  const handleStopRecording = async () => {
    if (!recorderRef.current || !isRecording) return;
    setIsRecording(false);
    setIsTranscribing(true);
    setStatusMessage("Transcribing audio with gemini-3.5-transcribe...");

    try {
      const { base64, mimeType } = await recorderRef.current.stop();
      const transcribedText = await transcribeAudioWithGemini(base64, mimeType);

      if (transcribedText.trim()) {
        setTranscript(transcribedText);
        setParsed(parseNaturalExpense(transcribedText));
        setStatusMessage("Transcribed successfully with gemini-3.5-transcribe ✨");
      } else {
        setStatusMessage("No speech detected. Try again or tap a sample.");
      }
    } catch (err: any) {
      console.error("Transcription error, using browser fallback:", err);
      setStatusMessage("Audio captured. Enter transaction details or tap below.");
    } finally {
      setIsTranscribing(false);
    }
  };

  const fallbackSpeechRecognition = () => {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = false;
      recognition.lang = 'en-IN';

      recognition.onstart = () => {
        setIsRecording(true);
        setStatusMessage("Listening...");
      };
      recognition.onresult = (event: any) => {
        const text = event.results[0][0].transcript;
        setTranscript(text);
        setParsed(parseNaturalExpense(text));
        setIsRecording(false);
        setStatusMessage("Voice captured!");
      };
      recognition.onerror = () => {
        setIsRecording(false);
        setStatusMessage("Could not hear clearly. Try again.");
      };
      recognition.onend = () => setIsRecording(false);

      try {
        recognition.start();
      } catch (e) {
        setIsRecording(false);
      }
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-sm bg-[#0F172A] border border-slate-700/80 rounded-3xl p-6 shadow-2xl text-center">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <h3 className="text-base font-bold text-white flex items-center gap-1.5">
              <Volume2 className="w-4 h-4 text-blue-400" />
              AI Voice Transcribe
            </h3>
            <span className="text-[9px] font-bold px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-300 border border-blue-500/30">
              gemini-3.5-transcribe
            </span>
          </div>
          <button onClick={onClose} className="p-1 rounded-full text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Pulsing Mic Visualizer */}
        <div className="my-5 relative flex items-center justify-center">
          <div className={`w-24 h-24 rounded-full bg-blue-500/15 absolute transition-all duration-700 ${
            isRecording ? 'scale-150 animate-ping opacity-75' : 'scale-100'
          }`} />
          <button
            onClick={isRecording ? handleStopRecording : handleStartRecording}
            disabled={isTranscribing}
            className={`w-20 h-20 rounded-full flex items-center justify-center text-white shadow-xl z-10 transition-transform active:scale-95 ${
              isRecording ? 'bg-rose-600 animate-pulse' : 'bg-gradient-to-r from-blue-600 to-emerald-500 hover:opacity-95'
            }`}
          >
            {isTranscribing ? (
              <Loader2 className="w-8 h-8 animate-spin" />
            ) : (
              <Mic className="w-8 h-8" />
            )}
          </button>
        </div>

        <p className="text-xs font-semibold text-white mb-1">
          {isRecording ? "🔴 Recording... Tap mic when finished" : isTranscribing ? "Processing audio..." : "Tap mic to speak"}
        </p>

        {statusMessage && (
          <p className="text-[11px] text-blue-400 mb-2 italic">
            {statusMessage}
          </p>
        )}

        <div className="p-3 bg-slate-900/80 border border-slate-800 rounded-2xl my-3 text-left">
          <span className="text-[10px] font-bold text-slate-400 block mb-1 uppercase tracking-wider">
            Transcribed Input:
          </span>
          <p className="text-xs text-slate-200 font-medium">“{transcript}”</p>
        </div>

        {/* Extracted Card */}
        {parsed && parsed.amount > 0 && (
          <div className="p-3 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 text-left my-3 flex items-center justify-between">
            <div>
              <div className="text-xs font-bold text-white flex items-center gap-1">
                <span>{parsed.title}</span>
                <Sparkles className="w-3 h-3 text-emerald-400" />
              </div>
              <div className="text-[10px] text-emerald-400 font-medium">
                {parsed.category} • {parsed.type}
              </div>
            </div>
            <div className="text-base font-extrabold text-emerald-400">
              ₹{parsed.amount}
            </div>
          </div>
        )}

        {/* Quick Sample Voice Chips */}
        <div className="my-3 text-left">
          <span className="text-[10px] uppercase tracking-wider font-bold text-slate-400 block mb-1.5">
            Or tap quick voice shortcut:
          </span>
          <div className="flex flex-wrap gap-1.5">
            {sampleCommands.map((cmd) => (
              <button
                key={cmd}
                onClick={() => {
                  setTranscript(cmd);
                  setParsed(parseNaturalExpense(cmd));
                  setStatusMessage("Selected sample command");
                }}
                className="text-[11px] px-2 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700/60 transition"
              >
                {cmd}
              </button>
            ))}
          </div>
        </div>

        <div className="flex gap-2 mt-4">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-300 transition"
          >
            Cancel
          </button>
          <button
            onClick={() => {
              if (parsed.amount > 0) {
                onConfirm({
                  title: parsed.title,
                  amount: parsed.amount,
                  type: parsed.type,
                  category: parsed.category
                });
                onClose();
              }
            }}
            disabled={!parsed.amount || parsed.amount <= 0}
            className="flex-1 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-xs font-bold text-white shadow-lg transition flex items-center justify-center gap-1.5"
          >
            <Check className="w-4 h-4" />
            <span>Confirm & Add</span>
          </button>
        </div>
      </div>
    </div>
  );
};
