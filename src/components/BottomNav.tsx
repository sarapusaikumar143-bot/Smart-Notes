import React from 'react';
import { Calendar, BarChart3, Bot, CheckSquare, Wallet } from 'lucide-react';
import { TabType } from '../types';

interface BottomNavProps {
  currentTab: TabType;
  onChangeTab: (tab: TabType) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ currentTab, onChangeTab }) => {
  const tabs = [
    { id: 'calendar' as TabType, label: 'Calendar', icon: Calendar },
    { id: 'analytics' as TabType, label: 'Analytics', icon: BarChart3 },
    { id: 'ai' as TabType, label: 'AI Copilot', icon: Bot, isSpecial: true },
    { id: 'notes' as TabType, label: 'Notes', icon: CheckSquare },
    { id: 'wallets' as TabType, label: 'Wallets', icon: Wallet },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#0F172A]/95 backdrop-blur-lg border-t border-slate-800/80 px-2 py-2 safe-area-bottom">
      <div className="max-w-md mx-auto flex items-center justify-around">
        {tabs.map(tab => {
          const Icon = tab.icon;
          const isActive = currentTab === tab.id;

          return (
            <button
              key={tab.id}
              onClick={() => onChangeTab(tab.id)}
              className={`flex flex-col items-center justify-center py-1 px-3 rounded-xl transition-all relative ${
                isActive 
                  ? 'text-white' 
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {isActive && (
                <div className="absolute inset-0 bg-gradient-to-r from-blue-500/20 to-emerald-500/20 rounded-xl -z-10 border border-blue-500/30" />
              )}
              
              <div className="relative">
                <Icon className={`w-5 h-5 transition-transform ${isActive ? 'scale-110 text-blue-400' : ''}`} />
                {tab.isSpecial && (
                  <span className="absolute -top-1 -right-1 w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                )}
              </div>
              <span className={`text-[11px] font-semibold mt-1 ${isActive ? 'text-white' : 'text-slate-400'}`}>
                {tab.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
