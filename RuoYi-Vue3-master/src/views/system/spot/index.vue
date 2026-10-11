<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="景点名称" prop="spotName">
        <el-input
          v-model="queryParams.spotName"
          placeholder="请输入景点名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属省份" prop="province">
        <el-input
          v-model="queryParams.province"
          placeholder="请输入所属省份"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所属城市" prop="city">
        <el-input
          v-model="queryParams.city"
          placeholder="请输入所属城市"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="详细地址" prop="address">
        <el-input
          v-model="queryParams.address"
          placeholder="请输入详细地址"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="经度" prop="longitude">
        <el-input
          v-model="queryParams.longitude"
          placeholder="请输入经度"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="纬度" prop="latitude">
        <el-input
          v-model="queryParams.latitude"
          placeholder="请输入纬度"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="景点等级" prop="spotLevel">
        <el-input
          v-model="queryParams.spotLevel"
          placeholder="请输入景点等级"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="建议游玩时长" prop="playHours">
        <el-input
          v-model="queryParams.playHours"
          placeholder="请输入建议游玩时长"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="成人票价" prop="adultPrice">
        <el-input
          v-model="queryParams.adultPrice"
          placeholder="请输入成人票价"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="儿童票价" prop="childPrice">
        <el-input
          v-model="queryParams.childPrice"
          placeholder="请输入儿童票价"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="老人票价" prop="oldPrice">
        <el-input
          v-model="queryParams.oldPrice"
          placeholder="请输入老人票价"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="开放时间描述" prop="openTimeDesc">
        <el-input
          v-model="queryParams.openTimeDesc"
          placeholder="请输入开放时间描述"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="最佳游玩季节" prop="bestSeason">
        <el-input
          v-model="queryParams.bestSeason"
          placeholder="请输入最佳游玩季节"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="封面图地址" prop="coverImg">
        <el-input
          v-model="queryParams.coverImg"
          placeholder="请输入封面图地址"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类ID" prop="categoryId">
        <el-input
          v-model="queryParams.categoryId"
          placeholder="请输入分类ID"
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
          v-hasPermi="['system:spot:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:spot:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:spot:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['system:spot:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Upload"
          @click="handleImport"
          v-hasPermi="['system:spot:add']"
        >导入新知识库</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="spotList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="景点名称" align="center" prop="spotName" min-width="120" show-overflow-tooltip />
      <el-table-column label="省份" align="center" prop="province" width="80" />
      <el-table-column label="城市" align="center" prop="city" width="80" />
      <el-table-column label="地址" align="center" prop="address" min-width="180" show-overflow-tooltip />
      <el-table-column label="等级" align="center" prop="spotLevel" width="60" />
      <el-table-column label="时长(h)" align="center" prop="playHours" width="70" />
      <el-table-column label="成人票价" align="center" prop="adultPrice" width="80" />
      <el-table-column label="开放时间" align="center" prop="openTimeDesc" min-width="140" show-overflow-tooltip />
      <el-table-column label="最佳季节" align="center" prop="bestSeason" min-width="120" show-overflow-tooltip />
      <el-table-column label="景点简介" align="center" prop="spotIntro" min-width="200" show-overflow-tooltip />
      <el-table-column label="游玩须知" align="center" prop="playNotice" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:spot:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:spot:remove']">删除</el-button>
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

    <!-- 添加或修改景点主对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="spotRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="景点名称" prop="spotName">
              <el-input v-model="form.spotName" placeholder="请输入景点名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属省份" prop="province">
              <el-input v-model="form.province" placeholder="请输入所属省份" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属城市" prop="city">
              <el-input v-model="form.city" placeholder="请输入所属城市" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入详细地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="经度" prop="longitude">
              <el-input v-model="form.longitude" placeholder="请输入经度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="纬度" prop="latitude">
              <el-input v-model="form.latitude" placeholder="请输入纬度" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="景点等级" prop="spotLevel">
              <el-input v-model="form.spotLevel" placeholder="请输入景点等级" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="建议游玩时长" prop="playHours">
              <el-input v-model="form.playHours" placeholder="请输入建议游玩时长" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成人票价" prop="adultPrice">
              <el-input v-model="form.adultPrice" placeholder="请输入成人票价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="儿童票价" prop="childPrice">
              <el-input v-model="form.childPrice" placeholder="请输入儿童票价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="老人票价" prop="oldPrice">
              <el-input v-model="form.oldPrice" placeholder="请输入老人票价" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开放时间描述" prop="openTimeDesc">
              <el-input v-model="form.openTimeDesc" placeholder="请输入开放时间描述" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="最佳游玩季节" prop="bestSeason">
              <el-input v-model="form.bestSeason" placeholder="请输入最佳游玩季节" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="景点简介" prop="spotIntro">
              <el-input v-model="form.spotIntro" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="游玩须知" prop="playNotice">
              <el-input v-model="form.playNotice" type="textarea" placeholder="请输入内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面图地址" prop="coverImg">
              <el-input v-model="form.coverImg" placeholder="请输入封面图地址" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="分类ID" prop="categoryId">
              <el-input v-model="form.categoryId" placeholder="请输入分类ID" />
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

    <!-- 导入CSV知识库对话框 -->
    <el-dialog title="导入新知识库" v-model="importOpen" width="520px" append-to-body>
      <el-form :model="importForm" label-width="100px">
        <el-form-item label="省份">
          <el-input v-model="importForm.province" placeholder="如：福建省" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="importForm.city" placeholder="如：龙岩市" />
        </el-form-item>
        <el-form-item label="CSV文件">
          <el-upload
            ref="uploadRef"
            :limit="1"
            accept=".csv"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-exceed="handleExceed"
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">将CSV文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip" style="color: #909399;">
                支持去哪儿网格式的CSV（含名字、地址、介绍、开放时间等列）
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="importLoading" @click="submitImport">开始导入</el-button>
          <el-button @click="importOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Spot">
import { listSpot, getSpot, delSpot, addSpot, updateSpot, importCsvSpot } from "@/api/system/spot"
import { UploadFilled } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const spotList = ref([])
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
    spotName: undefined,
    province: undefined,
    city: undefined,
    address: undefined,
    longitude: undefined,
    latitude: undefined,
    spotLevel: undefined,
    playHours: undefined,
    adultPrice: undefined,
    childPrice: undefined,
    oldPrice: undefined,
    openTimeDesc: undefined,
    bestSeason: undefined,
    spotIntro: undefined,
    playNotice: undefined,
    coverImg: undefined,
    categoryId: undefined,
  },
  rules: {
    spotName: [
      { required: true, message: "景点名称不能为空", trigger: "blur" }
    ],
    city: [
      { required: true, message: "所属城市不能为空", trigger: "blur" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 导入相关状态 */
const importOpen = ref(false)
const importLoading = ref(false)
const importFile = ref(null)
const importForm = reactive({
  province: '',
  city: ''
})

/** 打开导入弹窗 */
function handleImport() {
  importForm.province = ''
  importForm.city = ''
  importFile.value = null
  importOpen.value = true
}

/** 文件选择变化 */
function handleFileChange(file) {
  importFile.value = file.raw
}

/** 超出上传限制 */
function handleExceed() {
  proxy.$modal.msgWarning("只能上传一个CSV文件，请先移除已选文件")
}

/** 提交导入 */
function submitImport() {
  if (!importForm.province.trim()) {
    proxy.$modal.msgWarning("请输入省份")
    return
  }
  if (!importForm.city.trim()) {
    proxy.$modal.msgWarning("请输入城市")
    return
  }
  if (!importFile.value) {
    proxy.$modal.msgWarning("请选择CSV文件")
    return
  }
  const formData = new FormData()
  formData.append('file', importFile.value)
  formData.append('province', importForm.province.trim())
  formData.append('city', importForm.city.trim())
  importLoading.value = true
  importCsvSpot(formData).then(response => {
    proxy.$modal.msgSuccess(response.msg || '导入成功')
    importOpen.value = false
    getList()
  }).catch(() => {
    // 错误信息由request拦截器统一处理
  }).finally(() => {
    importLoading.value = false
  })
}

/** 查询景点主列表 */
function getList() {
  loading.value = true
  listSpot(queryParams.value).then(response => {
    spotList.value = response.rows
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
    spotName: null,
    province: null,
    city: null,
    address: null,
    longitude: null,
    latitude: null,
    spotLevel: null,
    playHours: null,
    adultPrice: null,
    childPrice: null,
    oldPrice: null,
    openTimeDesc: null,
    bestSeason: null,
    spotIntro: null,
    playNotice: null,
    coverImg: null,
    categoryId: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null,
    delFlag: null
  }
  proxy.resetForm("spotRef")
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
  title.value = "添加景点主"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const _id = row.id || ids.value
  getSpot(_id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改景点主"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["spotRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateSpot(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addSpot(form.value).then(() => {
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
  proxy.$modal.confirm('是否确认删除景点主编号为"' + _ids + '"的数据项？').then(function() {
    return delSpot(_ids)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('system/spot/export', {
    ...queryParams.value
  }, `spot_${new Date().getTime()}.xlsx`)
}

getList()
</script>
