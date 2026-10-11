<template>
  <!-- 登录注册页：简约旅游风，渐变天空背景 + 毛玻璃卡片 -->
  <div class="tour-login-page">
    <!-- 背景装饰云朵 -->
    <div class="cloud cloud-1"></div>
    <div class="cloud cloud-2"></div>
    <div class="cloud cloud-3"></div>

    <!-- 登录注册卡片 -->
    <div class="auth-card">
      <div class="auth-header">
        <div class="logo">🌍</div>
        <h2 class="auth-title">AI 智能旅游规划助手</h2>
        <p class="auth-subtitle">开启你的专属旅行计划</p>
      </div>

      <!-- 登录 / 注册 Tab 切换 -->
      <el-tabs v-model="activeTab" class="auth-tabs" stretch>
        <!-- 登录 Tab -->
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model.trim="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model.trim="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                class="submit-btn"
                :loading="loginLoading"
                @click="handleLogin"
              >登 录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 注册 Tab -->
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large">
            <el-form-item prop="username">
              <el-input
                v-model.trim="registerForm.username"
                placeholder="请输入用户名（3-20个字符）"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model.trim="registerForm.password"
                type="password"
                placeholder="请输入密码（5-20位）"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model.trim="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                v-model.trim="registerForm.phone"
                placeholder="请输入手机号"
                :prefix-icon="Iphone"
                maxlength="11"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                class="submit-btn"
                :loading="registerLoading"
                @click="handleRegister"
              >注 册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'
import { login, register } from '@/api/auth'

const router = useRouter()
const route = useRoute()

/** 当前激活的 Tab：login-登录 / register-注册 */
const activeTab = ref('login')

/** 登录按钮 loading 状态 */
const loginLoading = ref(false)
/** 注册按钮 loading 状态 */
const registerLoading = ref(false)

/** 登录表单数据 */
const loginForm = reactive({
  username: '',
  password: ''
})

/** 注册表单数据 */
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

/** 表单实例引用 */
const loginFormRef = ref(null)
const registerFormRef = ref(null)

/** 登录表单校验规则 */
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度为 5 到 20 个字符', trigger: 'blur' }
  ]
}

/** 注册表单校验规则 */
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度为 5 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      // 自定义校验：两次密码必须一致
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

/** 切换 Tab 时清空对应表单的校验状态，避免残留红色提示 */
watch(activeTab, () => {
  loginFormRef.value?.clearValidate()
  registerFormRef.value?.clearValidate()
})

/**
 * 登录提交
 * 成功后：存储 token 与用户信息 -> 跳转 AI 对话规划首页
 */
function handleLogin() {
  loginFormRef.value.validate(valid => {
    if (!valid) return
    loginLoading.value = true
    login({ username: loginForm.username, password: loginForm.password })
      .then(res => {
        // 存储 token 与用户信息（前台专用 key）
        localStorage.setItem('Tour-Token', res.data.token)
        localStorage.setItem('Tour-User', JSON.stringify(res.data.userInfo || {}))
        ElMessage.success('登录成功，开始规划你的旅行吧！')
        // 跳转 AI 旅游规划首页（若守卫携带了 redirect，则回跳到原目标页）
        router.push(route.query.redirect || '/travel/plan')
      })
      .finally(() => {
        loginLoading.value = false
      })
  })
}

/**
 * 注册提交
 * 成功后：自动切回登录 Tab 并回填用户名，引导用户登录
 */
function handleRegister() {
  registerFormRef.value.validate(valid => {
    if (!valid) return
    registerLoading.value = true
    register({
      username: registerForm.username,
      password: registerForm.password,
      phone: registerForm.phone
    })
      .then(() => {
        ElMessage.success('注册成功，请登录')
        // 回填用户名并切换到登录 Tab
        loginForm.username = registerForm.username
        loginForm.password = ''
        activeTab.value = 'login'
        resetRegisterForm()
      })
      .finally(() => {
        registerLoading.value = false
      })
  })
}

/** 重置注册表单 */
function resetRegisterForm() {
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.phone = ''
  registerFormRef.value?.resetFields()
}
</script>

<style scoped>
/* 全屏渐变背景：天蓝到青绿，贴合旅游清新主题 */
.tour-login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #4facfe 0%, #43e97b 100%);
}

/* 装饰云朵：半透明圆形叠加模拟 */
.cloud {
  position: absolute;
  height: 90px;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.35);
  filter: blur(2px);
  animation: drift 30s linear infinite;
}
.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
}
.cloud::before { width: 60px; height: 60px; top: -30px; left: 20px; }
.cloud::after  { width: 45px; height: 45px; top: -20px; right: 25px; }
.cloud-1 { width: 160px; top: 12%; left: 8%; }
.cloud-2 { width: 120px; top: 65%; left: 15%; animation-duration: 38s; animation-direction: reverse; }
.cloud-3 { width: 200px; top: 18%; right: 10%; animation-duration: 45s; }

@keyframes drift {
  0%   { transform: translateX(0); }
  50%  { transform: translateX(60px); }
  100% { transform: translateX(0); }
}

/* 登录注册卡片：毛玻璃效果 */
.auth-card {
  width: 400px;
  max-width: 92vw;
  padding: 36px 32px 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 40px rgba(0, 60, 120, 0.18);
  backdrop-filter: blur(10px);
  z-index: 1;
}

.auth-header { text-align: center; margin-bottom: 8px; }
.logo { font-size: 44px; line-height: 1; }
.auth-title {
  margin: 10px 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}
.auth-subtitle {
  margin: 0 0 6px;
  font-size: 13px;
  color: #8a9aa8;
}

/* 提交按钮：占满整行，采用主题渐变 */
.submit-btn {
  width: 100%;
  font-weight: 600;
  letter-spacing: 6px;
  background: linear-gradient(90deg, #4facfe, #38b2a3);
  border: none;
}
.submit-btn:hover { opacity: 0.9; }
</style>
