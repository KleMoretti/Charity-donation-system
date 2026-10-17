<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeading from '../components/PageHeading.vue'
import AdminNav from '../components/AdminNav.vue'
import * as api from '../api/projects'
import { dateTime, errorMessage, money, statusLabel, statusType, validAmount } from '../utils/display'

const projects = ref([])
const loading = ref(false)
const error = ref('')
const dialog = ref(false)
const busy = ref(false)
const changing = ref(null)
const editId = ref(null)
const formRef = ref()
const form = reactive({ title: '', description: '', targetAmount: '' })
const rules = {
  title: [{ required: true, whitespace: true, message: '请输入项目标题', trigger: 'blur' }, { max: 120, message: '标题最多120个字符', trigger: 'blur' }],
  description: [{ required: true, whitespace: true, message: '请输入项目说明', trigger: 'blur' }, { max: 10000, message: '说明最多10000个字符', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }, { validator: (_, value, callback) => callback(validAmount(value) ? undefined : new Error('金额必须大于零，最多10位整数和2位小数')), trigger: 'blur' }],
}
async function load() {
  loading.value = true; error.value = ''
  try { projects.value = await api.listAdminProjects() }
  catch (cause) { error.value = errorMessage(cause) }
  finally { loading.value = false }
}
async function openEditor(project) {
  editId.value = project?.id ?? null
  Object.assign(form, { title: project?.title || '', description: project?.description || '', targetAmount: project?.targetAmount || '' })
  dialog.value = true
  await nextTick(); formRef.value?.clearValidate()
}
async function save() {
  if (busy.value || !await formRef.value.validate().catch(() => false)) return
  busy.value = true
  try {
    const input = { title: form.title.trim(), description: form.description.trim(), targetAmount: form.targetAmount }
    if (editId.value !== null) await api.editProject(editId.value, input)
    else await api.createProject(input)
    dialog.value = false
    ElMessage.success(editId.value !== null ? '项目已保存' : '草稿项目已创建')
    await load()
  } catch (cause) { ElMessage.error(errorMessage(cause)) }
  finally { busy.value = false }
}
async function changeStatus(project, action) {
  if (changing.value !== null) return
  changing.value = project.id
  try {
    await ElMessageBox.confirm(action === 'publish' ? '发布后，访客可以浏览这个项目。' : '结束后，这个项目不能再接受模拟捐款。', action === 'publish' ? '确认发布项目' : '确认结束项目', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    if (action === 'publish') await api.publishProject(project.id)
    else await api.closeProject(project.id)
    ElMessage.success(action === 'publish' ? '项目已发布' : '项目已结束')
    await load()
  } catch (cause) { if (cause !== 'cancel' && cause !== 'close') ElMessage.error(errorMessage(cause)) }
  finally { changing.value = null }
}
onMounted(load)
</script>

<template>
  <AdminNav />
  <PageHeading title="项目管理" description="创建草稿，完善说明后发布；募集中项目由管理员手动结束。" />
  <div class="section-actions"><el-button type="primary" @click="openEditor()">新建项目</el-button><el-button :loading="loading" @click="load">刷新列表</el-button></div>
  <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
  <p v-else-if="loading" role="status">正在加载管理项目…</p>
  <el-empty v-else-if="!projects.length" description="暂无项目，先创建一个草稿吧" />
  <div v-else class="table-scroll">
    <el-table :data="projects" border>
      <el-table-column prop="title" label="项目标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="目标金额" width="170"><template #default="{ row }">¥ {{ money(row.targetAmount) }}</template></el-table-column>
      <el-table-column label="支持记录金额" width="190"><template #default="{ row }">¥ {{ money(row.raisedAmount) }} · {{ row.donationCount }} 笔</template></el-table-column>
      <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="创建时间" width="160"><template #default="{ row }">{{ dateTime(row.createdAt) }}</template></el-table-column>
      <el-table-column label="操作" min-width="210"><template #default="{ row }">
        <el-button size="small" :disabled="changing !== null" @click="openEditor(row)">编辑</el-button>
        <el-button v-if="row.status === 'DRAFT'" size="small" type="primary" :loading="changing === row.id" :disabled="changing !== null && changing !== row.id" @click="changeStatus(row, 'publish')">发布</el-button>
        <el-button v-if="row.status === 'ACTIVE'" size="small" type="warning" :loading="changing === row.id" :disabled="changing !== null && changing !== row.id" @click="changeStatus(row, 'close')">结束</el-button>
      </template></el-table-column>
    </el-table>
  </div>
  <el-dialog v-model="dialog" :title="editId === null ? '新建草稿项目' : '编辑项目'" width="min(680px, calc(100% - 32px))" :close-on-click-modal="false" :close-on-press-escape="!busy" :show-close="!busy">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="busy" @submit.prevent="save">
      <el-form-item label="项目标题" prop="title"><el-input v-model="form.title" maxlength="120" show-word-limit /></el-form-item>
      <el-form-item label="项目说明" prop="description"><el-input v-model="form.description" type="textarea" :rows="6" maxlength="10000" show-word-limit /></el-form-item>
      <el-form-item label="目标金额（元）" prop="targetAmount"><el-input v-model="form.targetAmount" inputmode="decimal" placeholder="例如 10000.00" /></el-form-item>
      <div class="dialog-actions"><el-button :disabled="busy" @click="dialog = false">取消</el-button><el-button type="primary" native-type="submit" :loading="busy">保存项目</el-button></div>
    </el-form>
  </el-dialog>
</template>
