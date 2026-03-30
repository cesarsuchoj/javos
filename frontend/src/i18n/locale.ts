import i18n from './index'

const LOCALE_MAP: Record<string, string> = {
  'pt-BR': 'pt-BR',
  en: 'en-US',
  es: 'es-ES',
}

const CURRENCY_MAP: Record<string, string> = {
  'pt-BR': 'BRL',
  en: 'BRL',
  es: 'BRL',
}

export function getLocale(): string {
  return LOCALE_MAP[i18n.language] ?? 'pt-BR'
}

export function formatCurrency(value: number | null | undefined): string {
  const locale = getLocale()
  const currency = CURRENCY_MAP[i18n.language] ?? 'BRL'
  return (value ?? 0).toLocaleString(locale, { style: 'currency', currency })
}

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString(getLocale())
}
