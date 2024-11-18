import { defineStore } from 'pinia';

export const useTravelStore = defineStore('travel', {
  state: () => ({
    name: '',
    startDate: '',
    endDate: '',
    formattedDateRange: '',
    selectedPlaces: {},
    latitude: null,
    longitude: null,
    id: null
  }),

  actions: {
    initializeState(data) {
      this.name = data.name || '';
      this.startDate = data.startDate || '';
      this.endDate = data.endDate || '';
      this.formattedDateRange = data.formattedDateRange || '';
      this.selectedPlaces = data.selectedPlaces || {};
      this.latitude = data.latitude || null;
      this.longitude = data.longitude || null;
      this.id = data.id || null;
    },

    updateDates({ startDate, endDate, formattedDateRange }) {
      this.startDate = startDate;
      this.endDate = endDate;
      this.formattedDateRange = formattedDateRange;
    },

    updateSelectedPlaces(selectedPlaces) {
      this.selectedPlaces = { ...selectedPlaces };
    },

    resetDates() {
      this.startDate = '';
      this.endDate = '';
      this.formattedDateRange = '';
    },

    resetAll() {
      this.startDate = '';
      this.endDate = '';
      this.formattedDateRange = '';
      this.selectedPlaces = {};
      this.name = '';
      this.latitude = null;
      this.longitude = null;
      this.id = null;
    }
  },
  
  persist: true, // 페이지 새로고침시에도 데이터 유지
});