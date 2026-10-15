import http from './http'

export const donate = (input) => http.post('/donations', input).then(({ data }) => data.data)
export const listMyDonations = () => http.get('/donations/mine').then(({ data }) => data.data)
export const listAdminDonations = () => http.get('/admin/donations').then(({ data }) => data.data)
export const getStats = () => http.get('/admin/stats').then(({ data }) => data.data)
