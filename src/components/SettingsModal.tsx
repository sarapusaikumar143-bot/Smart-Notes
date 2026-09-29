import React from 'react';
import { X, Shield, Lock, FileSpreadsheet, Database, RefreshCw, Cloud, LogIn, LogOut, CheckCircle } from 'lucide-react';
import { User } from 'firebase/auth';

interface SettingsModalProps {
  user: User | null;
  onSignInGoogle: () => void;
  onSignOut: () => void;
  isPinEnabled: boolean;
  onTogglePin: (enabled: boolean) => void;
  onOpenExport: () => void;
  onResetData: () => void;
  onClose: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  user,
  onSignInGoogle,
  onSignOut,
  isPinEnabled,
  onTogglePin,
  onOpenExport,
  onResetData,
  onClose
}) => {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-sm bg-[#0F172A] border border-slate-700/80 rounded-3xl p-6 shadow-2xl">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Shield className="w-4 h-4 text-blue-400" />
            Profile & Settings
          </h3>
          <button onClick={onClose} className="p-1 rounded-full text-slate-400 hover:text-white">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="space-y-3.5">
          {/* Firebase Authentication & Cloud Sync */}
          <div className="p-3.5 rounded-2xl bg-gradient-to-r from-blue-900/30 to-slate-800/60 border border-blue-500/30">
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-2">
                <Cloud className="w-4 h-4 text-blue-400" />
                <span className="text-xs font-bold text-white">Firebase & Firestore</span>
              </div>
              {user && (
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 flex items-center gap-1">
                  <CheckCircle className="w-2.5 h-2.5" />
                  Synced
                </span>
              )}
            </div>

            {user ? (
              <div className="flex items-center justify-between pt-1">
                <div className="flex items-center gap-2.5 overflow-hidden">
                  {user.photoURL ? (
                    <img src={user.photoURL} alt="User" className="w-7 h-7 rounded-full object-cover shrink-0" />
                  ) : (
                    <div className="w-7 h-7 rounded-full bg-blue-600 text-white text-xs flex items-center justify-center font-bold">
                      {user.displayName?.[0] || 'U'}
                    </div>
                  )}
                  <div className="truncate">
                    <p className="text-xs font-semibold text-white truncate">{user.displayName || 'Google User'}</p>
                    <p className="text-[10px] text-slate-400 truncate">{user.email}</p>
                  </div>
                </div>
                <button
                  onClick={onSignOut}
                  className="px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-[11px] font-bold text-rose-400 border border-slate-700 transition shrink-0 ml-2"
                >
                  Sign Out
                </button>
              </div>
            ) : (
              <div>
                <p className="text-[11px] text-slate-300 mb-2">
                  Sign in with Google to enable real-time cloud backup to Firebase Firestore.
                </p>
                <button
                  onClick={onSignInGoogle}
                  className="w-full py-2 px-3 rounded-xl bg-blue-600 hover:bg-blue-500 text-xs font-bold text-white transition flex items-center justify-center gap-2 shadow"
                >
                  <LogIn className="w-3.5 h-3.5" />
                  <span>Sign In with Google</span>
                </button>
              </div>
            )}
          </div>

          {/* Security App Lock */}
          <div className="flex items-center justify-between p-3 rounded-2xl bg-slate-800/60 border border-slate-700/50">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-blue-500/20 text-blue-400 flex items-center justify-center">
                <Lock className="w-4 h-4" />
              </div>
              <div>
                <div className="text-xs font-bold text-white">App Lock Protection</div>
                <div className="text-[10px] text-slate-400">4-digit PIN (Default: 1234)</div>
              </div>
            </div>

            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={isPinEnabled}
                onChange={(e) => onTogglePin(e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-9 h-5 bg-slate-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>

          {/* Export Reports */}
          <button
            onClick={() => {
              onClose();
              onOpenExport();
            }}
            className="w-full flex items-center justify-between p-3 rounded-2xl bg-slate-800/60 border border-slate-700/50 text-left hover:bg-slate-800 transition"
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
                <FileSpreadsheet className="w-4 h-4" />
              </div>
              <div>
                <div className="text-xs font-bold text-white">Export Financial Reports</div>
                <div className="text-[10px] text-slate-400">PDF, Excel (.xlsx), CSV</div>
              </div>
            </div>
            <span className="text-xs text-blue-400 font-bold">Export →</span>
          </button>

          {/* Reset Demo Data */}
          <button
            onClick={() => {
              if (confirm('Reset to initial realistic demo transactions & notes?')) {
                onResetData();
                onClose();
              }
            }}
            className="w-full flex items-center gap-3 p-3 rounded-2xl bg-slate-800/40 hover:bg-slate-800 text-left transition border border-slate-800"
          >
            <div className="w-8 h-8 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center">
              <RefreshCw className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold text-white">Reset Demo Data</div>
              <div className="text-[10px] text-slate-400">Restore default demo expenses and notes</div>
            </div>
          </button>

          {/* App Info */}
          <div className="p-2 text-center border-t border-slate-800 pt-3">
            <p className="text-xs font-extrabold text-white">AI Money: Smart Notes</p>
            <p className="text-[10px] text-slate-400 mt-0.5">“Track. Think. Grow your money with AI.”</p>
          </div>
        </div>
      </div>
    </div>
  );
};
