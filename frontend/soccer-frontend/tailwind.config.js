/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {

      // ─── PALETTE ────────────────────────────────────────────────────────────
      // Derived from pitchatdusk.jpg + footballer.jpg
      // Base:    near-black pitch shadows
      // Primary: deep forest greens of the pitch & jersey
      // Accent:  golden amber from the dusk sun & ball
      // Surface: cool grey-greens for cards/nav
      // Text:    off-white, never pure white
      colors: {
        pitch: {
          950: '#080d08',   // deepest shadow — near black with green tint
          900: '#0e1a0f',   // dark base background
          800: '#162518',   // slightly lifted dark green
          700: '#1e3320',   // rich forest — nav, sidebars
          600: '#275c2a',   // mid green — borders, dividers
          500: '#2e7d32',   // core pitch green — primary brand colour
          400: '#43a047',   // lighter active/hover state
          300: '#66bb6a',   // lighter text on dark bg, tags
          200: '#a5d6a7',   // very light green tint
          100: '#e8f5e9',   // near-white with green cast
        },
        amber: {
          600: '#c8861a',   // deep gold — pressed states
          500: '#e8a820',   // the dusk sun — primary accent
          400: '#f0bc3a',   // bright gold — hover accent
          300: '#f5d06a',   // soft gold — subtle highlights
        },
        surface: {
          // Cool grey-greens — for cards, modals, inputs
          900: '#111614',
          800: '#1a1f1b',
          700: '#232a24',
          600: '#2e372f',
          500: '#3d4a3e',
        },
        // Semantic text colours
        ink: {
          DEFAULT: '#f0f4f0', // off-white with green cast — default body text
          muted:   '#8fa890', // muted labels, captions
          faint:   '#4a5c4b', // placeholder, disabled
        },
      },

      // ─── TYPOGRAPHY ─────────────────────────────────────────────────────────
      fontFamily: {
        // Display: Bebas Neue — tall, athletic, punchy — for headings, numbers
        // Body:    DM Sans — clean, modern, readable at small sizes on mobile
        // Mono:    JetBrains Mono — for slot numbers, counts
        display: ['"Bebas Neue"', 'Impact', 'sans-serif'],
        sans:    ['"DM Sans"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono:    ['"JetBrains Mono"', 'ui-monospace', 'monospace'],
      },

      fontSize: {
        // Fluid display sizes for hero sections
        'display-xl': ['clamp(3rem, 10vw, 7rem)',    { lineHeight: '0.9', letterSpacing: '-0.01em' }],
        'display-lg': ['clamp(2.25rem, 7vw, 4.5rem)', { lineHeight: '0.95', letterSpacing: '-0.01em' }],
        'display-md': ['clamp(1.75rem, 5vw, 3rem)',   { lineHeight: '1',    letterSpacing: '-0.005em' }],
      },

      // ─── SPACING ────────────────────────────────────────────────────────────
      spacing: {
        '18': '4.5rem',
        '22': '5.5rem',
        '30': '7.5rem',
        '88': '22rem',
        '128': '32rem',
      },

      // ─── BORDER RADIUS ──────────────────────────────────────────────────────
      borderRadius: {
        'xl':  '0.875rem',
        '2xl': '1.25rem',
        '3xl': '1.75rem',
      },

      // ─── SHADOWS ────────────────────────────────────────────────────────────
      boxShadow: {
        'pitch':  '0 4px 24px 0 rgba(8,13,8,0.7)',
        'glow-green': '0 0 20px 2px rgba(46,125,50,0.35)',
        'glow-amber': '0 0 20px 4px rgba(232,168,32,0.4)',
        'card':   '0 2px 16px 0 rgba(8,13,8,0.5)',
        'inset':  'inset 0 1px 0 0 rgba(255,255,255,0.05)',
      },

      // ─── BACKGROUND IMAGES ──────────────────────────────────────────────────
      backgroundImage: {
        // Reusable gradient overlays to layer over the photos
        'pitch-overlay':
          'linear-gradient(to bottom, rgba(8,13,8,0.45) 0%, rgba(8,13,8,0.85) 100%)',
        'pitch-overlay-strong':
          'linear-gradient(to bottom, rgba(8,13,8,0.65) 0%, rgba(8,13,8,0.97) 100%)',
        'hero-gradient':
          'linear-gradient(135deg, rgba(14,26,15,0.92) 0%, rgba(46,125,50,0.15) 60%, rgba(232,168,32,0.08) 100%)',
        'card-gradient':
          'linear-gradient(145deg, rgba(35,42,36,0.9) 0%, rgba(22,37,24,0.95) 100%)',
        'amber-glow':
          'radial-gradient(ellipse at top, rgba(232,168,32,0.15) 0%, transparent 70%)',
        'green-glow':
          'radial-gradient(ellipse at bottom, rgba(46,125,50,0.2) 0%, transparent 70%)',
      },

      // ─── ANIMATIONS ─────────────────────────────────────────────────────────
      keyframes: {
        'fade-up': {
          '0%':   { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'pulse-amber': {
          '0%, 100%': { boxShadow: '0 0 0 0 rgba(232,168,32,0)' },
          '50%':       { boxShadow: '0 0 0 6px rgba(232,168,32,0.2)' },
        },
        'slide-in-right': {
          '0%':   { transform: 'translateX(100%)' },
          '100%': { transform: 'translateX(0)' },
        },
      },
      animation: {
        'fade-up':         'fade-up 0.5s ease-out both',
        'fade-up-slow':    'fade-up 0.8s ease-out both',
        'fade-in':         'fade-in 0.4s ease-out both',
        'pulse-amber':     'pulse-amber 2s ease-in-out infinite',
        'slide-in-right':  'slide-in-right 0.3s ease-out both',
      },

      // ─── BACKDROP BLUR ──────────────────────────────────────────────────────
      backdropBlur: {
        xs: '2px',
      },
    },
  },
  plugins: [],
};
