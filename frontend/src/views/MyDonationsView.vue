<script setup>
import { onMounted, ref } from 'vue'
import PageHeading from '../components/PageHeading.vue'
import DonationTable from '../components/DonationTable.vue'
import { listMyDonations } from '../api/donations'
import { errorMessage } from '../utils/display'

const records = ref([])
const loading = ref(false)
const error = ref('')
async function load() {
  loading.value = true; error.value = ''
  try { records.value = await listMyDonations() }
  catch (cause) { error.value = errorMessage(cause) }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <PageHeading title="我的捐款记录" description="仅展示当前账号提交的模拟成功记录，不涉及实际资金到账。" />
  <div class="section-actions"><el-button :loading="loading" @click="load">刷新记录</el-button><RouterLink to="/projects"><el-button type="primary" plain>浏览公益项目</el-button></RouterLink></div>
  <p v-if="loading" role="status">正在加载记录…</p>
  <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
  <el-empty v-else-if="!records.length" description="还没有模拟捐款记录" />
  <DonationTable v-else :records="records" />
</template>
