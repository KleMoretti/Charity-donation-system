<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeading from '../components/PageHeading.vue'
import { authState, signIn } from '../state/auth'
import { errorMessage } from '../utils/display'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const busy = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { max: 50, message: '用户名最多50个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { validator: (_, value, callback) => callback(new TextEncoder().encode(value).length > 72 ? new Error('密码UTF-8最多72字节') : undefined), trigger: 'blur' }],
}
async function submit() {
  if (busy.value || !await formRef.value.validate().catch(() => false)) return
  busy.value = true
  try {
    await signIn({ username: form.username.trim(), password: form.password })
    form.password = ''
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    const target = typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')
      ? redirect : authState.user.role === 'ADMIN' ? '/admin/projects' : '/projects'
    await router.replace(target)
  } catch (error) { ElMessage.error(errorMessage(error)) }
  finally { busy.value = false }
}
</script>

<template>
  <div class="auth-panel">
    <PageHeading title="用户登录" description="登录后可使用对应角色的功能。" />
    <el-card shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="busy" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" autocomplete="username" maxlength="50" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" maxlength="72" /></el-form-item>
        <el-button type="primary" native-type="submit" :loading="busy" class="full-width">登录</el-button>
      </el-form>
      <p class="form-link">还没有账号？<RouterLink to="/register">注册普通用户</RouterLink></p>
    </el-card>
  </div>
</template>
