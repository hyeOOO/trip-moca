import { defineStore } from 'pinia';
import detailTestData from "@/assets/data/detailTestData.js";

export const usePlanStore = defineStore('editPlanStore', {
  state: () => {
    const defaultPlan = {
      planId: null,
      planTitle: '',
      startDate: '',
      endDate: '',
      dayPlans: []
    };

    try {
      const savedCartItems = localStorage.getItem('cartItems');
      const initialPlan = JSON.parse(JSON.stringify(detailTestData)) || defaultPlan;
      
      return {
        originalPlan: initialPlan,
        editingPlan: initialPlan,
        cartItems: savedCartItems ? JSON.parse(savedCartItems) : [],
      };
    } catch (error) {
      console.error('State initialization error:', error);
      return {
        originalPlan: defaultPlan,
        editingPlan: defaultPlan,
        cartItems: [],
      };
    }
  },

  actions: {
    // 초기화 메서드
    initializePlan(planData = detailTestData) {
      try {
        const validPlanData = planData || {
          planId: null,
          planTitle: '',
          startDate: '',
          endDate: '',
          dayPlans: []
        };

        this.originalPlan = JSON.parse(JSON.stringify(validPlanData));
        this.editingPlan = JSON.parse(JSON.stringify(validPlanData));
        
        // localStorage에서 장바구니 항목 복원
        const savedCartItems = localStorage.getItem('cartItems');
        if (savedCartItems) {
          this.cartItems = JSON.parse(savedCartItems);
        }
      } catch (error) {
        console.error('Plan initialization error:', error);
        this.originalPlan = {
          planId: null,
          planTitle: '',
          startDate: '',
          endDate: '',
          dayPlans: []
        };
        this.editingPlan = { ...this.originalPlan };
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
        this.cartItems = Array.isArray(items) ? JSON.parse(JSON.stringify(items)) : [];
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
        if (index < 0 || index >= this.cartItems.length) throw new Error('Invalid index');
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
  },

  getters: {
    // 편집 중인 계획 반환
    getPlanData: (state) => {
      return state.editingPlan || {
        planId: null,
        planTitle: '',
        startDate: '',
        endDate: '',
        dayPlans: []
      };
    },

    // 장바구니 항목 반환
    getCartItems: (state) => state.cartItems || [],
  },
});