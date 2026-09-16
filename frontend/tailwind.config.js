/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        industrial: {
          900: '#0f172a',
          800: '#1e293b',
          700: '#334155',
          600: '#475569',
          100: '#f1f5f9',
          50: '#f8fafc',
        },
        cnc: {
          blue: '#1677ff',
          teal: '#0891b2',
          warning: '#faad14',
          danger: '#ff4d4f',
          success: '#52c41a'
        }
      }
    },
  },
  plugins: [],
  corePlugins: {
    preflight: false, // 避免与 Ant Design reset 样式冲突
  }
}
