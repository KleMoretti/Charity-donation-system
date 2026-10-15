<script setup>
import { dateTime, money } from '../utils/display'
defineProps({ records: { type: Array, required: true }, showUser: { type: Boolean, default: false } })
</script>

<template>
  <div class="table-scroll">
    <el-table :data="records" border>
      <el-table-column prop="id" label="记录编号" width="100" />
      <el-table-column v-if="showUser" prop="username" label="用户" min-width="160" show-overflow-tooltip />
      <el-table-column label="公益项目" min-width="230"><template #default="{ row }"><RouterLink class="record-link" :to="`/projects/${row.projectId}`">{{ row.projectTitle }}</RouterLink></template></el-table-column>
      <el-table-column label="模拟金额" width="180"><template #default="{ row }">¥ {{ money(row.amount) }}</template></el-table-column>
      <el-table-column label="结果" width="110"><template #default><el-tag type="success">模拟成功</el-tag></template></el-table-column>
      <el-table-column label="提交时间" width="180"><template #default="{ row }">{{ dateTime(row.createdAt) }}</template></el-table-column>
    </el-table>
  </div>
</template>
