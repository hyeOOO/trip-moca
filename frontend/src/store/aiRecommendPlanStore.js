import { defineStore } from "pinia";

export const useAiRecommendPlanStore = defineStore("aiRecommendPlan", {
  state: () => ({
    selectedDestination: {
      id: null,
      areaCode: "",
      areaName: "",
      title: "",
      image: "",
      numberOfDays: 0,
      info: "",
    },
    recommendPlan: {
      planTitle: "",
      dayPlans: [] // [{day: 1, details: []}, {day: 2, details: []}, ...]
    }
  }),

  getters: {
    getPlanData: (state) => state.recommendPlan,
    getTotalDays: (state) => state.selectedDestination.numberOfDays,
    // 지도 좌표는 제주도로 고정
    getMapCoordinates: () => ({
      latitude: 33.3846,
      longitude: 126.5534
    }),
  },

  actions: {
    // AI 추천 받은 여행 계획 저장
    async setRecommendPlan(plan) {
      // 제주도가 아닌 경우 빈 계획 반환
      if (this.selectedDestination.id !== 39 || this.selectedDestination.areaCode !== "39") {
        console.log('Currently, AI recommendations are only available for Jeju Island');
        this.recommendPlan = {
          planTitle: `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`,
          dayPlans: Array.from({ length: this.selectedDestination.numberOfDays }, (_, i) => ({
            day: i + 1,
            details: []
          }))
        };
        return false;
      }

      // 제주도인 경우에만 더미 데이터 처리
      if (!plan || !Array.isArray(plan)) {
        console.error('Invalid plan data:', plan);
        return false;
      }

      try {
        // 선택된 일수에 맞게 데이터 잘라내기 (3일 초과 선택 시에도 3일치만 사용)
        const maxDays = Math.min(this.selectedDestination.numberOfDays, 3);
        const limitedPlan = plan.slice(0, maxDays);

        this.recommendPlan = {
          planTitle: `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`,
          dayPlans: limitedPlan.map((dayPlan) => {
            if (!dayPlan.attractionDetails) {
              console.error('Missing attractionDetails in dayPlan:', dayPlan);
              return { day: dayPlan.day, details: [] };
            }
            
            return {
              day: dayPlan.day,
              details: dayPlan.attractionDetails.map(detail => ({
                attractionId: detail.attractionId,
                attractionTitle: detail.title,
                image: detail.image1 || '',
                latitude: Number(detail.latitude) || 33.3846,
                longitude: Number(detail.longitude) || 126.5534,
                addr1: detail.addr1 || '',
                memo: "",
                sequence: dayPlan.attractions.findIndex(a => a.title === detail.title) + 1
              }))
            };
          })
        };

        // 3일 미만 선택 시 나머지 일자는 빈 배열로 채움
        if (this.selectedDestination.numberOfDays > 3) {
          for (let i = 4; i <= this.selectedDestination.numberOfDays; i++) {
            this.recommendPlan.dayPlans.push({
              day: i,
              details: []
            });
          }
        }

        return true;
      } catch (error) {
        console.error('Error in setRecommendPlan:', error);
        this.recommendPlan = {
          planTitle: `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`,
          dayPlans: []
        };
        return false;
      }
    },

    // 나머지 액션들은 동일...
    updatePlan(newPlan) {
      if (!newPlan || !Array.isArray(newPlan.dayPlans)) {
        console.error('Invalid plan update data');
        return false;
      }
      this.recommendPlan = {
        ...newPlan,
        planTitle: newPlan.planTitle || `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`
      };
      return true;
    },

    updateDayPlanSequence(day, newDetails) {
      if (!Array.isArray(newDetails)) {
        console.error('Invalid details array');
        return false;
      }
      
      const dayPlanIndex = this.recommendPlan.dayPlans.findIndex(p => p.day === day);
      if (dayPlanIndex !== -1) {
        this.recommendPlan.dayPlans[dayPlanIndex].details = newDetails.map((detail, index) => ({
          ...detail,
          sequence: index + 1
        }));
        return true;
      }
      return false;
    },

    removePlace(day, placeIndex) {
      const dayPlan = this.recommendPlan.dayPlans.find(p => p.day === day);
      if (dayPlan && dayPlan.details[placeIndex]) {
        dayPlan.details.splice(placeIndex, 1);
        dayPlan.details = dayPlan.details.map((detail, index) => ({
          ...detail,
          sequence: index + 1
        }));
        return true;
      }
      return false;
    },

    resetStore() {
      this.selectedDestination = {
        id: null,
        areaCode: "",
        areaName: "",
        title: "",
        image: "",
        numberOfDays: 0,
        info: "",
      };
      this.recommendPlan = {
        planTitle: "",
        dayPlans: []
      };
    }
  },
});