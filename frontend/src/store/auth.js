// store/auth.js
import { defineStore } from "pinia";
import axios from "axios";

export const useAuthStore = defineStore("auth", {
  // 인증 관련 상태 관리
  state: () => ({
    // localStorage에서 토큰을 가져오거나 없으면 null
    accessToken: localStorage.getItem("accessToken") || null,
    // 로그인 상태
    isAuthenticated: false,
  }),

  actions: {
    // 로그인 처리
    async login(loginRequest) {
      try {
        const response = await axios.post("/api/auth/login", loginRequest);
        const { accessToken } = response.data;

        // 로그인 성공 시 토큰 저장 및 상태 업데이트
        this.updateToken(accessToken);
        return true;
      } catch (error) {
        console.error("Login failed:", error);
        return false;
      }
    },

    // 로그아웃 처리
    async logout() {
      try {
        // 서버에 로그아웃 요청 (Redis의 리프레시 토큰 삭제)
        await axios.post("/api/auth/logout");
      } finally {
        // 로컬의 인증 정보 초기화
        this.clearToken();
      }
    },

    // 토큰 업데이트 통합 메서드
    updateToken(token) {
      // 브라우저 localStorage에 토큰 저장
      localStorage.setItem("accessToken", token);
      // Pinia store 상태 업데이트
      this.accessToken = token;
      this.isAuthenticated = true;
      // axios 전역 설정에 토큰 추가
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    },

    // 토큰 제거 통합 메서드
    clearToken() {
      // 브라우저 localStorage에서 토큰 제거
      localStorage.removeItem("accessToken");
      // Pinia store 상태 초기화
      this.accessToken = null;
      this.isAuthenticated = false;
      // axios 전역 설정에서 토큰 제거
      delete axios.defaults.headers.common["Authorization"];
    },
  },
});
