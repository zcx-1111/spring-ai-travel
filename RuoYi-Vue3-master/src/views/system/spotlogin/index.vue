<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="行程天ID" prop="planDayId">
        <el-input
          v-model="queryParams.planDayId"
          placeholder="请输入行程天ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="景点ID" prop="spotId">
        <el-input
          v-model="queryParams.spotId"
          placeholder="请输入景点ID"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="游玩顺序" prop="sortNum">
        <el-input
          v-model="queryParams.sortNum"
          placeholder="请输入游玩顺序"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="预计游玩时长" prop="playDuration">
        <el-input
          v-model="queryParams.playDuration"
          placeholder="请输入预计游玩时长"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="门票花费" prop="ticketCost">
        <el-input
          v-model="queryParams.ticketCost"
          placeholder="请输入门票花费"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="游玩建议" prop="playTip">
        <el-input
          v-model="queryParams.playTip"
          placeholder="请输入游玩建议"
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
          v-hasPermi="['system:spotlogin:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:spotlogin:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:spotlogin:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:spotlogin:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="spotloginList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" />
      <el-table-column label="行程天ID" align="center" prop="planDayId" />
      <el-table-column label="景点ID" align="center" prop="spotId" />
      <el-table-column label="游玩顺序" align="center" prop="sortNum" />
      <el-table-column label="预计游玩时长" align="center" prop="playDuration" />
      <el-table-column label="门票花费" align="center" prop="ticketCost" />
      <el-table-column label="游玩建议" align="center" prop="playTip" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:spotlogin:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:spotlogin:remove']">删除</el-button>
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

    <!-- 添加或修改每日行程景点关联对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="spotloginRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="行程天ID" prop="planDayId">
              <el-input v-model="form.planDayId" placeholder="请输入行程天ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="景点ID" prop="spotId">
              <el-input v-model="form.spotId" placeholder="请输入景点ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="游玩顺序" prop="sortNum">
              <el-input v-model="form.sortNum" placeholder="请输入游玩顺序" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="预计游玩时长" prop="playDuration">
              <el-input v-model="form.playDuration" placeholder="请输入预计游玩时长（小时）" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="门票花费" prop="ticketCost">
              <el-input v-model="form.ticketCost" placeholder="请输入门票花费（元）" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="游玩建议" prop="playTip">
              <el-input v-model="form.playTip" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
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

<script setup name="Spotlogin">
import { listSpotlogin, getSpotlogin, delSpotlogin, addSpotlogin, updateSpotlogin } from "@/api/system/spotlogin"

const { proxy } = getCurrentInstance()

const spotloginList = ref([])
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
    planDayId: undefined,
    spotId: undefined,
    sortNum: undefined,
    playDuration: undefined,
    ticketCost: undefined,
    playTip: undefined,
  },
  rules: {
    planDayId: [
      { required: true, message: "行程天ID不能为空", trigger: "blur" }
    ],
    spotId: [
      { required: true, message: "景点ID不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询每日行程景点关联列表 */
function getList() {
  loading.value = true
  listSpotlogin(queryParams.value).then(response => {
    spotloginList.value = response.rows
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
    planDayId: null,
    spotId: null,
    sortNum: null,
    playDuration: null,
    ticketCost: null,
    playTip: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null,
    delFlag: null
  }
  proxy.resetForm("spotloginRef")
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
  title.value = "添加每日行程景点关联"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getSpotlogin(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改每日行程景点关联"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["spotloginRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateSpotlogin(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSpotlogin(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除每日行程景点关联编号为"' + _ids + '"的数据项？').then(function() {
    return delSpotlogin(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/spotlogin/export', {
    ...queryParams.value
  }, `spotlogin_${new Date().getTime()}.xlsx`)
}

getList()
</script>
