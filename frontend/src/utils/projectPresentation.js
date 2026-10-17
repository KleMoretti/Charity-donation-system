// 仅管理展示主题，不改变项目标题、金额、状态或保存的业务资料。
export function projectTheme(project) {
  const text = project.title + ' ' + project.description
  if (/阅读|图书|教育|儿童|学校/.test(text)) return { label: '教育与成长', icon: 'book', cover: '/images/reading-together.jpg', tone: 'education' }
  if (/环境|生态|绿色|动物|植树/.test(text)) return { label: '自然与未来', icon: 'leaf', cover: '/images/nature-cover.svg', tone: 'nature' }
  return { label: '关怀与陪伴', icon: 'heart', cover: '/images/community-cover.svg', tone: 'community' }
}

export function projectIntroduction(project) {
  // 开发阶段的固定说明不作为公开项目宣传语；原文仍保留在管理端。
  const text = String(project.description || '').replace(/^演示项目[：:]\s*/, '')
  const content = text.split(/(?<=[。！？])|\n/).map(line => line.trim())
    .filter(line => line && !/模拟捐款|本系统|联调|MySQL|教学|阶段.*演示/.test(line)).join('')
  return content || '每一份关注，都是改变的起点。了解项目目标与参与进展，让善意继续传递。'
}
