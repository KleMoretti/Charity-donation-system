<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import SiteIcon from '../components/SiteIcon.vue'
import { getProject } from '../api/projects'
import { donate } from '../api/donations'
import { authState } from '../state/auth'
import { dateTime, errorMessage, money, progressValue, statusLabel, statusType, validAmount } from '../utils/display'
import { projectIntroduction, projectTheme } from '../utils/projectPresentation'

const route = useRoute()
const project = ref(null)
const loading = ref(false)
const error = ref('')
const theme = computed(() => project.value ? projectTheme(project.value) : null)
const busy = ref(false)
const formRef = ref()
const form = reactive({ amount: '' })
const pending = ref(null)
const receipt = ref(null)
const submitError = ref('')
const pendingKey = computed(() => `charity-donation-pending:${authState.user?.id}:${route.params.id}`)
const rules = { amount: [{ required: true, message: '请输入支持金额', trigger: 'blur' }, {
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
      await ElMessageBox.confirm('当前尚未接入真实支付。确认后仅创建模拟参与记录，不会扣款，也不表示实际资金到账。', '提交前请确认', {
        confirmButtonText: '确认生成记录', cancelButtonText: '暂不提交', type: 'info', closeOnClickModal: false,
      })
      if (operationKey !== pendingKey.value) return
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
    ElMessage.success('参与记录已生成，未发生实际扣款')
    await load()
  } catch (cause) {
    if (cause === 'cancel' || cause === 'close') return
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
  <nav class="breadcrumbs" aria-label="当前位置"><RouterLink to="/projects"><SiteIcon name="back" :size="17" />返回公益项目</RouterLink><span>/</span><span>项目详情</span></nav>
  <p v-if="loading" class="loading-copy" role="status">正在为你加载项目…</p>
  <div v-else-if="error" class="friendly-state"><SiteIcon name="heart" :size="32" /><h1>暂时无法查看这个项目</h1><p>{{ error }}</p><button type="button" class="button button-outline" @click="load">重新加载</button></div>
  <template v-else-if="project">
    <div class="detail-heading"><p class="project-category"><SiteIcon :name="theme.icon" :size="18" />{{ theme.label }}<span class="detail-status" :class="{ ended: project.status === 'CLOSED' }">{{ statusLabel(project.status) }}</span></p><h1>{{ project.title }}</h1><p>每一份关心，都是改变的起点。</p></div>
    <div class="project-detail-layout">
      <div class="project-story">
        <figure class="detail-cover"><img :src="theme.cover" alt="公益主题配图，非项目现场照片" width="800" height="500" /><figcaption>公益主题配图</figcaption></figure>
        <section class="story-content"><span class="eyebrow">ABOUT THIS PROJECT</span><h2>关于这个项目</h2><p class="project-description">{{ projectIntroduction(project) }}</p><p class="story-note">请认真了解项目说明，选择适合自己的参与方式。你的每一次支持，都可以在个人记录中找到。</p><div class="detail-facts"><span><SiteIcon name="book" :size="19" />项目目标清晰</span><span><SiteIcon name="eye" :size="19" />参与记录可查</span><span><SiteIcon name="heart" :size="19" />量力而行参与</span></div></section>
      </div>
      <aside class="support-panel" aria-labelledby="support-title">
        <span class="eyebrow">让关心向前一步</span><h2 id="support-title">{{ project.status === 'ACTIVE' ? '支持这个项目' : '感谢每一份关心' }}</h2>
        <div class="support-total"><span>已记录的支持</span><strong>¥ {{ money(project.raisedAmount) }}</strong></div>
        <el-progress :percentage="progressValue(project)" :show-text="false" :stroke-width="6" />
        <div class="support-progress-label"><span>目标 ¥ {{ money(project.targetAmount) }}</span><strong>{{ project.progressPercent }}%</strong></div>
        <div class="support-participation"><SiteIcon name="people" :size="19" /><span>{{ project.donationCount }} 次参与，让善意在这里汇聚</span></div>
        <div v-if="receipt" class="donation-feedback" role="status"><el-alert :title="'记录 ' + receipt.id + ' 已生成，未发生实际扣款'" type="success" :closable="false" show-icon /><RouterLink class="record-link" to="/my-donations">查看我的参与记录<SiteIcon name="arrow" :size="16" /></RouterLink></div>
        <el-alert v-if="submitError" class="donation-feedback" :title="submitError" type="error" :closable="false" show-icon />
        <template v-if="project.status === 'ACTIVE' || pending">
          <div v-if="!authState.user" class="guest-support"><p>登录后，为你关心的项目送上一份支持。</p><RouterLink class="button button-primary full-width" :to="{ path: '/login', query: { redirect: route.fullPath } }">登录后参与<SiteIcon name="arrow" :size="19" /></RouterLink></div>
          <el-form v-else ref="formRef" :model="form" :rules="rules" label-position="top" class="donation-form" @submit.prevent="submit">
            <el-alert v-if="pending && !busy" title="上次提交结果尚未确认，请保留原金额，重试同一笔参与。" type="warning" :closable="false" show-icon />
            <div class="amount-presets" role="group" aria-label="选择支持金额"><button v-for="value in ['10.00', '30.00', '50.00', '100.00']" :key="value" type="button" :disabled="busy || !!pending" :aria-pressed="form.amount === value" :class="{ selected: form.amount === value }" @click="form.amount = value">¥ {{ value.split('.')[0] }}</button></div>
            <el-form-item label="支持金额（元）" prop="amount"><el-input v-model="form.amount" inputmode="decimal" :disabled="busy || !!pending" placeholder="输入其他金额" /></el-form-item>
            <el-button type="primary" native-type="submit" :loading="busy" class="full-width">{{ busy ? '正在处理' : pending ? '重试本次参与' : '支持这个项目' }}<SiteIcon v-if="!busy" name="heart" :size="18" /></el-button>
            <p class="support-fineprint">提交前将展示支付状态与记录说明，请确认后再参与。</p>
          </el-form>
        </template>
        <div v-else class="closed-support"><SiteIcon name="heart" :size="24" /><strong>这段善意，已留下温暖的足迹。</strong><p>项目已结束，感谢每一次关注与参与。</p><RouterLink to="/projects" class="button button-outline full-width">看看其他项目<SiteIcon name="arrow" :size="18" /></RouterLink></div>
        <div class="support-date">项目创建于 {{ dateTime(project.createdAt) }}</div>
      </aside>
    </div>
  </template>
</template>
