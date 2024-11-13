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

          <div class="step active">
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
        <div class="toggle-button" @click="toggleCollapse">
          <i class="fa-solid fa-arrow-left"></i>
        </div>

        <div class="header">
          <h2>{{ initialName }}</h2>
          <p v-if="initialFormattedDateRange" class="date-range">
            {{ initialFormattedDateRange }}
          </p>
        </div>

        <div class="search-section">
          <div class="search-box">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="여행지를 검색하세요"
            />
            <i class="fa-solid fa-search"></i>
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
              <div class="place-header">
                <h3>{{ place.title }}</h3>
                <label class="checkbox-container">
                  <input
                    type="checkbox"
                    :checked="isPlaceSelected(place)"
                    @change="togglePlace(place)"
                  />
                  <span class="checkmark"></span>
                </label>
              </div>
              <p>{{ place.addr1 }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Section -->
      <div class="right-section">
        <div class="toggle-button" @click="toggleRightSection">
          <i class="fa-solid fa-arrow-left"></i>
        </div>

        <div class="header">
          <h2>선택한 장소</h2>
        </div>

        <div class="selected-places">
          <div
            v-for="(dayPlaces, index) in selectedPlacesByDay"
            :key="index"
            class="day-section"
            @dragover.prevent
            @drop="onDrop($event, index)"
          >
            <div class="day-header">
              <h3>{{ index + 1 }}일차 {{ formatDate(getTripDate(index)) }}</h3>
              <button @click="clearDay(index)" class="clear-button">
                <i class="fa-solid fa-rotate-left"></i> 초기화
              </button>
            </div>

            <div
              v-if="dayPlaces.length === 0"
              class="empty-day"
              @dragover.prevent
              @drop="onDrop($event, index)"
            >
              <p>여행지를 드래그하여 추가하세요</p>
            </div>

            <draggable
              v-else
              v-model="selectedPlacesByDay[index]"
              class="day-places"
              group="places"
              @change="updateMarkers"
            >
              <div
                v-for="place in dayPlaces"
                :key="place.attractionId"
                class="selected-place"
              >
                <div class="place-image">
                  <img :src="getImageUrl(place.image1)" :alt="place.title" />
                </div>
                <div class="place-info">
                  <h4>{{ place.title }}</h4>
                  <button
                    @click="removePlace(index, place)"
                    class="remove-button"
                  >
                    <i class="fa-solid fa-times"></i>
                  </button>
                </div>
              </div>
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
import { reactive } from 'vue';
import draggable from "vuedraggable";
import navBar from "@/components/navBar.vue";
import testData from "@/assets/data/testData.js";

export default {
  name: "ChoosePlace",
  components: {
    navBar,
    draggable,
  },
  props: {
    initialName: {
      type: String,
      required: true,
    },
    initialFormattedDateRange: {
      type: String,
      required: true,
    },
    initialStartDate: {
      type: String,
      required: true,
    },
    initialEndDate: {
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
      name: this.initialName,
      startDate: this.initialStartDate,
      endDate: this.initialEndDate,
      formattedDateRange: this.initialFormattedDateRange,
      searchQuery: "",
      places: testData,
      selectedPlacesByDay: reactive({}),
      markers: [],
      isCollapsed: false,
      isRightCollapsed: false,
      tmap: null,
      currentDayIndex: 0,
    };
  },
  watch: {
    initialName(newVal) {
      this.name = newVal;
    },
    initialStartDate(newVal) {
      this.startDate = newVal;
    },
    initialEndDate(newVal) {
      this.endDate = newVal;
    },
    initialFormattedDateRange(newVal) {
      this.formattedDateRange = newVal;
    },
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
      if (!this.startDate || !this.endDate) return 0;
      const start = new Date(this.startDate);
      const end = new Date(this.endDate);
      return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    },
  },
  methods: {
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
      if (this.isPlaceSelected(place)) {
        Object.keys(this.selectedPlacesByDay).forEach((day) => {
          this.selectedPlacesByDay[day] = this.selectedPlacesByDay[day].filter(
            (p) => p.attractionId !== place.attractionId
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

      Object.keys(this.selectedPlacesByDay).forEach((day) => {
        this.selectedPlacesByDay[day] = this.selectedPlacesByDay[day].filter(
          (p) => p.attractionId !== place.attractionId
        );
      });

      if (!this.selectedPlacesByDay[dayIndex]) {
        this.selectedPlacesByDay[dayIndex] = [];
      }
      this.selectedPlacesByDay[dayIndex].push(place);
      this.updateMarkers();
    },

    removePlace(day, place) {
      const index = this.selectedPlacesByDay[day].findIndex(
        (p) => p.attractionId === place.attractionId
      );
      if (index > -1) {
        this.selectedPlacesByDay[day].splice(index, 1);
      }
      this.updateMarkers();
    },

    clearDay(day) {
      this.selectedPlacesByDay[day] = [];
      this.updateMarkers();
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
  padding: 24px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
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

.search-box i {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
}

/* Place items */
.places-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.place-item,
.selected-place {
  display: flex;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  gap: 12px;
}

.place-image {
  width: 100px;
  height: 100px;
  overflow: hidden;
  border-radius: 4px;
}

.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.place-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* Headers */
.header {
  margin-bottom: 24px;
  padding-right: 40px;
}

.header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

/* Buttons */
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
  background-color: transparent;
  transition: transform 0.3s ease;
}

.toggle-button i {
  font-size: 18px;
  color: #666;
  transition: color 0.3s ease;
}

.toggle-button:hover i {
  color: #f57c00;
}

/* Right section toggle button */
.right-section .toggle-button {
  transform: rotate(180deg);
}

.middle-section .toggle-button:hover {
  transform: scale(1.1);
}

.right-section .toggle-button:hover {
  transform: scale(1.1) rotate(180deg);
}

.select-button,
.clear-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.select-button {
  align-self: flex-end;
  background-color: #f57c00;
  color: white;
}

.select-button:hover {
  background-color: #ef6c00;
}

.clear-button {
  background-color: #f5f5f5;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.clear-button:hover {
  background-color: #e0e0e0;
}

.remove-button {
  padding: 4px;
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  transition: color 0.3s ease;
}

.remove-button:hover {
  color: #f44336;
}

/* Day section */
.day-section {
  margin-bottom: 24px;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.day-section.active {
  border: 2px solid #f57c00;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.day-header h3 {
  font-size: 16px;
  font-weight: bold;
  color: #f57c00;
}

.empty-day {
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 14px;
  background-color: #fff;
  border-radius: 4px;
  border: 1px dashed #ddd;
}

.day-places {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Map */
.map-container {
  width: 100%;
  height: 100%;
}

.place-item {
  cursor: move;
}

.empty-day {
  border: 2px dashed #ddd;
  background-color: #f9f9f9;
  padding: 20px;
  text-align: center;
  color: #666;
  border-radius: 8px;
}

.day-places {
  min-height: 50px;
  padding: 8px;
}

.selected-place {
  transition: transform 0.2s;
}

.selected-place:hover {
  transform: translateX(4px);
}
</style>
