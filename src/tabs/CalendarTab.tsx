import React, { useState } from 'react';
import { Plus, Trash2, Calendar as CalendarIcon, Utensils, Car, Receipt, ShoppingBag, Film, HeartPulse, DollarSign, TrendingUp, HelpCircle } from 'lucide-react';
import { DailyInsight, Transaction, TransactionType, Wallet } from '../types';
import { BalanceHeader } from '../components/BalanceHeader';
import { DailyInsightCard } from '../components/DailyInsightCard';
import { CalendarMonth } from '../components/CalendarMonth';

interface CalendarTabProps {
  currentDate: Date;
  selectedDateStr: string;
  transactions: Transaction[];
  wallets: Wallet[];
  dailyInsight: DailyInsight;
  onSelectDate: (dateStr: string) => void;
  onChangeMonth: (delta: number) => void;
  onOpenQuickAdd: () => void;
  onDeleteTransaction: (id: string) => void;
}

export const CalendarTab: React.FC<CalendarTabProps> = ({
  currentDate,
  selectedDateStr,
  transactions,
  wallets,
  dailyInsight,
  onSelectDate,
  onChangeMonth,
  onOpenQuickAdd,
  onDeleteTransaction
}) => {
  const [filterType, setFilterType] = useState<TransactionType | 'ALL'>('ALL');
  const [selectedWalletId, setSelectedWalletId] = useState<string | 'ALL'>('ALL');

  const walletMap = new Map(wallets.map(w => [w.id, w.name]));

  // Month transactions
  const month = currentDate.getMonth();
  const year = currentDate.getFullYear();
  const monthTransactions = transactions.filter(t => {
    const d = new Date(t.date);
    return d.getMonth() === month && d.getFullYear() === year;
  });

  const totalIncome = monthTransactions.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0);
  const totalExpense = monthTransactions.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0);

  // Selected Day transactions
  const dayTransactions = transactions.filter(t => {
    const matchDate = t.date === selectedDateStr;
    const matchType = filterType === 'ALL' || t.type === filterType;
    const matchWallet = selectedWalletId === 'ALL' || t.walletId === selectedWalletId || t.toWalletId === selectedWalletId;
    return matchDate && matchType && matchWallet;
  });

  const formattedDate = new Date(selectedDateStr).toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric'
  });

  const getCategoryIcon = (category: string) => {
    switch (category.toLowerCase()) {
      case 'food': return <Utensils className="w-4 h-4 text-amber-400" />;
      case 'transport': return <Car className="w-4 h-4 text-blue-400" />;
      case 'bills': return <Receipt className="w-4 h-4 text-purple-400" />;
      case 'shopping': return <ShoppingBag className="w-4 h-4 text-pink-400" />;
      case 'entertainment': return <Film className="w-4 h-4 text-cyan-400" />;
      case 'health': return <HeartPulse className="w-4 h-4 text-rose-400" />;
      case 'salary': return <DollarSign className="w-4 h-4 text-emerald-400" />;
      case 'investment': return <TrendingUp className="w-4 h-4 text-emerald-400" />;
      default: return <HelpCircle className="w-4 h-4 text-slate-400" />;
    }
  };

  return (
    <div className="space-y-4 pb-24">
      {/* 1. Balance Header Card */}
      <BalanceHeader
        totalIncome={totalIncome}
        totalExpense={totalExpense}
        periodLabel={currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
      />

      {/* 2. Daily Insight AI */}
      <DailyInsightCard insight={dailyInsight} />

      {/* 3. Calendar Month Grid */}
      <CalendarMonth
        currentDate={currentDate}
        selectedDateStr={selectedDateStr}
        transactions={transactions}
        onSelectDate={onSelectDate}
        onChangeMonth={onChangeMonth}
        onQuickAddForDate={() => onOpenQuickAdd()}
      />

      {/* 4. Transactions List for Selected Date */}
      <div className="space-y-3">
        <div className="flex items-center justify-between px-1">
          <div>
            <h4 className="text-sm font-bold text-white flex items-center gap-1.5">
              <span>{formattedDate}</span>
              <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 font-medium">
                {dayTransactions.length}
              </span>
            </h4>
          </div>

          {/* Filter Pills */}
          <div className="flex items-center gap-1 bg-slate-900/80 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setFilterType('ALL')}
              className={`text-[10px] font-bold px-2 py-1 rounded-lg transition ${
                filterType === 'ALL' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setFilterType('EXPENSE')}
              className={`text-[10px] font-bold px-2 py-1 rounded-lg transition ${
                filterType === 'EXPENSE' ? 'bg-rose-500 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              Expenses
            </button>
            <button
              onClick={() => setFilterType('INCOME')}
              className={`text-[10px] font-bold px-2 py-1 rounded-lg transition ${
                filterType === 'INCOME' ? 'bg-emerald-500 text-white' : 'text-slate-400 hover:text-white'
              }`}
            >
              Income
            </button>
          </div>
        </div>

        {dayTransactions.length === 0 ? (
          <div className="rounded-2xl p-6 bg-slate-900/50 border border-slate-800/80 text-center">
            <CalendarIcon className="w-8 h-8 text-slate-600 mx-auto mb-2" />
            <p className="text-xs font-semibold text-slate-300">No transactions recorded for this day</p>
            <p className="text-[11px] text-slate-500 mt-1">
              Tap the floating + button to add an entry with AI auto-detect.
            </p>
          </div>
        ) : (
          <div className="space-y-2">
            {dayTransactions.map(tx => {
              const isIncome = tx.type === 'INCOME';
              const isTransfer = tx.type === 'TRANSFER';
              const walletName = walletMap.get(tx.walletId) || 'Wallet';

              return (
                <div
                  key={tx.id}
                  className="rounded-2xl p-3.5 bg-[#0F172A] border border-slate-800 hover:border-slate-700/80 transition flex items-center justify-between group shadow-sm"
                >
                  <div className="flex items-center gap-3">
                    <div className={`w-9 h-9 rounded-xl flex items-center justify-center border ${
                      isIncome 
                        ? 'bg-emerald-500/15 border-emerald-500/30' 
                        : isTransfer 
                        ? 'bg-blue-500/15 border-blue-500/30'
                        : 'bg-slate-800 border-slate-700'
                    }`}>
                      {getCategoryIcon(tx.category)}
                    </div>

                    <div>
                      <div className="text-sm font-bold text-white leading-tight">
                        {tx.title}
                      </div>
                      <div className="text-[11px] text-slate-400 mt-0.5 flex items-center gap-1.5">
                        <span>{tx.category}</span>
                        <span>•</span>
                        <span className="text-blue-400 font-medium">{walletName}</span>
                        {tx.note && (
                          <>
                            <span>•</span>
                            <span className="truncate max-w-[120px]">{tx.note}</span>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <div className={`text-sm font-extrabold ${
                      isIncome ? 'text-emerald-400' : isTransfer ? 'text-blue-400' : 'text-rose-400'
                    }`}>
                      {isIncome ? '+' : isTransfer ? '⇄' : '-'}₹{Math.round(tx.amount).toLocaleString()}
                    </div>

                    <button
                      onClick={() => onDeleteTransaction(tx.id)}
                      className="opacity-60 hover:opacity-100 p-1.5 text-slate-400 hover:text-rose-400 transition"
                      title="Delete"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Floating Action Button */}
      <button
        onClick={onOpenQuickAdd}
        className="fixed bottom-20 right-4 sm:right-auto sm:left-1/2 sm:translate-x-36 z-30 w-14 h-14 rounded-full bg-gradient-to-r from-blue-600 to-emerald-500 text-white shadow-xl shadow-blue-500/25 flex items-center justify-center hover:scale-105 active:scale-95 transition-all"
        title="Add Expense or Income"
      >
        <Plus className="w-6 h-6 stroke-[2.5]" />
      </button>
    </div>
  );
};
