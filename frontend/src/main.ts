import 'admin-lte/dist/css/adminlte.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'admin-lte/dist/js/adminlte.min.js'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from '@/stores/auth.store'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const authStore = useAuthStore(pinia)

// Vérifie le token et récupère l'utilisateur avant l'affichage
await authStore.initialize()

app.mount('#app')