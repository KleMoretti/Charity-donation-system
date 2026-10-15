<script setup>
import { onMounted, ref } from 'vue'
import PageHeading from '../components/PageHeading.vue'
import { getHealth } from '../api/system'

const loading = ref(false)
const health = ref(null)
const errorMessage = ref('')

async function checkConnection() {
  loading.value = true
  health.value = null
  errorMessage.value = ''
  try { health.value = await getHealth() }
  catch (error) { errorMessage.value = error.response?.data?.message || '暂时无法连接后端，请检查服务是否启动及数据库环境变量是否配置。' }
  finally { loading.value = false }
}

onMounted(checkConnection)
</script>

<template>
  <PageHeading title="让每一份善意有迹可循" description="从项目发布到模拟捐款与记录查询，构建一个简单、清晰的公益演示流程。" />
  <el-alert title="项目发布、模拟捐款、记录查询与统计已接通，仅用于教学演示。" type="info" :closable="false" show-icon />
  <div class="section-actions"><RouterLink to="/projects"><el-button type="primary">浏览公益项目</el-button></RouterLink></div>
  <section class="overview-grid">
    <el-card shadow="never">
      <template #header><div class="card-header"><h2>服务连接</h2><el-button :loading="loading" @click="checkConnection">重新检查</el-button></div></template>
      <p v-if="loading" role="status">正在检查后端与数据库连接…</p>
      <el-alert v-else-if="errorMessage" :title="errorMessage" type="warning" :closable="false" show-icon />
      <el-descriptions v-else-if="health" :column="1" border>
        <el-descriptions-item label="后端服务"><el-tag type="success">{{ health.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="数据库"><el-tag type="success">{{ health.database }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="当前阶段">{{ health.stage }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card shadow="never">
      <template #header><h2>演示流程</h2></template>
      <ol class="flow-list"><li>管理员创建并发布公益项目</li><li>用户浏览项目并提交模拟捐款</li><li>用户与管理员查询捐款记录</li><li>管理员查看项目数、捐款笔数和总额</li></ol>
      <p class="muted">登录后可提交模拟捐款并查询个人记录，管理员可查看全部记录与基础统计。</p>
    </el-card>
  </section>
</template>
