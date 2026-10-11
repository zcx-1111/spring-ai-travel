<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="关联游客用户id" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入关联游客用户id"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预算偏好" prop="budget">
        <el-input
          v-model="queryParams.budget"
          placeholder="请输入预算偏好"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="偏好出行天数" prop="travelDays">
        <el-input
          v-model="queryParams.travelDays"
          placeholder="请输入偏好出行天数"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="偏好出行季节" prop="preferredSeason">
        <el-input
          v-model="queryParams.preferredSeason"
          placeholder="请输入偏好出行季节"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="出行人群：单人/情侣/家庭" prop="crowd">
        <el-input
          v-model="queryParams.crowd"
          placeholder="请输入出行人群：单人/情侣/家庭"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:preferenceloginn:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:preferenceloginn:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:preferenceloginn:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:preferenceloginn:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="preferenceloginnList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="关联游客用户id" align="center" prop="userId" />
      <el-table-column label="预算偏好" align="center" prop="budget" />
      <el-table-column label="偏好出行天数" align="center" prop="travelDays" />
      <el-table-column label="出行类型偏好" align="center" prop="travelType" />
      <el-table-column label="偏好出行季节" align="center" prop="preferredSeason" />
      <el-table-column label="出行人群：单人/情侣/家庭" align="center" prop="crowd" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:preferenceloginn:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:preferenceloginn:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改用户旅游偏好对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="preferenceloginnRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="关联游客用户id" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入关联游客用户id" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预算偏好" prop="budget">
              <el-input v-model="form.budget" placeholder="请输入预算偏好" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="偏好出行天数" prop="travelDays">
              <el-input v-model="form.travelDays" placeholder="请输入偏好出行天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="偏好出行季节" prop="preferredSeason">
              <el-input v-model="form.preferredSeason" placeholder="请输入偏好出行季节" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="出行人群：单人/情侣/家庭" prop="crowd">
              <el-input v-model="form.crowd" placeholder="请输入出行人群：单人/情侣/家庭" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="逻辑删除" prop="delFlag">
              <el-input v-model="form.delFlag" placeholder="请输入逻辑删除" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Preferenceloginn">
import { listPreferenceloginn, getPreferenceloginn, delPreferenceloginn, addPreferenceloginn, updatePreferenceloginn } from "@/api/system/preferenceloginn"

const { proxy } = getCurrentInstance()

const preferenceloginnList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userId: undefined,
    budget: undefined,
    travelDays: undefined,
    travelType: undefined,
    preferredSeason: undefined,
    crowd: undefined,
  },
  rules: {
    userId: [
      { required: true, message: "关联游客用户id不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询用户旅游偏好列表 */
function getList() {
  loading.value = true
  listPreferenceloginn(queryParams.value).then(response => {
    preferenceloginnList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: null,
    userId: null,
    budget: null,
    travelDays: null,
    travelType: null,
    preferredSeason: null,
    crowd: null,
    createTime: null,
    updateTime: null,
    delFlag: null
  }
  proxy.resetForm("preferenceloginnRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加用户旅游偏好"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getPreferenceloginn(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改用户旅游偏好"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["preferenceloginnRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updatePreferenceloginn(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPreferenceloginn(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value
  proxy.$modal.confirm('是否确认删除用户旅游偏好编号为"' + _ids + '"的数据项？').then(function() {
    return delPreferenceloginn(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/preferenceloginn/export', {
    ...queryParams.value
  }, `preferenceloginn_${new Date().getTime()}.xlsx`)
}

getList()
</script>
