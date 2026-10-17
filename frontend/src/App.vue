<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SiteIcon from './components/SiteIcon.vue'
import { authState, signOut } from './state/auth'
import { errorMessage } from './utils/display'

const router = useRouter()
const route = useRoute()
const signingOut = ref(false)
const menuOpen = ref(false)
const menuButton = ref()
const isAdmin = computed(() => route.path.startsWith('/admin'))
watch(() => route.fullPath, () => { menuOpen.value = false })
function closeMenu() { menuOpen.value = false; menuButton.value?.focus() }
async function logout() {
  signingOut.value = true
  try { await signOut(); await router.push('/projects'); ElMessage.success('已退出登录') }
  catch (error) { ElMessage.error(errorMessage(error)) }
  finally { signingOut.value = false }
}
</script>

<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <header class="site-header" @keyup.esc="closeMenu">
      <div class="header-inner">
        <RouterLink to="/" class="brand" aria-label="善意同行，返回首页">
          <span class="brand-mark"><SiteIcon name="heart" :size="25" /></span>
          <span class="brand-wordmark">善意同行<small>TOGETHER, FOR GOOD</small></span>
        </RouterLink>
        <nav id="site-navigation" class="site-nav" :class="{ 'is-open': menuOpen }" aria-label="主导航">
          <RouterLink to="/" :class="{ 'is-active': route.path === '/' && !route.hash }">公益首页</RouterLink>
          <RouterLink to="/projects" :class="{ 'is-active': route.path.startsWith('/projects') }">发现项目</RouterLink>
          <RouterLink to="/#our-belief" :class="{ 'is-active': route.hash === '#our-belief' }">我们的初心</RouterLink>
          <RouterLink v-if="authState.user" to="/my-donations" :class="{ 'is-active': route.path === '/my-donations' }">我的捐赠</RouterLink>
          <RouterLink v-if="!authState.user" to="/login" class="mobile-only-link">登录账号</RouterLink>
          <RouterLink v-if="authState.user?.role === 'ADMIN'" to="/admin/projects" class="mobile-only-link">管理中心</RouterLink>
        </nav>
        <div class="header-actions">
          <template v-if="!authState.user">
            <RouterLink to="/login" class="header-login">登录</RouterLink>
            <RouterLink to="/register" class="button button-small button-primary">加入善意<SiteIcon name="arrow" :size="16" /></RouterLink>
          </template>
          <template v-else>
            <RouterLink v-if="authState.user.role === 'ADMIN'" to="/admin/projects" class="header-login" :class="{ 'is-active': isAdmin }">管理中心</RouterLink>
            <span class="nav-user" :title="authState.user.username">{{ authState.user.username }}</span>
            <el-button text :loading="signingOut" @click="logout">退出</el-button>
          </template>
          <button ref="menuButton" class="menu-toggle" type="button" :aria-expanded="menuOpen" aria-controls="site-navigation" :aria-label="menuOpen ? '收起导航' : '展开导航'" @click="menuOpen = !menuOpen"><SiteIcon :name="menuOpen ? 'close' : 'menu'" /></button>
        </div>
      </div>
    </header>
    <main id="main-content" class="main-content" :class="{ 'admin-layout': isAdmin }" tabindex="-1"><RouterView /></main>
    <footer class="site-footer">
      <div class="footer-inner">
        <div><RouterLink to="/" class="footer-brand">善意同行<span class="brand-dot"></span></RouterLink><p>让关心成为行动，让善意走得更远。</p></div>
        <div class="footer-links"><RouterLink to="/projects">发现公益项目</RouterLink><RouterLink to="/#our-belief">我们的初心</RouterLink><RouterLink :to="authState.user ? '/my-donations' : '/login'">我的参与记录</RouterLink></div>
      </div>
      <div class="footer-bottom"><span>© {{ new Date().getFullYear() }} 善意同行</span><span>每一份善意，都值得被认真对待。</span></div>
    </footer>
  </div>
</template>
