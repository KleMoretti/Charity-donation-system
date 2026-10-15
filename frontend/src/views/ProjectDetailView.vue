<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeading from '../components/PageHeading.vue'
import { getProject } from '../api/projects'
import { donate } from '../api/donations'
import { authState } from '../state/auth'
import { dateTime, errorMessage, money, progressValue, statusLabel, statusType, validAmount } from '../utils/display'

const route = useRoute()
const project = ref(null)
const loading = ref(false)
const error = ref('')
const busy = ref(false)
const formRef = ref()
const form = reactive({ amount: '' })
const pending = ref(null)
const receipt = ref(null)
const submitError = ref('')
const pendingKey = computed(() => `charity-donation-pending:${authState.user?.id}:${route.params.id}`)
const rules = { amount: [{ required: true, message: '请输入模拟捐款金额', trigger: 'blur' }, {
  validator: (_, value, callback) => callback(validAmount(value) ? undefined : new Error('金额必须大于零，最多10位整数和2位小数')), trigger: 'blur',
}] }

function restorePending() {
  pending.value = null
  if (!authState.user) return
  try {
    const saved = JSON.parse(sessionStorage.getItem(pendingKey.value))
    if (saved && saved.projectId === Number(route.params.id) && validAmount(saved.amount)
        && typeof saved.requestId === 'string' && /^[a-zA-Z0-9_-]{1,64}$/.test(saved.requestId)) {
      pending.value = saved; form.amount = saved.amount
    }
  } catch { /* 无有效缓存时按新提交处理。 */ }
}

async function submit() {
  if (busy.value) return
  const operationKey = pendingKey.value
  busy.value = true
  submitError.value = ''
  try {
    if (!await formRef.value.validate().catch(() => false)) return
    if (operationKey !== pendingKey.value) return
    if (!pending.value) {
      const [whole, decimal = ''] = form.amount.split('.')
      const input = { projectId: project.value.id, amount: `${whole}.${decimal.padEnd(2, '0')}`, requestId: crypto.randomUUID() }
      // 保存成功后才发送；结果不确定时，刷新和重试都复用同一个请求。
      try { sessionStorage.setItem(operationKey, JSON.stringify(input)) }
      catch { submitError.value = '浏览器无法保存提交状态，请允许本站会话存储后重试'; return }
      pending.value = input
    }
    const result = await donate({ ...pending.value })
    sessionStorage.removeItem(operationKey)
    if (operationKey !== pendingKey.value) return
    pending.value = null; form.amount = ''; receipt.value = result
    ElMessage.success('模拟捐款成功，不涉及实际资金到账')
    await load()
  } catch (cause) {
    if (cause.response?.status >= 400 && cause.response.status < 500) {
      sessionStorage.removeItem(operationKey)
      if (operationKey === pendingKey.value) pending.value = null
    }
    if (operationKey === pendingKey.value) submitError.value = errorMessage(cause)
  } finally { busy.value = false }
}
let sequence = 0
async function load() {
  const current = ++sequence
  loading.value = true; error.value = ''; project.value = null
  try { const result = await getProject(route.params.id); if (current === sequence) project.value = result }
  catch (cause) { if (current === sequence) error.value = errorMessage(cause) }
  finally { if (current === sequence) loading.value = false }
}
watch(() => route.params.id, () => {
  form.amount = ''; receipt.value = null; submitError.value = ''
  restorePending(); load()
}, { immediate: true })
</script>

<template>
  <div class="section-actions"><RouterLink to="/projects"><el-button>返回项目列表</el-button></RouterLink></div>
  <p v-if="loading" role="status">正在加载项目详情…</p>
  <template v-else-if="error"><el-alert :title="error" type="error" :closable="false" show-icon /><div class="section-actions"><el-button @click="load">重新加载</el-button></div></template>
  <template v-else-if="project">
    <PageHeading :title="project.title" />
    <el-card shadow="never">
      <div class="card-header"><el-tag :type="statusType(project.status)">{{ statusLabel(project.status) }}</el-tag><span class="muted">创建于 {{ dateTime(project.createdAt) }}</span></div>
      <p class="target-amount">目标金额 <strong>¥ {{ money(project.targetAmount) }}</strong></p>
      <p>已模拟募集 <strong>¥ {{ money(project.raisedAmount) }}</strong> · {{ project.donationCount }} 笔</p>
      <el-progress :percentage="progressValue(project)" :format="() => `${project.progressPercent}%`" />
      <h2>项目说明</h2><p class="project-description">{{ project.description }}</p>
      <el-alert title="仅模拟捐款，不会扣款，不表示实际资金到账。" type="info" :closable="false" show-icon />
      <div v-if="receipt" class="donation-feedback" role="status"><el-alert :title="`模拟成功：记录编号 ${receipt.id}，金额 ¥ ${money(receipt.amount)}`" type="success" :closable="false" show-icon /><RouterLink class="record-link" to="/my-donations">查看我的捐款记录</RouterLink></div>
      <el-alert v-if="submitError" class="donation-feedback" :title="submitError" type="error" :closable="false" show-icon />
      <template v-if="project.status === 'ACTIVE' || pending">
        <div v-if="!authState.user" class="section-actions"><RouterLink :to="{ path: '/login', query: { redirect: route.fullPath } }"><el-button type="primary">登录后模拟捐款</el-button></RouterLink></div>
        <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top" class="donation-form" @submit.prevent="submit">
          <el-alert v-if="pending" title="上次提交结果尚未确认，请重试同一笔以确认结果；确认前保留原金额。" type="warning" :closable="false" show-icon />
          <el-form-item label="模拟捐款金额（元）" prop="amount"><el-input v-model="form.amount" inputmode="decimal" :disabled="busy || !!pending" placeholder="例如 10.00" /></el-form-item>
          <el-button type="primary" native-type="submit" :loading="busy">{{ pending ? '重试本次模拟捐款' : '提交模拟捐款' }}</el-button>
        </el-form>
      </template>
      <p v-else class="muted">该项目已结束，不能接受新的模拟捐款。</p>
    </el-card>
  </template>
</template>
