import http from './http'

export const listProjects = () => http.get('/projects').then(({ data }) => data.data)
export const getProject = (id) => http.get(`/projects/${id}`).then(({ data }) => data.data)
export const listAdminProjects = () => http.get('/admin/projects').then(({ data }) => data.data)
export const createProject = (input) => http.post('/admin/projects', input).then(({ data }) => data.data)
export const editProject = (id, input) => http.put(`/admin/projects/${id}`, input).then(({ data }) => data.data)
export const publishProject = (id) => http.post(`/admin/projects/${id}/publish`).then(({ data }) => data.data)
export const closeProject = (id) => http.post(`/admin/projects/${id}/close`).then(({ data }) => data.data)
