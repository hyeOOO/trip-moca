<template>
  <div class="layout-container">
    <navBar />
    <div
      class="content-wrapper"
      :class="{ collapsed: isCollapsed, 'right-collapsed': isRightCollapsed }"
    >
      <!-- Left Sidebar -->
      <div class="steps-sidebar">
        <div class="steps-nav">
          <div class="step" @click="goToDateSelection">
            <div class="step-number">STEP 1</div>
            <div class="step-title">날짜 선택</div>
          </div>
          <div class="step" :class="{ active: isStep2Active }" @click="toggleStep2">
            <div class="step-number">STEP 2</div>
            <div class="step-title">장소 선택</div>
          </div>
          <div class="step" @click="checkAndNavigateToSavePlan">
            <div class="step-number">STEP 3</div>
            <div class="step-title">계획 생성</div>
          </div>
        </div>
      </div>

      <!-- Middle Section -->
      <div class="middle-section">
        <div class="toggle-button" @click="toggleCollapse">
          <i
            class="fa-solid"
            :class="{
              'fa-arrow-left': !isCollapsed,
              'fa-arrow-right': isCollapsed,
            }"
          ></i>
        </div>
        <div class="header">
          <h2>{{ name }}</h2>
          <p v-if="formattedDateRange" class="date-range">
            {{ formattedDateRange }}
          </p>
        </div>
        <div class="search-section">
          <div class="search-box">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="여행지를 검색하세요"
              @keyup.enter="handleSearch"
            />
            <i class="fa-solid fa-search" @click="handleSearch"></i>
          </div>
        </div>
        <div class="places-list">
          <div v-if="isLoading && currentPage === 0" class="loading-state">
            데이터를 불러오는 중...
          </div>
          <div v-else-if="error" class="error-state">
            {{ error }}
          </div>
          <div
            v-for="place in filteredPlaces"
            :key="place.attractionId"
            class="place-item"
            draggable="true"
            @dragstart="dragStart($event, place)"
          >
            <div class="place-image">
              <img :src="getImageUrl(place.image1)" :alt="place.title" />
            </div>
            <div class="place-info">
              <h3 class="place-title">{{ place.title }}</h3>
              <p class="place-address">{{ place.addr1 }}</p>
            </div>
          </div>
          <div v-if="isFetching && currentPage > 0" class="loading-more">
            추가 데이터를 불러오는 중...
          </div>
        </div>
      </div>

      <!-- Right Section -->
      <div class="right-section">
        <div class="toggle-button" @click="toggleRightSection">
          <i
            class="fa-solid"
            :class="{
              'fa-arrow-left': !isRightCollapsed,
              'fa-arrow-right': isRightCollapsed,
            }"
          ></i>
        </div>
        <div class="header">
          <h2>선택한 장소</h2>
        </div>
        <div class="selected-places">
          <div
            v-for="dayIndex in numberOfDays"
            :key="dayIndex - 1"
            class="day-section"
            @dragover.prevent
            @drop="onDrop($event, dayIndex - 1)"
          >
            <div class="day-header">
              <h3 @click="selectDay(dayIndex - 1)" style="cursor: pointer">
                {{ dayIndex }}일차 {{ formatDate(getTripDate(dayIndex - 1)) }}
              </h3>
              <div class="day-header-buttons">
                <button @click="showAllMarkers" class="view-all-button">전체보기</button>
                <button @click="clearDay(dayIndex - 1)" class="clear-button">
                  <i class="fa-solid fa-rotate-left"></i> 초기화
                </button>
              </div>
            </div>
            <div v-if="!selectedPlacesByDay[dayIndex - 1]?.length" class="empty-day">
              <p></p>
            </div>
            <div v-else class="selected-day-places">
              <div
                v-for="(place, index) in selectedPlacesByDay[dayIndex - 1]"
                :key="place.attractionId"
                class="selected-place"
                draggable="true"
                @dragstart="
                  dragStartSelected($event, place, dayIndex - 1, index)
                "
                @dragover.prevent
                @drop.stop="onDrop($event, dayIndex - 1, index)"
              >
                <div class="order-number">{{ index + 1 }}</div>
                <div class="place-image">
                  <img :src="getImageUrl(place.image1)" :alt="place.title" />
                </div>
                <div class="place-info">
                  <h4>{{ place.title }}</h4>
                  <button @click="removePlace(dayIndex - 1, place)" class="remove-button">
                    <i class="fa-solid fa-times"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <button @click="saveAndNavigate" class="save-button">저장</button>
      </div>
      <div class="map-container">
        <Tmap
          ref="tmap"
          :latitude="latitude"
          :longitude="longitude"
          :selected-places-by-day="selectedPlacesByDay"
          :selected-day="selectedDay"
          :show-all-days="showAllDays"
        />
        <!-- selected-places-by-day 선택한 장소에 대한 정보들 -->
      </div>
    </div>
  </div>
</template>

<script>
import navBar from "@/components/navBar.vue";

import Tmap from "@/components/Tmap/Tmap.vue";
import { useAuthStore } from "@/store/auth"; // auth store 추가
import { usePlanStore } from "@/store/planStore";
import api from "@/plugins/axios";

export default {
  name: "ChoosePlace",
  components: {
    navBar,
    Tmap,
  },
  props: {
    name: String,
    latitude: Number,
    longitude: Number,
  },
  setup() {
    const planStore = usePlanStore();
    const authStore = useAuthStore(); // auth store 설정
    return { planStore, authStore };
  },
  data() {
    return {
      searchQuery: "",
      places: [], // 빈 배열로 초기화
      isCollapsed: false,
      isRightCollapsed: false,
      isStep2Active: true,
      selectedDay: null,
      showAllDays: false,
      isLoading: false, // 로딩 상태 추가
      error: null, // 에러 상태 추가
      currentPage: 0,
      totalPages: 0,
      pageSize: 15,
      isLastPage: false,
      isFetching: false,
      localSelectedPlaces: {}, // selectedPlacesByDay를 대체

    };
  },
  computed: {
    startDate() {
      return this.planStore.dates.startDate;
    },
    endDate() {
      return this.planStore.dates.endDate;
    },
    formattedDateRange() {
      return this.planStore.dates.formattedDateRange;
    },
    areaCode() {
      return this.planStore.selectedDestination.areaCode;
    },
    filteredPlaces() {
      if (!this.searchQuery) return this.places;
      const query = this.searchQuery.toLowerCase();
      return this.places.filter(
        (place) =>
          place.title.toLowerCase().includes(query) || place.addr1.toLowerCase().includes(query)
      );
    },
    numberOfDays() {
      if (!this.startDate || !this.endDate) return 0;
      const start = new Date(this.startDate);
      const end = new Date(this.endDate);
      return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    },
    selectedPlacesByDay: {
      get() {
        return this.localSelectedPlaces;
      },
      set(newValue) {
        this.localSelectedPlaces = newValue;
        const formattedPlaces = Object.entries(newValue).map(([day, places]) => ({
          day: parseInt(day),
          details: places.map((place, index) => ({
            attractionId: place.attractionId,
            sequence: index,
            memo: place.memo || "",
          })),
        }));
        this.planStore.setSelectedPlaces(formattedPlaces);
      },
    },
  },
  methods: {

    async fetchAttractions(page = 0, searchQuery = "") {
      if (!this.areaCode || this.isFetching) {
        return;
      }

      this.isFetching = true;
      this.error = null;
      this.isLoading = true;

      try {
        const response = await api.get(`/domain/attraction/search`, {
          params: {
            areaCode: this.areaCode,
            title: searchQuery, // 검색어 추가
            page: page,
            size: this.pageSize,
            sort: "title,asc",
          },
        });

        // 데이터 설정 후 selectedPlacesByDay 갱신을 위해 computed 재계산 트리거
        if (page === 0) {
          this.places = response.data.content;
        } else {
          this.places = [...this.places, ...response.data.content];
        }

        // 페이지네이션 정보 업데이트
        this.currentPage = response.data.number;
        this.totalPages = response.data.totalPages;
        this.isLastPage = response.data.last;

        // 검색 결과가 변경되더라도 선택된 장소들은 유지
        // 기존 선택된 장소들의 정보를 보존
        const currentSelectedPlaces = { ...this.selectedPlacesByDay };
        this.selectedPlacesByDay = currentSelectedPlaces;
      } catch (error) {
        console.error("관광지 데이터 조회 실패:", error);
        this.error = "관광지 정보를 불러오는데 실패했습니다.";
      } finally {
        this.isFetching = false;
        this.isLoading = false;
      }
    },
    // 새로운 메서드: 선택된 장소들의 전체 데이터 복원
    // 새로운 메서드: store의 데이터를 사용해 선택된 장소들 복원
    restoreSelectedPlaces() {
      const storePlaces = this.planStore.selectedPlaces;
      const restoredPlaces = {};

      storePlaces.forEach((dayPlan) => {
        const fullPlaces = dayPlan.details
          .map((detail) => {
            const fullPlace = this.places.find((p) => p.attractionId === detail.attractionId);
            if (fullPlace) {
              return {
                ...fullPlace,
                sequence: detail.sequence,
                memo: detail.memo,
              };
            }
            return null;
          })
          .filter((place) => place !== null);

        if (fullPlaces.length > 0) {
          restoredPlaces[dayPlan.day] = fullPlaces;
        }
      });

      this.selectedPlacesByDay = restoredPlaces;
    },

    // 스크롤 이벤트 핸들러
    async handleScroll({ target }) {
      const { scrollTop, clientHeight, scrollHeight } = target;

      // 스크롤이 bottom에 가까워지면 다음 페이지 로드
      if (!this.isLastPage && !this.isFetching && scrollTop + clientHeight >= scrollHeight - 100) {
        await this.fetchAttractions(this.currentPage + 1, this.searchQuery.trim());
      }
    },


    checkAndNavigateToSavePlan() {
      // 선택된 장소가 있는지 확인
      const hasSelectedPlaces = Object.values(this.selectedPlacesByDay).some(
        (places) => places && places.length > 0
      );

      if (!hasSelectedPlaces) {
        alert("장소를 선택해주세요!");
        return;
      }

      // 장소가 선택되었다면 저장 페이지로 이동
      this.saveAndNavigate();
    },
    // 저장하고 다음 페이지로 이동
    saveAndNavigate() {
      this.$router.push({
        name: "savePlan",
        params: {
          name: this.name,
          selectedPlaces: this.selectedPlacesByDay,
        },
        query: {
          startDate: this.localStartDate,
          endDate: this.localEndDate,
          id: this.$route.query.id,
        },
      });
    },
    // 일차 선택 시 호출될 메서드
    selectDay(dayIndex) {
      this.selectedDay = dayIndex;
      this.showAllDays = false;
    },

    // 전체 보기 선택 시 호출될 메서드
    showAllMarkers() {
      this.showAllDays = true;
      this.selectedDay = null;
    },
    updateMapSize() {
      if (this.$refs.tmap) {
        setTimeout(() => {
          this.$refs.tmap.getMap().resize();
          const center = new Tmapv2.LatLng(this.latitude, this.longitude);
          this.$refs.tmap.getMap().setCenter(center);
        }, 400);
      }
    },
    // 드래그 시작
    dragStart(event, place) {
      this.isSelectedPlaceDrag = false;
      event.dataTransfer.setData(
        "text/plain",
        JSON.stringify({
          place,
          isNew: true,
        })
      );
    },


    dragStartSelected(event, place, dayIndex, placeIndex) {
      this.isSelectedPlaceDrag = true;
      event.dataTransfer.setData(
        "text/plain",
        JSON.stringify({
          place,
          fromDay: dayIndex,
          fromIndex: placeIndex,
          isNew: false,
        })
      );
    },

   
      // 현재 날짜의 장소들 복사
      const currentPlaces = [
        ...(this.selectedPlacesByDay[targetDayIndex] || []),
      ];

      if (!isNew) {
        // 다른 날짜의 장소를 이동하는 경우
        if (fromDay !== targetDayIndex) {
          // 원래 위치에서 제거
          const fromPlaces = [...(this.selectedPlacesByDay[fromDay] || [])];
          fromPlaces.splice(fromIndex, 1);
          this.selectedPlacesByDay[fromDay] = fromPlaces;

          // 지정된 위치에 추가
          if (targetIndex !== undefined) {
            currentPlaces.splice(targetIndex, 0, place);
          } else {
            currentPlaces.push(place);
          }
        }
        // 같은 날짜 내에서 이동하는 경우
        else {
          if (targetIndex !== undefined) {
            currentPlaces.splice(fromIndex, 1); // 원래 위치에서 제거
            currentPlaces.splice(targetIndex, 0, place); // 새 위치에 추가
          }
        }
      }
      // 새로운 장소를 추가하는 경우 (middle-section에서 가져올 때)
      else {
        if (!place.latitude && place.mapy) {
          place.latitude = place.mapy;
        }
        if (!place.longitude && place.mapx) {
          place.longitude = place.mapx;
        }
        // 새로운 장소는 항상 끝에 추가
        currentPlaces.push(place);
      }

      this.selectedPlacesByDay[targetDayIndex] = currentPlaces;
      this.selectedPlacesByDay = { ...this.selectedPlacesByDay };
    },

    onDropReorder(event, dayIndex, targetIndex) {
      event.stopPropagation();

      if (!this.isSelectedPlaceDrag) return;

      const data = JSON.parse(event.dataTransfer.getData("text/plain"));
      const { fromDay, fromIndex } = data;

      if (fromDay === dayIndex && fromIndex !== targetIndex) {
        const places = [...this.selectedPlacesByDay[dayIndex]];
        const [removed] = places.splice(fromIndex, 1);
        places.splice(targetIndex, 0, removed);
        this.selectedPlacesByDay[dayIndex] = places;
        // 반응성을 위해 객체를 새로 할당
        this.selectedPlacesByDay = { ...this.selectedPlacesByDay };
      }

      this.resetDragState();
    },

    resetDragState() {
      this.draggedItem = null;
      this.draggedDay = null;
      this.draggedIndex = null;
      this.isSelectedPlaceDrag = false;
    },


    // 드롭 했을때
    onDrop(event, dayIndex) {
      const place = JSON.parse(event.dataTransfer.getData("text/plain"));
      const newPlacesByDay = { ...this.selectedPlacesByDay };

      if (!newPlacesByDay[dayIndex]) {
        newPlacesByDay[dayIndex] = [];
      }

      newPlacesByDay[dayIndex] = [
        ...newPlacesByDay[dayIndex],
        {
          ...place,
          sequence: newPlacesByDay[dayIndex].length,
          memo: "",
        },
      ];

      this.selectedPlacesByDay = newPlacesByDay;
    },

    updateStorePlaces() {
      const formattedPlaces = Object.keys(this.selectedPlacesByDay).map((dayIndex) => ({
        day: parseInt(dayIndex),
        details: this.selectedPlacesByDay[dayIndex].map((place, index) => ({
          attractionId: place.attractionId,
          sequence: index,
          memo: "",
        })),
      }));

      this.planStore.setSelectedPlaces(formattedPlaces);
    },


    removePlace(dayIndex, place) {
      this.selectedPlacesByDay[dayIndex] = this.selectedPlacesByDay[dayIndex].filter(
        (p) => p.attractionId !== place.attractionId
      );
    },
    clearDay(dayIndex) {
      this.selectedPlacesByDay[dayIndex] = [];
    },
    getImageUrl(imageUrl) {
      return (
        imageUrl || "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png"
      );
    },
    getTripDate(dayIndex) {
      if (!this.localStartDate) return "";
      const date = new Date(this.localStartDate);
      date.setDate(date.getDate() + dayIndex);
      return date;
    },
    formatDate(date) {
      if (!date) return "";
      const days = ["일", "월", "화", "수", "목", "금", "토"];
      return `(${days[date.getDay()]})`;
    },
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
      setTimeout(() => {
        this.updateMapSize();
      }, 300);
    },
    toggleRightSection() {
      this.isRightCollapsed = !this.isRightCollapsed;
      setTimeout(() => {
        this.updateMapSize();
      }, 300);
    },
    toggleStep2() {
      this.isStep2Active = !this.isStep2Active;
      if (!this.isStep2Active) {
        this.isCollapsed = true;
        this.isRightCollapsed = true;
      } else {
        this.isCollapsed = false;
        this.isRightCollapsed = false;
      }
      this.updateMapSize();
    },
    async handleSearch() {
      if (!this.searchQuery.trim()) {
        // 검색어가 비어있으면 전체 목록 조회
        await this.fetchAttractions(0);
        return;
      }

      // 검색 시작 시 로딩 상태 설정
      this.isLoading = true;
      this.currentPage = 0;

      // 백엔드 API 호출하여 검색 수행
      await this.fetchAttractions(0, this.searchQuery.trim());
    },
    goToDateSelection() {
      this.$router.push({
        name: "chooseDate",
        query: {
          name: this.name,
          startDate: this.localStartDate,
          endDate: this.localEndDate,
        },
      });
    },
  },
  // 4.
  async mounted() {
    // 컴포넌트 마운트 시 관광지 데이터 가져오기
    await this.fetchAttractions();
    // 스크롤 이벤트 리스너 추가
    const middleSection = this.$el.querySelector(".middle-section");
    if (middleSection) {
      middleSection.addEventListener("scroll", this.handleScroll);
    }
  },

  watch: {
    areaCode: {
      handler: "fetchAttractions",
      immediate: true,
    },
  },

  beforeUnmount() {
    // 스크롤 이벤트 리스너 제거
    const middleSection = this.$el.querySelector(".middle-section");
    if (middleSection) {
      middleSection.removeEventListener("scroll", this.handleScroll);
    }
  },
};
</script>

<style scoped>
/* Base layout */
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 200px 380px 380px 1fr;
  grid-template-rows: 1fr;
  height: calc(100vh - 64px);
  overflow: hidden;
  transition: all 0.3s ease;
  gap: 0;
}

/* Collapse states */
.content-wrapper.collapsed {
  grid-template-columns: 200px 0 380px 1fr;
}

.content-wrapper.right-collapsed {
  grid-template-columns: 200px 380px 0 1fr;
}

.content-wrapper.collapsed.right-collapsed {
  grid-template-columns: 200px 0 0 1fr;
}

/* Common section styles */
.steps-sidebar,
.middle-section,
.right-section {
  background: white;
  padding: 20px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
  position: relative;
  transition: all 0.3s ease;
}

/* Section collapse states */
.content-wrapper.collapsed .middle-section,
.content-wrapper.right-collapsed .right-section {
  width: 0;
  padding: 0;
  overflow: hidden;
  opacity: 0;
}

/* Common scrollbar styles */
.middle-section::-webkit-scrollbar,
.right-section::-webkit-scrollbar {
  width: 8px;
}

.middle-section::-webkit-scrollbar-track,
.right-section::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.middle-section::-webkit-scrollbar-thumb,
.right-section::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.middle-section::-webkit-scrollbar-thumb:hover,
.right-section::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* Header styles */
.header {
  margin-bottom: 24px;
}

.header h2 {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.date-range {
  color: #f57c00;
  font-size: 14px;
}

/* Step styles */
.step {
  display: flex;
  flex-direction: column;
  padding: 16px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.step:hover,
.step.active {
  background-color: #f5f5f5;
}

.step-number {
  font-size: 14px;
  font-weight: bold;
  color: #f57c00;
  margin-bottom: 4px;
}

/* Search styles */
.search-box {
  position: relative;
  margin-bottom: 20px;
}

.search-box i {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  cursor: pointer;
}

.search-box input {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 1px solid #ddd;
  border-radius: 25px;
  font-size: 14px;
}

/* Place item styles */
.places-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 4px;
}

.place-item,
.selected-place {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
}

.place-item {
  cursor: move;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.place-image {
  width: 80px;
  height: 80px;
  margin-right: 8px;
}
.order-number {
  margin-right: 8px;
}
.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.place-info {
  flex: 1;
}

.place-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.place-address {
  font-size: 13px;
  color: #666;
}

/* Day section styles */
.day-section {
  background-color: #f8f9fa;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 16px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.day-header h3 {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.day-header h3:hover {
  background-color: #f0f0f0;
}

.day-header-buttons {
  display: flex;
  gap: 8px;
}

.empty-day {
  text-align: center;
  padding: 20px;
  background: #fff;
  border: 2px dashed #ddd;
  border-radius: 8px;
}

/* Button styles */
.toggle-button {
  position: absolute;
  top: 20px;
  right: 10px;
  width: 32px;
  height: 32px;
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  transition: transform 0.3s ease;
}

.toggle-button i {
  font-size: 18px;
  color: #666;
  transition: color 0.3s ease;
}

.toggle-button:hover {
  transform: scale(1.1);
}

.toggle-button:hover i {
  color: #f57c00;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.remove-button,
.clear-button,
.view-all-button,
.save-button {
  border: none;
  border-radius: 4px;
  cursor: pointer;
  padding: 6px 12px;
}

.remove-button {
  background: none;
  color: #666;
}

.clear-button {
  background-color: #f1f1f1;
}

.view-all-button {
  background-color: #4caf50;
  color: white;
}

.save-button {
  width: 100%;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  background: #2b2b2b;
  color: white;
}

/* Map container */
.map-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  margin: 0;
}

.map-container :deep(.map-wrapper) {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
}


/* 기존 스타일 유지하고 selected-place에 대한 스타일 추가 */
.selected-place {
  position: relative;
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
  padding-left: 48px;
  margin-bottom: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.selected-place:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.selected-place .order-number {
  position: absolute;
  left: 12px;
  width: 24px;
  height: 24px;
  background-color: #f57c00;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.selected-place.dragging {
  opacity: 0.5;
}

/* 드롭 영역 스타일 */
.day-section {
  position: relative;
}

.day-section::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
}

.day-section.drag-over::after {
  opacity: 1;
}
.loading-state,
.error-state {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin: 10px 0;
}

.error-state {
  color: #dc3545;
  background: #f8d7da;

.loading-more {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

/* places-list에 스크롤 관련 스타일 추가 */
.places-list {
  max-height: calc(100vh - 200px); /* 적절한 높이로 조정 */
  overflow-y: auto;
  padding-right: 8px; /* 스크롤바 공간 확보 */
}
</style>

