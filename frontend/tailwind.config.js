/** @type {import('tailwindcss').Config} */
import daisyui from 'daisyui';

export default {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        tea: '#26BB69',
        beige: '#E9EDC9',
        cornsilk: '#FEFAE0',
        papaya: '#FAEDCD',
        wood: '#D4A373',
        teabg: '#E6F9E4',
        disabled: '#626262',
        dborder: '#EEEEEE',
        emerald: {
          50: '#f0fdf5',
          100: '#ddfbe9',
          200: '#bdf5d6',
          300: '#89ecb5',
          400: '#4fd98c',
          500: '#26bb69',
          600: '#1a9f56',
          700: '#187d46',
          800: '#18633b',
          900: '#165132',
          950: '#062d19',
        },
      },
      gridTemplateRows: {
        // Simple 16 row grid
        14: 'repeat(14, minmax(0, 1fr))',
        16: 'repeat(16, minmax(0, 1fr))',
      },
    },
  },
  plugins: [daisyui],
};
