import React, { useState } from 'react';
import { X, ArrowRightLeft, Check } from 'lucide-react';
import { Wallet } from '../types';

interface TransferModalProps {
  wallets: Wallet[];
  onClose: () => void;
  onTransfer: (fromId: string, toId: string, amount: number, note?: string) => void;
}

export const TransferModal: React.FC<TransferModalProps> = ({ wallets, onClose, onTransfer }) => {
  const [fromId, setFromId] = useState(wallets[0]?.id || '');
  const [toId, setToId] = useState(wallets[1]?.id || wallets[0]?.id || '');
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');

  const fromWallet = wallets.find(w => w.id === fromId);
  const toWallet = wallets.find(w => w.id === toId);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseFloat(amount);
    if (!val || val <= 0 || fromId === toId) return;

    onTransfer(fromId, toId, val, note);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-sm bg-[#0F172A] border border-slate-700/80 rounded-3xl p-5 shadow-2xl">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <ArrowRightLeft className="w-4 h-4 text-blue-400" />
            Transfer Funds
          </h3>
          <button onClick={onClose} className="p-1 rounded-full text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-slate-400 mb-1">From Wallet</label>
            <select
              value={fromId}
              onChange={(e) => setFromId(e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {wallets.map(w => (
                <option key={w.id} value={w.id}>
                  {w.name} (Balance: ₹{Math.round(w.balance)})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-slate-400 mb-1">To Wallet</label>
            <select
              value={toId}
              onChange={(e) => setToId(e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {wallets.filter(w => w.id !== fromId).map(w => (
                <option key={w.id} value={w.id}>
                  {w.name} (Balance: ₹{Math.round(w.balance)})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-slate-400 mb-1">Transfer Amount (₹)</label>
            <input
              type="number"
              step="any"
              required
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="e.g. 2000"
              className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2.5 text-sm text-white focus:outline-none focus:ring-2 focus:ring-blue-500 font-bold"
            />
          </div>

          <div>
            <label className="block text-[11px] font-semibold text-slate-400 mb-1">Note (Optional)</label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="e.g. ATM withdrawal / UPI top-up"
              className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={fromId === toId || !amount || parseFloat(amount) <= 0}
            className="w-full py-3.5 rounded-xl font-bold text-sm text-white bg-blue-600 hover:bg-blue-500 disabled:opacity-50 shadow-lg transition flex items-center justify-center gap-2"
          >
            <Check className="w-4 h-4" />
            <span>Complete Transfer</span>
          </button>
        </form>
      </div>
    </div>
  );
};
