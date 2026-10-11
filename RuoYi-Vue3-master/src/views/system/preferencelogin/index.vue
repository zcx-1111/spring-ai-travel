<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户ID" prop="userId">
        <el-input
          v-model="queryParams.userId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="默认出发城市" prop="defaultDeparture">
        <el-input
          v-model="queryParams.defaultDeparture"
          placeholder="请输入默认出发城市"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="默认预算等级" prop="budgetLevel">
        <el-input
          v-model="queryParams.budgetLevel"
          placeholder="请输入默认预算等级"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="出行风格偏好" prop="travelStyle">
        <el-input
          v-model="queryParams.travelStyle"
          placeholder="请输入出行风格偏好"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="景点类型偏好" prop="scenicTypePrefer">
        <el-input
          v-model="queryParams.scenicTypePrefer"
          placeholder="请输入景点类型偏好"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="交通偏好" prop="trafficPrefer">
        <el-input
          v-model="queryParams.trafficPrefer"
          placeholder="请输入交通偏好"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="住宿偏好" prop="accommodationPrefer">
        <el-input
          v-model="queryParams.accommodationPrefer"
          placeholder="请输入住宿偏好"
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
          v-hasPermi="['system:preferencelogin:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:preferencelogin:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:preferencelogin:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:preferencelogin:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="preferenceloginList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="默认出发城市" align="center" prop="defaultDeparture" />
      <el-table-column label="默认预算等级" align="center" prop="budgetLevel" />
      <el-table-column label="出行风格偏好" align="center" prop="travelStyle" />
      <el-table-column label="景点类型偏好" align="center" prop="scenicTypePrefer" />
      <el-table-column label="交通偏好" align="center" prop="trafficPrefer" />
      <el-table-column label="住宿偏好" align="center" prop="accommodationPrefer" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:preferencelogin:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:preferencelogin:remove']">删除</el-button>
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
      <el-form ref="preferenceloginRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="用户ID" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="默认出发城市" prop="defaultDeparture">
              <el-input v-model="form.defaultDeparture" placeholder="请输入默认出发城市" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="默认预算等级" prop="budgetLevel">
              <el-input v-model="form.budgetLevel" placeholder="请输入默认预算等级" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="出行风格偏好" prop="travelStyle">
              <el-input v-model="form.travelStyle" placeholder="请输入出行风格偏好" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="景点类型偏好" prop="scenicTypePrefer">
              <el-input v-model="form.scenicTypePrefer" placeholder="请输入景点类型偏好" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="交通偏好" prop="trafficPrefer">
              <el-input v-model="form.trafficPrefer" placeholder="请输入交通偏好" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="住宿偏好" prop="accommodationPrefer">
              <el-input v-model="form.accommodationPrefer" placeholder="请输入住宿偏好" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="软删除标记" prop="delFlag">
              <el-input v-model="form.delFlag" placeholder="请输入软删除标记" />
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

<script setup name="Preferencelogin">
import { listPreferencelogin, getPreferencelogin, delPreferencelogin, addPreferencelogin, updatePreferencelogin } from "@/api/system/preferencelogin"

const { proxy } = getCurrentInstance()

const preferenceloginList = ref([])
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
    defaultDeparture: undefined,
    budgetLevel: undefined,
    travelStyle: undefined,
    scenicTypePrefer: undefined,
    trafficPrefer: undefined,
    accommodationPrefer: undefined,
  },
  rules: {
    userId: [
      { required: true, message: "用户ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询用户旅游偏好列表 */
function getList() {
  loading.value = true
  listPreferencelogin(queryParams.value).then(response => {
    preferenceloginList.value = response.rows
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
    defaultDeparture: null,
    budgetLevel: null,
    travelStyle: null,
    scenicTypePrefer: null,
    trafficPrefer: null,
    accommodationPrefer: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null,
    delFlag: null
  }
  proxy.resetForm("preferenceloginRef")
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
  getPreferencelogin(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改用户旅游偏好"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["preferenceloginRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updatePreferencelogin(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPreferencelogin(form.value).then(() => {
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
    return delPreferencelogin(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/preferencelogin/export', {
    ...queryParams.value
  }, `preferencelogin_${new Date().getTime()}.xlsx`)
}

getList()
</script>
