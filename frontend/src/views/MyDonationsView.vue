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
  <span class="eyebrow">MY CONTRIBUTIONS</span><PageHeading title="我的捐赠记录" description="每一次参与，都是值得珍藏的善意。" />
  <p class="records-notice">当前尚未接入真实支付，以下为已提交的参与记录，未发生实际扣款。</p>
  <div class="section-actions"><el-button :loading="loading" @click="load">刷新记录</el-button><RouterLink to="/projects"><el-button type="primary" plain>浏览公益项目</el-button></RouterLink></div>
  <p v-if="loading" role="status">正在加载记录…</p>
  <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />
  <el-empty v-else-if="!records.length" description="你的第一份善意，正等待启程" />
  <DonationTable v-else :records="records" />
</template>
