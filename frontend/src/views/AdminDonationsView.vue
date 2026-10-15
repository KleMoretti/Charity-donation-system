<script setup>
import { onMounted, ref } from 'vue'
import AdminNav from '../components/AdminNav.vue'
import PageHeading from '../components/PageHeading.vue'
import DonationTable from '../components/DonationTable.vue'
import { listAdminDonations } from '../api/donations'
import { errorMessage } from '../utils/display'

const records = ref([])
const loading = ref(false)
const error = ref('')
async function load() {
  loading.value = true; error.value = ''
  try { records.value = await listAdminDonations() }
  catch (cause) { error.value = errorMessage(cause) }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <AdminNav />
  <PageHeading title="全部捐款记录" description="查询所有账号的模拟捐款记录，历史记录不提供修改或删除入口。" />
  <div class="section-actions"><el-button :loading="loading" @click="load">刷新记录</el-button></div>
  <p v-if="loading" role="status">正在加载记录…</p>
  <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
  <el-empty v-else-if="!records.length" description="暂无模拟捐款记录" />
  <DonationTable v-else :records="records" show-user />
</template>
