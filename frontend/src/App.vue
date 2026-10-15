<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authState, signOut } from './state/auth'
import { errorMessage } from './utils/display'

const router = useRouter()
const signingOut = ref(false)
async function logout() {
  signingOut.value = true
  try { await signOut(); await router.push('/projects'); ElMessage.success('已退出登录') }
  catch (error) { ElMessage.error(errorMessage(error)) }
  finally { signingOut.value = false }
}
</script>

<template>
  <div class="app-shell">
    <header class="site-header">
      <RouterLink to="/" class="brand">善意同行<span>慈善捐款系统</span></RouterLink>
      <nav aria-label="主导航">
        <RouterLink to="/">工程概览</RouterLink>
        <RouterLink to="/projects">公益项目</RouterLink>
        <template v-if="!authState.user"><RouterLink to="/login">登录</RouterLink><RouterLink to="/register">注册</RouterLink></template>
        <RouterLink v-if="authState.user?.role === 'ADMIN'" to="/admin/projects">管理后台</RouterLink>
        <RouterLink v-if="authState.user" to="/my-donations">我的记录</RouterLink>
        <template v-if="authState.user"><span class="nav-user">{{ authState.user.username }}</span><el-button :loading="signingOut" @click="logout">退出登录</el-button></template>
      </nav>
    </header>
    <main class="main-content"><RouterView /></main>
    <footer>本地教学演示 · 仅模拟捐款，不涉及实际资金到账</footer>
  </div>
</template>
