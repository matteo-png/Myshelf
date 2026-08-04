import api from '@/api/axios'
import type { AuthResponse, LoginRequest, MeResponse, RegisterRequest } from '@/types/auth'

export const authService = {
  register(data: RegisterRequest) {
    return api.post('/auth/register', data)
  },

  login(data: LoginRequest) {
    return api.post<AuthResponse>('/auth/login', data)
  },

  me() {
    return api.get<MeResponse>('/auth/me')
  },
}