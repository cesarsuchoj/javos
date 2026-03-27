import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  token: string | null
  refreshToken: string | null
  username: string | null
  name: string | null
  role: string | null
  login: (token: string, refreshToken: string, username: string, name: string, role: string) => void
  setToken: (token: string) => void
  logout: () => void
  isAuthenticated: () => boolean
  isAdmin: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      refreshToken: null,
      username: null,
      name: null,
      role: null,
      login: (token, refreshToken, username, name, role) =>
        set({ token, refreshToken, username, name, role }),
      setToken: (token) => set({ token }),
      logout: () => set({ token: null, refreshToken: null, username: null, name: null, role: null }),
      isAuthenticated: () => !!get().token,
      isAdmin: () => get().role === 'ROLE_ADMIN',
    }),
    {
      name: 'javos-auth',
    },
  ),
)
