<script setup>
import { computed, onMounted, ref } from 'vue'
import { listProjects } from '../api/projects'
import ProjectCard from '../components/ProjectCard.vue'
import SiteIcon from '../components/SiteIcon.vue'

const projects = ref([])
const loading = ref(true)
const error = ref(false)
const activeProjects = computed(() => projects.value.filter(project => project.status === 'ACTIVE'))
const featured = computed(() => activeProjects.value.slice(0, 3))
const causes = [
  { label: '教育与成长', text: '让阅读打开更大的世界', icon: 'book', keyword: '阅读', tone: 'education' },
  { label: '关怀与陪伴', text: '让需要被看见，让关心抵达', icon: 'heart', keyword: '关怀', tone: 'community' },
  { label: '自然与未来', text: '为共同的家园，多做一点', icon: 'leaf', keyword: '环境', tone: 'nature' },
]
async function load() {
  loading.value = true; error.value = false
  try { projects.value = await listProjects() }
  catch { error.value = true }
  finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <section class="home-hero">
    <div class="hero-copy">
      <span class="eyebrow"><i class="live-dot"></i> 一起，让美好发生</span>
      <h1>让每一份善意，<br /><em>都有温暖的回响。</em></h1>
      <p class="hero-description">世界的改变，始于一份小小的关心。<br class="desktop-break" />发现你在意的公益项目，让心中的善意向前一步。</p>
      <div class="hero-actions"><RouterLink to="/projects" class="button button-primary">发现公益项目<SiteIcon name="arrow" :size="20" /></RouterLink><RouterLink to="/#our-belief" class="text-link">了解我们的初心<SiteIcon name="northeast" :size="17" /></RouterLink></div>
      <div class="hero-note"><span class="little-hearts"><SiteIcon name="heart" :size="16" /></span><p>不论大小，每一次参与都很珍贵。</p></div>
    </div>
    <div class="hero-visual">
      <span class="hero-orbit" aria-hidden="true"></span>
      <figure class="hero-image"><img src="/images/reading-together.jpg" alt="阳光中的阅读时光，公益主题配图" width="1400" height="1867" fetchpriority="high" /><figcaption>阅读与成长 <span>公益主题影像</span></figcaption></figure>
      <div class="hero-sticker" aria-hidden="true"><SiteIcon name="leaf" :size="26" /><span>把温暖<br />带到远方</span></div>
      <div class="hero-floating-note"><span class="floating-icon"><SiteIcon name="heart" :size="24" /></span><div><strong>善意，正在发生</strong><p v-if="!loading && !error && activeProjects.length">{{ activeProjects.length }} 个项目，等待与你相遇</p><p v-else>从一份关心开始</p></div><SiteIcon name="northeast" :size="21" /></div>
    </div>
  </section>

  <section class="values-strip" aria-label="参与理念">
    <div><SiteIcon name="book" /><span><strong>从了解出发</strong><small>认真读懂每一个项目</small></span></div>
    <div><SiteIcon name="heart" /><span><strong>让善意有回应</strong><small>每一次参与都有记录</small></span></div>
    <div><SiteIcon name="leaf" /><span><strong>让关心可持续</strong><small>选择你愿意长期关注的事</small></span></div>
  </section>

  <section class="home-section" aria-labelledby="featured-title">
    <div class="section-heading"><div><span class="eyebrow">值得你的一份关注</span><h2 id="featured-title">正在发生的善意<span class="heading-dot">.</span></h2></div><RouterLink to="/projects" class="text-link">查看全部项目<SiteIcon name="arrow" :size="19" /></RouterLink></div>
    <div v-if="loading" class="project-skeleton-grid" aria-busy="true" aria-label="正在加载公益项目"><div v-for="i in 2" :key="i" class="project-skeleton"></div></div>
    <div v-else-if="error" class="friendly-state"><SiteIcon name="heart" :size="30" /><h3>项目暂时未能加载</h3><p>请稍后再来看看，或重新试一次。</p><button type="button" class="button button-outline" @click="load">重新加载</button></div>
    <div v-else-if="featured.length" class="project-grid" :class="{ 'single-project': featured.length === 1 }"><ProjectCard v-for="project in featured" :key="project.id" :project="project" /></div>
    <div v-else class="friendly-state"><h3>新的善意，正在路上</h3><p>你也可以看看已经结束的项目，回顾留下的温暖。</p><RouterLink to="/projects?status=all" class="button button-outline">浏览全部项目<SiteIcon name="arrow" :size="18" /></RouterLink></div>
  </section>

  <section class="home-section causes-section" aria-labelledby="causes-title">
    <div class="section-heading"><div><span class="eyebrow">从你关心的事开始</span><h2 id="causes-title">善意，有很多种模样。</h2></div><p class="section-description">为成长，为陪伴，<br />也为我们共同的明天。</p></div>
    <div class="cause-grid"><RouterLink v-for="cause in causes" :key="cause.label" :to="{ path: '/projects', query: { keyword: cause.keyword, status: 'all' } }" class="cause-card" :class="cause.tone"><span class="cause-icon"><SiteIcon :name="cause.icon" :size="29" /></span><h3>{{ cause.label }}</h3><p>{{ cause.text }}</p><SiteIcon name="northeast" :size="23" /></RouterLink></div>
  </section>

  <section id="our-belief" class="belief-section">
    <div class="belief-copy"><span class="eyebrow">我们的初心 · OUR BELIEF</span><h2>不必成为伟大的人，<br />也能做温暖的事。</h2><p>我们相信，善意不应停留在心里。一本书、一份陪伴、一次力所能及的支持，都可能让平凡的一天有所不同。</p><RouterLink to="/projects" class="button button-light">找到你关心的项目<SiteIcon name="arrow" :size="19" /></RouterLink></div>
    <div class="belief-art" aria-hidden="true"><SiteIcon name="heart" :size="132" /><span>GOODNESS<br />STARTS WITH YOU.</span><i></i></div>
  </section>
</template>
