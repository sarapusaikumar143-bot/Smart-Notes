import React, { useState, useRef } from 'react';
import {
  Plus,
  CheckSquare,
  Square,
  Trash2,
  Edit3,
  Sparkles,
  ListChecks,
  Globe,
  ArrowRight,
  Search,
  Mic,
  Loader2
} from 'lucide-react';
import { ChecklistItem, Note, TransactionType, Wallet } from '../types';
import { summarizeNote, convertToChecklist, translateNote, parseNaturalExpense } from '../utils/ai';
import { AudioRecorder, transcribeAudioWithGemini } from '../utils/geminiAudio';

interface NotesTabProps {
  notes: Note[];
  wallets?: Wallet[];
  onSaveNote: (note: Note) => void;
  onDeleteNote: (id: string) => void;
  onToggleChecklistItem?: (noteId: string, itemId: string) => void;
  onQuickAddExpense?: (entry: { title: string; amount: number; type: TransactionType; category: string }) => void;
  onAddExpenseFromNote?: (title: string, amount: number, category: string) => void;
}

export const NotesTab: React.FC<NotesTabProps> = ({
  notes,
  wallets,
  onSaveNote,
  onDeleteNote,
  onToggleChecklistItem,
  onQuickAddExpense,
  onAddExpenseFromNote
}) => {
  const [search, setSearch] = useState('');
  const [editingNote, setEditingNote] = useState<Note | null>(null);
  const [isNew, setIsNew] = useState(false);

  // New/Edit Note Form States
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isChecklist, setIsChecklist] = useState(false);
  const [checklistItems, setChecklistItems] = useState<ChecklistItem[]>([]);
  const [category, setCategory] = useState('Finance');
  const [color, setColor] = useState('#3B82F6');
  const [detectedExpense, setDetectedExpense] = useState<{ title: string; amount: number; category: string } | null>(null);

  // Audio Transcription state
  const [isDictating, setIsDictating] = useState(false);
  const [dictateStatus, setDictateStatus] = useState<string | null>(null);
  const recorderRef = useRef<AudioRecorder | null>(null);

  const colors = ['#3B82F6', '#22C55E', '#F59E0B', '#8B5CF6', '#EC4899', '#0F172A'];

  const filteredNotes = notes.filter(n =>
    n.title.toLowerCase().includes(search.toLowerCase()) ||
    n.content.toLowerCase().includes(search.toLowerCase()) ||
    n.category.toLowerCase().includes(search.toLowerCase())
  );

  const openEditor = (note?: Note) => {
    if (note) {
      setEditingNote(note);
      setIsNew(false);
      setTitle(note.title);
      setContent(note.content);
      setIsChecklist(note.isChecklist);
      setChecklistItems(note.checklistItems || []);
      setCategory(note.category);
      setColor(note.color);
    } else {
      setEditingNote(null);
      setIsNew(true);
      setTitle('');
      setContent('');
      setIsChecklist(false);
      setChecklistItems([]);
      setCategory('Finance');
      setColor('#3B82F6');
    }
    setDetectedExpense(null);
    setDictateStatus(null);
  };

  const handleContentChange = (text: string) => {
    setContent(text);
    const parsed = parseNaturalExpense(text);
    if (parsed.amount > 0) {
      setDetectedExpense({
        title: parsed.title,
        amount: parsed.amount,
        category: parsed.category
      });
    } else {
      setDetectedExpense(null);
    }
  };

  const handleStartDictation = async () => {
    try {
      setDictateStatus("Listening... Dictate your note");
      recorderRef.current = new AudioRecorder();
      await recorderRef.current.start();
      setIsDictating(true);
    } catch (e) {
      setDictateStatus("Microphone access needed");
      setTimeout(() => setDictateStatus(null), 2000);
    }
  };

  const handleStopDictation = async () => {
    if (!recorderRef.current || !isDictating) return;
    setIsDictating(false);
    setDictateStatus("Transcribing with gemini-3.5-transcribe...");

    try {
      const { base64, mimeType } = await recorderRef.current.stop();
      const transcribed = await transcribeAudioWithGemini(base64, mimeType);
      if (transcribed.trim()) {
        const updated = content ? `${content}\n${transcribed}` : transcribed;
        handleContentChange(updated);
        setDictateStatus("Dictation added ✨");
        setTimeout(() => setDictateStatus(null), 2500);
      } else {
        setDictateStatus("No speech detected");
        setTimeout(() => setDictateStatus(null), 2000);
      }
    } catch (e) {
      setDictateStatus("Transcription failed");
      setTimeout(() => setDictateStatus(null), 2000);
    }
  };

  const handleSave = () => {
    const noteObj: Note = {
      id: editingNote ? editingNote.id : `note_${Date.now()}`,
      title: title.trim() || 'Untitled Note',
      content: content.trim(),
      isChecklist,
      checklistItems,
      category,
      color,
      createdAt: editingNote ? editingNote.createdAt : Date.now(),
      updatedAt: Date.now()
    };
    onSaveNote(noteObj);
    setEditingNote(null);
    setIsNew(false);
  };

  const handleAddDetectedExpense = () => {
    if (!detectedExpense) return;
    if (onQuickAddExpense) {
      onQuickAddExpense({
        title: detectedExpense.title,
        amount: detectedExpense.amount,
        type: 'EXPENSE',
        category: detectedExpense.category
      });
    } else if (onAddExpenseFromNote) {
      onAddExpenseFromNote(detectedExpense.title, detectedExpense.amount, detectedExpense.category);
    }
    setDetectedExpense(null);
  };

  return (
    <div className="space-y-4 pb-24">
      {/* Search Bar */}
      <div className="relative">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search smart notes, checklists, finance tags..."
          className="w-full bg-[#0F172A] border border-slate-800 rounded-2xl px-4 py-3 pl-10 text-xs text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 shadow-sm"
        />
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
      </div>

      {/* Notes Grid / List */}
      {filteredNotes.length === 0 ? (
        <div className="rounded-2xl p-8 bg-[#0F172A] border border-slate-800 text-center">
          <p className="text-xs font-semibold text-slate-300">No Smart Notes Yet</p>
          <p className="text-[11px] text-slate-500 mt-1">
            Create grocery checklists, monthly savings rules, or bill reminders with AI features.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredNotes.map(note => (
            <div
              key={note.id}
              className="rounded-2xl p-4 bg-[#0F172A] border border-slate-800 hover:border-slate-700 transition shadow-sm space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: note.color }} />
                  <h4 className="text-sm font-bold text-white">{note.title}</h4>
                </div>

                <div className="flex items-center gap-1.5">
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700">
                    {note.category}
                  </span>
                  <button
                    onClick={() => openEditor(note)}
                    className="p-1 rounded-lg text-slate-400 hover:text-blue-400 hover:bg-slate-800"
                    title="Edit Note"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => onDeleteNote(note.id)}
                    className="p-1 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800"
                    title="Delete Note"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Note Body */}
              {note.isChecklist ? (
                <div className="space-y-1.5 pt-1">
                  {note.checklistItems.map(item => (
                    <div
                      key={item.id}
                      onClick={() => onToggleChecklistItem && onToggleChecklistItem(note.id, item.id)}
                      className="flex items-center gap-2 text-xs text-slate-300 cursor-pointer select-none"
                    >
                      {item.done ? (
                        <CheckSquare className="w-4 h-4 text-emerald-400 shrink-0" />
                      ) : (
                        <Square className="w-4 h-4 text-slate-500 shrink-0" />
                      )}
                      <span className={item.done ? 'line-through text-slate-500' : ''}>
                        {item.text}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-slate-300 whitespace-pre-line leading-relaxed">
                  {note.content}
                </p>
              )}

              <div className="text-[10px] text-slate-500 pt-1 border-t border-slate-800/80">
                Updated {new Date(note.updatedAt).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Floating Add Note Button */}
      <button
        onClick={() => openEditor()}
        className="fixed bottom-20 right-6 w-14 h-14 rounded-full bg-blue-600 hover:bg-blue-500 active:scale-95 text-white flex items-center justify-center shadow-2xl transition z-20"
        title="Create Smart Note"
      >
        <Plus className="w-6 h-6 stroke-[2.5]" />
      </button>

      {/* Editor Modal */}
      {(isNew || editingNote) && (
        <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="w-full max-w-md bg-[#0F172A] border border-slate-700 rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-base font-bold text-white">
                {isNew ? 'Create Smart Note' : 'Edit Note'}
              </h3>
              <button
                onClick={() => { setIsNew(false); setEditingNote(null); }}
                className="p-1 rounded-full text-slate-400 hover:text-white"
              >
                ✕
              </button>
            </div>

            {/* AI Toolbar with Dictate (gemini-3.5-transcribe) */}
            <div className="p-2 bg-slate-800/80 rounded-xl mb-3 flex items-center justify-around border border-slate-700/60">
              <button
                type="button"
                onClick={isDictating ? handleStopDictation : handleStartDictation}
                className={`flex items-center gap-1 text-[11px] font-bold py-1 px-2 rounded-lg transition ${
                  isDictating ? 'bg-rose-600 text-white animate-pulse' : 'text-blue-400 hover:text-blue-300 hover:bg-slate-700/50'
                }`}
                title="Dictate with gemini-3.5-transcribe"
              >
                <Mic className="w-3.5 h-3.5" />
                <span>{isDictating ? 'Stop' : 'Dictate'}</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  const summary = summarizeNote(content);
                  setContent(prev => `${prev}\n\n📝 AI Summary:\n${summary}`);
                }}
                className="flex items-center gap-1 text-[11px] font-bold text-blue-400 hover:text-blue-300 py-1 px-2 rounded-lg hover:bg-slate-700/50"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>Summarize</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  const items = convertToChecklist(content);
                  setIsChecklist(true);
                  setChecklistItems(items);
                }}
                className="flex items-center gap-1 text-[11px] font-bold text-emerald-400 hover:text-emerald-300 py-1 px-2 rounded-lg hover:bg-slate-700/50"
              >
                <ListChecks className="w-3.5 h-3.5" />
                <span>To Checklist</span>
              </button>

              <button
                type="button"
                onClick={() => {
                  const translated = translateNote(content, 'Telugu');
                  setContent(prev => `${prev}\n\n${translated}`);
                }}
                className="flex items-center gap-1 text-[11px] font-bold text-amber-400 hover:text-amber-300 py-1 px-2 rounded-lg hover:bg-slate-700/50"
              >
                <Globe className="w-3.5 h-3.5" />
                <span>Telugu</span>
              </button>
            </div>

            {dictateStatus && (
              <div className="text-[11px] text-blue-400 italic mb-2 px-1 flex items-center gap-1">
                {isDictating && <Loader2 className="w-3 h-3 animate-spin" />}
                <span>{dictateStatus}</span>
              </div>
            )}

            {/* Detected Expense in Note Banner */}
            {detectedExpense && (
              <div className="p-2.5 rounded-xl bg-emerald-500/15 border border-emerald-500/30 mb-3 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-white">Expense detected:</span> {detectedExpense.title} (₹{detectedExpense.amount})
                </div>
                <button
                  type="button"
                  onClick={handleAddDetectedExpense}
                  className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg font-bold flex items-center gap-1"
                >
                  <span>+ Add</span>
                  <ArrowRight className="w-3 h-3" />
                </button>
              </div>
            )}

            <div className="space-y-3">
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Note Title..."
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3.5 py-2.5 text-sm font-bold text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              />

              <textarea
                rows={5}
                value={content}
                onChange={(e) => handleContentChange(e.target.value)}
                placeholder="Write your note, list items, or finance reminders (e.g. Bought groceries for 850)..."
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 leading-relaxed"
              />

              {/* Color picker */}
              <div className="flex items-center gap-2 pt-1">
                <span className="text-[11px] text-slate-400 font-semibold">Color:</span>
                <div className="flex items-center gap-2">
                  {colors.map(c => (
                    <button
                      key={c}
                      type="button"
                      onClick={() => setColor(c)}
                      className={`w-6 h-6 rounded-full transition ${color === c ? 'ring-2 ring-white scale-110' : ''}`}
                      style={{ backgroundColor: c }}
                    />
                  ))}
                </div>
              </div>

              <button
                type="button"
                onClick={handleSave}
                className="w-full py-3.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-lg transition mt-2"
              >
                Save Note
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
