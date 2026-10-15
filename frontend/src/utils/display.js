export const errorMessage = (error) => error.response?.data?.message || '暂时无法连接服务，请稍后重试'
export const statusLabel = (status) => ({ DRAFT: '草稿', ACTIVE: '募集中', CLOSED: '已结束' }[status] || status)
export const statusType = (status) => ({ DRAFT: 'warning', ACTIVE: 'success', CLOSED: 'info' }[status] || 'info')
export function money(value) {
  const [whole, decimal = ''] = String(value).split('.')
  return `${whole.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}.${decimal.padEnd(2, '0')}`
}
export const dateTime = (value) => value?.replace('T', ' ').slice(0, 16) || '—'
export const progressValue = (project) => Math.min(100, Math.max(0, Number(project.progressPercent)))
export function validPassword(value) {
  return value.length >= 8 && value.length <= 72 && new TextEncoder().encode(value).length <= 72
}
export function validAmount(value) {
  return /^(?:0|[1-9]\d{0,9})(?:\.\d{1,2})?$/.test(value) && Number(value) > 0
}
