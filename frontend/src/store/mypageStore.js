// stores/mypageStore.js
import { defineStore } from "pinia";
import api from "@/plugins/axios";

export const useMypageStore = defineStore("mypage", {
  state: () => ({
    userCards: [],
    userPlans: [],
    totalCards: [],
    userInfo: null,
    currentWidth: 0,
    targetWidth: 0,
    animationStarted: false,
  }),

  getters: {
    progressStyle() {
      return {
        width: `${this.currentWidth}%`,
        transition: "width 1.2s ease-out",
      };
    },
  },

  actions: {
    async fetchCardData() {
      try {
        const [totalCardsResponse, userCardsResponse] = await Promise.all([
          api.get("/domain/card/list"),
          api.get("/domain/card/user-card"),
        ]);

        this.totalCards = totalCardsResponse.data;
        this.userCards = userCardsResponse.data;

        // 진행률 계산 - 수정된 부분
        if (this.totalCards.length > 0) {
          const percentage =
            this.userCards.length > 0 ? (this.userCards.length / this.totalCards.length) * 100 : 0; // 보유 카드가 없으면 0%로 설정

          this.animationStarted = true;
          this.currentWidth = 0;

          requestAnimationFrame(() => {
            this.currentWidth = percentage;
          });
        }
      } catch (error) {
        console.error("카드 데이터 조회 실패:", error);
      }
    },

    async fetchPlanData() {
      try {
        const response = await api.get("/domain/plans");
        this.userPlans = response.data;
      } catch (error) {
        console.error("여행 계획 데이터 조회 실패:", error);
      }
    },

    resetProgress() {
      this.currentWidth = 0;
      this.targetWidth = 0;
      this.animationStarted = false;
    },

    async deleteUserData() {
      try {
        await api.delete("/domain/member/delete");

        // localStorage 비우기
        localStorage.removeItem("accessToken");

        // 메인 페이지로 이동 및 새로고침
        window.location.href = "/"; // 또는 특정 로그인 페이지 경로
      } catch (error) {
        console.error("유저 데이터 삭제 실패:", error);
        throw error;
      }
    },

    async updateUserData(updateData) {
      try {
        const response = await api.patch("/domain/member/update", updateData);
        if (response.status === 200) {
          this.userInfo = response.data;
          return response.data;
        }
      } catch (error) {
        if (error.response?.status === 401) {
          throw new Error("현재 비밀번호가 일치하지 않습니다.");
        }
        throw error;
      }
    },

    async fetchUserData() {
      try {
        const response = await api.get("/domain/member/info");
        this.userInfo = response.data;
        return response.data;
      } catch (error) {
        console.error("유저 데이터 조회 실패:", error);
        throw error;
      }
    },

    async deletePlan(planId) {
      try {
        await api.delete(`/domain/plans/${planId}`);
        // 삭제 성공 후 목록 다시 불러오기
        await this.fetchPlanData();
        // 또는 로컬 상태 업데이트
        this.userPlans = this.userPlans.filter((plan) => plan.planId !== planId);
      } catch (error) {
        console.error("여행 계획 삭제 실패:", error);
        alert("여행 계획 삭제에 실패했습니다.");
      }
    },

    async checkPassword(password) {
      try {
        const response = await api.post("/domain/member/check-password", { password });
        return response.data; // 백엔드에서 반환하는 boolean 값
      } catch (error) {
        console.error("비밀번호 확인 실패:", error);
        throw error; // 에러를 상위로 전파
      }
    },
  },
});
