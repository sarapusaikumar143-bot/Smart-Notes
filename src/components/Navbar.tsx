import React, { useState } from 'react';
import { Mic, Settings, Radio, LogIn, LogOut, Cloud, CloudCheck, User as UserIcon } from 'lucide-react';
import { User } from 'firebase/auth';

interface NavbarProps {
  user: User | null;
  isCloudSyncing?: boolean;
  onOpenVoice: () => void;
  onOpenLiveVoice: () => void;
  onOpenSettings: () => void;
  onSignInGoogle: () => void;
  onSignOut: () => void;
  isPinLocked: boolean;
}

export const Navbar: React.FC<NavbarProps> = ({
  user,
  isCloudSyncing,
  onOpenVoice,
  onOpenLiveVoice,
  onOpenSettings,
  onSignInGoogle,
  onSignOut
}) => {
  const [showProfileMenu, setShowProfileMenu] = useState(false);

  return (
    <header className="sticky top-0 z-30 bg-[#0F172A]/95 backdrop-blur-md border-b border-slate-800/80 px-4 py-3">
      <div className="max-w-md mx-auto flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="font-extrabold text-base tracking-tight text-white flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-gradient-to-r from-blue-500 to-emerald-400"></span>
              AI Money
            </span>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-blue-500/20 text-blue-400 border border-blue-500/30">
              Smart Notes
            </span>
          </div>
          <div className="flex items-center gap-1.5 text-[10px] text-slate-400 font-medium">
            {user ? (
              <span className="flex items-center gap-1 text-emerald-400">
                <Cloud className="w-3 h-3 text-emerald-400" />
                <span>Cloud Synced (Firestore)</span>
              </span>
            ) : (
              <span className="text-slate-400">Track. Think. Grow with AI.</span>
            )}
          </div>
        </div>

        <div className="flex items-center gap-2 relative">
          {/* Live Voice API Button (gemini-3.8-live) */}
          <button
            onClick={onOpenLiveVoice}
            className="h-8 px-2.5 rounded-full bg-gradient-to-r from-purple-600/90 to-blue-600/90 hover:opacity-90 active:scale-95 transition flex items-center gap-1.5 text-white text-[11px] font-bold shadow-sm"
            title="Live Voice AI (gemini-3.8-live)"
          >
            <Radio className="w-3.5 h-3.5 text-amber-300 animate-pulse" />
            <span className="hidden sm:inline">Live</span>
          </button>

          {/* Audio Transcribe (gemini-3.5-transcribe) */}
          <button
            onClick={onOpenVoice}
            className="w-8 h-8 rounded-full bg-slate-800 hover:bg-slate-700 active:scale-95 transition flex items-center justify-center text-blue-400 border border-slate-700/60 shadow-sm"
            title="Voice Transcribe (gemini-3.5-transcribe)"
          >
            <Mic className="w-4 h-4" />
          </button>

          {/* User Profile / Google Sign-in */}
          {user ? (
            <div className="relative">
              <button
                onClick={() => setShowProfileMenu(!showProfileMenu)}
                className="w-8 h-8 rounded-full border border-blue-500/50 overflow-hidden active:scale-95 transition flex items-center justify-center bg-slate-800 text-white"
                title={user.displayName || user.email || 'User Account'}
              >
                {user.photoURL ? (
                  <img src={user.photoURL} alt="Profile" className="w-full h-full object-cover" />
                ) : (
                  <UserIcon className="w-4 h-4 text-blue-400" />
                )}
              </button>

              {/* Profile Dropdown */}
              {showProfileMenu && (
                <div className="absolute right-0 mt-2 w-52 bg-[#0F172A] border border-slate-700 rounded-2xl p-3 shadow-2xl z-50 animate-in fade-in zoom-in-95 text-xs">
                  <div className="pb-2 border-b border-slate-800">
                    <p className="font-bold text-white truncate">{user.displayName || 'Google User'}</p>
                    <p className="text-[10px] text-slate-400 truncate">{user.email}</p>
                    <div className="flex items-center gap-1 text-[10px] text-emerald-400 font-semibold mt-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
                      <span>Firestore Sync Active</span>
                    </div>
                  </div>

                  <div className="pt-2 space-y-1">
                    <button
                      onClick={() => {
                        setShowProfileMenu(false);
                        onOpenSettings();
                      }}
                      className="w-full text-left py-1.5 px-2 rounded-lg text-slate-300 hover:bg-slate-800 transition flex items-center gap-2"
                    >
                      <Settings className="w-3.5 h-3.5" />
                      <span>Settings & App Lock</span>
                    </button>
                    <button
                      onClick={() => {
                        setShowProfileMenu(false);
                        onSignOut();
                      }}
                      className="w-full text-left py-1.5 px-2 rounded-lg text-rose-400 hover:bg-rose-500/10 transition flex items-center gap-2"
                    >
                      <LogOut className="w-3.5 h-3.5" />
                      <span>Sign Out</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <button
              onClick={onSignInGoogle}
              className="h-8 px-2.5 rounded-full bg-slate-800 hover:bg-slate-700 border border-slate-700 active:scale-95 transition flex items-center gap-1.5 text-slate-200 text-[11px] font-bold shadow-sm"
              title="Sign in with Google (Firebase)"
            >
              <LogIn className="w-3.5 h-3.5 text-blue-400" />
              <span>Sign In</span>
            </button>
          )}

          {/* Settings Button */}
          <button
            onClick={onOpenSettings}
            className="w-8 h-8 rounded-full bg-slate-800 hover:bg-slate-700 active:scale-95 transition flex items-center justify-center text-slate-300 border border-slate-700/60 shadow-sm"
            title="Profile & Settings"
          >
            <Settings className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
