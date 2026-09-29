import { initializeApp, getApps, getApp } from 'firebase/app';
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  signOut,
  onAuthStateChanged,
  User
} from 'firebase/auth';
import { getFirestore, Firestore } from 'firebase/firestore';

export const firebaseConfig = {
  projectId: "gen-lang-client-0687894287",
  appId: "1:1062662380247:web:e22c688109e55f0a7b6e92",
  apiKey: "AIzaSyDxAw3NYxK2YPQJkr6mnvw-R7dlSYmSuhk",
  authDomain: "gen-lang-client-0687894287.firebaseapp.com",
  firestoreDatabaseId: "ai-studio-aimoneysmartnote-17f1943b-f07a-4728-9b54-b9300e6928fd",
  storageBucket: "gen-lang-client-0687894287.firebasestorage.app",
  messagingSenderId: "1062662380247"
};

// Initialize Firebase
const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
export const auth = getAuth(app);

// Firestore: initialize with custom databaseId if configured, or default
let firestoreDb: Firestore;
try {
  firestoreDb = getFirestore(app, firebaseConfig.firestoreDatabaseId);
} catch (e) {
  firestoreDb = getFirestore(app);
}
export const db = firestoreDb;

const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({
  prompt: 'select_account'
});

export async function signInWithGoogle(): Promise<User | null> {
  try {
    const result = await signInWithPopup(auth, googleProvider);
    return result.user;
  } catch (error: any) {
    console.error('Google Sign-In Error:', error);
    // Throw descriptive error or handle gracefully
    throw error;
  }
}

export async function logoutUser(): Promise<void> {
  try {
    await signOut(auth);
  } catch (error) {
    console.error('Logout error:', error);
  }
}

export function subscribeToAuthChanges(callback: (user: User | null) => void) {
  return onAuthStateChanged(auth, callback);
}
