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

          <div
            class="step"
            :class="{ active: isStep2Active }"
            @click="toggleStep2"
          >
            <div class="step-number">STEP 2</div>
            <div class="step-title">장소 선택</div>
          </div>

          <router-link to="/make-plan" class="step">
            <div class="step-number">STEP 3</div>
            <div class="step-title">계획 생성</div>
          </router-link>
        </div>
      </div>

      <!-- Middle Section -->
      <div class="middle-section">
        <div class="toggle-button left" @click="toggleCollapse">
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
          <p v-if="localFormattedDateRange" class="date-range">
            {{ localFormattedDateRange }}
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
            <i
              class="fa-solid fa-search"
              @click="handleSearch"
              style="cursor: pointer"
            ></i>
          </div>
        </div>

        <div class="places-list">
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
            <div class="checkbox-wrapper">
              <input
                type="checkbox"
                :id="'place-' + place.attractionId"
                :checked="isPlaceSelected(place)"
                @change="togglePlace(place)"
                class="round-checkbox"
              />
              <label
                :for="'place-' + place.attractionId"
                class="round-checkbox-label"
              ></label>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Section 부분 -->
      <div class="right-section">
        <div class="toggle-button left" @click="toggleRightSection">
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
            v-for="(dayPlaces, index) in selectedPlacesByDay"
            :key="index"
            class="day-section"
          >
            <div class="day-header">
              <h3>{{ index + 1 }}일차 {{ formatDate(getTripDate(index)) }}</h3>
              <button @click="clearDay(index)" class="clear-button">
                <i class="fa-solid fa-rotate-left"></i> 초기화
              </button>
            </div>

            <div
              v-if="!dayPlaces || dayPlaces.length === 0"
              class="empty-day"
              @dragover.prevent
              @drop="onDrop($event, index)"
            >
              <p>여행지를 드래그하여 추가하세요</p>
            </div>

            <draggable
              v-else
              v-model="selectedPlacesByDay[index]"
              :group="{ name: 'places' }"
              item-key="attractionId"
              class="day-places"
              @change="updateMarkers"
            >
              <template #item="{ element }">
                <div class="selected-place">
                  <div class="place-image">
                    <img
                      :src="getImageUrl(element.image1)"
                      :alt="element.title"
                    />
                  </div>
                  <div class="place-info">
                    <h4>{{ element.title }}</h4>
                    <button
                      @click="removePlace(index, element)"
                      class="remove-button"
                    >
                      <i class="fa-solid fa-times"></i>
                    </button>
                  </div>
                </div>
              </template>
            </draggable>
          </div>
        </div>
      </div>

      <!-- Map -->
      <div class="map-container" ref="tmap"></div>
    </div>
  </div>
</template>

<script>
import { VueDraggable } from 'vue-draggable-next';
import navBar from "@/components/navBar.vue";
import testData from "@/assets/data/testData.js";

export default {
  name: "ChoosePlace",
  components: {
    navBar,
    draggable: VueDraggable,
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    formattedDateRange: {
      type: String,
      required: true,
    },
    startDate: {
      type: String,
      required: true,
    },
    endDate: {
      type: String,
      required: true,
    },
    latitude: {
      type: Number,
      required: true,
    },
    longitude: {
      type: Number,
      required: true,
    },
  },

  data() {
    return {
      localStartDate: this.startDate,
      localEndDate: this.endDate,
      localFormattedDateRange: this.formattedDateRange,
      searchQuery: "",
      places: testData,
      markers: [],
      isCollapsed: false,
      isRightCollapsed: false,
      tmap: null,
      currentDayIndex: 0,
      isStep2Active: true,
      selectedPlacesByDay: {},
    };
  },

  computed: {
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
      if (!this.localStartDate || !this.localEndDate) return 0;
      const start = new Date(this.localStartDate);
      const end = new Date(this.localEndDate);
      return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    },
  },

  methods: {
    handleSearch() {
      if (!this.searchQuery.trim()) return;
      // 추가 검색 로직 구현하는곳
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

    isPlaceSelected(place) {
      return Object.values(this.selectedPlacesByDay).some(
        (dayPlaces) =>
          dayPlaces &&
          dayPlaces.some((p) => p.attractionId === place.attractionId)
      );
    },

    dragStart(event, place) {
      event.dataTransfer.effectAllowed = "move";
      event.dataTransfer.setData("text/plain", JSON.stringify(place));
    },

    getImageUrl(imageUrl) {
      if (!imageUrl) {
        return "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png";
      }
      return imageUrl;
    },

    initializePlacesByDay() {
      const days = this.numberOfDays;
      for (let i = 0; i < days; i++) {
        this.selectedPlacesByDay[i] = [];
      }
    },

    selectDay(index) {
      this.currentDayIndex = index;
    },

    getCurrentDay() {
      return this.currentDayIndex;
    },

    togglePlace(place) {
      const currentDay = this.getCurrentDay();
      const isSelected = this.isPlaceSelected(place);

      if (isSelected) {
        Object.keys(this.selectedPlacesByDay).forEach(day => {
          this.selectedPlacesByDay[day] = this.selectedPlacesByDay[day].filter(
            p => p.attractionId !== place.attractionId
          );
        });
      } else {
        if (!this.selectedPlacesByDay[currentDay]) {
          this.selectedPlacesByDay[currentDay] = [];
        }
        this.selectedPlacesByDay[currentDay].push(place);
      }
      this.updateMarkers();
    },

    onDrop(event, dayIndex) {
      const place = JSON.parse(event.dataTransfer.getData("text/plain"));
      
      Object.keys(this.selectedPlacesByDay).forEach(day => {
        this.selectedPlacesByDay[day] = this.selectedPlacesByDay[day].filter(
          p => p.attractionId !== place.attractionId
        );
      });

      if (!this.selectedPlacesByDay[dayIndex]) {
        this.selectedPlacesByDay[dayIndex] = [];
      }
      this.selectedPlacesByDay[dayIndex].push(place);
      this.updateMarkers();
    },

    removePlace(dayIndex, place) {
      if (this.selectedPlacesByDay[dayIndex]) {
        this.selectedPlacesByDay[dayIndex] = this.selectedPlacesByDay[dayIndex].filter(
          p => p.attractionId !== place.attractionId
        );
        this.updateMarkers();
      }
    },

    clearDay(dayIndex) {
      this.selectedPlacesByDay[dayIndex] = [];
      this.updateMarkers();
    },

    getTripDate(dayIndex) {
      if (!this.localStartDate) return "";
      const date = new Date(this.localStartDate);
      date.setDate(date.getDate() + dayIndex);
      return date;
    },

    formatDate(dateString) {
      if (!dateString) return "";
      const date = new Date(dateString);
      const days = ["일", "월", "화", "수", "목", "금", "토"];
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const dayOfWeek = days[date.getDay()];

      if (arguments.length > 1 || dateString instanceof Date) {
        return `(${days[date.getDay()]})`;
      }
      return `${year}.${month}.${day}(${dayOfWeek})`;
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

    clearMap() {
      this.markers.forEach((marker) => marker.setMap(null));
      this.markers = [];

      if (this.tmap) {
        this.tmap.destroy();
        this.tmap = null;
      }
    },

    updateMapSize() {
      if (this.tmap) {
        this.tmap.resize();
      }
    },

    updateMarkers() {
      if (!this.tmap) return;

      this.markers.forEach((marker) => marker.setMap(null));
      this.markers = [];

      Object.values(this.selectedPlacesByDay).forEach((dayPlaces) => {
        dayPlaces.forEach((place) => {
          if (place.mapx && place.mapy) {
            const markerPosition = new window.Tmapv2.LatLng(
              place.mapy,
              place.mapx
            );
            const marker = new window.Tmapv2.Marker({
              position: markerPosition,
              map: this.tmap,
              title: place.title,
              icon:
                "http://tmapapi.sktelecom.com/upload/tmap/marker/pin_r_m_" +
                this.markers.length +
                ".png",
            });
            this.markers.push(marker);
          }
        });
      });

      if (this.markers.length > 0) {
        const bounds = new window.Tmapv2.LatLngBounds();
        this.markers.forEach((marker) => {
          bounds.extend(marker.getPosition());
        });
        this.tmap.fitBounds(bounds);
      }
    },

    initializeMap() {
      try {
        if (!window.Tmapv2) {
          console.error("TMap API가 로드되지 않았습니다.");
          return;
        }

        const option = {
          center: new window.Tmapv2.LatLng(this.latitude, this.longitude),
          width: "100%",
          height: "100%",
          zoom: 11,
        };

        if (this.tmap) {
          this.clearMap();
        }

        this.tmap = new window.Tmapv2.Map(this.$refs.tmap, option);

        this.tmap.addListener("idle", () => {
          this.updateMarkers();
        });

        setTimeout(() => {
          window.dispatchEvent(new Event("resize"));
        }, 200);
      } catch (error) {
        console.error("지도 초기화 중 오류 발생:", error);
      }
    },
  },

  mounted() {
    this.initializePlacesByDay();
    setTimeout(() => {
      this.initializeMap();
    }, 100);
    window.addEventListener("resize", this.updateMapSize);
  },

  beforeUnmount() {
    this.clearMap();
    window.removeEventListener("resize", this.updateMapSize);
  },
};
</script>

<style scoped>
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 200px 320px 320px 1fr;
  height: calc(100vh - 64px);
  overflow: hidden;
  transition: all 0.3s ease;
}

.content-wrapper.collapsed {
  grid-template-columns: 200px 0 320px 1fr;
}

.content-wrapper.right-collapsed {
  grid-template-columns: 200px 320px 0 1fr;
}

.content-wrapper.collapsed.right-collapsed {
  grid-template-columns: 200px 0 0 1fr;
}

/* Common section styles */
.steps-sidebar,
.middle-section,
.right-section {
  background: white;
  padding: 10px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
  position: relative;
  transition: all 0.3s ease;
}

.content-wrapper.collapsed .middle-section,
.content-wrapper.right-collapsed .right-section {
  padding: 0;
  width: 0;
}

/* Header styles */
.header {
  margin-bottom: 24px;
}

.header h2 {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.date-range {
  color: #f57c00;
  font-size: 14px;
}

/* Navigation styles */
.steps-nav {
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

.step-title {
  font-size: 16px;
}

/* Search box */
.search-box {
  position: relative;
  width: 100%;
  margin: 20px 0;
}

.search-box input {
  width: 100%;
  padding: 12px 40px 12px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-box input:focus {
  outline: none;
  border-color: #f57c00;
  box-shadow: 0 0 0 2px rgba(245, 124, 0, 0.1);
}

.search-box i {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  cursor: pointer;
  padding: 8px;
  transition: color 0.3s ease;
}

.search-box i:hover {
  color: #f57c00;
}

/* Place items */
.places-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  padding-right: 8px;
}

.places-list::-webkit-scrollbar {
  width: 8px;
}

.places-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.places-list::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.places-list::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.place-item {
  display: grid;
  grid-template-columns: 4fr 5fr 1fr;
  align-items: center;
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.place-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* Place components */
.place-image {
  width: 100%;
  overflow: hidden;
  border-radius: 4px;
}

.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.place-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.place-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Checkbox styles */
.checkbox-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.round-checkbox {
  display: none;
}

.round-checkbox-label {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.round-checkbox-label:hover {
  border-color: #f57c00;
}

.round-checkbox:checked + .round-checkbox-label {
  background-color: #f57c00;
  border-color: #f57c00;
}

.round-checkbox:checked + .round-checkbox-label::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  background-color: white;
  border-radius: 50%;
}

/* Day section */
.day-section {
  background-color: #f8f9fa;
  border-radius: 5px;
  padding: 5px;
  margin-bottom: 16px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.day-header h3 {
  font-size: 16px;
  font-weight: bold;
  color: #f57c00;
  margin: 0;
}

.empty-day {
  text-align: center;
  padding: 20px;
  background: #fff;
  border: 2px dashed #ddd;
  border-radius: 8px;
  color: #666;
}

.day-places {
  background: #fff;
  border-radius: 8px;
  min-height: 100px;
  padding: 8px;
  margin-top: 8px;
}

.day-places.dragover {
  background-color: #f5f5f5;
  border: 2px dashed #f57c00;
}

.selected-places::-webkit-scrollbar {
  width: 8px;
}

.selected-places::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.selected-places::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.selected-places::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* Buttons */
.toggle-button {
  position: absolute;
  top: 20px;
  right: 24px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  background-color: white;
  border: 1px solid #eee;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.toggle-button:hover {
  background-color: #f5f5f5;
  transform: scale(1.1);
}

.toggle-button i {
  font-size: 16px;
  color: #666;
  transition: color 0.3s ease;
}

.toggle-button:hover i {
  color: #f57c00;
}

.clear-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background-color: #f1f1f1;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-button:hover {
  background-color: #e0e0e0;
}

/* Map */
.map-container {
  width: 100%;
  height: 100%;
}
</style>
