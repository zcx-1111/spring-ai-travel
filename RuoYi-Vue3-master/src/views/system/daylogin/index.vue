<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="行程ID" prop="planId">
        <el-input
          v-model="queryParams.planId"
          placeholder="请输入行程ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="第几天" prop="dayNum">
        <el-input
          v-model="queryParams.dayNum"
          placeholder="请输入第几天"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当日主题" prop="dayTheme">
        <el-input
          v-model="queryParams.dayTheme"
          placeholder="请输入当日主题"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当日预算" prop="dailyBudget">
        <el-input
          v-model="queryParams.dailyBudget"
          placeholder="请输入当日预算"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="住宿推荐" prop="hotelRecommend">
        <el-input
          v-model="queryParams.hotelRecommend"
          placeholder="请输入住宿推荐"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="当日交通方式描述" prop="trafficDesc">
        <el-input
          v-model="queryParams.trafficDesc"
          placeholder="请输入当日交通方式描述"
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
          v-hasPermi="['system:daylogin:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:daylogin:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:daylogin:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:daylogin:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dayloginList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="行程ID" align="center" prop="planId" />
      <el-table-column label="第几天" align="center" prop="dayNum" />
      <el-table-column label="当日主题" align="center" prop="dayTheme" />
      <el-table-column label="当日预算" align="center" prop="dailyBudget" />
      <el-table-column label="住宿推荐" align="center" prop="hotelRecommend" />
      <el-table-column label="当日交通方式描述" align="center" prop="trafficDesc" />
      <el-table-column label="当日行程概述" align="center" prop="daySummary" />
      <el-table-column label="当日注意事项" align="center" prop="attention" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:daylogin:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:daylogin:remove']">删除</el-button>
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

    <!-- 添加或修改行程每日详情对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="dayloginRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="行程ID" prop="planId">
              <el-input v-model="form.planId" placeholder="请输入行程ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="第几天" prop="dayNum">
              <el-input v-model="form.dayNum" placeholder="请输入第几天" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="当日主题" prop="dayTheme">
              <el-input v-model="form.dayTheme" placeholder="请输入当日主题" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="当日预算" prop="dailyBudget">
              <el-input v-model="form.dailyBudget" placeholder="请输入当日预算" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="住宿推荐" prop="hotelRecommend">
              <el-input v-model="form.hotelRecommend" placeholder="请输入住宿推荐" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="当日交通方式描述" prop="trafficDesc">
              <el-input v-model="form.trafficDesc" placeholder="请输入当日交通方式描述" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="当日行程概述" prop="daySummary">
              <el-input v-model="form.daySummary" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="当日注意事项" prop="attention">
              <el-input v-model="form.attention" type="textarea" placeholder="请输入内容" />
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

<script setup name="Daylogin">
import { listDaylogin, getDaylogin, delDaylogin, addDaylogin, updateDaylogin } from "@/api/system/daylogin"

const { proxy } = getCurrentInstance()

const dayloginList = ref([])
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
    planId: undefined,
    dayNum: undefined,
    dayTheme: undefined,
    dailyBudget: undefined,
    hotelRecommend: undefined,
    trafficDesc: undefined,
    daySummary: undefined,
    attention: undefined,
  },
  rules: {
    planId: [
      { required: true, message: "行程ID不能为空", trigger: "blur" }
    ],
    dayNum: [
      { required: true, message: "第几天不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询行程每日详情列表 */
function getList() {
  loading.value = true
  listDaylogin(queryParams.value).then(response => {
    dayloginList.value = response.rows
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
    planId: null,
    dayNum: null,
    dayTheme: null,
    dailyBudget: null,
    hotelRecommend: null,
    trafficDesc: null,
    daySummary: null,
    attention: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null,
    delFlag: null
  }
  proxy.resetForm("dayloginRef")
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
  title.value = "添加行程每日详情"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getDaylogin(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改行程每日详情"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["dayloginRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateDaylogin(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addDaylogin(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除行程每日详情编号为"' + _ids + '"的数据项？').then(function() {
    return delDaylogin(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/daylogin/export', {
    ...queryParams.value
  }, `daylogin_${new Date().getTime()}.xlsx`)
}

getList()
</script>
