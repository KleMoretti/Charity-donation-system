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
  <PageHeading title="全部参与记录" description="集中查看每一个项目的支持情况与参与明细。" />
  <p class="records-notice">记录金额不代表实际到账，当前未接入支付通道。</p>
  <div class="section-actions"><el-button :loading="loading" @click="load">刷新记录</el-button></div>
  <p v-if="loading" role="status">正在加载记录…</p>
  <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
  <el-empty v-else-if="!records.length" description="暂时没有参与记录" />
  <DonationTable v-else :records="records" show-user />
</template>
