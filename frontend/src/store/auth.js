// store/auth.js
import { defineStore } from 'pinia';
import api from '@/plugins/axios'; // 우리가 만든 api instance 사용
import { jwtDecode } from 'jwt-decode';

export const useAuthStore = defineStore('auth', {
  // 인증 관련 상태 관리
  state: () => ({
    // localStorage에서 토큰을 가져오거나 없으면 null
    accessToken: localStorage.getItem('accessToken') || null,
    // 로그인 상태
    isAuthenticated: false,
    memberId: null,
  }),

  actions: {
    // 로그인 처리
    async login(loginRequest) {
      try {
        const response = await api.post('/api/auth/login', loginRequest);
        const accessToken = response.data;

        if (accessToken) {
          // jwt 토큰을 디코딩해서 memberId 추출
          const decoded = jwtDecode(accessToken);
          this.memberId = decoded.sub;
          alert(this.memberId + '님 반갑습니다!');

          this.updateToken(accessToken);
          return true;
        }
        return false;
      } catch (error) {
        console.error('Login failed:', error);
        return false;
      }
    },

    // 로그아웃 처리
    async logout() {
      try {
        // 서버에 로그아웃 요청 (Redis의 리프레시 토큰 삭제)
        await api.post('/api/auth/logout');
      } finally {
        // 로컬의 인증 정보 초기화
        this.clearToken();
      }
    },

    // 토큰 업데이트 통합 메서드
    updateToken(token) {
      // 브라우저 localStorage에 토큰 저장
      localStorage.setItem('accessToken', token);
      // Pinia store 상태 업데이트
      this.accessToken = token;
      this.isAuthenticated = true;
      // axios 전역 설정에 토큰 추가
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    },

    // 토큰 제거 통합 메서드
    clearToken() {
      // 브라우저 localStorage에서 토큰 제거
      localStorage.removeItem('accessToken');
      // Pinia store 상태 초기화
      this.accessToken = null;
      this.isAuthenticated = false;
      this.memberId = null; // memberId도 초기화
      // axios 전역 설정에서 토큰 제거
      delete api.defaults.headers.common['Authorization'];
    },
  },
});
