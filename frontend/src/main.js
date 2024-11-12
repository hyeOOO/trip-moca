import { createApp } from "vue";
import { createPinia } from "pinia"; // Pinia import 추가
import App from "./App.vue";
import "./main.css";
import router from "@/router/router.js";

const app = createApp(App);
const pinia = createPinia(); // Pinia 인스턴스 생성

// Pinia와 Router 추가
app.use(pinia);
app.use(router);

app.mount("#app");
