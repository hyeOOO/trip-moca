<template>
  <div class="layout-container">
    <navBar />
    <div class="content-wrapper" :class="{ collapsed: isCollapsed, 'right-collapsed': isRightCollapsed }">
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
          <router-link to="/make-plan" class="step">
            <div class="step-number">STEP 3</div>
            <div class="step-title">계획 생성</div>
          </router-link>
        </div>
      </div>

      <!-- Middle Section -->
      <div class="middle-section">
        <div class="toggle-button" @click="toggleCollapse">
          <i class="fa-solid" :class="{ 'fa-arrow-left': !isCollapsed, 'fa-arrow-right': isCollapsed }"></i>
        </div>
        <div class="header">
          <h2>{{ name }}</h2>
          <p v-if="localFormattedDateRange" class="date-range">
            {{ localFormattedDateRange }}
          </p>
        </div>
        <div class="search-section">
          <div class="search-box">
            <input type="text" v-model="searchQuery" placeholder="여행지를 검색하세요" @keyup.enter="handleSearch" />
            <i class="fa-solid fa-search" @click="handleSearch"></i>
          </div>
        </div>
        <div class="places-list">
          <div v-for="place in filteredPlaces" :key="place.attractionId" class="place-item" draggable="true" @dragstart="dragStart($event, place)">
            <div class="place-image">
              <img :src="getImageUrl(place.image1)" :alt="place.title" />
            </div>
            <div class="place-info">
              <h3 class="place-title">{{ place.title }}</h3>
              <p class="place-address">{{ place.addr1 }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Section -->
      <div class="right-section">
        <div class="toggle-button" @click="toggleRightSection">
          <i class="fa-solid" :class="{ 'fa-arrow-left': !isRightCollapsed, 'fa-arrow-right': isRightCollapsed }"></i>
        </div>
        <div class="header">
          <h2>선택한 장소</h2>
        </div>
        <div class="selected-places">
          <div v-for="dayIndex in numberOfDays" :key="dayIndex - 1" class="day-section" @dragover.prevent @drop="onDrop($event, dayIndex - 1)">
            <div class="day-header">
              <h3>
                {{ dayIndex }}일차 {{ formatDate(getTripDate(dayIndex - 1)) }}
              </h3>
              <button @click="clearDay(dayIndex - 1)" class="clear-button">
                <i class="fa-solid fa-rotate-left"></i> 초기화
              </button>
            </div>
            <div v-if="!selectedPlacesByDay[dayIndex - 1]?.length" class="empty-day">
              <p></p>
            </div>
            <div v-else class="selected-day-places">
              <div v-for="place in selectedPlacesByDay[dayIndex - 1]" :key="place.attractionId" class="selected-place">
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
      </div>
      <div class="map-container" ref="tmap"></div>
    </div>
  </div>
</template>

<script>
import navBar from "@/components/navBar.vue";
import testData from "@/assets/data/testData.js";

export default {
  name: "ChoosePlace",
  components: {
    navBar,
  },
  props: {
    name: String,
    formattedDateRange: String,
    startDate: String,
    endDate: String,
    latitude: Number,
    longitude: Number,
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
    dragStart(event, place) {
      event.dataTransfer.setData("text/plain", JSON.stringify(place));
    },
    onDrop(event, dayIndex) {
      const place = JSON.parse(event.dataTransfer.getData("text/plain"));
      console.log("Dropped place:", place);
      console.log("Coordinates:", place.mapx, place.mapy);
      if (!this.selectedPlacesByDay[dayIndex]) {
        this.selectedPlacesByDay[dayIndex] = [];
      }
      this.selectedPlacesByDay[dayIndex].push(place);
      this.$nextTick(() => {
        this.updateMarkers();
      });
    },
    removePlace(dayIndex, place) {
      this.selectedPlacesByDay[dayIndex] = this.selectedPlacesByDay[dayIndex].filter(
        (p) => p.attractionId !== place.attractionId
      );
      this.updateMarkers();
    },
    clearDay(dayIndex) {
      this.selectedPlacesByDay[dayIndex] = [];
      this.updateMarkers();
    },
    getImageUrl(imageUrl) {
      return imageUrl || "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png";
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
    handleSearch() {
      if (!this.searchQuery.trim()) return;
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
    updateMapSize() {
      if (this.tmap) this.tmap.resize();
    },
    updateMarkers() {
      if (!this.tmap) return;
      this.markers.forEach((marker) => marker.setMap(null));
      this.markers = [];
      Object.entries(this.selectedPlacesByDay).forEach(([dayIndex, dayPlaces]) => {
        if (!dayPlaces) return;
        dayPlaces.forEach((place) => {
          const mapx = parseFloat(place.mapx) || this.longitude;
          const mapy = parseFloat(place.mapy) || this.latitude;
          if (!isNaN(mapx) && !isNaN(mapy)) {
            console.log(`Creating marker for ${place.title}: ${mapx}, ${mapy}`);
            const markerPosition = new window.Tmapv2.LatLng(mapy, mapx);
            const marker = new window.Tmapv2.Marker({
              position: markerPosition,
              map: this.tmap,
              title: place.title,
              label: `${parseInt(dayIndex) + 1}일차: ${place.title}`,
            });
            this.markers.push(marker);
            marker.addListener("click", () => {
              new window.Tmapv2.InfoWindow({
                position: markerPosition,
                content: `
                  <div style="padding: 10px;">
                    <h4>${place.title}</h4>
                    <p>${place.addr1}</p>
                  </div>
                `,
                type: 2,
                map: this.tmap,
              });
            });
          } else {
            console.warn(`Missing or invalid coordinates for ${place.title}`);
          }
        });
      });
      console.log(`Total markers created: ${this.markers.length}`);
      if (this.markers.length > 0) {
        const bounds = new window.Tmapv2.LatLngBounds();
        this.markers.forEach((marker) => bounds.extend(marker.getPosition()));
        this.tmap.fitBounds(bounds);
      }
    },
    initializeMap() {
      if (!window.Tmapv2) {
        console.error("Tmapv2 is not loaded");
        return;
      }
      const mapOptions = {
        center: new window.Tmapv2.LatLng(this.latitude, this.longitude),
        width: "100%",
        height: "100%",
        zoom: 11,
      };
      this.tmap = new window.Tmapv2.Map(this.$refs.tmap, mapOptions);
      console.log("Map initialized:", this.tmap);
      this.tmap.addListener("idle", this.updateMarkers);
    },
  },
  mounted() {
    console.log("Component mounted");
    setTimeout(() => {
      this.initializeMap();
      console.log("Map initialization attempted");
    }, 100);
    window.addEventListener("resize", this.updateMapSize);
  },
  beforeUnmount() {
    if (this.tmap) {
      this.markers.forEach((marker) => marker.setMap(null));
      this.tmap.destroy();
    }
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
  grid-template-columns: 200px 380px 380px 1fr;
  height: calc(100vh - 64px);
  overflow: hidden;
  transition: all 0.3s ease;
}

.content-wrapper.collapsed {
  grid-template-columns: 200px 0 380px 1fr;
}

.content-wrapper.right-collapsed {
  grid-template-columns: 200px 380px 0 1fr;
}

.content-wrapper.collapsed.right-collapsed {
  grid-template-columns: 200px 0 0 1fr;
}

/* Common styles for sections */
.steps-sidebar,
.middle-section,
.right-section {
  background: white;
  padding: 20px 20px 40px 20px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
  position: relative;
  transition: all 0.3s ease;
}

/* Scrollbar styles */
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

/* Section specific styles */
.middle-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right-section {
  opacity: 1;
  width: 100%;
}

/* Collapse states */
.content-wrapper.collapsed .middle-section,
.content-wrapper.right-collapsed .right-section {
  width: 0;
  padding: 0;
  overflow: hidden;
  opacity: 0;
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
  font-size: 14px;
  color: #666;
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

/* Search box styles */
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

/* Places list styles */
.places-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 4px;
}

.place-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: move;
}

.place-image {
  width: 80px;
  height: 80px;
  margin-right: 16px;
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

.empty-day {
  text-align: center;
  padding: 20px;
  background: #fff;
  border: 2px dashed #ddd;
  border-radius: 8px;
}

.selected-place {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 8px;
  margin-bottom: 8px;
  padding: 8px;
}

/* Button styles */
.remove-button {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 4px;
}

.toggle-button {
  position: absolute;
  top: 20px;
  right: 10px;
  cursor: pointer;
  padding: 8px;
  display: none;
}

.toggle-button .fa-arrow-left {
  display: block;
}

.toggle-button .fa-arrow-right {
  display: none;
}

.middle-section:not(.collapsed) .toggle-button,
.right-section:not(.right-collapsed) .toggle-button {
  display: block;
}

.clear-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background-color: #f1f1f1;
  cursor: pointer;
}

/* Map container */
.map-container {
  width: 100%;
  height: 100%;
}

.day-container {
  padding: 16px;
  background: #fff;
  margin-bottom: 16px;
}

.day-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.day-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.day-date {
  font-size: 14px;
  color: #666;
  margin-left: 8px;
}

/* Place item styles */
.place-card {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fff;
}

.place-card img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 12px;
}

.place-details {
  flex: 1;
}

.place-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.place-description {
  font-size: 13px;
  color: #666;
}

/* Action button */
.action-button {
  width: 100%;
  padding: 12px;
  background: #000;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-top: 16px;
  cursor: pointer;
}

.action-button:hover {
  background: #333;
}
</style>
