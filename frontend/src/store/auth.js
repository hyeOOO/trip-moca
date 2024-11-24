// store/auth.js
import { defineStore } from 'pinia';
import api from '@/plugins/axios'; // 우리가 만든 api instance 사용
import { jwtDecode } from 'jwt-decode';

export const useAuthStore = defineStore('auth', {
  // 인증 관련 상태 관리
  state: () => {
    // 초기 상태를 localStorage에서 복원
    const accessToken = localStorage.getItem('accessToken');
    let isAuthenticated = false;
    let memberId = null;

    // 저장된 토큰이 있다면 디코딩하여 상태 복원
    if (accessToken) {
      try {
        const decoded = jwtDecode(accessToken);
        memberId = decoded.sub;
        isAuthenticated = true;
        // API 인스턴스의 헤더도 복원
        api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
      } catch (error) {
        // 토큰이 유효하지 않은 경우 localStorage 정리
        localStorage.removeItem('accessToken');
        accessToken = null;
      }
    }

    return {
      accessToken,
      isAuthenticated,
      memberId,
      pendingAction: null,
    };
  },

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
