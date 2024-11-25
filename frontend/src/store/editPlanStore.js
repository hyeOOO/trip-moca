import { defineStore } from 'pinia';
import api from '@/plugins/axios';
// import detailTestData from "@/assets/data/detailTestData.js";

export const usePlanStore = defineStore('editPlanStore', {
  state: () => ({
    originalPlan: {
      planId: null,
      planTitle: '',
      areaCode: null,
      sidoName: '',
      startDate: '',
      endDate: '',
      dayPlans: [],
    },
    editingPlan: {
      planId: null,
      planTitle: '',
      areaCode: null,
      sidoName: '',
      startDate: '',
      endDate: '',
      dayPlans: [],
    },
    cartItems: [],
  }),

  actions: {
    // 초기화 메서드
    async initializePlan(planId) {
      try {
        const response = await api.get(`/domain/plans/${planId}`);
        const planData = response.data;

        console.log('API 응답 데이터:', response.planData);

        const formattedPlan = {
          planId: planData.planId,
          planTitle: planData.planTitle,
          areaCode: planData.areaCode,
          sidoName: planData.sidoName,
          startDate: planData.startDate,
          endDate: planData.endDate,
          dayPlans: planData.dayPlans.map((day) => ({
            day: day.day,
            date: day.date,
            details: day.details.map((detail) => ({
              planDetailId: detail.planDetailId,
              attractionId: detail.attractionId,
              attractionTitle: detail.attractionTitle,
              image:
                !detail.image || detail.image.trim() === ''
                  ? 'https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png'
                  : detail.image,
              addr1: detail.addr1,
              addr2: detail.addr2,
              contentTypeId: detail.contentTypeId,
              contentTypeName: detail.contentTypeName,
              latitude: detail.latitude,
              longitude: detail.longitude,
              memo: detail.memo,
            })),
          })),
        };

        console.log(formattedPlan);

        this.originalPlan = JSON.parse(JSON.stringify(formattedPlan));
        this.editingPlan = JSON.parse(JSON.stringify(formattedPlan));

        console.log('orginalPlan : ', this.originalPlan);
        console.log('editingPlan : ', this.editingPlan);

        // Restore cart items if they exist
        const savedCartItems = localStorage.getItem('cartItems');
        if (savedCartItems) {
          this.cartItems = JSON.parse(savedCartItems);
        }
      } catch (error) {
        console.error('Plan initialization error:', error);
        throw error;
      }
    },

    // 편집 중인 계획을 원본으로 재설정
    resetToOriginal() {
      try {
        this.editingPlan = JSON.parse(JSON.stringify(this.originalPlan));
      } catch (error) {
        console.error('Reset to original error:', error);
        this.editingPlan = { ...this.originalPlan };
      }
    },

    // 편집 중인 계획 업데이트
    updateEditingPlan(newPlan) {
      try {
        if (!newPlan) throw new Error('Invalid plan data');
        this.editingPlan = JSON.parse(JSON.stringify(newPlan));
      } catch (error) {
        console.error('Update editing plan error:', error);
      }
    },

    // 계획 저장 (원본 계획에 적용)
    savePlan() {
      try {
        this.originalPlan = JSON.parse(JSON.stringify(this.editingPlan));
      } catch (error) {
        console.error('Save plan error:', error);
        this.originalPlan = { ...this.editingPlan };
      }
    },

    // 장바구니 항목 전체 설정 및 localStorage에 저장
    setCartItems(items) {
      try {
        this.cartItems = Array.isArray(items)
          ? JSON.parse(JSON.stringify(items))
          : [];
        localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
      } catch (error) {
        console.error('Set cart items error:', error);
        this.cartItems = [];
        localStorage.setItem('cartItems', '[]');
      }
    },

    // 장바구니에 항목 추가 및 localStorage 업데이트
    addCartItem(item) {
      try {
        if (!item) throw new Error('Invalid cart item');
        const newItem = JSON.parse(JSON.stringify(item));
        this.cartItems.push(newItem);
        localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
      } catch (error) {
        console.error('Add cart item error:', error);
      }
    },

    // 장바구니에서 항목 제거 및 localStorage 업데이트
    removeCartItem(index) {
      try {
        if (index < 0 || index >= this.cartItems.length)
          throw new Error('Invalid index');
        this.cartItems.splice(index, 1);
        localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
      } catch (error) {
        console.error('Remove cart item error:', error);
      }
    },

    // 장바구니 비우기 및 localStorage에서 삭제
    clearCartItems() {
      this.cartItems = [];
      localStorage.removeItem('cartItems');
    },
    removeFromCart(attractionId) {
      try {
        this.cartItems = this.cartItems.filter(item => item.attractionId !== attractionId);
        localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
      } catch (error) {
        console.error('Remove from cart error:', error);
      }
    },
  },

  getters: {
    getPlanData: (state) => {
      return (
        state.editingPlan || {
          planId: null,
          planTitle: '',
          areaCode: null,
          sidoName: '',
          startDate: '',
          endDate: '',
          dayPlans: [],
        }
      );
    },

    getCartItems: (state) => state.cartItems || [],

    getOriginalPlan: (state) => {
      return (
        state.originalPlan || {
          planId: null,
          planTitle: '',
          areaCode: null,
          sidoName: '',
          startDate: '',
          endDate: '',
          dayPlans: [],
        }
      );
    },

    hasChanges: (state) => {
      return (
        JSON.stringify(state.originalPlan) !== JSON.stringify(state.editingPlan)
      );
    },
  },
});
