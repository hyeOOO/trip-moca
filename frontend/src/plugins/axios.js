import axios from "axios";
import { useAuthStore } from "@/store/auth";
import { useRouter } from "vue-router"; // router import 추가

// axios 인스턴스 생성
const api = axios.create({
  baseURL: "http://localhost:8081",
  timeout: 5000,
});

// 요청 인터셉터 설정
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
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
    const newToken = response.headers["authorization"];
    const tokenRenewed = response.headers["token-renewed"];

    if (tokenRenewed === "true" && newToken) {
      const token = newToken.replace("Bearer ", "");
      const authStore = useAuthStore();
      authStore.updateToken(token);
    }

    return response;
  },
  async (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      const router = useRouter(); // router 인스턴스 생성
      authStore.clearToken();
      router.push("/login");
    }
    return Promise.reject(error);
  }
);

export default api;
