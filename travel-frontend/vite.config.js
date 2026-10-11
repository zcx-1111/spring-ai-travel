import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 设置 @ 指向 src 目录
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    host: true,
    open: true,
    proxy: {
      // 前台登录注册等接口
      '/api/tour': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // AI行程生成等旅游业务接口
      '/api/travel': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
