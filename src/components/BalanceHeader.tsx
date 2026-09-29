import React from 'react';
import { ArrowDownLeft, ArrowUpRight, Wallet as WalletIcon } from 'lucide-react';

interface BalanceHeaderProps {
  totalIncome: number;
  totalExpense: number;
  periodLabel: string;
}

export const BalanceHeader: React.FC<BalanceHeaderProps> = ({ totalIncome, totalExpense, periodLabel }) => {
  const net = totalIncome - totalExpense;
  const total = totalIncome + totalExpense;
  const incomeRatio = total > 0 ? (totalIncome / total) * 100 : 50;

  return (
    <div className="rounded-2xl p-5 bg-gradient-to-b from-[#0F172A] to-[#1E293B] border border-slate-700/60 shadow-lg relative overflow-hidden">
      <div className="flex items-center justify-between mb-3">
        <div>
          <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
            Net Balance ({periodLabel})
          </span>
          <div className="text-3xl font-extrabold text-white mt-1 tracking-tight">
            ₹{Math.round(net).toLocaleString()}
          </div>
        </div>

        <div className="w-11 h-11 rounded-2xl bg-blue-500/15 border border-blue-500/30 flex items-center justify-center text-blue-400 shadow-inner">
          <WalletIcon className="w-5 h-5" />
        </div>
      </div>

      {/* Progress Balance Bar */}
      <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden flex my-3.5 border border-slate-700/50">
        <div 
          className="h-full bg-emerald-500 transition-all duration-500" 
          style={{ width: `${Math.min(95, Math.max(5, incomeRatio))}%` }}
          title={`Income: ${Math.round(incomeRatio)}%`}
        />
        <div 
          className="h-full bg-rose-500 flex-1 transition-all duration-500" 
          title={`Expense: ${Math.round(100 - incomeRatio)}%`}
        />
      </div>

      {/* Income and Expense stats */}
      <div className="grid grid-cols-2 gap-3 pt-1">
        <div className="flex items-center gap-2.5 p-2 rounded-xl bg-slate-800/60 border border-slate-700/40">
          <div className="w-7 h-7 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
            <ArrowDownLeft className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] text-slate-400 font-medium">Income</div>
            <div className="text-xs font-bold text-emerald-400">
              +₹{Math.round(totalIncome).toLocaleString()}
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2.5 p-2 rounded-xl bg-slate-800/60 border border-slate-700/40">
          <div className="w-7 h-7 rounded-lg bg-rose-500/20 text-rose-400 flex items-center justify-center">
            <ArrowUpRight className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] text-slate-400 font-medium">Expenses</div>
            <div className="text-xs font-bold text-rose-400">
              -₹{Math.round(totalExpense).toLocaleString()}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
