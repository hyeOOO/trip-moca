import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import "./main.css";
import router from "@/router/router.js";
import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";

library.add(fas);

const app = createApp(App);
const pinia = createPinia();

app.component("font-awesome-icon", FontAwesomeIcon);
app.use(pinia).use(router);

app.mount("#app");