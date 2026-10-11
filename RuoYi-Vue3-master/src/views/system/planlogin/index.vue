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
      <el-form-item label="出发城市" prop="departureCity">
        <el-input
          v-model="queryParams.departureCity"
          placeholder="请输入出发城市"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="目的地城市" prop="destinationCity">
        <el-input
          v-model="queryParams.destinationCity"
          placeholder="请输入目的地城市"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="出行天数" prop="travelDays">
        <el-input
          v-model="queryParams.travelDays"
          placeholder="请输入出行天数"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker clearable
          v-model="queryParams.startDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择开始日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker clearable
          v-model="queryParams.endDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="总预算" prop="totalBudget">
        <el-input
          v-model="queryParams.totalBudget"
          placeholder="请输入总预算"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="出行人数" prop="peopleCount">
        <el-input
          v-model="queryParams.peopleCount"
          placeholder="请输入出行人数"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="消费等级" prop="consumptionLevel">
        <el-input
          v-model="queryParams.consumptionLevel"
          placeholder="请输入消费等级"
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
      <el-form-item label="景点类型偏好" prop="scenicTypePrefer">
        <el-input
          v-model="queryParams.scenicTypePrefer"
          placeholder="请输入景点类型偏好"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="行程标题" prop="planTitle">
        <el-input
          v-model="queryParams.planTitle"
          placeholder="请输入行程标题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="方案来源" prop="planSource">
        <el-input
          v-model="queryParams.planSource"
          placeholder="请输入方案来源"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预计总花费" prop="totalCost">
        <el-input
          v-model="queryParams.totalCost"
          placeholder="请输入预计总花费"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="景点总数" prop="spotCount">
        <el-input
          v-model="queryParams.spotCount"
          placeholder="请输入景点总数"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所用AI模型" prop="aiModel">
        <el-input
          v-model="queryParams.aiModel"
          placeholder="请输入所用AI模型"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="生成耗时" prop="generationTime">
        <el-input
          v-model="queryParams.generationTime"
          placeholder="请输入生成耗时"
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
          v-hasPermi="['system:planlogin:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:planlogin:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:planlogin:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:planlogin:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="planloginList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="用户ID" align="center" prop="userId" />
      <el-table-column label="出发城市" align="center" prop="departureCity" />
      <el-table-column label="目的地城市" align="center" prop="destinationCity" />
      <el-table-column label="出行天数" align="center" prop="travelDays" />
      <el-table-column label="开始日期" align="center" prop="startDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束日期" align="center" prop="endDate" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.endDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总预算" align="center" prop="totalBudget" />
      <el-table-column label="出行人数" align="center" prop="peopleCount" />
      <el-table-column label="出行人员类型" align="center" prop="peopleType" />
      <el-table-column label="消费等级" align="center" prop="consumptionLevel" />
      <el-table-column label="交通偏好" align="center" prop="trafficPrefer" />
      <el-table-column label="住宿偏好" align="center" prop="accommodationPrefer" />
      <el-table-column label="景点类型偏好" align="center" prop="scenicTypePrefer" />
      <el-table-column label="行程标题" align="center" prop="planTitle" />
      <el-table-column label="行程摘要" align="center" prop="planSummary" />
      <el-table-column label="方案来源" align="center" prop="planSource" />
      <el-table-column label="行程状态" align="center" prop="planStatus" />
      <el-table-column label="预计总花费" align="center" prop="totalCost" />
      <el-table-column label="景点总数" align="center" prop="spotCount" />
      <el-table-column label="所用AI模型" align="center" prop="aiModel" />
      <el-table-column label="生成耗时" align="center" prop="generationTime" />
      <el-table-column label="生成提示词快照" align="center" prop="promptSnapshot" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:planlogin:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:planlogin:remove']">删除</el-button>
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

    <!-- 添加或修改行程方案主对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="planloginRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="用户ID" prop="userId">
              <el-input v-model="form.userId" placeholder="请输入用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="出发城市" prop="departureCity">
              <el-input v-model="form.departureCity" placeholder="请输入出发城市" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="目的地城市" prop="destinationCity">
              <el-input v-model="form.destinationCity" placeholder="请输入目的地城市" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="出行天数" prop="travelDays">
              <el-input v-model="form.travelDays" placeholder="请输入出行天数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker clearable
                v-model="form.startDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择开始日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker clearable
                v-model="form.endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择结束日期">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="总预算" prop="totalBudget">
              <el-input v-model="form.totalBudget" placeholder="请输入总预算" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="出行人数" prop="peopleCount">
              <el-input v-model="form.peopleCount" placeholder="请输入出行人数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="消费等级" prop="consumptionLevel">
              <el-input v-model="form.consumptionLevel" placeholder="请输入消费等级" />
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
            <el-form-item label="景点类型偏好" prop="scenicTypePrefer">
              <el-input v-model="form.scenicTypePrefer" placeholder="请输入景点类型偏好" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="行程标题" prop="planTitle">
              <el-input v-model="form.planTitle" placeholder="请输入行程标题" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="行程摘要" prop="planSummary">
              <el-input v-model="form.planSummary" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="方案来源" prop="planSource">
              <el-input v-model="form.planSource" placeholder="请输入方案来源" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预计总花费" prop="totalCost">
              <el-input v-model="form.totalCost" placeholder="请输入预计总花费" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="景点总数" prop="spotCount">
              <el-input v-model="form.spotCount" placeholder="请输入景点总数" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所用AI模型" prop="aiModel">
              <el-input v-model="form.aiModel" placeholder="请输入所用AI模型" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生成耗时" prop="generationTime">
              <el-input v-model="form.generationTime" placeholder="请输入生成耗时" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生成提示词快照" prop="promptSnapshot">
              <el-input v-model="form.promptSnapshot" type="textarea" placeholder="请输入内容" />
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

<script setup name="Planlogin">
import { listPlanlogin, getPlanlogin, delPlanlogin, addPlanlogin, updatePlanlogin } from "@/api/system/planlogin"

const { proxy } = getCurrentInstance()

const planloginList = ref([])
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
    departureCity: undefined,
    destinationCity: undefined,
    travelDays: undefined,
    startDate: undefined,
    endDate: undefined,
    totalBudget: undefined,
    peopleCount: undefined,
    peopleType: undefined,
    consumptionLevel: undefined,
    trafficPrefer: undefined,
    accommodationPrefer: undefined,
    scenicTypePrefer: undefined,
    planTitle: undefined,
    planSummary: undefined,
    planSource: undefined,
    planStatus: undefined,
    totalCost: undefined,
    spotCount: undefined,
    aiModel: undefined,
    generationTime: undefined,
    promptSnapshot: undefined,
  },
  rules: {
    userId: [
      { required: true, message: "用户ID不能为空", trigger: "blur" }
    ],
    destinationCity: [
      { required: true, message: "目的地城市不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询行程方案主列表 */
function getList() {
  loading.value = true
  listPlanlogin(queryParams.value).then(response => {
    planloginList.value = response.rows
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
    departureCity: null,
    destinationCity: null,
    travelDays: null,
    startDate: null,
    endDate: null,
    totalBudget: null,
    peopleCount: null,
    peopleType: null,
    consumptionLevel: null,
    trafficPrefer: null,
    accommodationPrefer: null,
    scenicTypePrefer: null,
    planTitle: null,
    planSummary: null,
    planSource: null,
    planStatus: null,
    totalCost: null,
    spotCount: null,
    aiModel: null,
    generationTime: null,
    promptSnapshot: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null,
    delFlag: null
  }
  proxy.resetForm("planloginRef")
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
  title.value = "添加行程方案主"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getPlanlogin(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改行程方案主"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["planloginRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updatePlanlogin(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addPlanlogin(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除行程方案主编号为"' + _ids + '"的数据项？').then(function() {
    return delPlanlogin(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/planlogin/export', {
    ...queryParams.value
  }, `planlogin_${new Date().getTime()}.xlsx`)
}

getList()
</script>
