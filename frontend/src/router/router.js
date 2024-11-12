import { createWebHistory, createRouter } from "vue-router";
import Main from "@/views/mainPage/mainPage.vue";
import Login from "@/views/login/loginPage.vue";
import DestinationGallery from "@/views/travelPlan/DestinationGallery.vue";

// 도시별 좌표 정보
function getLatLng(cityName) {
  const coordinates = {
    '서울': { lat: 37.5666805, lng: 126.9784147 },
    '부산': { lat: 35.1795543, lng: 129.0756416 },
    '제주도': { lat: 33.4996213, lng: 126.5311884 },
    // 필요한 다른 도시들의 좌표를 추가
  }
  return coordinates[cityName] || { lat: 37.5666805, lng: 126.9784147 } // 기본값은 서울
}

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
  {
    path: "/chooseDate/:name",  // URL 파라미터로 name을 받도록 수정
    name: "chooseDate",
    component: () => import("@/views/travelPlan/chooseDate.vue"),
    props: (route) => ({
      name: route.params.name,
      id: route.query.id,
      latitude: getLatLng(route.params.name).lat,
      longitude: getLatLng(route.params.name).lng
    }),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;