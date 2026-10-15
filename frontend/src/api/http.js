import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 10000, withCredentials: true })
let csrfToken
let csrfRequest

export function resetCsrfToken() {
  // 登录、退出或会话失效后重新获取令牌。
  csrfToken = undefined
}

export async function loadCsrfToken() {
  if (!csrfRequest) {
    csrfRequest = http.get('/auth/csrf').then(({ data }) => {
      csrfToken = data.data
      return csrfToken
    }).finally(() => { csrfRequest = undefined })
  }
  return csrfRequest
}

http.interceptors.request.use(async (config) => {
  if (!['get', 'head', 'options'].includes((config.method || 'get').toLowerCase())) {
    const token = csrfToken || await loadCsrfToken()
    config.headers[token.headerName] = token.token
  }
  return config
})

http.interceptors.response.use((response) => response, (error) => {
  if ([401, 403].includes(error.response?.status)) resetCsrfToken()
  if (error.response?.status === 401 && error.config?.url !== '/auth/login') {
    window.dispatchEvent(new Event('auth:expired'))
  }
  // 不自动重试写请求，交由页面明确处理失败。
  return Promise.reject(error)
})

export default http
