import { defineStore } from 'pinia';
import detailTestData from "@/assets/data/detailTestData.js";

export const usePlanStore = defineStore('plan', {
  state: () => {
    const savedCartItems = localStorage.getItem('cartItems');
    return {
      originalPlan: JSON.parse(JSON.stringify(detailTestData)), // 초기 계획
      editingPlan: JSON.parse(JSON.stringify(detailTestData)),  // 편집 중인 계획
      cartItems: savedCartItems ? JSON.parse(savedCartItems) : [], // localStorage에서 복원된 장바구니 항목
    };
  },

  actions: {
    // 초기화 메서드
    initializePlan(planData = detailTestData) {
      this.originalPlan = JSON.parse(JSON.stringify(planData));
      this.editingPlan = JSON.parse(JSON.stringify(planData));
      
      // localStorage에서 장바구니 항목 복원
      const savedCartItems = localStorage.getItem('cartItems');
      if (savedCartItems) {
        this.cartItems = JSON.parse(savedCartItems);
      }
    },
    
    // 편집 중인 계획을 원본으로 재설정
    resetToOriginal() {
      this.editingPlan = JSON.parse(JSON.stringify(this.originalPlan));
    },

    // 편집 중인 계획 업데이트
    updateEditingPlan(newPlan) {
      this.editingPlan = JSON.parse(JSON.stringify(newPlan));
    },

    // 계획 저장 (원본 계획에 적용)
    savePlan() {
      this.originalPlan = JSON.parse(JSON.stringify(this.editingPlan));
    },

    // 장바구니 항목 전체 설정 및 localStorage에 저장
    setCartItems(items) {
      this.cartItems = JSON.parse(JSON.stringify(items));
      localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
    },

    // 장바구니에 항목 추가 및 localStorage 업데이트
    addCartItem(item) {
      this.cartItems.push(JSON.parse(JSON.stringify(item)));
      localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
    },

    // 장바구니에서 항목 제거 및 localStorage 업데이트
    removeCartItem(index) {
      this.cartItems.splice(index, 1);
      localStorage.setItem('cartItems', JSON.stringify(this.cartItems));
    },

    // 장바구니 비우기 및 localStorage에서 삭제
    clearCartItems() {
      this.cartItems = [];
      localStorage.removeItem('cartItems');
    },
  },

  getters: {
    // 편집 중인 계획 반환
    getPlanData: (state) => state.editingPlan,

    // 장바구니 항목 반환
    getCartItems: (state) => state.cartItems,
  },
});
