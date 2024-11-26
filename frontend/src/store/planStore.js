import { defineStore } from "pinia";

export const usePlanStore = defineStore("createPlan", {
  state: () => ({
    selectedDestination: {
      id: null,
      areaCode: "",
      areaName: "",
      title: "",
      image: "",
    },
    dates: {
      startDate: "",
      endDate: "",
      formattedDateRange: "",
    },
    selectedPlaces: [],
  }),

  actions: {
    setDestination(destination) {
      this.selectedDestination = {
        id: destination.id || destination.areaCode,
        areaCode: destination.areaCode || destination.id,
        areaName: destination.areaName,
        title: destination.title || destination.areaName,
        image: destination.image || 'https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png'
      };
    },

    setDates(startDate, endDate) {
      this.dates.startDate = startDate;
      this.dates.endDate = endDate;
      this.dates.formattedDateRange = this.formatDateRange(startDate, endDate);
    },

    // 선택된 장소들을 저장하는 액션 수정
    setSelectedPlaces(places) {
      this.selectedPlaces = places.map((dayPlaces) => ({
        day: dayPlaces.day,
        details: dayPlaces.details.map((detail) => ({
          attractionId: detail.attractionId,
          sequence: detail.sequence,
          memo: detail.memo || "",
        })),
      }));
    },

    formatDateRange(startDate, endDate) {
      if (!startDate || !endDate) return "";

      const formatDate = (dateString) => {
        const date = new Date(dateString);
        const days = ["일", "월", "화", "수", "목", "금", "토"];
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const dayOfWeek = days[date.getDay()];

        return `${year}.${month}.${day}(${dayOfWeek})`;
      };

      return `${formatDate(startDate)} - ${formatDate(endDate)}`;
    },

    resetStore() {
      this.selectedDestination = {
        id: null,
        areaCode: "",
        areaName: "",
        title: "",
        image: "",
      };
      this.dates = {
        startDate: "",
        endDate: "",
        formattedDateRange: "",
      };
      this.selectedPlaces = [];
    },
  },
});
