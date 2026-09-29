import { GoogleGenAI } from '@google/genai';
import { Transaction, Wallet } from '../types';

const apiKey = (process.env.GEMINI_API_KEY || (import.meta as any).env?.VITE_GEMINI_API_KEY || '') as string;

export type ChatModelTier = 'gemini-3.1-pro-preview' | 'gemini-3.5-flash' | 'gemini-3.1-flash-lite';

export interface GroundingSource {
  title: string;
  url: string;
}

export interface ChatCompletionResult {
  text: string;
  sources?: GroundingSource[];
  searchQueries?: string[];
}

export interface ChatHistoryTurn {
  role: 'user' | 'model';
  text: string;
}

export function buildSystemInstruction(
  role: string,
  wallets: Wallet[],
  transactions: Transaction[]
): string {
  const totalBalance = wallets.reduce((acc, w) => acc + w.balance, 0);
  const now = new Date();
  const currentMonth = now.getMonth();
  const currentYear = now.getFullYear();

  const monthTxs = transactions.filter(t => {
    const d = new Date(t.timestamp);
    return d.getMonth() === currentMonth && d.getFullYear() === currentYear;
  });

  const monthIncome = monthTxs.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0);
  const monthExpense = monthTxs.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0);

  const walletSummary = wallets.map(w => `${w.name} (${w.type}): ₹${Math.round(w.balance).toLocaleString()}`).join(', ');

  const recentTxs = transactions.slice(-10).map(t => `${t.date}: ${t.title} [${t.type}] ₹${t.amount} (${t.category})`).join('\n');

  return `You are SpendWise AI, an elite, data-driven personal financial copilot and smart notes advisor.
Your current role persona is: ${role}.

USER FINANCIAL CONTEXT (LIVE DATA):
• Current Liquid Net Worth: ₹${Math.round(totalBalance).toLocaleString()} across ${wallets.length} accounts.
• Accounts: ${walletSummary}
• Current Month Flow: Income +₹${Math.round(monthIncome).toLocaleString()} | Expenses -₹${Math.round(monthExpense).toLocaleString()} | Net: ₹${Math.round(monthIncome - monthExpense).toLocaleString()}
• Recent Transactions:
${recentTxs || 'No transactions recorded yet.'}

RULES:
1. Always ground advice directly on the user's real numbers above.
2. Be encouraging, realistic, and highly practical. Provide structured bullet points with actionable next steps.
3. If asked about current market rates, inflation, or external financial tools, provide accurate insights.
4. When relevant, recommend specific rupee amounts to save or cut back on.
5. Format key metrics and numbers in bold (e.g. **₹450**).`;
}

export async function sendGeminiChatMessage(options: {
  model: ChatModelTier;
  history: ChatHistoryTurn[];
  newMessage: string;
  systemInstruction: string;
  enableSearchGrounding?: boolean;
}): Promise<ChatCompletionResult> {
  const currentKey = apiKey || (process.env.GEMINI_API_KEY || (import.meta as any).env?.VITE_GEMINI_API_KEY || '');
  
  if (!currentKey || currentKey === 'MY_GEMINI_API_KEY') {
    return {
      text: "⚠️ Gemini API key is missing. Please add your GEMINI_API_KEY in the AI Studio Secrets panel."
    };
  }

  // Model selection:
  // If search grounding is requested, must use gemini-3.5-flash with googleSearch tool
  const effectiveModel = options.enableSearchGrounding ? 'gemini-3.5-flash' : options.model;

  // Build multi-turn contents array
  const contents = options.history.map(turn => ({
    role: turn.role,
    parts: [{ text: turn.text }]
  }));
  contents.push({
    role: 'user',
    parts: [{ text: options.newMessage }]
  });

  try {
    const ai = new GoogleGenAI({ apiKey: currentKey });

    const config: any = {
      systemInstruction: options.systemInstruction,
      temperature: 0.7
    };

    if (options.enableSearchGrounding) {
      config.tools = [{ googleSearch: {} }];
    }

    const response = await ai.models.generateContent({
      model: effectiveModel,
      contents,
      config
    });

    const text = response.text || "No response received.";
    
    // Extract grounding sources if available
    const sources: GroundingSource[] = [];
    const searchQueries: string[] = [];

    const candidates = response.candidates;
    if (candidates && candidates.length > 0) {
      const gMetadata = candidates[0].groundingMetadata;
      if (gMetadata) {
        if (gMetadata.webSearchQueries) {
          searchQueries.push(...gMetadata.webSearchQueries);
        }
        if (gMetadata.groundingChunks) {
          gMetadata.groundingChunks.forEach((chunk: any) => {
            if (chunk.web?.uri && chunk.web?.title) {
              sources.push({
                title: chunk.web.title,
                url: chunk.web.uri
              });
            }
          });
        }
      }
    }

    return {
      text,
      sources: sources.length > 0 ? sources : undefined,
      searchQueries: searchQueries.length > 0 ? searchQueries : undefined
    };
  } catch (error: any) {
    console.warn(`Error calling ${effectiveModel} via SDK, trying REST:`, error);

    // Fallback via Direct REST
    try {
      const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${effectiveModel}:generateContent?key=${currentKey}`;
      const payload: any = {
        contents: contents.map(c => ({
          role: c.role,
          parts: c.parts
        })),
        systemInstruction: {
          parts: [{ text: options.systemInstruction }]
        }
      };

      if (options.enableSearchGrounding) {
        payload.tools = [{ googleSearch: {} }];
      }

      const res = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        const json = await res.json();
        const cand = json.candidates?.[0];
        const text = cand?.content?.parts?.[0]?.text || "No response received.";
        
        const sources: GroundingSource[] = [];
        const queries: string[] = [];
        if (cand?.groundingMetadata?.groundingChunks) {
          cand.groundingMetadata.groundingChunks.forEach((c: any) => {
            if (c.web?.uri && c.web?.title) {
              sources.push({ title: c.web.title, url: c.web.uri });
            }
          });
        }
        if (cand?.groundingMetadata?.webSearchQueries) {
          queries.push(...cand.groundingMetadata.webSearchQueries);
        }

        return {
          text,
          sources: sources.length > 0 ? sources : undefined,
          searchQueries: queries.length > 0 ? queries : undefined
        };
      }
    } catch (restErr) {
      console.error('REST call failed:', restErr);
    }

    return {
      text: `❌ Error communicating with ${effectiveModel}: ${error.message || 'Network request failed'}`
    };
  }
}
