import React, { useRef } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Transaction } from '../types';

interface CalendarMonthProps {
  currentDate: Date;
  selectedDateStr: string;
  transactions: Transaction[];
  onSelectDate: (dateStr: string) => void;
  onChangeMonth: (delta: number) => void;
  onQuickAddForDate: (dateStr: string) => void;
}

export const CalendarMonth: React.FC<CalendarMonthProps> = ({
  currentDate,
  selectedDateStr,
  transactions,
  onSelectDate,
  onChangeMonth,
  onQuickAddForDate
}) => {
  const monthNames = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  ];
  const year = currentDate.getFullYear();
  const month = currentDate.getMonth();

  const firstDayIndex = new Date(year, month, 1).getDay(); // 0 = Sun
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const todayStr = new Date().toISOString().split('T')[0];

  const touchStartX = useRef<number | null>(null);

  const handleTouchStart = (e: React.TouchEvent) => {
    touchStartX.current = e.touches[0].clientX;
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    if (touchStartX.current === null) return;
    const diff = e.changedTouches[0].clientX - touchStartX.current;
    if (diff > 50) {
      onChangeMonth(-1);
    } else if (diff < -50) {
      onChangeMonth(1);
    }
    touchStartX.current = null;
  };

  // Group day-by-day aggregates: income and expense sums
  const dayAggregates: Record<string, { income: number; expense: number }> = {};
  transactions.forEach(t => {
    if (!dayAggregates[t.date]) {
      dayAggregates[t.date] = { income: 0, expense: 0 };
    }
    if (t.type === 'INCOME') dayAggregates[t.date].income += t.amount;
    if (t.type === 'EXPENSE') dayAggregates[t.date].expense += t.amount;
  });

  const daysArray = [];
  for (let i = 0; i < firstDayIndex; i++) {
    daysArray.push(null);
  }
  for (let d = 1; d <= daysInMonth; d++) {
    daysArray.push(d);
  }

  return (
    <div 
      className="rounded-2xl p-4 bg-[#0F172A] border border-slate-800 shadow-md select-none"
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      {/* Month Navigation */}
      <div className="flex items-center justify-between mb-3 px-1">
        <button
          onClick={() => onChangeMonth(-1)}
          className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 active:scale-95 transition"
          aria-label="Previous month"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>

        <div className="text-center">
          <h3 className="text-sm font-bold text-white tracking-wide">
            {monthNames[month]} {year}
          </h3>
          <p className="text-[10px] text-slate-400">
            Swipe left/right • Click day to select
          </p>
        </div>

        <button
          onClick={() => onChangeMonth(1)}
          className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-300 active:scale-95 transition"
          aria-label="Next month"
        >
          <ChevronRight className="w-5 h-5" />
        </button>
      </div>

      {/* Week Header */}
      <div className="grid grid-cols-7 gap-1 text-center mb-1">
        {['S', 'M', 'T', 'W', 'T', 'F', 'S'].map((d, i) => (
          <div key={i} className="text-[11px] font-semibold text-slate-400 py-1">
            {d}
          </div>
        ))}
      </div>

      {/* Days Grid with DayCell (+income in Green, -expense in Red) */}
      <div className="grid grid-cols-7 gap-1 text-center">
        {daysArray.map((day, idx) => {
          if (day === null) {
            return <div key={`empty_${idx}`} className="h-12" />;
          }

          const dayStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
          const isSelected = selectedDateStr === dayStr;
          const isToday = todayStr === dayStr;
          const agg = dayAggregates[dayStr];
          const income = agg?.income || 0;
          const expense = agg?.expense || 0;

          return (
            <div
              key={`day_${day}`}
              onClick={() => onSelectDate(dayStr)}
              onDoubleClick={() => onQuickAddForDate(dayStr)}
              className={`h-12 rounded-xl flex flex-col items-center justify-center cursor-pointer transition p-1 ${
                isSelected
                  ? 'bg-blue-600 text-white font-bold shadow-md shadow-blue-500/20'
                  : isToday
                  ? 'bg-blue-500/15 text-blue-400 font-semibold border border-blue-500/30'
                  : 'text-slate-200 hover:bg-slate-800/80 bg-slate-900/40'
              }`}
            >
              <span className="text-xs leading-none font-semibold">
                {day}
              </span>

              {income > 0 ? (
                <span className={`text-[8px] font-extrabold leading-tight mt-0.5 ${isSelected ? 'text-white' : 'text-emerald-400'}`}>
                  +{Math.round(income)}
                </span>
              ) : null}

              {expense > 0 ? (
                <span className={`text-[8px] font-extrabold leading-tight ${isSelected ? 'text-white' : 'text-rose-400'}`}>
                  -{Math.round(expense)}
                </span>
              ) : null}

              {income === 0 && expense === 0 && (
                <span className="text-[8px] opacity-0 leading-tight">•</span>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
