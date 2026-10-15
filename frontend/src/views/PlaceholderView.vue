<script setup>
import PageHeading from '../components/PageHeading.vue'
defineProps({ title: String, description: String, pending: { type: Boolean, default: true } })
</script>

<template>
  <PageHeading :title="title" :description="description" />
  <el-card shadow="never"><el-empty :description="pending ? '页面框架已建立，功能待实现' : '找不到这个页面'"><RouterLink to="/"><el-button type="primary">返回工程概览</el-button></RouterLink></el-empty></el-card>
</template>
