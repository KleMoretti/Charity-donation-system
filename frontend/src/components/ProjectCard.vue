<script setup>
import { computed } from 'vue'
import SiteIcon from './SiteIcon.vue'
import { money, progressValue, statusLabel } from '../utils/display'
import { projectIntroduction, projectTheme } from '../utils/projectPresentation'

const props = defineProps({ project: { type: Object, required: true } })
const theme = computed(() => projectTheme(props.project))
</script>

<template>
  <article class="project-card" :class="theme.tone">
    <div class="project-cover"><img :src="theme.cover" alt="" width="640" height="400" loading="lazy" /><span class="project-status" :class="{ ended: project.status === 'CLOSED' }"><i></i>{{ statusLabel(project.status) }}</span><span class="image-caption">公益主题配图</span></div>
    <div class="project-card-body">
      <p class="project-category"><SiteIcon :name="theme.icon" :size="16" />{{ theme.label }}</p>
      <h3><RouterLink :to="'/projects/' + project.id">{{ project.title }}</RouterLink></h3>
      <p class="project-excerpt">{{ projectIntroduction(project) }}</p>
      <div class="project-progress"><el-progress :percentage="progressValue(project)" :show-text="false" :stroke-width="5" /><span>{{ project.progressPercent }}%</span></div>
      <div class="project-card-meta"><span>目标 <strong>¥ {{ money(project.targetAmount) }}</strong></span><span>{{ project.donationCount }} 次参与</span></div>
      <div class="project-card-footer"><span>{{ project.status === 'ACTIVE' ? '了解项目，传递一份关心' : '看看善意留下的足迹' }}</span><SiteIcon name="northeast" :size="21" /></div>
    </div>
  </article>
</template>
