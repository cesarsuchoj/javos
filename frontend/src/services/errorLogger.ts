/**
 * Serviço de rastreamento de erros do frontend.
 *
 * Captura exceções JavaScript não tratadas e as envia para o backend
 * via console estruturado. Pode ser extendido para integração com
 * Sentry ou outro serviço de error tracking externo.
 */

export interface ErrorReport {
  message: string
  source?: string
  stack?: string
  userAgent: string
  url: string
  timestamp: string
  componentStack?: string
}

function buildReport(
  error: Error | string,
  extra?: { componentStack?: string; source?: string },
): ErrorReport {
  const message = error instanceof Error ? error.message : String(error)
  const stack = error instanceof Error ? error.stack : undefined
  return {
    message,
    stack,
    source: extra?.source,
    componentStack: extra?.componentStack,
    userAgent: navigator.userAgent,
    url: window.location.href,
    timestamp: new Date().toISOString(),
  }
}

export const errorLogger = {
  /**
   * Registra um erro capturado por um ErrorBoundary React.
   */
  captureError(error: Error, componentStack: string): void {
    const report = buildReport(error, { componentStack })
    // Emite log estruturado no console (visível em ferramentas de DevTools e sistemas de coleta de logs).
    console.error('[ErrorBoundary]', JSON.stringify(report))
  },

  /**
   * Registra um erro genérico de runtime ou de ação do usuário.
   */
  captureException(error: unknown, source?: string): void {
    const err = error instanceof Error ? error : new Error(String(error))
    const report = buildReport(err, { source })
    console.error('[ErrorLogger]', JSON.stringify(report))
  },
}

/**
 * Instala handlers globais para erros não capturados.
 * Deve ser chamado uma única vez na inicialização da aplicação.
 */
export function installGlobalErrorHandlers(): void {
  window.addEventListener('unhandledrejection', (event) => {
    errorLogger.captureException(event.reason, 'unhandledrejection')
  })

  window.addEventListener('error', (event) => {
    if (event.error) {
      errorLogger.captureException(event.error, 'window.onerror')
    }
  })
}
