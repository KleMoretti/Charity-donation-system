import { reactive } from 'vue'
import * as authApi from '../api/auth'
import { resetCsrfToken } from '../api/http'

export const authState = reactive({ user: null, initialized: false })
let sessionRequest

export async function ensureSession() {
  if (authState.initialized) return
  if (!sessionRequest) {
    sessionRequest = authApi.getCurrentUser().then((user) => {
      authState.user = user
      authState.initialized = true
    }).catch((error) => {
      if (error.response?.status !== 401) throw error
      authState.user = null
      authState.initialized = true
    }).finally(() => { sessionRequest = undefined })
  }
  return sessionRequest
}

export async function signIn(input) {
  const user = await authApi.login(input)
  resetCsrfToken()
  authState.user = user
  authState.initialized = true
}

export async function signOut() {
  await authApi.logout()
  resetCsrfToken()
  authState.user = null
  authState.initialized = true
}

window.addEventListener('auth:expired', () => {
  authState.user = null
  authState.initialized = true
})
