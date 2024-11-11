import { createApp } from 'vue'
import App from './App.vue'
import './main.css';
import router from "@/router/router.js"; 

createApp(App).use(router).mount("#app");
