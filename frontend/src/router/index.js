import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authState, ensureSession } from '../state/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('../views/HomeView.vue') },
    { path: '/projects', component: () => import('../views/ProjectsView.vue') },
    { path: '/projects/:id', component: () => import('../views/ProjectDetailView.vue') },
    { path: '/login', component: () => import('../views/LoginView.vue'), meta: { guestOnly: true } },
    { path: '/register', component: () => import('../views/RegisterView.vue'), meta: { guestOnly: true } },
    { path: '/my-donations', component: () => import('../views/MyDonationsView.vue'), meta: { requiresAuth: true } },
    { path: '/admin', redirect: '/admin/projects' },
    { path: '/admin/projects', component: () => import('../views/AdminProjectsView.vue'), meta: { requiresAuth: true, adminOnly: true } },
    { path: '/admin/donations', component: () => import('../views/AdminDonationsView.vue'), meta: { requiresAuth: true, adminOnly: true } },
    { path: '/admin/stats', component: () => import('../views/AdminStatsView.vue'), meta: { requiresAuth: true, adminOnly: true } },
    { path: '/:pathMatch(.*)*', component: () => import('../views/PlaceholderView.vue'), props: { title: '页面不存在', description: '请返回公益首页，或发现其他值得关注的项目。', pending: false } },
  ],
  scrollBehavior: (to) => to.hash ? { el: to.hash, top: 112, behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' } : { top: 0 },
})

router.beforeEach(async (to) => {
  try { await ensureSession() }
  catch { if (to.meta.requiresAuth) return { path: '/login', query: { redirect: to.fullPath } } }
  if (to.meta.requiresAuth && !authState.user) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.meta.adminOnly && authState.user?.role !== 'ADMIN') {
    ElMessage.error('只有管理员可以访问管理后台')
    return '/projects'
  }
  if (to.meta.guestOnly && authState.user) return authState.user.role === 'ADMIN' ? '/admin/projects' : '/projects'
})

window.addEventListener('auth:expired', () => {
  const route = router.currentRoute.value
  if (route.meta.requiresAuth) router.replace({ path: '/login', query: { redirect: route.fullPath } })
})

export default router
