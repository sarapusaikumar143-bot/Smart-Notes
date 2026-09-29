export type TransactionType = 'EXPENSE' | 'INCOME' | 'TRANSFER';

export type WalletType = 'CASH' | 'BANK' | 'UPI' | 'CARD' | 'SAVINGS';

export interface Transaction {
  id: string;
  title: string;
  amount: number;
  type: TransactionType;
  category: string;
  walletId: string;
  toWalletId?: string;
  date: string; // YYYY-MM-DD
  timestamp: number;
  note?: string;
}

export interface Wallet {
  id: string;
  name: string;
  type: WalletType;
  balance: number;
  color: string;
  icon: string;
}

export interface ChecklistItem {
  id: string;
  text: string;
  done: boolean;
}

export interface Note {
  id: string;
  title: string;
  content: string;
  isChecklist: boolean;
  checklistItems: ChecklistItem[];
  category: string;
  color: string;
  createdAt: number;
  updatedAt: number;
}

export interface ChatMessage {
  id: string;
  text: string;
  isUser: boolean;
  timestamp: number;
}

export interface DailyInsight {
  title: string;
  description: string;
  badge: string;
  changePercent?: number | null;
  isPositive: boolean;
}

export interface FinancialPrediction {
  runwayDays: number | null;
  dailyBurnRate: number;
  projectedMonthEndBalance: number;
  overspendingAlert: string | null;
  savingsRatePercent: number;
  summaryText: string;
}

export type TabType = 'calendar' | 'analytics' | 'ai' | 'notes' | 'wallets';
