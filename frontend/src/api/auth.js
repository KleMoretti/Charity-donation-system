import http from './http'

export const register = (input) => http.post('/auth/register', input).then(({ data }) => data.data)
export const login = (input) => http.post('/auth/login', input).then(({ data }) => data.data)
export const logout = () => http.post('/auth/logout')
export const getCurrentUser = () => http.get('/auth/me').then(({ data }) => data.data)
