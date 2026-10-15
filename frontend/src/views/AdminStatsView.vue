<script setup>
import { onMounted, ref } from 'vue'
import AdminNav from '../components/AdminNav.vue'
import PageHeading from '../components/PageHeading.vue'
import { getStats } from '../api/donations'
import { errorMessage, money } from '../utils/display'

const stats = ref(null)
const loading = ref(false)
const error = ref('')
async function load() {
  loading.value = true; error.value = ''
  try { stats.value = await getStats() }
  catch (cause) { error.value = errorMessage(cause) }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <AdminNav />
  <PageHeading title="基础统计" description="金额与笔数由捐款记录实时汇总，全部为教学演示中的模拟数据。" />
  <div class="section-actions"><el-button :loading="loading" @click="load">刷新统计</el-button></div>
  <p v-if="loading" role="status">正在加载统计…</p>
  <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
  <div v-else-if="stats" class="stats-grid">
    <el-card shadow="never"><h2>项目数</h2><p class="stat-value" data-stat="projects">{{ stats.projectCount }}</p><p class="muted">包含草稿、募集中和已结束项目</p></el-card>
    <el-card shadow="never"><h2>捐款笔数</h2><p class="stat-value" data-stat="donations">{{ stats.donationCount }}</p><p class="muted">成功生成的模拟记录</p></el-card>
    <el-card shadow="never"><h2>模拟捐款总金额</h2><p class="stat-value" data-stat="amount">¥ {{ money(stats.totalAmount) }}</p><p class="muted">不代表实际资金到账</p></el-card>
  </div>
</template>
