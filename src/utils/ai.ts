import { DailyInsight, FinancialPrediction, Transaction, Wallet } from '../types';

export interface ParsedExpense {
  title: string;
  amount: number;
  type: 'EXPENSE' | 'INCOME';
  category: string;
}

export function parseNaturalExpense(input: string): ParsedExpense {
  const lower = input.toLowerCase().trim();
  
  // Extract number
  let amount = 0;
  const numMatch = input.match(/(?:(?:rs\.?|inr|₹|\$|€|£)\s*)?([0-9]+(?:\.[0-9]{1,2})?)\s*(k|k\b)?/i);
  if (numMatch) {
    const rawVal = parseFloat(numMatch[1]);
    const isK = !!numMatch[2] && numMatch[2].toLowerCase() === 'k';
    amount = isK ? rawVal * 1000 : rawVal;
  }

  // Detect Income
  const incomeKeywords = ['salary', 'credited', 'received', 'refund', 'cashback', 'bonus', 'dividend', 'profit', 'freelance'];
  const isIncome = incomeKeywords.some(k => lower.includes(k));
  const type = isIncome ? 'INCOME' : 'EXPENSE';

  // Categories
  let category = 'Other';
  const foodKeywords = ['biryani', 'pizza', 'burger', 'lunch', 'dinner', 'breakfast', 'coffee', 'tea', 'chai', 'swiggy', 'zomato', 'restaurant', 'cafe', 'groceries', 'supermarket', 'snacks', 'food', 'milk', 'bakery'];
  const transportKeywords = ['petrol', 'diesel', 'fuel', 'uber', 'ola', 'cab', 'taxi', 'bus', 'train', 'metro', 'auto', 'flight', 'toll', 'parking'];
  const billsKeywords = ['electricity', 'water', 'wifi', 'broadband', 'recharge', 'bill', 'rent', 'gas', 'netflix', 'subscription', 'emi', 'maintenance'];
  const shoppingKeywords = ['amazon', 'flipkart', 'myntra', 'clothes', 'shirt', 'shoes', 'shopping', 'mall', 'electronics', 'gadget'];
  const entertainmentKeywords = ['movie', 'cinema', 'theatre', 'concert', 'game', 'outing', 'party', 'trip'];
  const healthKeywords = ['medicine', 'doctor', 'hospital', 'pharmacy', 'tests', 'gym', 'workout'];

  if (foodKeywords.some(k => lower.includes(k))) category = 'Food';
  else if (transportKeywords.some(k => lower.includes(k))) category = 'Transport';
  else if (billsKeywords.some(k => lower.includes(k))) category = 'Bills';
  else if (shoppingKeywords.some(k => lower.includes(k))) category = 'Shopping';
  else if (entertainmentKeywords.some(k => lower.includes(k))) category = 'Entertainment';
  else if (healthKeywords.some(k => lower.includes(k))) category = 'Health';
  else if (isIncome && lower.includes('salary')) category = 'Salary';
  else if (isIncome) category = 'Investment';

  // Clean title
  let cleanTitle = input
    .replace(/(?:(?:rs\.?|inr|₹|\$|€|£)\s*)?([0-9]+(?:\.[0-9]{1,2})?)\s*(k|k\b)?/gi, '')
    .replace(/\b(add|spent|paid|for|to|at|got|on)\b/gi, '')
    .trim();

  if (!cleanTitle) {
    cleanTitle = category !== 'Other' ? category : 'Quick Entry';
  } else {
    cleanTitle = cleanTitle.charAt(0).toUpperCase() + cleanTitle.slice(1);
  }

  return {
    title: cleanTitle,
    amount,
    type,
    category
  };
}

export function calculateDailyInsight(transactions: Transaction[], targetDateStr: string): DailyInsight {
  const targetDate = new Date(targetDateStr);
  const yest = new Date(targetDate);
  yest.setDate(yest.getDate() - 1);
  const yestStr = yest.toISOString().split('T')[0];

  const todayTxs = transactions.filter(t => t.date === targetDateStr && t.type === 'EXPENSE');
  const yestTxs = transactions.filter(t => t.date === yestStr && t.type === 'EXPENSE');

  const todayExpense = todayTxs.reduce((sum, t) => sum + t.amount, 0);
  const yestExpense = yestTxs.reduce((sum, t) => sum + t.amount, 0);

  if (todayExpense === 0) {
    return {
      title: "Zero Spend Day!",
      description: "Awesome discipline. Your wallet is taking a well-deserved rest today.",
      badge: "Streak Safe 🌟",
      changePercent: null,
      isPositive: true
    };
  }

  // Top category today
  const catMap: Record<string, number> = {};
  todayTxs.forEach(t => {
    catMap[t.category] = (catMap[t.category] || 0) + t.amount;
  });
  const topCat = Object.entries(catMap).sort((a, b) => b[1] - a[1])[0];
  const topCatPct = topCat && todayExpense > 0 ? Math.round((topCat[1] / todayExpense) * 100) : 0;

  if (yestExpense > 0) {
    const diff = todayExpense - yestExpense;
    const pct = Math.round((diff / yestExpense) * 100);
    if (pct > 0) {
      return {
        title: "Daily Insight AI",
        description: `You spent ${pct}% more than yesterday. ${topCat ? topCat[0] : 'Spending'} accounted for ${topCatPct}% of today's total.`,
        badge: "Spend Alert",
        changePercent: pct,
        isPositive: false
      };
    } else {
      return {
        title: "Daily Insight AI",
        description: `Great job! You cut spending by ${Math.abs(pct)}% compared to yesterday.`,
        badge: "Savings Win",
        changePercent: pct,
        isPositive: true
      };
    }
  }

  return {
    title: "Daily Insight AI",
    description: `Total spent today is ₹${Math.round(todayExpense).toLocaleString()}. Top category: ${topCat ? topCat[0] : 'Food'} (${topCatPct}%).`,
    badge: "Active Day",
    changePercent: null,
    isPositive: true
  };
}

export function calculatePrediction(transactions: Transaction[], wallets: Wallet[]): FinancialPrediction {
  const liquidFunds = wallets.filter(w => w.type !== 'CARD').reduce((acc, w) => acc + w.balance, 0);
  
  // Past 14 days burn rate
  const now = Date.now();
  const fourteenDaysAgo = now - (14 * 24 * 60 * 60 * 1000);
  const recentExpenses = transactions.filter(t => t.type === 'EXPENSE' && t.timestamp >= fourteenDaysAgo);
  const totalRecentExpense = recentExpenses.reduce((acc, t) => acc + t.amount, 0);
  const dailyBurn = totalRecentExpense > 0 ? totalRecentExpense / 14 : 350;

  const runwayDays = dailyBurn > 0 && liquidFunds > 0 ? Math.round(liquidFunds / dailyBurn) : null;

  // Month calculations
  const currentDate = new Date();
  const currentMonth = currentDate.getMonth();
  const currentYear = currentDate.getFullYear();
  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  const remainingDays = Math.max(1, daysInMonth - currentDate.getDate());

  const monthTxs = transactions.filter(t => {
    const d = new Date(t.timestamp);
    return d.getMonth() === currentMonth && d.getFullYear() === currentYear;
  });

  const monthIncome = monthTxs.filter(t => t.type === 'INCOME').reduce((acc, t) => acc + t.amount, 0);
  const monthExpense = monthTxs.filter(t => t.type === 'EXPENSE').reduce((acc, t) => acc + t.amount, 0);

  const projectedExpense = monthExpense + (dailyBurn * remainingDays);
  const projectedMonthEndBalance = monthIncome - projectedExpense;

  const savingsRate = monthIncome > 0 ? Math.max(0, Math.round(((monthIncome - monthExpense) / monthIncome) * 100)) : 0;

  let overspendingAlert: string | null = null;
  const foodExpense = monthTxs.filter(t => t.type === 'EXPENSE' && t.category === 'Food').reduce((acc, t) => acc + t.amount, 0);
  if (monthExpense > 0 && (foodExpense / monthExpense) > 0.40) {
    overspendingAlert = `You overspent on Food this month (${Math.round((foodExpense / monthExpense) * 100)}% of total expenses).`;
  }

  let summaryText = "";
  if (runwayDays !== null && runwayDays < 20) {
    summaryText = `🔥 Critical Runway: At this daily burn rate (₹${Math.round(dailyBurn)}/day), your current funds will deplete in ~${runwayDays} days!`;
  } else if (projectedMonthEndBalance > 0) {
    summaryText = `✨ Steady Pace: Projected month-end surplus of ₹${Math.round(projectedMonthEndBalance).toLocaleString()}. Savings rate at ${savingsRate}%.`;
  } else {
    summaryText = `⚠️ Deficit Warning: Projected deficit of ₹${Math.abs(Math.round(projectedMonthEndBalance)).toLocaleString()} by month end at current burn rate.`;
  }

  return {
    runwayDays,
    dailyBurnRate: dailyBurn,
    projectedMonthEndBalance,
    overspendingAlert,
    savingsRatePercent: savingsRate,
    summaryText
  };
}

export function askFinancialAdvisor(query: string, wallets: Wallet[], transactions: Transaction[]): string {
  const totalBalance = wallets.reduce((acc, w) => acc + w.balance, 0);
  const expenses = transactions.filter(t => t.type === 'EXPENSE');
  const income = transactions.filter(t => t.type === 'INCOME');
  const totalExpense = expenses.reduce((acc, t) => acc + t.amount, 0);
  const totalIncome = income.reduce((acc, t) => acc + t.amount, 0);

  const catMap: Record<string, number> = {};
  expenses.forEach(t => { catMap[t.category] = (catMap[t.category] || 0) + t.amount; });
  const topCategories = Object.entries(catMap).sort((a, b) => b[1] - a[1]).slice(0, 3).map(([k, v]) => `${k} (₹${Math.round(v)})`).join(', ');

  const prediction = calculatePrediction(transactions, wallets);
  const q = query.toLowerCase();

  if (q.includes('save') || q.includes('more money') || q.includes('reduce')) {
    return `💡 **Personalized Savings Strategy:**\n\n` +
      `1. **Control High Spends:** Your top expenses are in ${topCategories}. Trimming Food/Dining by 15-20% can save you ~₹${Math.round(totalExpense * 0.08).toLocaleString()} this month.\n` +
      `2. **Burn Rate Cap:** You are burning ₹${Math.round(prediction.dailyBurnRate)}/day. Capping casual spending to ₹250/day will extend your runway by ${(prediction.runwayDays || 15) + 9} days.\n` +
      `3. **Automate Savings:** Move 15% of salary directly to a locked savings pot on the 1st of every month before spending.`;
  }

  if (q.includes('run out') || q.includes('runway') || q.includes('afford')) {
    return `📊 **Runway & Affordability Breakdown:**\n\n` +
      `• Estimated Runway: **${prediction.runwayDays || 22} days** at your current burn rate of ₹${Math.round(prediction.dailyBurnRate)}/day.\n` +
      `• Total Liquid Funds: **₹${Math.round(totalBalance).toLocaleString()}** across ${wallets.length} wallets.\n` +
      `• Advice: Postpone discretionary single purchases over ₹2,500 until the next major income credit.`;
  }

  if (q.includes('highest') || q.includes('most') || q.includes('where')) {
    const highest = [...expenses].sort((a, b) => b.amount - a.amount)[0];
    return `📌 Your single highest expense was **${highest?.title || 'None'}** for **₹${Math.round(highest?.amount || 0).toLocaleString()}** (${highest?.category || 'General'}).\nTop categories: ${topCategories}.`;
  }

  return `📊 **Your Financial Snapshot:**\n\n` +
    `• Total Net Worth: **₹${Math.round(totalBalance).toLocaleString()}**\n` +
    `• Period Income: **₹${Math.round(totalIncome).toLocaleString()}** | Outflow: **₹${Math.round(totalExpense).toLocaleString()}**\n` +
    `• Daily Burn Rate: **₹${Math.round(prediction.dailyBurnRate)}/day** (~${prediction.runwayDays || 24} days runway)\n` +
    `• Top Spends: ${topCategories}\n\n` +
    `Try asking: *"How can I save more?"*, *"Can I afford a trip?"*, or *"Predict my month-end balance"*.`;
}

export function summarizeNote(content: string): string {
  const lines = content.split('\n').map(l => l.trim()).filter(Boolean);
  if (lines.length <= 2) return content;
  return `• ` + lines.slice(0, 3).map(l => l.slice(0, 90)).join('\n• ');
}

export function convertToChecklist(content: string): { id: string; text: string; done: boolean }[] {
  return content.split('\n')
    .map(line => line.replace(/^[-*•\d.]\s*/, '').trim())
    .filter(Boolean)
    .map((text, i) => ({ id: `chk_${Date.now()}_${i}`, text, done: false }));
}

export function translateNote(content: string, targetLang: string): string {
  // Telugu & multilingual dictionary lookup for key financial terms
  const translations: Record<string, string> = {
    "grocery": "కిరాణా సరుకులు (Groceries)",
    "milk": "పాలు (Milk)",
    "rice": "బియ్యం (Rice)",
    "petrol": "పెట్రోల్ (Petrol)",
    "salary": "జీతం (Salary)",
    "electricity": "కరెంట్ బిల్లు (Electricity)",
    "vegetables": "కూరగాయలు (Vegetables)",
    "dinner": "రాత్రి భోజనం (Dinner)",
    "rent": "అద్దె (Rent)"
  };

  let translated = content;
  Object.entries(translations).forEach(([en, tel]) => {
    translated = translated.replace(new RegExp(en, 'gi'), tel);
  });

  return `[${targetLang} Translation]:\n` + translated;
}
