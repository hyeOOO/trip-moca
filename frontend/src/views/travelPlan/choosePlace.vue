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
        <div class="middleHeader">
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
              placeholder="장소를 입력해주세요."
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
import { useAuthStore } from "@/store/auth";
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
    const authStore = useAuthStore();
    return { planStore, authStore };
  },
  data() {
    return {
      searchQuery: "",
      places: [],
      isCollapsed: false,
      isRightCollapsed: false,
      isStep2Active: true,
      selectedDay: null,
      showAllDays: false,
      isLoading: false,
      error: null,
      currentPage: 0,
      totalPages: 0,
      pageSize: 15,
      isLastPage: false,
      isFetching: false,
      localSelectedPlaces: {},
      draggedItem: null,
      draggedDay: null,
      draggedIndex: null,
      isSelectedPlaceDrag: false,
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
          place.title.toLowerCase().includes(query) || 
          place.addr1.toLowerCase().includes(query)
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
            title: searchQuery,
            page: page,
            size: this.pageSize,
            sort: "title,asc",
          },
        });

        if (page === 0) {
          this.places = response.data.content;
        } else {
          this.places = [...this.places, ...response.data.content];
        }

        this.currentPage = response.data.number;
        this.totalPages = response.data.totalPages;
        this.isLastPage = response.data.last;

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

    restoreSelectedPlaces() {
      const storePlaces = this.planStore.selectedPlaces;
      const restoredPlaces = {};

      storePlaces.forEach((dayPlan) => {
        const fullPlaces = dayPlan.details
          .map((detail) => {
            const fullPlace = this.places.find(
              (p) => p.attractionId === detail.attractionId
            );
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

    async handleScroll({ target }) {
      const { scrollTop, clientHeight, scrollHeight } = target;
      if (
        !this.isLastPage &&
        !this.isFetching &&
        scrollTop + clientHeight >= scrollHeight - 100
      ) {
        await this.fetchAttractions(this.currentPage + 1, this.searchQuery.trim());
      }
    },

    // Drag and Drop methods
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

    onDragPlace(event, dayIndex, targetIndex) {
      event.preventDefault();
      event.stopPropagation();

      try {
        const data = JSON.parse(event.dataTransfer.getData("text/plain"));
        const { place, fromDay, fromIndex, isNew } = data;

        const currentPlaces = [...(this.selectedPlacesByDay[dayIndex] || [])];

        if (!isNew) {
          if (fromDay !== dayIndex) {
            const fromPlaces = [...(this.selectedPlacesByDay[fromDay] || [])];
            fromPlaces.splice(fromIndex, 1);
            this.selectedPlacesByDay[fromDay] = fromPlaces;

            if (targetIndex !== undefined) {
              currentPlaces.splice(targetIndex, 0, place);
            } else {
              currentPlaces.push(place);
            }
          } else if (targetIndex !== undefined) {
            currentPlaces.splice(fromIndex, 1);
            currentPlaces.splice(targetIndex, 0, place);
          }
        } else {
          if (!place.latitude && place.mapy) {
            place.latitude = place.mapy;
          }
          if (!place.longitude && place.mapx) {
            place.longitude = place.mapx;
          }

          if (targetIndex !== undefined) {
            currentPlaces.splice(targetIndex, 0, place);
          } else {
            currentPlaces.push(place);
          }
        }

        this.selectedPlacesByDay[dayIndex] = currentPlaces;
        this.selectedPlacesByDay = { ...this.selectedPlacesByDay };
        this.resetDragState();
      } catch (error) {
        console.error('Error in onDragPlace:', error);
      }
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

    onDrop(event, dayIndex) {
      const data = JSON.parse(event.dataTransfer.getData("text/plain"));
      const place = data.place;
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

    // Navigation and UI methods
    checkAndNavigateToSavePlan() {
      const hasSelectedPlaces = Object.values(this.selectedPlacesByDay).some(
        (places) => places && places.length > 0
      );

      if (!hasSelectedPlaces) {
        alert("장소를 선택해주세요!");
        return;
      }

      this.saveAndNavigate();
    },

    saveAndNavigate() {
      this.$router.push({
        name: "savePlan",
        params: {
          name: this.name,
          selectedPlaces: this.selectedPlacesByDay,
        },
        query: {
          startDate: this.startDate,
          endDate: this.endDate,
          id: this.$route.query.id,
        },
      });
    },

    selectDay(dayIndex) {
      this.selectedDay = dayIndex;
      this.showAllDays = false;
    },

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

    removePlace(dayIndex, place) {
      this.selectedPlacesByDay[dayIndex] = this.selectedPlacesByDay[dayIndex].filter(
        (p) => p.attractionId !== place.attractionId
      );
    },

    clearDay(dayIndex) {
      this.selectedPlacesByDay[dayIndex] = [];
    },

    getImageUrl(imageUrl) {
      return imageUrl || "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png";
    },

    getTripDate(dayIndex) {
      if (!this.startDate) return "";
      const date = new Date(this.startDate);
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
        await this.fetchAttractions(0);
        return;
      }

      this.isLoading = true;
      this.currentPage = 0;
      await this.fetchAttractions(0, this.searchQuery.trim());
    },

    goToDateSelection() {
      this.$router.push({
        name: "chooseDate",
        query: {
          name: this.name,
          startDate: this.startDate,
          endDate: this.endDate,
        },
      });
    },
  },

  async mounted() {
    await this.fetchAttractions();
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
    const middleSection = this.$el.querySelector(".middle-section");
    if (middleSection) {
      middleSection.removeEventListener("scroll", this.handleScroll);
    }
  },
};
</script>

<style scoped>
/* 기본 레이아웃 설정 */
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 107px 370px 370px minmax(0, 1fr); /* 좌측 사이드바, 중앙섹션, 우측섹션, 지도영역 */
  grid-template-rows: 1fr;
  height: calc(100vh - 64px);
  overflow: hidden;
  transition: all 0.3s ease;
  gap: 0;
}

/* 섹션 접힘/펼침 상태 */
.content-wrapper.collapsed {
  grid-template-columns: 200px 0 380px 1fr;
}

.content-wrapper.right-collapsed {
  grid-template-columns: 200px 380px 0 1fr;
}

.content-wrapper.collapsed.right-collapsed {
  grid-template-columns: 200px 0 0 1fr;
}

/* 공통 섹션 스타일 */
.steps-sidebar,
.middle-section,
.right-section {
  background: white;
  height: 100%;
  position: relative;
}

.steps-sidebar,
.middle-section {
  padding: 10px;
}

.middle-section,
.right-section {
  padding: 10px;
  overflow-y: auto;
  transition: all 0.3s ease;
}

/* 섹션 접힘 상태 스타일 */
.content-wrapper.collapsed .middle-section,
.content-wrapper.right-collapsed .right-section {
  width: 0;
  padding: 0;
  overflow: hidden;
  opacity: 0;
}

/* 가로/세로 스크롤바 공통 스타일 */
::-webkit-scrollbar {
 width: 6px;
 height: 6px;
}

::-webkit-scrollbar-track {
 background: #ffffff;
 border-radius: 3px;
}

::-webkit-scrollbar-thumb {
 background: #ECB27B;
 border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
 background: #C3A386;
}

/* 헤더 영역 스타일 */
.middleHeader {
  margin-bottom: 24px;
}

.middleHeader h2 {
  font-family: "EliceDigitalBaeum_bold";
  font-size: 32px;
  margin-bottom: 8px;
}
.header {
  margin-bottom: 50px;
}

.date-range {
  font-family: "EliceDigitalBaeum_regular";
  color: #ECB27B;
  font-size: 14px;
}

/* 단계 네비게이션 스타일 */
.steps-nav {
  text-align: center;
  font-family: "EliceDigitalBaeum_Regular";
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.step {
  display: flex;
  flex-direction: column;
  padding: 16px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.step-number,
.step-title {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 14px;
  color: #B4B4B4;
}

.step-number {
  margin-bottom: 4px;
}

.step:hover .step-number,
.step:hover .step-title,
.step.active .step-number,
.step.active .step-title {
  color: #ECB27B;
}

/* 검색 영역 스타일 */
.search-box {
  position: relative;
  margin-bottom: 20px;
}

.search-box input {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 1px solid #ddd;
  border-radius: 25px;
  font-size: 14px;
}

.search-box i {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  cursor: pointer;
}

/* 장소 목록 스타일 */
.places-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding: 0 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 장소 아이템 공통 스타일 */
.place-item,
.selected-place {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 8px 12px;
}

.place-image {
  width: 80px;
  height: 80px;
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

/* 장소 아이템 세부 스타일 */
.place-item {
  cursor: move;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

/* 선택된 장소 스타일 */
.selected-place {
  position: relative;
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
  background-color: #ECB27B;
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

/* 일자별 섹션 스타일 */
.day-section {
  position: relative;
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

/* 드래그 앤 드롭 영역 스타일 */
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

/* 버튼 스타일 */
.toggle-button {
  position: absolute;
  top: 20px;
  right: 10px;
  width: 32px;
  height: 32px;
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

.toggle-button:hover i {
  color: #ECB27B;
}

/* 액션 버튼 스타일 */
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

/* 지도 컨테이너 스타일 */
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

/* 로딩 및 에러 상태 스타일 */
.loading-state,
.error-state,
.loading-more {
  text-align: center;
  margin: 10px 0;
}

.error-state {
  color: #ECB27B;
}

.loading-more {
  padding: 16px;
  font-size: 14px;
  color: #666;
}

/* 빈 일정 상태 스타일 */
.empty-day {
  text-align: center;
  padding: 44px;
  border-radius: 8px;
}
</style>
