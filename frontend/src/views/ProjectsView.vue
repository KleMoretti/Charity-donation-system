<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listProjects } from '../api/projects'
import ProjectCard from '../components/ProjectCard.vue'
import SiteIcon from '../components/SiteIcon.vue'

const route = useRoute()
const router = useRouter()
const projects = ref([])
const loading = ref(true)
const error = ref(false)
const keyword = ref(String(route.query.keyword || ''))
const selected = ref(['all', 'closed'].includes(route.query.status) ? route.query.status : 'active')
const tabs = [{ key: 'active', label: '正在募集中' }, { key: 'all', label: '全部项目' }, { key: 'closed', label: '已结束' }]
const activeCount = computed(() => projects.value.filter(project => project.status === 'ACTIVE').length)
const visible = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  return [...projects.value].filter(project => (selected.value === 'all' || project.status === (selected.value === 'active' ? 'ACTIVE' : 'CLOSED'))
    && (!query || (project.title + ' ' + project.description).toLowerCase().includes(query)))
    .sort((a, b) => Number(b.status === 'ACTIVE') - Number(a.status === 'ACTIVE'))
})
watch(() => route.query, query => {
  keyword.value = String(query.keyword || '')
  selected.value = ['all', 'closed'].includes(query.status) ? query.status : 'active'
})
function search() { router.replace({ path: '/projects', query: { status: selected.value, ...(keyword.value.trim() ? { keyword: keyword.value.trim() } : {}) } }) }
function selectTab(key) { selected.value = key; search() }
function reset() { keyword.value = ''; selected.value = 'all'; search() }
async function load() {
  loading.value = true; error.value = false
  try { projects.value = await listProjects() }
  catch { error.value = true }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <section class="discovery-heading">
    <div><span class="eyebrow">DISCOVER GOODNESS / 发现善意</span><h1>让关心，<em>有处可去。</em></h1><p>找到与你共鸣的公益项目，为你在意的美好向前一步。</p></div>
    <div v-if="!loading && !error" class="discovery-count"><strong>{{ activeCount }}<span>项</span></strong><p>公益项目正在募集中</p></div>
  </section>
  <section class="project-discovery" aria-label="筛选与浏览项目">
    <div class="project-toolbar">
      <div class="filter-tabs" role="group" aria-label="项目状态"><button v-for="tab in tabs" :key="tab.key" type="button" :aria-pressed="selected === tab.key" :class="{ active: selected === tab.key }" @click="selectTab(tab.key)">{{ tab.label }}<span v-if="tab.key === 'active' && !loading">{{ activeCount }}</span></button></div>
      <form class="project-search" role="search" @submit.prevent="search"><label class="sr-only" for="project-search">搜索公益项目</label><SiteIcon name="search" :size="19" /><input id="project-search" v-model="keyword" type="search" placeholder="寻找你关心的项目" /><button type="submit" aria-label="搜索项目"><SiteIcon name="arrow" :size="19" /></button></form>
    </div>
    <div class="results-caption"><p v-if="!loading && !error">共 <strong>{{ visible.length }}</strong> 个项目<span v-if="keyword.trim()"> · “{{ keyword.trim() }}”</span></p><p v-else>一份关心，等待一次相遇。</p><span>每一份心意，都有意义</span></div>
    <div v-if="loading" class="project-skeleton-grid" aria-busy="true" aria-label="正在加载项目"><div v-for="i in 3" :key="i" class="project-skeleton"></div></div>
    <div v-else-if="error" class="friendly-state"><SiteIcon name="heart" :size="32" /><h2>暂时没能加载项目</h2><p>请稍后再试，善意不会缺席。</p><button type="button" class="button button-outline" @click="load">重新加载</button></div>
    <div v-else-if="!visible.length" class="friendly-state"><SiteIcon name="search" :size="32" /><h2>还没有找到合适的项目</h2><p>换一个关键词，或看看其他项目。</p><button type="button" class="button button-outline" @click="reset">查看全部项目<SiteIcon name="arrow" :size="18" /></button></div>
    <div v-else class="project-grid"><ProjectCard v-for="project in visible" :key="project.id" :project="project" /></div>
  </section>
</template>
