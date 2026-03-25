import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  token: string | null
  username: string | null
  name: string | null
  role: string | null
  login: (token: string, username: string, name: string, role: string) => void
  logout: () => void
  isAuthenticated: () => boolean
  isAdmin: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      username: null,
      name: null,
      role: null,
      login: (token, username, name, role) =>
        set({ token, username, name, role }),
      logout: () => set({ token: null, username: null, name: null, role: null }),
      isAuthenticated: () => !!get().token,
      isAdmin: () => get().role === 'ROLE_ADMIN',
    }),
    {
      name: 'javos-auth',
    },
  ),
)
