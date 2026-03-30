/**
 * Serviço de analytics e rastreamento de eventos do frontend.
 *
 * Rastreia page views, eventos de login, operações CRUD e erros.
 * Respeita consentimento GDPR/LGPD — nenhum dado é coletado sem
 * consentimento explícito do usuário.
 *
 * Por padrão, os eventos são registrados no console em modo dev e
 * descartados silenciosamente quando o consentimento não foi concedido.
 * Para integrar com Google Analytics ou Plausible, configure
 * VITE_ANALYTICS_PROVIDER e VITE_ANALYTICS_ID no .env.
 */

export type AnalyticsEvent =
  | { type: 'page_view'; page: string }
  | { type: 'login'; username: string }
  | { type: 'logout' }
  | { type: 'crud'; entity: string; operation: 'create' | 'update' | 'delete' }
  | { type: 'error'; message: string; source?: string }
  | { type: 'custom'; name: string; data?: Record<string, unknown> }

const CONSENT_KEY = 'javos-analytics-consent'

export type ConsentStatus = 'granted' | 'denied' | 'pending'

function getConsent(): ConsentStatus {
  const stored = localStorage.getItem(CONSENT_KEY)
  if (stored === 'granted') return 'granted'
  if (stored === 'denied') return 'denied'
  return 'pending'
}

function isProduction(): boolean {
  return import.meta.env.PROD
}

function sendToPlausible(event: AnalyticsEvent): void {
  const siteId = import.meta.env.VITE_ANALYTICS_ID
  if (!siteId || typeof window.plausible !== 'function') return

  if (event.type === 'page_view') {
    window.plausible('pageview')
    return
  }

  let name: string
  let props: Record<string, unknown> | undefined

  if (event.type === 'login') {
    name = 'Login'
  } else if (event.type === 'logout') {
    name = 'Logout'
  } else if (event.type === 'crud') {
    name = `${event.entity}_${event.operation}`
  } else if (event.type === 'error') {
    name = 'Error'
    props = { message: event.message }
  } else {
    name = event.name
    props = event.data
  }

  window.plausible(name, props ? { props } : undefined)
}

function sendToGA4(event: AnalyticsEvent): void {
  if (typeof window.gtag !== 'function') return

  if (event.type === 'page_view') {
    window.gtag('event', 'page_view', { page_path: event.page })
  } else if (event.type === 'login') {
    window.gtag('event', 'login', { method: 'credentials' })
  } else if (event.type === 'logout') {
    window.gtag('event', 'logout')
  } else if (event.type === 'crud') {
    window.gtag('event', `${event.entity}_${event.operation}`)
  } else if (event.type === 'error') {
    window.gtag('event', 'exception', { description: event.message, fatal: false })
  } else if (event.type === 'custom') {
    window.gtag('event', event.name, event.data)
  }
}

export const analyticsService = {
  /**
   * Retorna o status atual do consentimento do usuário.
   */
  getConsentStatus(): ConsentStatus {
    return getConsent()
  },

  /**
   * Registra o consentimento do usuário para coleta de analytics.
   */
  grantConsent(): void {
    localStorage.setItem(CONSENT_KEY, 'granted')
  },

  /**
   * Registra a recusa do usuário à coleta de analytics.
   */
  denyConsent(): void {
    localStorage.setItem(CONSENT_KEY, 'denied')
  },

  /**
   * Registra um evento de analytics, respeitando o consentimento do usuário.
   */
  track(event: AnalyticsEvent): void {
    if (getConsent() !== 'granted') return

    if (!isProduction()) {
      console.debug('[Analytics]', event)
    }

    const provider = import.meta.env.VITE_ANALYTICS_PROVIDER
    if (provider === 'plausible') {
      sendToPlausible(event)
    } else if (provider === 'ga4') {
      sendToGA4(event)
    }
  },

  /**
   * Rastreia uma visualização de página.
   */
  trackPageView(page: string): void {
    this.track({ type: 'page_view', page })
  },

  /**
   * Rastreia um evento de login bem-sucedido.
   */
  trackLogin(username: string): void {
    this.track({ type: 'login', username })
  },

  /**
   * Rastreia um evento de logout.
   */
  trackLogout(): void {
    this.track({ type: 'logout' })
  },

  /**
   * Rastreia uma operação CRUD.
   */
  trackCrud(entity: string, operation: 'create' | 'update' | 'delete'): void {
    this.track({ type: 'crud', entity, operation })
  },

  /**
   * Rastreia um erro de aplicação.
   */
  trackError(message: string, source?: string): void {
    this.track({ type: 'error', message, source })
  },
}

// Extensões de tipos globais para provedores de analytics externos
declare global {
  interface Window {
    gtag?: (...args: unknown[]) => void
    plausible?: (event: string, options?: { props?: Record<string, unknown> }) => void
  }
}
