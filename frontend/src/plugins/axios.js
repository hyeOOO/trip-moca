import axios from 'axios';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'vue-router'; // router import 추가

// axios 인스턴스 생성
const api = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 5000,
});

// 요청 인터셉터 설정
api.interceptors.request.use(
  // 요청 보낼 때, 로컬 스토리지에서 액세스토큰을 꺼내서 요청 헤더에 포함해서 전달
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
  // 재발급 토큰에 대해 재저장
  (response) => {
    const newToken = response.headers['authorization'];
    const tokenRenewed = response.headers['token-renewed'];

    if (tokenRenewed === 'true' && newToken) {
      const token = newToken.replace('Bearer ', '');
      const authStore = useAuthStore();
      authStore.updateToken(token);
    }

    return response;
  },
  async (error) => {
    // 리프레시 토큰마저 만료되었다면 재로그인 유도
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      const router = useRouter(); // router 인스턴스 생성
      authStore.clearToken();
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default api;
