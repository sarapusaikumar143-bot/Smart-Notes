import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiKey = process.env.GEMINI_API_KEY || env.GEMINI_API_KEY || '';

  return {
    plugins: [react()],
    define: {
      'process.env.GEMINI_API_KEY': JSON.stringify(apiKey),
      'import.meta.env.VITE_GEMINI_API_KEY': JSON.stringify(apiKey),
      '__GEMINI_API_KEY__': JSON.stringify(apiKey)
    },
    server: {
      host: '0.0.0.0',
      port: 3000,
      allowedHosts: true
    }
  };
});
