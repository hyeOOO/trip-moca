import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import './main.css';
import router from '@/router/router.js';
import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { setAuthStoreGetter, setRouterGetter } from '@/plugins/axios';
import { useAuthStore } from '@/store/auth';

library.add(fas);

const app = createApp(App);
const pinia = createPinia();

app.component('font-awesome-icon', FontAwesomeIcon);
app.use(pinia).use(router);

// Pinia store와 router 설정을 app 마운트 이후에 수행
app.mount('#app');

// 전역 스토어와 라우터 설정
const authStore = useAuthStore();
setAuthStoreGetter(() => useAuthStore());
setRouterGetter(() => router);
