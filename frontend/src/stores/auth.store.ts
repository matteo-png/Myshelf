import { defineStore } from 'pinia'
import { authService } from '@/services/auth.service'
import type { LoginRequest, RegisterRequest } from '@/types/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') as string | null,
    displayName: null as string | null,
    loading: false,
    initialized : false
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
  },

  actions: {
    async register(data: RegisterRequest) {
      this.loading = true
      try {
        await authService.register(data)
      } finally {
        this.loading = false
      }
    },

    async login(data: LoginRequest) {
      this.loading = true
      try {
        const response = await authService.login(data)

        this.token = response.data.token
        localStorage.setItem('token', response.data.token)

        await this.fetchMe()
      } finally {
        this.loading = false
      }
    },

    async fetchMe() {
      const response = await authService.me()
      this.displayName = response.data.displayName
    },

    logout() {
      this.token = null
      this.displayName = null
      localStorage.removeItem('token')
    },

    async initialize() {
    try {
      if (this.token) {
        await this.fetchMe()
      }
    } catch {
      this.logout()
    } finally {
      this.initialized = true
    }
  }

  },
})