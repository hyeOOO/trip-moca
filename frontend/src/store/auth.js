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
    // 로그인 필요한 작업을 저장할 상태 추가
    pendingAction: null,
  }),

  actions: {
    // 보류 중인 작업 설정
    setPendingAction(action) {
      this.pendingAction = action;
    },

    // 보류 중인 작업 실행 및 초기화
    async executePendingAction() {
      if (this.pendingAction) {
        const action = this.pendingAction;
        this.pendingAction = null; // 실행 전 초기화
        await action();
      }
    },

    async login(loginRequest) {
      try {
        const response = await api.post('/api/auth/login', loginRequest);
        const accessToken = response.data;

        if (accessToken) {
          const decoded = jwtDecode(accessToken);
          this.memberId = decoded.sub;
          alert(this.memberId + '님 반갑습니다!');

          this.updateToken(accessToken);

          // 보류 중인 작업이 있으면 실행
          await this.executePendingAction();
          return true;
        }
        return false;
      } catch (error) {
        console.error('Login failed:', error);
        return false;
      }
    },

    async logout() {
      try {
        await api.post('/api/auth/logout');
      } finally {
        this.clearToken();
      }
    },

    updateToken(token) {
      localStorage.setItem('accessToken', token);
      this.accessToken = token;
      this.isAuthenticated = true;
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    },

    clearToken() {
      localStorage.removeItem('accessToken');
      this.accessToken = null;
      this.isAuthenticated = false;
      this.memberId = null;
      this.pendingAction = null; // 보류 중인 작업도 초기화
      delete api.defaults.headers.common['Authorization'];
    },
  },
});
