import React, { useState, useEffect, useRef } from 'react';
import { User } from 'firebase/auth';
import { Navbar } from './components/Navbar';
import { BottomNav } from './components/BottomNav';
import { CalendarTab } from './tabs/CalendarTab';
import { AnalyticsTab } from './tabs/AnalyticsTab';
import { AiTab } from './tabs/AiTab';
import { NotesTab } from './tabs/NotesTab';
import { WalletsTab } from './tabs/WalletsTab';
import { QuickAddModal } from './components/QuickAddModal';
import { VoiceModal } from './components/VoiceModal';
import { LiveVoiceModal } from './components/LiveVoiceModal';
import { TransferModal } from './components/TransferModal';
import { ExportModal } from './components/ExportModal';
import { SettingsModal } from './components/SettingsModal';
import { AppLockScreen } from './components/AppLockScreen';

import {
  ChatMessage,
  Note,
  TabType,
  Transaction,
  TransactionType,
  Wallet
} from './types';

import {
  getInitialNotes,
  getInitialTransactions,
  getInitialWallets,
  loadData,
  saveData
} from './utils/storage';

import { calculateDailyInsight, calculatePrediction } from './utils/ai';
import {
  subscribeToAuthChanges,
  signInWithGoogle,
  logoutUser
} from './utils/firebase';
import {
  syncUserFinanceData,
  subscribeToUserFinanceData
} from './utils/firestoreService';

export function App() {
  const [currentTab, setCurrentTab] = useState<TabType>('calendar');

  // Firebase User Auth State
  const [user, setUser] = useState<User | null>(null);
  const [isCloudSyncing, setIsCloudSyncing] = useState<boolean>(false);

  // Financial Persistence State
  const [transactions, setTransactions] = useState<Transaction[]>(() =>
    loadData('aimoney_transactions', getInitialTransactions())
  );
  const [wallets, setWallets] = useState<Wallet[]>(() =>
    loadData('aimoney_wallets', getInitialWallets())
  );
  const [notes, setNotes] = useState<Note[]>(() =>
    loadData('aimoney_notes', getInitialNotes())
  );

  // Security Lock
  const [pinEnabled, setPinEnabled] = useState<boolean>(() =>
    loadData('aimoney_pin_enabled', false)
  );
  const [pin, setPin] = useState<string>(() =>
    loadData('aimoney_pin', '1234')
  );
  const [isLocked, setIsLocked] = useState<boolean>(pinEnabled);

  // Calendar State
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [selectedDateStr, setSelectedDateStr] = useState<string>(
    new Date().toISOString().split('T')[0]
  );

  // Chat State
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([
    {
      id: 'm1',
      text: "👋 Hello! I'm SpendWise AI, your personal financial advisor and smart notes copilot. Ask me how to save on dining, calculate your runway, or inspect your top spending categories!",
      isUser: false,
      timestamp: Date.now()
    }
  ]);

  // Modals
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const [showVoice, setShowVoice] = useState(false);
  const [showLiveVoice, setShowLiveVoice] = useState(false);
  const [showTransfer, setShowTransfer] = useState(false);
  const [showExport, setShowExport] = useState(false);
  const [showSettings, setShowSettings] = useState(false);

  const isInitialRemoteLoad = useRef(true);

  // 1. Listen for Firebase Auth changes
  useEffect(() => {
    const unsubscribe = subscribeToAuthChanges((firebaseUser) => {
      setUser(firebaseUser);
    });
    return () => unsubscribe();
  }, []);

  // 2. Real-time Firestore Sync when User is signed in
  useEffect(() => {
    if (!user) return;

    setIsCloudSyncing(true);
    const unsubscribeFirestore = subscribeToUserFinanceData(user.uid, (cloudData) => {
      if (cloudData) {
        if (cloudData.transactions && cloudData.transactions.length > 0) {
          setTransactions(cloudData.transactions);
        }
        if (cloudData.wallets && cloudData.wallets.length > 0) {
          setWallets(cloudData.wallets);
        }
        if (cloudData.notes && cloudData.notes.length > 0) {
          setNotes(cloudData.notes);
        }
      } else if (isInitialRemoteLoad.current) {
        // First time user on cloud: seed with current local data
        syncUserFinanceData(user.uid, { transactions, wallets, notes });
      }
      setIsCloudSyncing(false);
      isInitialRemoteLoad.current = false;
    });

    return () => unsubscribeFirestore();
  }, [user]);

  // 3. Local Storage Backup & Push changes to Firestore
  useEffect(() => {
    saveData('aimoney_transactions', transactions);
    if (user && !isInitialRemoteLoad.current) {
      syncUserFinanceData(user.uid, { transactions, wallets, notes });
    }
  }, [transactions]);

  useEffect(() => {
    saveData('aimoney_wallets', wallets);
    if (user && !isInitialRemoteLoad.current) {
      syncUserFinanceData(user.uid, { transactions, wallets, notes });
    }
  }, [wallets]);

  useEffect(() => {
    saveData('aimoney_notes', notes);
    if (user && !isInitialRemoteLoad.current) {
      syncUserFinanceData(user.uid, { transactions, wallets, notes });
    }
  }, [notes]);

  useEffect(() => {
    saveData('aimoney_pin_enabled', pinEnabled);
  }, [pinEnabled]);

  useEffect(() => {
    saveData('aimoney_pin', pin);
  }, [pin]);

  // Derived calculations
  const dailyInsight = React.useMemo(() => {
    return calculateDailyInsight(transactions, selectedDateStr);
  }, [transactions, selectedDateStr]);

  const prediction = React.useMemo(() => {
    return calculatePrediction(transactions, wallets);
  }, [transactions, wallets]);

  // Auth Handlers
  const handleGoogleSignIn = async () => {
    try {
      await signInWithGoogle();
    } catch (err: any) {
      alert(`Sign in failed: ${err.message || 'Please check popup settings'}`);
    }
  };

  const handleSignOut = async () => {
    try {
      await logoutUser();
      setUser(null);
    } catch (err) {
      console.error(err);
    }
  };

  // Financial Handlers
  const handleAddTransaction = (txData: {
    title: string;
    amount: number;
    type: TransactionType;
    category: string;
    walletId: string;
    date: string;
    note?: string;
  }) => {
    const newTx: Transaction = {
      id: `tx_${Date.now()}`,
      ...txData,
      timestamp: new Date(txData.date).getTime() || Date.now()
    };

    setTransactions(prev => [newTx, ...prev]);

    // Update wallet balance
    setWallets(prev =>
      prev.map(w => {
        if (w.id === txData.walletId) {
          const delta = txData.type === 'INCOME' ? txData.amount : -txData.amount;
          return { ...w, balance: Math.max(0, w.balance + delta) };
        }
        return w;
      })
    );
  };

  const handleDeleteTransaction = (id: string) => {
    const tx = transactions.find(t => t.id === id);
    if (!tx) return;

    // Reverse balance
    setWallets(prev =>
      prev.map(w => {
        if (w.id === tx.walletId) {
          const delta = tx.type === 'INCOME' ? -tx.amount : tx.amount;
          return { ...w, balance: Math.max(0, w.balance + delta) };
        }
        if (tx.toWalletId && w.id === tx.toWalletId) {
          return { ...w, balance: Math.max(0, w.balance - tx.amount) };
        }
        return w;
      })
    );

    setTransactions(prev => prev.filter(t => t.id !== id));
  };

  const handleTransfer = (fromId: string, toId: string, amount: number, note?: string) => {
    const newTx: Transaction = {
      id: `tx_${Date.now()}`,
      title: 'Wallet Transfer',
      amount,
      type: 'TRANSFER',
      category: 'Transfer',
      walletId: fromId,
      toWalletId: toId,
      date: new Date().toISOString().split('T')[0],
      timestamp: Date.now(),
      note: note || 'Transferred funds'
    };

    setTransactions(prev => [newTx, ...prev]);

    setWallets(prev =>
      prev.map(w => {
        if (w.id === fromId) return { ...w, balance: Math.max(0, w.balance - amount) };
        if (w.id === toId) return { ...w, balance: w.balance + amount };
        return w;
      })
    );
  };

  const handleAddWallet = (wallet: Omit<Wallet, 'id'>) => {
    const newW: Wallet = {
      id: `w_${Date.now()}`,
      ...wallet
    };
    setWallets(prev => [...prev, newW]);
  };

  const handleSaveNote = (noteData: Omit<Note, 'id' | 'createdAt' | 'updatedAt'> & { id?: string }) => {
    if (noteData.id) {
      setNotes(prev =>
        prev.map(n =>
          n.id === noteData.id
            ? { ...n, ...noteData, updatedAt: Date.now() }
            : n
        )
      );
    } else {
      const newN: Note = {
        id: `note_${Date.now()}`,
        ...noteData,
        createdAt: Date.now(),
        updatedAt: Date.now()
      };
      setNotes(prev => [newN, ...prev]);
    }
  };

  const handleDeleteNote = (id: string) => {
    setNotes(prev => prev.filter(n => n.id !== id));
  };

  const handleToggleChecklist = (noteId: string, itemId: string) => {
    setNotes(prev =>
      prev.map(n => {
        if (n.id === noteId) {
          const updated = n.checklistItems.map(item =>
            item.id === itemId ? { ...item, done: !item.done } : item
          );
          return { ...n, checklistItems: updated, updatedAt: Date.now() };
        }
        return n;
      })
    );
  };

  const handleResetData = () => {
    setTransactions(getInitialTransactions());
    setWallets(getInitialWallets());
    setNotes(getInitialNotes());
  };

  if (isLocked) {
    return <AppLockScreen correctPin={pin} onUnlock={() => setIsLocked(false)} />;
  }

  return (
    <div className="min-h-screen bg-[#020617] text-slate-100 flex flex-col font-sans">
      {/* Top Navbar */}
      <Navbar
        user={user}
        isCloudSyncing={isCloudSyncing}
        onOpenVoice={() => setShowVoice(true)}
        onOpenLiveVoice={() => setShowLiveVoice(true)}
        onOpenSettings={() => setShowSettings(true)}
        onSignInGoogle={handleGoogleSignIn}
        onSignOut={handleSignOut}
        isPinLocked={pinEnabled}
      />

      {/* Main Container */}
      <main className="flex-1 max-w-md w-full mx-auto p-4 flex flex-col">
        {currentTab === 'calendar' && (
          <CalendarTab
            currentDate={currentDate}
            selectedDateStr={selectedDateStr}
            transactions={transactions}
            wallets={wallets}
            dailyInsight={dailyInsight}
            onSelectDate={setSelectedDateStr}
            onChangeMonth={(delta) => {
              const d = new Date(currentDate);
              d.setMonth(d.getMonth() + delta);
              setCurrentDate(d);
            }}
            onQuickAddDay={(dateStr) => {
              setSelectedDateStr(dateStr);
              setShowQuickAdd(true);
            }}
            onDeleteTransaction={handleDeleteTransaction}
          />
        )}

        {currentTab === 'analytics' && (
          <AnalyticsTab
            transactions={transactions}
            wallets={wallets}
            prediction={prediction}
          />
        )}

        {currentTab === 'ai' && (
          <AiTab
            messages={chatMessages}
            wallets={wallets}
            transactions={transactions}
            onSendMessage={(msg) => setChatMessages(prev => [...prev, msg])}
            onOpenLiveVoice={() => setShowLiveVoice(true)}
          />
        )}

        {currentTab === 'notes' && (
          <NotesTab
            notes={notes}
            wallets={wallets}
            onSaveNote={handleSaveNote}
            onDeleteNote={handleDeleteNote}
            onToggleChecklistItem={handleToggleChecklist}
            onQuickAddExpense={(entry) => {
              handleAddTransaction({
                ...entry,
                walletId: wallets[0]?.id || 'w1',
                date: selectedDateStr
              });
            }}
          />
        )}

        {currentTab === 'wallets' && (
          <WalletsTab
            wallets={wallets}
            onOpenTransfer={() => setShowTransfer(true)}
            onAddWallet={handleAddWallet}
          />
        )}
      </main>

      {/* Persistent Bottom Bar */}
      <BottomNav
        currentTab={currentTab}
        onChangeTab={setCurrentTab}
        onOpenAdd={() => setShowQuickAdd(true)}
      />

      {/* Modal: Quick Add Transaction */}
      {showQuickAdd && (
        <QuickAddModal
          selectedDateStr={selectedDateStr}
          wallets={wallets}
          onClose={() => setShowQuickAdd(false)}
          onAdd={handleAddTransaction}
          onOpenVoice={() => {
            setShowQuickAdd(false);
            setShowVoice(true);
          }}
        />
      )}

      {/* Modal: Audio Transcribe with gemini-3.5-transcribe */}
      {showVoice && (
        <VoiceModal
          onClose={() => setShowVoice(false)}
          onConfirm={(entry) => {
            handleAddTransaction({
              ...entry,
              walletId: wallets[0]?.id || 'w1',
              date: selectedDateStr
            });
          }}
        />
      )}

      {/* Modal: Live Voice Conversation with gemini-3.8-live */}
      {showLiveVoice && (
        <LiveVoiceModal
          wallets={wallets}
          transactions={transactions}
          onClose={() => setShowLiveVoice(false)}
        />
      )}

      {/* Modal: Wallet Transfer */}
      {showTransfer && (
        <TransferModal
          wallets={wallets}
          onClose={() => setShowTransfer(false)}
          onTransfer={handleTransfer}
        />
      )}

      {/* Modal: Export Report */}
      {showExport && (
        <ExportModal
          transactions={transactions}
          wallets={wallets}
          onClose={() => setShowExport(false)}
        />
      )}

      {/* Modal: Settings, Google Sign-in & App Lock */}
      {showSettings && (
        <SettingsModal
          user={user}
          onSignInGoogle={handleGoogleSignIn}
          onSignOut={handleSignOut}
          isPinEnabled={pinEnabled}
          onTogglePin={setPinEnabled}
          onOpenExport={() => setShowExport(true)}
          onResetData={handleResetData}
          onClose={() => setShowSettings(false)}
        />
      )}
    </div>
  );
}

export default App;
