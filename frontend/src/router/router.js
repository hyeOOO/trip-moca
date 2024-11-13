import { createWebHistory, createRouter } from "vue-router";
import Main from "@/views/mainPage/mainPage.vue";
import Login from "@/views/login/loginPage.vue";
import DestinationGallery from "@/views/travelPlan/DestinationGallery.vue";
import choosePlace from "@/views/travelPlan/choosePlace.vue";

// 도시별 좌표 정보
function getLatLng(cityName) {
  const coordinates = {
    '서울': { lat: (37.4288 + 37.7017) / 2, lng: (126.7644 + 127.1840) / 2 },
    '인천': { lat: (37.3794 + 37.7242) / 2, lng: (126.3644 + 126.7156) / 2 },
    '대전': { lat: (36.1098 + 36.4895) / 2, lng: (127.2465 + 127.5211) / 2 },
    '대구': { lat: (35.6016 + 36.0103) / 2, lng: (128.3420 + 128.7607) / 2 },
    '광주': { lat: (35.0905 + 35.2472) / 2, lng: (126.6518 + 126.9539) / 2 },
    '부산': { lat: (34.8799 + 35.3891) / 2, lng: (128.7369 + 129.3160) / 2 },
    '울산': { lat: (35.4669 + 35.7366) / 2, lng: (129.0756 + 129.4729) / 2 },
    '세종특별자치시': { lat: (36.4641 + 36.6284) / 2, lng: (127.1565 + 127.3649) / 2 },
    '경기도': { lat: (36.8924 + 38.2846) / 2, lng: (126.3923 + 127.8264) / 2 },
    '강원특별자치도': { lat: (37.0498 + 38.6149) / 2, lng: (127.0495 + 129.3722) / 2 },
    '충청북도': { lat: (36.0657 + 37.2173) / 2, lng: (127.2493 + 128.7959) / 2 },
    '충청남도': { lat: (35.9832 + 37.0291) / 2, lng: (125.9066 + 127.3458) / 2 },
    '경상북도': { lat: (35.7133 + 37.1909) / 2, lng: (127.8108 + 129.6919) / 2 },
    '경상남도': { lat: (34.5565 + 35.6753) / 2, lng: (127.4669 + 129.2876) / 2 },
    '전북특별자치도': { lat: (35.0641 + 35.9942) / 2, lng: (126.3322 + 127.8573) / 2 },
    '전라남도': { lat: (33.8968 + 35.4961) / 2, lng: (125.0662 + 127.5347) / 2 },
    '제주도': { lat: (33.1060 + 34.0070) / 2, lng: (126.1524 + 126.9848) / 2 }
  }
  return coordinates[cityName] || { lat: 37.5666805, lng: 126.9784147 } 
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
    path: "/chooseDate/:name",
    name: "chooseDate",
    component: () => import("@/views/travelPlan/chooseDate.vue"),
    props: (route) => ({
      name: route.params.name,
      id: route.query.id,
      latitude: getLatLng(route.params.name).lat,
      longitude: getLatLng(route.params.name).lng
    }),
  },
  {
    path: "/choosePlace/:name",
    name: "choosePlace",
    component: choosePlace,
    props: (route) => ({
      name: route.params.name,
      startDate: route.query.startDate || '',
      endDate: route.query.endDate || '',
      formattedDateRange: route.query.formattedDateRange || '',
      latitude: getLatLng(route.params.name)?.lat,
      longitude: getLatLng(route.params.name)?.lng,
      id: route.query.id
    })
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
});

export default router;