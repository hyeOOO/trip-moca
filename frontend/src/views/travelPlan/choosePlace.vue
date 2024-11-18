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
            <i class="fa-solid fa-search" @click="handleSearch"></i>
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
                <button @click="showAllMarkers" class="view-all-button">
                  전체보기
                </button>
                <button @click="clearDay(dayIndex - 1)" class="clear-button">
                  <i class="fa-solid fa-rotate-left"></i> 초기화
                </button>
              </div>
            </div>
            <div
              v-if="!selectedPlacesByDay[dayIndex - 1]?.length"
              class="empty-day"
            >
              <p></p>
            </div>
            <div v-else class="selected-day-places">
              <div
                v-for="place in selectedPlacesByDay[dayIndex - 1]"
                :key="place.attractionId"
                class="selected-place"
              >
                <div class="place-image">
                  <img :src="getImageUrl(place.image1)" :alt="place.title" />
                </div>
                <div class="place-info">
                  <h4>{{ place.title }}</h4>
                  <button
                    @click="removePlace(dayIndex - 1, place)"
                    class="remove-button"
                  >
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
import testData from "@/assets/data/testData.js"; // 임시 데이터
// import axios from "@/api/axiosConfig"; // 1. Axios 설정 임시 데이터 지우고 가져오삼
import Tmap from "@/components/Tmap/Tmap.vue";

export default {
  name: "ChoosePlace",
  components: {
    navBar,
    Tmap,
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
      places: testData, // 2. 이거 places: [], 로 바꾸고 axios로 가져오면 될걸?
      isCollapsed: false,
      isRightCollapsed: false,
      isStep2Active: true,
      selectedPlacesByDay: {},
      selectedDay: null,
      showAllDays: false,
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
    // 3.
    //  async fetchPlaces() {
    //   try {
    //     const response = await axios.get("/places"); // 백엔드 API 엔드포인트
    //     this.places = response.data; // JSON 데이터를 상태에 저장
    //   } catch (error) {
    //     console.error("데이터를 가져오는 중 오류 발생:", error);
    //     alert("장소 데이터를 가져올 수 없습니다.");
    //   }
    // }, -> 데이터 가져오는 메서드..?
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
      event.dataTransfer.setData("text/plain", JSON.stringify(place));
    },
    // 드롭 했을때
    onDrop(event, dayIndex) {
      const place = JSON.parse(event.dataTransfer.getData("text/plain"));
      console.log("Dropped place full data:", place);

      // longitude와 latitude 값이 있는지 확인하고, 없다면 영어 이름의 프로퍼티에서 값을 가져옴
      if (!place.latitude && place.mapy) {
        place.latitude = place.mapy;
      }
      if (!place.longitude && place.mapx) {
        place.longitude = place.mapx;
      }

      console.log("Processed coordinates:", {
        latitude: place.latitude,
        longitude: place.longitude,
      });

      if (!this.selectedPlacesByDay[dayIndex]) {
        this.selectedPlacesByDay[dayIndex] = [];
      }
      this.selectedPlacesByDay[dayIndex].push(place);
    },
    removePlace(dayIndex, place) {
      this.selectedPlacesByDay[dayIndex] = this.selectedPlacesByDay[
        dayIndex
      ].filter((p) => p.attractionId !== place.attractionId);
    },
    clearDay(dayIndex) {
      this.selectedPlacesByDay[dayIndex] = [];
    },
    getImageUrl(imageUrl) {
      return (
        imageUrl ||
        "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png"
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
  },
  // 4.
  // async mounted() {
  //   await this.fetchPlaces(); // 컴포넌트가 마운트될 때 백엔드에서 데이터 가져오는부분
  // },
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
  background: #2B2B2B;
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
</style>

// 백엔드 API 예시
// /place 엔드포인트에서 Json 형태로 데이터를 가져와야함
// app.get("/places", (req, res) => {
//   res.json([
//     {
//       attractionId: 12497,
//       title: "가마오름",
//       addr1: "제주특별자치도 제주시 한경면 청수서5길 63",
//       latitude: 33.3059197039,
//       longitude: 126.2507039833,
//       image1: "http://tong.visitkorea.or.kr/cms/resource/95/3026695_image2_1.jpg",
//     },
//     // 추가 데이터...
//   ]);
// });