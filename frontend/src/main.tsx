import React from 'react'
import ReactDOM from 'react-dom/client'
import './i18n/index'
import App from './App.tsx'
import './index.css'
import { ErrorBoundary } from './components/ui/ErrorBoundary.tsx'
import { installGlobalErrorHandlers } from './services/errorLogger.ts'

installGlobalErrorHandlers()

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </React.StrictMode>,
)
