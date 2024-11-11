import { createWebHistory, createRouter } from "vue-router";
import Main from '@/views/mainPage/mainPage.vue'
import Login from '@/views/login/loginPage.vue';
import DestinationGallery from '@/views/travelPlan/DestinationGallery.vue';
const routes = [
  {
    path: "/",
    component: Main,
  },
  {
    path: "/login",
    component: Login,
  },
  {
    path: "/travelPlan",
    component: DestinationGallery,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router; 