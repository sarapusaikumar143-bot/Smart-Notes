/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        navy: {
          900: '#0F172A',
          950: '#020617',
        },
        money: {
          500: '#22C55E',
          600: '#16A34A',
          100: '#DCFCE7'
        },
        electric: {
          500: '#3B82F6',
          600: '#2563EB',
          100: '#DBEAFE'
        },
        danger: {
          500: '#EF4444',
          100: '#FEE2E2'
        }
      }
    },
  },
  plugins: [],
}
