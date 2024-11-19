import { defineStore } from 'pinia'
import detailTestData from "@/assets/data/detailTestData.js"

export const usePlanStore = defineStore('plan', {
  state: () => ({
    originalPlan: { ...detailTestData },  // 초기값 설정
    editingPlan: { ...detailTestData },   // 초기값 설정
  }),

  actions: {
    initializePlan(planData = detailTestData) {
      this.originalPlan = JSON.parse(JSON.stringify(planData))
      this.editingPlan = JSON.parse(JSON.stringify(planData))
    },
    
    resetToOriginal() {
      this.editingPlan = JSON.parse(JSON.stringify(this.originalPlan))
    },
    
    updateEditingPlan(newPlan) {
      this.editingPlan = newPlan
    },
    
    savePlan() {
      this.originalPlan = JSON.parse(JSON.stringify(this.editingPlan))
    }
  },
  
  getters: {
    getPlanData: (state) => state.editingPlan
  }
})