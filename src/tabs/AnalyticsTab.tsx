import React, { useState } from 'react';
import { BarChart3, TrendingUp, TrendingDown, AlertTriangle, ShieldCheck, Flame } from 'lucide-react';
import { FinancialPrediction, Transaction } from '../types';

interface AnalyticsTabProps {
  transactions: Transaction[];
  prediction: FinancialPrediction;
}

export const AnalyticsTab: React.FC<AnalyticsTabProps> = ({ transactions, prediction }) => {
  const [timeframe, setTimeframe] = useState<'weekly' | 'monthly'>('monthly');

  const now = Date.now();
  const cutoff = timeframe === 'weekly' ? now - (7 * 24 * 60 * 60 * 1000) : now - (30 * 24 * 60 * 60 * 1000);
  
  const filteredTxs = transactions.filter(t => t.timestamp >= cutoff);
  const totalIncome = filteredTxs.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0);
  const totalExpense = filteredTxs.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0);

  // Category Breakdown
  const catMap: Record<string, number> = {};
  filteredTxs.filter(t => t.type === 'EXPENSE').forEach(t => {
    catMap[t.category] = (catMap[t.category] || 0) + t.amount;
  });
  const categoryList = Object.entries(catMap).sort((a, b) => b[1] - a[1]);

  const maxExpense = Math.max(totalIncome, totalExpense, 1);
  const incomeBarHeight = Math.max(12, (totalIncome / maxExpense) * 130);
  const expenseBarHeight = Math.max(12, (totalExpense / maxExpense) * 130);

  const categoryColors = [
    '#3B82F6', '#F59E0B', '#10B981', '#8B5CF6', '#EC4899', '#06B6D4', '#64748B'
  ];

  return (
    <div className="space-y-4 pb-24">
      {/* 1. Weekly vs Monthly Toggle */}
      <div className="grid grid-cols-2 gap-2 bg-[#0F172A] p-1.5 rounded-2xl border border-slate-800">
        <button
          onClick={() => setTimeframe('weekly')}
          className={`py-2 text-xs font-bold rounded-xl transition ${
            timeframe === 'weekly' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-400 hover:text-white'
          }`}
        >
          Last 7 Days (Weekly)
        </button>
        <button
          onClick={() => setTimeframe('monthly')}
          className={`py-2 text-xs font-bold rounded-xl transition ${
            timeframe === 'monthly' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-400 hover:text-white'
          }`}
        >
          Last 30 Days (Monthly)
        </button>
      </div>

      {/* 2. AI Prediction Engine Banner */}
      <div className="rounded-2xl p-4 bg-gradient-to-br from-[#0F172A] via-[#1E293B] to-[#0F172A] border border-blue-500/30 shadow-lg relative overflow-hidden">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-blue-500/20 text-blue-400 flex items-center justify-center">
              <Flame className="w-4 h-4 text-amber-400" />
            </div>
            <h4 className="text-sm font-bold text-white">AI Prediction Engine</h4>
          </div>

          <div className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full border ${
            (prediction.runwayDays || 30) > 15 
              ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' 
              : 'bg-rose-500/15 text-rose-400 border-rose-500/30'
          }`}>
            Runway: ~{prediction.runwayDays || 22} Days
          </div>
        </div>

        <p className="text-xs text-slate-200 leading-relaxed font-medium">
          {prediction.summaryText}
        </p>

        {prediction.overspendingAlert && (
          <div className="mt-2.5 p-2 rounded-xl bg-rose-500/15 border border-rose-500/30 flex items-center gap-2 text-rose-300 text-xs">
            <AlertTriangle className="w-4 h-4 shrink-0 text-rose-400" />
            <span>{prediction.overspendingAlert}</span>
          </div>
        )}
      </div>

      {/* 3. Cashflow Graph: Income vs Expenses */}
      <div className="rounded-2xl p-5 bg-[#0F172A] border border-slate-800 shadow-md">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h4 className="text-sm font-bold text-white">Cashflow Overview</h4>
            <p className="text-[11px] text-slate-400">Total inflow vs total expense outflow</p>
          </div>
          <div className="text-xs font-bold text-blue-400">
            {timeframe === 'weekly' ? '7 Days' : '30 Days'}
          </div>
        </div>

        {/* Animated Bar Visual */}
        <div className="h-44 flex items-end justify-around pt-4 pb-2 border-b border-slate-800/80">
          {/* Income Column */}
          <div className="flex flex-col items-center gap-2 w-28">
            <span className="text-xs font-bold text-emerald-400">
              ₹{Math.round(totalIncome).toLocaleString()}
            </span>
            <div 
              className="w-14 rounded-t-xl bg-gradient-to-t from-emerald-600 to-emerald-400 shadow-lg shadow-emerald-500/20 transition-all duration-700"
              style={{ height: `${incomeBarHeight}px` }}
            />
            <span className="text-xs font-semibold text-slate-300">Income</span>
          </div>

          {/* Expense Column */}
          <div className="flex flex-col items-center gap-2 w-28">
            <span className="text-xs font-bold text-rose-400">
              ₹{Math.round(totalExpense).toLocaleString()}
            </span>
            <div 
              className="w-14 rounded-t-xl bg-gradient-to-t from-rose-600 to-rose-400 shadow-lg shadow-rose-500/20 transition-all duration-700"
              style={{ height: `${expenseBarHeight}px` }}
            />
            <span className="text-xs font-semibold text-slate-300">Expenses</span>
          </div>
        </div>

        <div className="flex items-center justify-between pt-3 text-[11px] text-slate-400">
          <span>Burn Rate: <strong className="text-white">₹{Math.round(prediction.dailyBurnRate)}/day</strong></span>
          <span>Savings Rate: <strong className="text-emerald-400">{prediction.savingsRatePercent}%</strong></span>
        </div>
      </div>

      {/* 4. Category Spend Heatmap */}
      <div className="rounded-2xl p-5 bg-[#0F172A] border border-slate-800 shadow-md">
        <h4 className="text-sm font-bold text-white mb-1">Category Spend Heatmap</h4>
        <p className="text-[11px] text-slate-400 mb-4">Detailed intensity where your capital is directed</p>

        {categoryList.length === 0 ? (
          <p className="text-xs text-slate-500 py-4 text-center">No expense entries in this period</p>
        ) : (
          <div className="space-y-3.5">
            {categoryList.map(([cat, amt], idx) => {
              const color = categoryColors[idx % categoryColors.length];
              const pct = totalExpense > 0 ? Math.round((amt / totalExpense) * 100) : 0;

              return (
                <div key={cat} className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-slate-200 flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: color }} />
                      {cat}
                    </span>
                    <span className="font-bold text-white">
                      ₹{Math.round(amt).toLocaleString()} <span className="text-slate-400 font-normal">({pct}%)</span>
                    </span>
                  </div>

                  <div className="w-full h-2 rounded-full bg-slate-800 overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-700"
                      style={{
                        width: `${Math.max(4, pct)}%`,
                        backgroundColor: color
                      }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
