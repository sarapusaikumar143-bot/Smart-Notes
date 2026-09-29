import React from 'react';
import { Sparkles, TrendingDown, TrendingUp, AlertTriangle } from 'lucide-react';
import { DailyInsight } from '../types';

interface DailyInsightCardProps {
  insight: DailyInsight;
}

export const DailyInsightCard: React.FC<DailyInsightCardProps> = ({ insight }) => {
  return (
    <div className="rounded-2xl p-4 bg-gradient-to-br from-[#0F172A] to-[#1E293B] border border-slate-700/60 shadow-md relative overflow-hidden">
      <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500/10 rounded-full blur-2xl pointer-events-none" />

      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-full bg-blue-500/20 text-blue-400 flex items-center justify-center">
            <Sparkles className="w-3.5 h-3.5" />
          </div>
          <h4 className="text-sm font-bold text-white tracking-wide">
            {insight.title}
          </h4>
        </div>

        <div className={`flex items-center gap-1 text-[11px] font-bold px-2 py-0.5 rounded-full border ${
          insight.isPositive 
            ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' 
            : 'bg-rose-500/15 text-rose-400 border-rose-500/30'
        }`}>
          {insight.isPositive ? (
            <TrendingDown className="w-3 h-3" />
          ) : (
            <TrendingUp className="w-3 h-3" />
          )}
          <span>{insight.badge}</span>
        </div>
      </div>

      <p className="text-xs text-slate-300 leading-relaxed font-normal">
        {insight.description}
      </p>
    </div>
  );
};
