import React from 'react'
import ReactDOM from 'react-dom/client'
import './i18n/index'
import App from './App.tsx'
import './index.css'
import { ErrorBoundary } from './components/ui/ErrorBoundary.tsx'
import { installGlobalErrorHandlers } from './services/errorLogger.ts'

installGlobalErrorHandlers()

// Apply stored theme before React renders to avoid flash
;(() => {
  try {
    const raw = localStorage.getItem('javos-theme')
    const stored = raw ? (JSON.parse(raw) as { state?: { theme?: string } }).state?.theme : null
    const theme =
      stored === 'dark' || stored === 'light'
        ? stored
        : window.matchMedia('(prefers-color-scheme: dark)').matches
          ? 'dark'
          : 'light'
    document.documentElement.setAttribute('data-theme', theme)
  } catch {
    document.documentElement.setAttribute('data-theme', 'light')
  }
})()

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>,
)
