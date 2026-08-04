import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/global.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn, size: 'large' })

// 启动时恢复登录态
const auth = useAuthStore()
auth.initFromStorage()
if (auth.isLoggedIn && auth.token) {
  auth.fetchMe().catch(() => auth.logout())
}

app.mount('#app')
