import { createWebHistory, createRouter } from 'vue-router';
import Main from '@/views/mainPage/mainPage.vue';
import DestinationGallery from '@/views/travelPlan/DestinationGallery.vue';
import ChooseDate from '@/views/travelPlan/chooseDate.vue';
import ChoosePlace from '@/views/travelPlan/choosePlace.vue';
import Mypage from '@/views/mypage/mypage.vue';
import MypagePlan from '@/views/mypage/mypagePlan.vue';
import MypageCard from '@/views/mypage/mypageCard.vue';
import { useAuthStore } from '@/store/auth';
import { showLoginModalFlag } from '@/eventBus';
import SavePlan from '@/views/travelPlan/savePlan.vue';
import TmapSearch from '@/components/TmapSearch.vue';
import RouteSearch from '@/views/routeSearch/routeSearch.vue';
import searchPlace from '@/views/search/searchPlace.vue';
import popularPlace from '@/views/popular/popularPlace.vue';

function getLatLng(cityName) {
  const coordinates = {
    서울: { lat: (37.4288 + 37.7017) / 2, lng: (126.7644 + 127.184) / 2 },
    인천: { lat: (37.3794 + 37.7242) / 2, lng: (126.3644 + 126.7156) / 2 },
    대전: { lat: (36.1098 + 36.4895) / 2, lng: (127.2465 + 127.5211) / 2 },
    대구: { lat: (35.6016 + 36.0103) / 2, lng: (128.342 + 128.7607) / 2 },
    광주: { lat: (35.0905 + 35.2472) / 2, lng: (126.6518 + 126.9539) / 2 },
    부산: { lat: (34.8799 + 35.3891) / 2, lng: (128.7369 + 129.316) / 2 },
    울산: { lat: (35.4669 + 35.7366) / 2, lng: (129.0756 + 129.4729) / 2 },
    세종특별자치시: {
      lat: (36.4641 + 36.6284) / 2,
      lng: (127.1565 + 127.3649) / 2,
    },
    경기도: { lat: (36.8924 + 38.2846) / 2, lng: (126.3923 + 127.8264) / 2 },
    강원특별자치도: {
      lat: (37.0498 + 38.6149) / 2,
      lng: (127.0495 + 129.3722) / 2,
    },
    충청북도: { lat: (36.0657 + 37.2173) / 2, lng: (127.2493 + 128.7959) / 2 },
    충청남도: { lat: (35.9832 + 37.0291) / 2, lng: (125.9066 + 127.3458) / 2 },
    경상북도: { lat: (35.7133 + 37.1909) / 2, lng: (127.8108 + 129.6919) / 2 },
    경상남도: { lat: (34.5565 + 35.6753) / 2, lng: (127.4669 + 129.2876) / 2 },
    전북특별자치도: {
      lat: (35.0641 + 35.9942) / 2,
      lng: (126.3322 + 127.8573) / 2,
    },
    전라남도: { lat: (33.8968 + 35.4961) / 2, lng: (125.0662 + 127.5347) / 2 },
    제주도: { lat: (33.106 + 34.007) / 2, lng: (126.1524 + 126.9848) / 2 },
  };
  return coordinates[cityName] || { lat: 37.5666805, lng: 126.9784147 };
}

const routes = [
  {
    path: '/routeSearch',
    name: 'RouteSearch',
    component: RouteSearch,
  },
  {
    path: '/search',
    name: 'searchPlace',
    component: searchPlace,
  },
  {
    path: '/',
    name: 'main',
    component: Main,
  },
  {
    path: '/travelPlan',
    name: 'travelPlan',
    component: DestinationGallery,
  },
  {
    path: '/popularPlace',
    name: 'popularPlace',
    component: popularPlace,
  },
  {
    path: '/chooseDate/:name',
    name: 'chooseDate',
    component: ChooseDate,
    props: (route) => {
      const selectedPlaces = route.params.selectedPlaces || {};
      return {
        name: route.params.name,
        id: route.query.id,
        latitude: getLatLng(route.params.name).lat,
        longitude: getLatLng(route.params.name).lng,
        selectedPlaces: selectedPlaces,
        startDate: '',
        endDate: '',
        formattedDateRange: '',
      };
    },
  },
  {
    path: '/choosePlace/:name',
    name: 'choosePlace',
    component: ChoosePlace,
    props: (route) => ({
      name: route.params.name,
      startDate: route.query.startDate || '',
      endDate: route.query.endDate || '',
      formattedDateRange: route.query.formattedDateRange || '',
      latitude: getLatLng(route.params.name)?.lat,
      longitude: getLatLng(route.params.name)?.lng,
      id: route.query.id,
      selectedPlaces: route.params.selectedPlaces
        ? JSON.parse(route.params.selectedPlaces)
        : {}, // JSON 파싱
    }),
  },
  {
    path: '/save-plan/:name',
    name: 'savePlan',
    component: SavePlan,
    props: (route) => ({
      name: route.params.name,
      startDate: route.query.startDate || '',
      endDate: route.query.endDate || '',
      formattedDateRange: route.query.formattedDateRange || '',
      selectedPlaces: route.params.selectedPlaces || {},
      latitude: getLatLng(route.params.name).lat,
      longitude: getLatLng(route.params.name).lng,
      id: route.query.id,
    }),
  },
  {
    path: '/mypage',
    component: Mypage,
    meta: { requiresAuth: true }, // 인증 필요 표시
    children: [
      {
        path: '',
        redirect: '/mypage/plan',
      },
      {
        path: 'plan',
        component: MypagePlan,
        meta: { requiresAuth: true },
      },
      {
        path: 'card',
        component: MypageCard,
        meta: { requiresAuth: true },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from) {
    // savedPosition 매개변수 제거
    if (to.path.startsWith('/mypage') && from.path.startsWith('/mypage')) {
      return false;
    }
    return { top: 0 };
  },
});

// 네비게이션 가드 추가
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const isAuthenticated = authStore.isAuthenticated;

  // 인증이 필요한 페이지에 접근하려 할 때
  if (to.matched.some((record) => record.meta.requiresAuth)) {
    if (!isAuthenticated) {
      showLoginModalFlag.value = true; // 로그인 모달 표시
      next('/');
    } else {
      next();
    }
  } else {
    next(); // 인증이 필요없는 페이지는 그냥 진행
  }
});

export default router;
