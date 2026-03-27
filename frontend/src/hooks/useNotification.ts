import { useState, useCallback, useRef, useEffect } from 'react'

type NotificationType = 'success' | 'error'

interface Notification {
  type: NotificationType
  message: string
}

export function useNotification(timeout = 4000) {
  const [notification, setNotification] = useState<Notification | null>(null)
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current)
    }
  }, [])

  const notify = useCallback(
    (type: NotificationType, message: string) => {
      if (timerRef.current) clearTimeout(timerRef.current)
      setNotification({ type, message })
      timerRef.current = setTimeout(() => setNotification(null), timeout)
    },
    [timeout],
  )

  const clearNotification = useCallback(() => {
    if (timerRef.current) clearTimeout(timerRef.current)
    setNotification(null)
  }, [])

  return { notification, notify, clearNotification }
}
