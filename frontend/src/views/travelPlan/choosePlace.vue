<template>
  <div class="layout-container">
    <navBar />

    <div class="content-wrapper" :class="{ collapsed: isCollapsed }">
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
              @input="searchPlaces"
            />
            <i class="fa-solid fa-search"></i>
          </div>
        </div>

        <div class="places-list">
          <div
            v-for="place in filteredPlaces"
            :key="place.attractionId"
            class="place-item"
          >
            <div class="place-image">
              <img
                :src="place.image1 || '/default-place.jpg'"
                :alt="place.title"
              />
            </div>
            <div class="place-info">
              <h3>{{ place.title }}</h3>
              <p>{{ place.addr1 }}</p>
              <button @click="selectPlace(place)" class="select-button">
                선택
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Section -->
      <div class="right-section">
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
            <div class="day-places">
              <div
                v-for="place in dayPlaces"
                :key="place.attractionId"
                class="selected-place"
              >
                <div class="place-image">
                  <img
                    :src="place.image1 || '/default-place.jpg'"
                    :alt="place.title"
                  />
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
            </div>
          </div>
        </div>
      </div>

      <!-- Map -->
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
  },
  data() {
    return {
      searchQuery: "",
      places: testData,
      selectedPlacesByDay: {},
      map: null,
      markers: [],
      isCollapsed: false,
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
      if (!this.startDate || !this.endDate) return 0;
      const start = new Date(this.startDate);
      const end = new Date(this.endDate);
      return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    },
  },
  methods: {
    goToDateSelection() {
      this.$router.push({
        path: `/chooseDate/${this.name}`, // name을 URL 파라미터로 전달
        query: {
          id: this.$route.query.id || "2", // 현재 route에서 id를 가져오거나 기본값 '2' 사용
        },
      });
    },

    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
      setTimeout(() => {
        this.updateMapSize();
      }, 300);
    },

    searchPlaces() {
      // 검색 로직 구현
    },

    selectPlace(place) {
      const currentDay = this.getCurrentDay();
      if (!this.selectedPlacesByDay[currentDay]) {
        this.selectedPlacesByDay[currentDay] = []; // 직접 할당
      }
      this.selectedPlacesByDay[currentDay].push(place);
      this.updateMap();
    },

    removePlace(day, place) {
      const index = this.selectedPlacesByDay[day].findIndex(
        (p) => p.attractionId === place.attractionId
      );
      if (index > -1) {
        this.selectedPlacesByDay[day].splice(index, 1);
      }
      this.updateMap();
    },

    clearDay(day) {
      this.selectedPlacesByDay[day] = []; // 직접 할당
      this.updateMap();
    },

    getCurrentDay() {
      // 현재 선택된 날짜에 해당하는 일차를 반환
      return 0; // 기본값
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

    updateMap() {
      // TMap 업데이트 로직
    },

    updateMapSize() {
      if (this.map) {
        this.map.resize();
      }
    },

    initializeMap() {
      // TMap 초기화 로직
    },
  },
  mounted() {
    this.initializeMap();
    // 일수만큼 selectedPlacesByDay 초기화
    for (let i = 0; i < this.numberOfDays; i++) {
      this.selectedPlacesByDay[i] = []; // 직접 할당
    }
  },
  beforeUnmount() {
    if (this.map) {
      this.map.destroy();
    }
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

/* Left sidebar styles from previous component */
.steps-sidebar {
  background: white;
  padding: 24px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
}

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

/* Middle section styles */
.middle-section {
  position: relative;
  background: white;
  padding: 24px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
}

.search-section {
  margin: 20px 0;
}

.search-box {
  position: relative;
  width: 100%;
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

.places-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.place-item {
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

.select-button {
  align-self: flex-end;
  padding: 8px 16px;
  background-color: #f57c00;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.select-button:hover {
  background-color: #ef6c00;
}

/* Right section styles */
.right-section {
  background: white;
  padding: 24px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
}

.day-section {
  margin-bottom: 24px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.clear-button {
  padding: 6px 12px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #666;
  transition: background-color 0.3s ease;
}

.clear-button:hover {
  background-color: #e0e0e0;
}

.day-places {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selected-place {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
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

/* Map container styles */
.map-container {
  width: 100%;
  height: 100%;
}

/* Toggle button styles */
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

.toggle-button:hover {
  transform: scale(1.1);
}

.toggle-button i {
  font-size: 18px;
  color: #666;
  transition: color 0.3s ease;
}

.toggle-button:hover i {
  color: #f57c00;
}
</style>
