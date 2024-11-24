import { defineStore } from 'pinia';
import api from '@/plugins/axios';

export const useAiRecommendPlanStore = defineStore('aiRecommendPlan', {
  state: () => ({
    selectedDestination: {
      id: null,
      areaCode: '',
      areaName: '',
      title: '',
      image: '',
      numberOfDays: 0,
      info: '',
    },
    recommendPlan: {
      planTitle: '',
      dayPlans: [], // [{day: 1, details: []}, {day: 2, details: []}, ...]
    },
  }),

  getters: {
    getPlanData: (state) => state.recommendPlan,
    getTotalDays: (state) => state.selectedDestination.numberOfDays,
    getMapCoordinates: (state) => {
      const firstDay = state.recommendPlan.dayPlans[0];
      const firstSpot = firstDay?.details[0];
      return {
        latitude: firstSpot?.latitude || 33.3846,
        longitude: firstSpot?.longitude || 126.5534,
      };
    },
  },

  actions: {
    // AI 추천 받은 여행 계획 저장
    async setRecommendPlan(plan) {
      if (!plan || !Array.isArray(plan)) {
        console.error('Invalid plan data:', plan);
        return false;
      }

      try {
        this.recommendPlan = {
          planTitle: `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`,
          dayPlans: plan.map((dayPlan) => {
            if (!dayPlan.attractionDetails) {
              console.error('Missing attractionDetails in dayPlan:', dayPlan);
              return { day: dayPlan.day, details: [] };
            }

            return {
              day: dayPlan.day,
              details: dayPlan.attractionDetails.map((detail) => ({
                attractionId: detail.attractionId,
                attractionTitle: detail.title,
                image: detail.image1 || '',
                contentTypeId: detail.contentTypeId || null, // null 대신 실제 값 사용
                contentTypeName: detail.contentTypeName || '', // 빈 문자열 대신 실제 값 사용
                latitude: Number(detail.latitude),
                longitude: Number(detail.longitude),
                addr1: detail.addr1 || '',
                memo: '',
                sequence:
                  dayPlan.attractions?.findIndex(
                    (a) => a.title === detail.title
                  ) + 1 || 1,
              })),
            };
          }),
        };
        console.log('Plan after setting:', this.recommendPlan); // 디버깅용
        return true;
      } catch (error) {
        console.error('Error in setRecommendPlan:', error);
        this.recommendPlan = {
          planTitle: `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`,
          dayPlans: [],
        };
        return false;
      }
    },

    // API 호출하여 여행 계획 받아오기
    async generateAiPlan() {
      try {
        if (
          !this.selectedDestination?.areaCode ||
          !this.selectedDestination?.numberOfDays
        ) {
          throw new Error('목적지 정보가 없습니다.');
        }

        this.recommendPlan = {
          planTitle: '',
          dayPlans: [],
        };

        const response = await api.get(
          `/api/attraction/ai/plan/${this.selectedDestination.areaCode}/${this.selectedDestination.numberOfDays}`
        );

        if (!response.data) {
          throw new Error('API 응답에 데이터가 없습니다.');
        }

        // 데이터 확인용 로그
        console.log('API Response:', response.data);

        this.recommendPlan = {
          planTitle: `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`,
          dayPlans: response.data.map((dayPlan) => ({
            day: dayPlan.day,
            details: dayPlan.attractionDetails.map((detail) => ({
              attractionId: detail.attractionId,
              attractionTitle: detail.title,
              image: detail.image1 || '',
              contentTypeId: detail.contentTypeId, // 수정
              contentTypeName: detail.contentTypeName, // 수정
              latitude: Number(detail.latitude),
              longitude: Number(detail.longitude),
              addr1: detail.addr1 || '',
              memo: '',
              sequence:
                dayPlan.attractions?.findIndex(
                  (a) => a.title === detail.title
                ) + 1 || 1,
            })),
          })),
        };

        console.log('Generated plan:', this.recommendPlan); // 디버깅용
        return true;
      } catch (error) {
        console.error('AI 여행 계획 생성 실패:', error);
        throw error;
      }
    },

    // 나머지 액션들은 그대로 유지
    updatePlan(newPlan) {
      if (!newPlan || !Array.isArray(newPlan.dayPlans)) {
        console.error('Invalid plan update data');
        return false;
      }
      this.recommendPlan = {
        ...newPlan,
        planTitle:
          newPlan.planTitle ||
          `${this.selectedDestination.areaName} ${this.selectedDestination.numberOfDays}일 여행`,
      };
      return true;
    },

    updateDayPlanSequence(day, newDetails) {
      if (!Array.isArray(newDetails)) {
        console.error('Invalid details array');
        return false;
      }

      const dayPlanIndex = this.recommendPlan.dayPlans.findIndex(
        (p) => p.day === day
      );
      if (dayPlanIndex !== -1) {
        this.recommendPlan.dayPlans[dayPlanIndex].details = newDetails.map(
          (detail, index) => ({
            ...detail,
            sequence: index + 1,
          })
        );
        return true;
      }
      return false;
    },

    removePlace(day, placeIndex) {
      const dayPlan = this.recommendPlan.dayPlans.find((p) => p.day === day);
      if (dayPlan && dayPlan.details[placeIndex]) {
        dayPlan.details.splice(placeIndex, 1);
        dayPlan.details = dayPlan.details.map((detail, index) => ({
          ...detail,
          sequence: index + 1,
        }));
        return true;
      }
      return false;
    },

    resetStore() {
      this.selectedDestination = {
        id: null,
        areaCode: '',
        areaName: '',
        title: '',
        image: '',
        numberOfDays: 0,
        info: '',
      };
      this.recommendPlan = {
        planTitle: '',
        dayPlans: [],
      };
    },
  },
});
