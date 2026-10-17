<script setup>
import PageHeading from '../components/PageHeading.vue'
defineProps({ title: String, description: String, pending: { type: Boolean, default: true } })
</script>

<template>
  <PageHeading :title="title" :description="description" />
  <el-card shadow="never"><el-empty :description="pending ? '内容正在准备中' : '找不到这个页面'"><RouterLink to="/"><el-button type="primary">返回公益首页</el-button></RouterLink></el-empty></el-card>
</template>
