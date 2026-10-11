<template>
  <div class="login">
    <!-- 暗色遮罩 -->
    <div class="login-mask"></div>

    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <!-- 主标题 -->
      <h3 class="title">AI智能旅游规划系统</h3>
      <!-- 副标题 -->
      <p class="subtitle">基于 SpringAI 与向量知识库的旅游智能问答与行程规划平台</p>

      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          size="large"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>

    <!-- 底部版权 -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
// 验证码开关
const captchaEnabled = ref(true)
// 注册开关
const register = ref(false)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

getCode()
getCookie()
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
  background-position: center;
  position: relative;
}

/* 半透明暗色遮罩，让登录框更突出 */
.login-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(31, 78, 95, 0.55), rgba(0, 0, 0, 0.45));
  z-index: 0;
}

/* 主标题 */
.title {
  margin: 0px auto 6px auto;
  text-align: center;
  color: #1F4E5F;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}

/* 副标题 */
.subtitle {
  margin: 0 auto 28px auto;
  text-align: center;
  color: #90A4AE;
  font-size: 12px;
  line-height: 1.6;
  letter-spacing: 0.5px;
}

/* 登录弹窗 */
.login-form {
  position: relative;
  z-index: 1;
  border-radius: 12px;
  background: #ffffff;
  width: 400px;
  padding: 36px 32px 12px 32px;
  box-shadow: 0 8px 36px rgba(0, 0, 0, 0.2);

  .el-input {
    height: 42px;
    input {
      height: 42px;
    }
  }
  .input-icon {
    height: 40px;
    width: 14px;
    margin-left: 0px;
  }

  /* 输入框聚焦湖蓝主色调 */
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    transition: all 0.25s ease;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
  }
  :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #4DB6AC inset;
  }
  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #2196F3 inset, 0 0 6px rgba(33, 150, 243, 0.15);
  }
  :deep(.el-input__inner) {
    font-size: 14px;
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

/* 验证码区域 */
.login-code {
  width: 33%;
  height: 42px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.login-code-img {
  height: 42px;
  padding-left: 12px;
}

/* 记住密码复选框颜色 */
:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #2196F3;
  border-color: #2196F3;
}
:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #2196F3;
}

/* 登录按钮美化 + hover过渡 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #2196F3, #4DB6AC);
  border: none;
  border-radius: 8px;
  height: 42px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  transition: all 0.3s ease;
}
:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #1E88E5, #26A69A);
  box-shadow: 0 4px 16px rgba(33, 150, 243, 0.35);
  transform: translateY(-1px);
}
:deep(.el-button--primary:active) {
  transform: translateY(0);
}

/* 底部版权 */
.el-login-footer {
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.85);
  font-family: Arial, "Microsoft YaHei", sans-serif;
  font-size: 12px;
  letter-spacing: 1px;
  line-height: 40px;
  height: 40px;
  z-index: 1;
}

/* 暗黑模式适配 */
html.dark .login {
  .login-mask {
    background: linear-gradient(135deg, rgba(31, 78, 95, 0.65), rgba(0, 0, 0, 0.6));
  }
  .login-form {
    background: var(--el-bg-color-overlay) !important;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
    .title {
      color: #4DB6AC;
    }
    .subtitle {
      color: #78909C;
    }
  }
}
</style>
