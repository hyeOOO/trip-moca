import axios from 'axios';

// auth store를 가져오는 함수 정의
let getAuthStore;
export const setAuthStoreGetter = (authStoreFn) => {
  getAuthStore = authStoreFn;
};

// router를 가져오는 함수 정의
let getRouter;
export const setRouterGetter = (routerFn) => {
  getRouter = routerFn;
};

// axios 인스턴스 생성
const api = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 60000,
});

// 요청 인터셉터 설정
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 설정
api.interceptors.response.use(
  (response) => {
    const newToken = response.headers['authorization'];
    const tokenRenewed = response.headers['token-renewed'];

    if (tokenRenewed === 'true' && newToken) {
      const token = newToken.replace('Bearer ', '');
      const authStore = getAuthStore?.();
      if (authStore) {
        authStore.updateToken(token);
      }
    }

    return response;
  },
  async (error) => {
    // 에러 응답이 있고, status가 401일 때만 처리
    if (error.response) {
      const { status } = error.response;

      if (status === 401) {
        const authStore = getAuthStore?.();
        const router = getRouter?.();
        if (authStore) {
          authStore.clearToken();
        }
        if (router) {
          router.push('/main');
        }
      }

      // 에러 응답이 있는 경우 해당 에러를 그대로 전달
      return Promise.reject(error);
    }

    // 그 외의 경우 (네트워크 에러 등)
    return Promise.reject(error);
  }
);

export default api;
