<script setup>
import { onMounted, ref } from 'vue'
import PageHeading from '../components/PageHeading.vue'
import { listProjects } from '../api/projects'
import { dateTime, errorMessage, money, progressValue, statusLabel, statusType } from '../utils/display'

const projects = ref([])
const loading = ref(false)
const error = ref('')
async function load() {
  loading.value = true; error.value = ''
  try { projects.value = await listProjects() }
  catch (cause) { error.value = errorMessage(cause) }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <PageHeading title="公益项目" description="浏览募集中和已结束的项目，了解每一份善意的去向。" />
  <div class="section-actions"><el-button :loading="loading" @click="load">刷新项目</el-button></div>
  <p v-if="loading" role="status">正在加载项目…</p>
  <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
  <el-empty v-else-if="!projects.length" description="暂无公开项目，请稍后再来" />
  <div v-else class="project-grid">
    <el-card v-for="project in projects" :key="project.id" shadow="never">
      <div class="card-header"><h2>{{ project.title }}</h2><el-tag :type="statusType(project.status)">{{ statusLabel(project.status) }}</el-tag></div>
      <p class="project-excerpt">{{ project.description }}</p>
      <p>目标金额 <strong>¥ {{ money(project.targetAmount) }}</strong></p>
      <p>已模拟募集 <strong>¥ {{ money(project.raisedAmount) }}</strong> · {{ project.donationCount }} 笔</p>
      <el-progress :percentage="progressValue(project)" :format="() => `${project.progressPercent}%`" />
      <p class="muted">创建于 {{ dateTime(project.createdAt) }}</p>
      <RouterLink :to="`/projects/${project.id}`"><el-button type="primary" plain>查看详情</el-button></RouterLink>
    </el-card>
  </div>
</template>
