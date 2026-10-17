<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeading from '../components/PageHeading.vue'
import { register } from '../api/auth'
import { errorMessage, validPassword } from '../utils/display'

const router = useRouter()
const formRef = ref()
const busy = ref(false)
const form = reactive({ username: '', password: '', confirmPassword: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { pattern: /^[a-zA-Z0-9_]{3,50}$/, message: '用户名需为3至50位字母、数字或下划线', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { validator: (_, value, callback) => callback(validPassword(value) ? undefined : new Error('密码至少8位，UTF-8最多72字节')), trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: (_, value, callback) => callback(value === form.password ? undefined : new Error('两次密码不一致')), trigger: 'blur' }],
}
async function submit() {
  if (busy.value || !await formRef.value.validate().catch(() => false)) return
  busy.value = true
  try {
    await register({ username: form.username, password: form.password })
    form.password = ''; form.confirmPassword = ''
    ElMessage.success('注册成功，请登录')
    await router.replace('/login')
  } catch (error) { ElMessage.error(errorMessage(error)) }
  finally { busy.value = false }
}
</script>

<template>
  <div class="auth-panel">
    <span class="eyebrow">A LITTLE KINDNESS, A NEW BEGINNING</span><PageHeading title="从一份关心开始" description="加入善意同行，找到你愿意支持的美好。" />
    <el-card shadow="never">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="busy" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" autocomplete="username" maxlength="50" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password autocomplete="new-password" maxlength="72" /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" maxlength="72" /></el-form-item>
        <el-button type="primary" native-type="submit" :loading="busy" class="full-width">注册</el-button>
      </el-form>
      <p class="form-link">已有账号？<RouterLink to="/login">前往登录</RouterLink></p>
    </el-card>
  </div>
</template>
