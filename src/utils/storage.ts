import { Note, Transaction, Wallet } from '../types';

const STORAGE_KEYS = {
  TRANSACTIONS: 'aimoney_transactions',
  WALLETS: 'aimoney_wallets',
  NOTES: 'aimoney_notes',
  PIN: 'aimoney_pin',
  PIN_ENABLED: 'aimoney_pin_enabled'
};

export function getInitialWallets(): Wallet[] {
  return [
    { id: 'w1', name: 'Bank Account', type: 'BANK', balance: 34500, color: '#3B82F6', icon: 'Landmark' },
    { id: 'w2', name: 'UPI / Digital', type: 'UPI', balance: 4850, color: '#22C55E', icon: 'QrCode' },
    { id: 'w3', name: 'Cash Wallet', type: 'CASH', balance: 2200, color: '#F59E0B', icon: 'Banknote' },
    { id: 'w4', name: 'Credit Card', type: 'CARD', balance: 15000, color: '#8B5CF6', icon: 'CreditCard' },
  ];
}

export function getInitialTransactions(): Transaction[] {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth();
  const today = now.getDate();

  const getDateStr = (day: number) => {
    const d = new Date(year, month, Math.min(day, 28));
    return d.toISOString().split('T')[0];
  };

  return [
    {
      id: 'tx1',
      title: 'Monthly Salary Credit',
      amount: 55000,
      type: 'INCOME',
      category: 'Salary',
      walletId: 'w1',
      date: getDateStr(1),
      timestamp: new Date(year, month, 1, 10, 0).getTime(),
      note: 'Direct payroll'
    },
    {
      id: 'tx2',
      title: 'Supermarket Groceries',
      amount: 2450,
      type: 'EXPENSE',
      category: 'Food',
      walletId: 'w2',
      date: getDateStr(2),
      timestamp: new Date(year, month, 2, 17, 30).getTime(),
      note: 'Weekly essentials'
    },
    {
      id: 'tx3',
      title: 'Petrol Refill',
      amount: 650,
      type: 'EXPENSE',
      category: 'Transport',
      walletId: 'w2',
      date: getDateStr(3),
      timestamp: new Date(year, month, 3, 9, 15).getTime(),
      note: 'Bike tank full'
    },
    {
      id: 'tx4',
      title: 'Biryani & Kebabs Dinner',
      amount: 580,
      type: 'EXPENSE',
      category: 'Food',
      walletId: 'w2',
      date: getDateStr(Math.max(1, today - 1)),
      timestamp: new Date(year, month, Math.max(1, today - 1), 20, 0).getTime(),
      note: 'With colleagues'
    },
    {
      id: 'tx5',
      title: 'Electricity & Utility Bill',
      amount: 1850,
      type: 'EXPENSE',
      category: 'Bills',
      walletId: 'w1',
      date: getDateStr(5),
      timestamp: new Date(year, month, 5, 14, 0).getTime(),
      note: 'Online payment'
    },
    {
      id: 'tx6',
      title: 'Freelance Design Milestone',
      amount: 12000,
      type: 'INCOME',
      category: 'Investment',
      walletId: 'w1',
      date: getDateStr(8),
      timestamp: new Date(year, month, 8, 11, 45).getTime(),
      note: 'Side project'
    },
    {
      id: 'tx7',
      title: 'Espresso & Bakery Snack',
      amount: 220,
      type: 'EXPENSE',
      category: 'Food',
      walletId: 'w3',
      date: now.toISOString().split('T')[0],
      timestamp: now.getTime(),
      note: 'Morning coffee'
    }
  ];
}

export function getInitialNotes(): Note[] {
  return [
    {
      id: 'n1',
      title: 'Monthly Savings Rules',
      content: '1. Limit dining out to ₹1,500 max per weekend.\n2. Keep UPI wallet topped up only with weekly budget.\n3. Transfer 20% to emergency savings on the 1st of every month.\n4. Check AI prediction before large purchases.',
      isChecklist: false,
      checklistItems: [],
      category: 'Finance',
      color: '#22C55E',
      createdAt: Date.now() - 3600000,
      updatedAt: Date.now() - 3600000
    },
    {
      id: 'n2',
      title: 'Weekend Groceries Checklist',
      content: 'Items to restock for kitchen',
      isChecklist: true,
      checklistItems: [
        { id: 'c1', text: 'Basmati Rice 5kg', done: true },
        { id: 'c2', text: 'Olive oil & spices', done: false },
        { id: 'c3', text: 'Fresh Arabica coffee beans', done: true },
        { id: 'c4', text: 'Almonds & Walnuts', done: false }
      ],
      category: 'Groceries',
      color: '#3B82F6',
      createdAt: Date.now() - 7200000,
      updatedAt: Date.now() - 7200000
    },
    {
      id: 'n3',
      title: 'Laptop Maintenance Expense',
      content: 'Hardware service done on 15th. Total expense ₹1,400 paid via card. Claim warranty before next quarter.',
      isChecklist: false,
      checklistItems: [],
      category: 'Tech',
      color: '#8B5CF6',
      createdAt: Date.now() - 10800000,
      updatedAt: Date.now() - 10800000
    }
  ];
}

export function loadData<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch (e) {
    return fallback;
  }
}

export function saveData<T>(key: string, data: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(data));
  } catch (e) {
    console.error('Save failed', e);
  }
}

export function generateCsv(transactions: Transaction[], wallets: Wallet[]): string {
  const walletMap = new Map(wallets.map(w => [w.id, w.name]));
  const header = 'ID,Date,Title,Type,Category,Amount,Wallet,Note\n';
  const rows = transactions.map(t => {
    const wName = walletMap.get(t.walletId) || 'Unknown';
    const cleanTitle = t.title.replace(/,/g, ' ');
    const cleanNote = (t.note || '').replace(/,/g, ' ');
    return `${t.id},${t.date},"${cleanTitle}",${t.type},"${t.category}",${t.amount},"${wName}","${cleanNote}"`;
  }).join('\n');
  return header + rows;
}

export function generateReportText(transactions: Transaction[], wallets: Wallet[]): string {
  const totalIncome = transactions.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0);
  const totalExpense = transactions.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0);
  const net = totalIncome - totalExpense;

  const catMap: Record<string, number> = {};
  transactions.filter(t => t.type === 'EXPENSE').forEach(t => {
    catMap[t.category] = (catMap[t.category] || 0) + t.amount;
  });

  let report = `=========================================\n`;
  report += `    AI MONEY + SMART NOTES REPORT\n`;
  report += `    Generated: ${new Date().toLocaleDateString()}\n`;
  report += `=========================================\n\n`;
  report += `FINANCIAL SUMMARY:\n`;
  report += `• Total Income:   ₹${Math.round(totalIncome).toLocaleString()}\n`;
  report += `• Total Expenses: ₹${Math.round(totalExpense).toLocaleString()}\n`;
  report += `• Net Surplus:    ₹${Math.round(net).toLocaleString()}\n\n`;
  report += `CURRENT WALLET BALANCES:\n`;
  wallets.forEach(w => {
    report += `• ${w.name}: ₹${Math.round(w.balance).toLocaleString()}\n`;
  });
  report += `\nEXPENSES BY CATEGORY:\n`;
  Object.entries(catMap).sort((a, b) => b[1] - a[1]).forEach(([k, v]) => {
    const pct = totalExpense > 0 ? Math.round((v / totalExpense) * 100) : 0;
    report += `• ${k}: ₹${Math.round(v).toLocaleString()} (${pct}%)\n`;
  });
  report += `\nRECENT TRANSACTIONS (${transactions.length} records):\n`;
  report += `-----------------------------------------\n`;
  transactions.slice(0, 20).forEach(t => {
    const sign = t.type === 'INCOME' ? '+' : '-';
    report += `${t.date} | ${t.title.slice(0, 20).padEnd(20)} | ${sign}₹${Math.round(t.amount)} [${t.category}]\n`;
  });
  report += `=========================================\n`;
  return report;
}
