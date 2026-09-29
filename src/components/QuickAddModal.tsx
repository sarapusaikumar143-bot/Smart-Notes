import React, { useState, useEffect } from 'react';
import { X, Sparkles, Mic, Check } from 'lucide-react';
import { parseNaturalExpense } from '../utils/ai';
import { TransactionType, Wallet } from '../types';

interface QuickAddModalProps {
  initialDate: string;
  wallets: Wallet[];
  onClose: () => void;
  onSave: (tx: {
    title: string;
    amount: number;
    type: TransactionType;
    category: string;
    walletId: string;
    date: string;
    note?: string;
  }) => void;
  onOpenVoice: () => void;
}

export const QuickAddModal: React.FC<QuickAddModalProps> = ({
  initialDate,
  wallets,
  onClose,
  onSave,
  onOpenVoice
}) => {
  const [naturalInput, setNaturalInput] = useState('');
  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [type, setType] = useState<TransactionType>('EXPENSE');
  const [category, setCategory] = useState('Food');
  const [walletId, setWalletId] = useState(wallets[0]?.id || 'w1');
  const [date, setDate] = useState(initialDate);
  const [note, setNote] = useState('');
  const [aiBadge, setAiBadge] = useState<string | null>(null);

  const categories = ['Food', 'Transport', 'Bills', 'Shopping', 'Entertainment', 'Health', 'Salary', 'Investment', 'Other'];

  // Smart Live AI Categorization as user types
  useEffect(() => {
    if (naturalInput.trim().length > 1) {
      const parsed = parseNaturalExpense(naturalInput);
      if (parsed.amount > 0 || parsed.title) {
        setTitle(parsed.title);
        if (parsed.amount > 0) setAmount(parsed.amount.toString());
        setType(parsed.type);
        setCategory(parsed.category);
        setAiBadge(`🤖 AI Detected: ${parsed.category} • ${parsed.type} • ₹${parsed.amount}`);
      }
    }
  }, [naturalInput]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);
    if (!parsedAmount || parsedAmount <= 0) return;

    onSave({
      title: title.trim() || category,
      amount: parsedAmount,
      type,
      category,
      walletId,
      date,
      note: note.trim()
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-md bg-[#0F172A] border border-slate-700/80 rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-blue-500/20 text-blue-400 flex items-center justify-center">
              <Sparkles className="w-4 h-4" />
            </div>
            <h3 className="text-base font-bold text-white">Smart Add Entry</h3>
          </div>
          <button onClick={onClose} className="p-1 rounded-full text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* AI Natural Input Bar */}
        <div className="relative mb-3">
          <input
            type="text"
            value={naturalInput}
            onChange={(e) => setNaturalInput(e.target.value)}
            placeholder="Type 'Biryani 250' or 'Petrol 500'..."
            className="w-full bg-slate-800/90 border border-slate-700 rounded-xl px-3.5 py-3 text-sm text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
            autoFocus
          />
          <button
            type="button"
            onClick={onOpenVoice}
            className="absolute right-2.5 top-1/2 -translate-y-1/2 w-7 h-7 rounded-lg bg-blue-500/20 text-blue-400 flex items-center justify-center hover:bg-blue-500/30 transition"
            title="Voice Input"
          >
            <Mic className="w-4 h-4" />
          </button>
        </div>

        {aiBadge && (
          <div className="mb-4 text-xs font-semibold px-3 py-1.5 rounded-lg bg-blue-500/15 text-blue-300 border border-blue-500/30">
            {aiBadge}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Income / Expense Toggle */}
          <div className="grid grid-cols-2 gap-2 bg-slate-800/80 p-1 rounded-xl border border-slate-700/60">
            <button
              type="button"
              onClick={() => setType('EXPENSE')}
              className={`py-2 text-xs font-bold rounded-lg transition ${
                type === 'EXPENSE'
                  ? 'bg-rose-500 text-white shadow'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Expense
            </button>
            <button
              type="button"
              onClick={() => setType('INCOME')}
              className={`py-2 text-xs font-bold rounded-lg transition ${
                type === 'INCOME'
                  ? 'bg-emerald-500 text-white shadow'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Income
            </button>
          </div>

          {/* Amount & Title */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-slate-400 mb-1">Amount (₹)</label>
              <input
                type="number"
                step="any"
                required
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="250"
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500 font-bold"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-400 mb-1">Title / Item</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Biryani"
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Wallet Selector */}
          <div>
            <label className="block text-[11px] font-semibold text-slate-400 mb-1">Account / Wallet</label>
            <div className="flex flex-wrap gap-2">
              {wallets.map(w => (
                <button
                  key={w.id}
                  type="button"
                  onClick={() => setWalletId(w.id)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition border ${
                    walletId === w.id
                      ? 'bg-blue-600 text-white border-blue-500'
                      : 'bg-slate-800/80 text-slate-300 border-slate-700 hover:bg-slate-700'
                  }`}
                >
                  {w.name} (₹{Math.round(w.balance)})
                </button>
              ))}
            </div>
          </div>

          {/* Category Chips */}
          <div>
            <label className="block text-[11px] font-semibold text-slate-400 mb-1">Category</label>
            <div className="flex flex-wrap gap-1.5">
              {categories.map(cat => (
                <button
                  key={cat}
                  type="button"
                  onClick={() => setCategory(cat)}
                  className={`px-2.5 py-1 rounded-lg text-[11px] font-medium transition ${
                    category === cat
                      ? type === 'INCOME'
                        ? 'bg-emerald-600 text-white font-bold'
                        : 'bg-blue-600 text-white font-bold'
                      : 'bg-slate-800/60 text-slate-300 hover:bg-slate-700'
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>

          {/* Date & Note */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-semibold text-slate-400 mb-1">Date</label>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-[11px] font-semibold text-slate-400 mb-1">Note (Optional)</label>
              <input
                type="text"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="Dinner with team"
                className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <button
            type="submit"
            className={`w-full py-3.5 rounded-xl font-bold text-sm text-white shadow-lg transition active:scale-[0.99] flex items-center justify-center gap-2 ${
              type === 'INCOME' ? 'bg-emerald-600 hover:bg-emerald-500' : 'bg-blue-600 hover:bg-blue-500'
            }`}
          >
            <Check className="w-4 h-4" />
            <span>Save {type === 'INCOME' ? 'Income' : 'Expense'}</span>
          </button>
        </form>
      </div>
    </div>
  );
};
