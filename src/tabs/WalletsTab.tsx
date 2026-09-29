import React, { useState } from 'react';
import { Plus, ArrowRightLeft, Landmark, QrCode, Banknote, CreditCard, PiggyBank, Check } from 'lucide-react';
import { Wallet, WalletType } from '../types';

interface WalletsTabProps {
  wallets: Wallet[];
  onOpenTransfer: () => void;
  onAddWallet: (wallet: Wallet) => void;
}

export const WalletsTab: React.FC<WalletsTabProps> = ({ wallets, onOpenTransfer, onAddWallet }) => {
  const [showAddModal, setShowAddModal] = useState(false);
  const [name, setName] = useState('');
  const [type, setType] = useState<WalletType>('BANK');
  const [balance, setBalance] = useState('');

  const totalBalance = wallets.reduce((acc, w) => acc + w.balance, 0);

  const getWalletIcon = (wType: WalletType) => {
    switch (wType) {
      case 'BANK': return <Landmark className="w-5 h-5 text-blue-400" />;
      case 'UPI': return <QrCode className="w-5 h-5 text-emerald-400" />;
      case 'CASH': return <Banknote className="w-5 h-5 text-amber-400" />;
      case 'CARD': return <CreditCard className="w-5 h-5 text-purple-400" />;
      case 'SAVINGS': return <PiggyBank className="w-5 h-5 text-cyan-400" />;
    }
  };

  const handleAddSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const bal = parseFloat(balance) || 0;
    if (!name.trim()) return;

    const colors: Record<WalletType, string> = {
      BANK: '#3B82F6',
      UPI: '#22C55E',
      CASH: '#F59E0B',
      CARD: '#8B5CF6',
      SAVINGS: '#06B6D4'
    };

    onAddWallet({
      id: `w_${Date.now()}`,
      name: name.trim(),
      type,
      balance: bal,
      color: colors[type],
      icon: type
    });

    setName('');
    setBalance('');
    setShowAddModal(false);
  };

  return (
    <div className="space-y-4 pb-24">
      {/* Net Worth Banner */}
      <div className="rounded-2xl p-5 bg-gradient-to-br from-[#0F172A] to-[#1E293B] border border-slate-700/60 shadow-lg relative overflow-hidden">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              Total Net Worth (Wallets)
            </span>
            <div className="text-3xl font-extrabold text-white mt-1">
              ₹{Math.round(totalBalance).toLocaleString()}
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              Synced across {wallets.length} active wallets & accounts
            </p>
          </div>

          <button
            onClick={onOpenTransfer}
            className="flex items-center gap-1.5 px-3.5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-md transition active:scale-95"
          >
            <ArrowRightLeft className="w-4 h-4" />
            <span>Transfer</span>
          </button>
        </div>
      </div>

      {/* Wallet Cards Section */}
      <div className="space-y-3">
        <div className="flex items-center justify-between px-1">
          <h4 className="text-sm font-bold text-white">My Accounts & Wallets</h4>
          <button
            onClick={() => setShowAddModal(true)}
            className="text-xs text-blue-400 font-semibold hover:text-blue-300 flex items-center gap-1"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Wallet</span>
          </button>
        </div>

        <div className="space-y-2.5">
          {wallets.map(wallet => (
            <div
              key={wallet.id}
              className="rounded-2xl p-4 bg-[#0F172A] border border-slate-800 hover:border-slate-700 transition flex items-center justify-between shadow-sm"
            >
              <div className="flex items-center gap-3.5">
                <div 
                  className="w-11 h-11 rounded-2xl flex items-center justify-center border"
                  style={{ 
                    backgroundColor: `${wallet.color}15`,
                    borderColor: `${wallet.color}35`
                  }}
                >
                  {getWalletIcon(wallet.type)}
                </div>

                <div>
                  <h5 className="text-sm font-bold text-white leading-tight">
                    {wallet.name}
                  </h5>
                  <span className="text-[10px] text-slate-400 font-medium tracking-wide uppercase">
                    {wallet.type}
                  </span>
                </div>
              </div>

              <div className="text-right">
                <div className="text-base font-extrabold text-white">
                  ₹{Math.round(wallet.balance).toLocaleString()}
                </div>
                <div className="text-[10px] text-emerald-400 font-semibold flex items-center justify-end gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                  Active
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Add Wallet Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="w-full max-w-sm bg-[#0F172A] border border-slate-700/80 rounded-3xl p-5 shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-bold text-white">Add New Account</h3>
              <button onClick={() => setShowAddModal(false)} className="p-1 rounded-full text-slate-400 hover:text-white">
                ✕
              </button>
            </div>

            <form onSubmit={handleAddSubmit} className="space-y-4">
              <div>
                <label className="block text-[11px] font-semibold text-slate-400 mb-1">Account Name</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. HDFC Bank, GPay, Cash"
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-400 mb-1">Account Type</label>
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value as WalletType)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="BANK">Bank Account</option>
                  <option value="UPI">UPI / Digital Wallet</option>
                  <option value="CASH">Cash in Hand</option>
                  <option value="CARD">Credit / Debit Card</option>
                  <option value="SAVINGS">Savings Pot</option>
                </select>
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-400 mb-1">Initial Balance (₹)</label>
                <input
                  type="number"
                  step="any"
                  required
                  value={balance}
                  onChange={(e) => setBalance(e.target.value)}
                  placeholder="e.g. 5000"
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:ring-2 focus:ring-blue-500 font-bold"
                />
              </div>

              <button
                type="submit"
                className="w-full py-3 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-lg transition flex items-center justify-center gap-1.5"
              >
                <Check className="w-4 h-4" />
                <span>Save Account</span>
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
