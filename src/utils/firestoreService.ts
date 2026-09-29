import {
  doc,
  setDoc,
  getDoc,
  onSnapshot,
  serverTimestamp
} from 'firebase/firestore';
import { db } from './firebase';
import { Note, Transaction, Wallet } from '../types';

export interface UserFinanceData {
  transactions: Transaction[];
  wallets: Wallet[];
  notes: Note[];
  lastSyncedAt?: number;
}

export async function syncUserFinanceData(
  userId: string,
  data: { transactions: Transaction[]; wallets: Wallet[]; notes: Note[] }
): Promise<void> {
  if (!userId) return;
  try {
    const userDocRef = doc(db, 'users', userId, 'userData', 'finances');
    await setDoc(userDocRef, {
      transactions: data.transactions,
      wallets: data.wallets,
      notes: data.notes,
      updatedAt: serverTimestamp(),
      lastSyncedAt: Date.now()
    }, { merge: true });
  } catch (err) {
    console.error('Firestore sync error:', err);
  }
}

export async function fetchUserFinanceData(userId: string): Promise<UserFinanceData | null> {
  if (!userId) return null;
  try {
    const userDocRef = doc(db, 'users', userId, 'userData', 'finances');
    const snap = await getDoc(userDocRef);
    if (snap.exists()) {
      const d = snap.data();
      return {
        transactions: d.transactions || [],
        wallets: d.wallets || [],
        notes: d.notes || [],
        lastSyncedAt: d.lastSyncedAt || Date.now()
      };
    }
    return null;
  } catch (err) {
    console.error('Firestore fetch error:', err);
    return null;
  }
}

export function subscribeToUserFinanceData(
  userId: string,
  onUpdate: (data: UserFinanceData) => void
) {
  if (!userId) return () => {};
  const userDocRef = doc(db, 'users', userId, 'userData', 'finances');
  return onSnapshot(userDocRef, (snap) => {
    if (snap.exists()) {
      const d = snap.data();
      onUpdate({
        transactions: d.transactions || [],
        wallets: d.wallets || [],
        notes: d.notes || [],
        lastSyncedAt: d.lastSyncedAt || Date.now()
      });
    }
  }, (err) => {
    console.warn('Firestore snapshot listener error:', err);
  });
}
