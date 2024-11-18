import { createApp } from "vue";
import { createPinia } from "pinia"; // Pinia import 추가
import App from "./App.vue";
import "./main.css";
import router from "@/router/router.js";
import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
library.add(fas);

const app = createApp(App);
const pinia = createPinia(); // Pinia 인스턴스 생성

// createApp을 통해 생성한Application 인스턴스의 component API 활용
app.component("font-awesome-icon", FontAwesomeIcon);
// Pinia와 Router 추가
app.use(pinia);
app.use(router);

app.mount("#app");
