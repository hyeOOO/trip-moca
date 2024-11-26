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

      <!-- Middle Section - 검색 및 장소 목록 -->
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
          <!-- 검색 결과 장소 목록 - 단순 레이아웃 -->
          <div
            v-for="place in filteredPlaces"
            :key="place.attractionId"
            class="place-item"
            draggable="true"
            @dragstart="dragStart($event, place)"
          >
            <div class="image-container">
              <img :src="getImageUrl(place.image1)" :alt="place.title" />
            </div>
            <div class="place-info">
              <div class="flex gap-2 mb-2">
                <span
                  v-if="place.contentTypeName"
                  class="px-2 py-1 rounded-lg text-xs text-white tag"
                  :style="{
                    backgroundColor: getContentTypeColor(place.contentTypeId),
                  }"
                >
                  {{ place.contentTypeName }}
                </span>
              </div>
              <h3>{{ place.title }}</h3>
              <p class="place-address">{{ place.addr1 }}</p>
            </div>
          </div>
          <div v-if="isFetching && currentPage > 0" class="loading-more">
            추가 데이터를 불러오는 중...
          </div>
        </div>
      </div>

      <!-- Right Section - 선택된 장소 목록 -->
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
          <button @click="showAllMarkers" class="view-all-button">전체보기</button>
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
                <span class="day">{{ dayIndex }}일차</span>
                <span class="date">{{ formatFullDate(getTripDate(dayIndex - 1)) }}</span>
              </h3>
              <div class="day-header-buttons">
                <button @click="clearDay(dayIndex - 1)" class="clear-button">
                  <i class="fa-solid fa-rotate-left"></i>
                </button>
              </div>
            </div>
            <div v-if="!selectedPlacesByDay[dayIndex - 1]?.length" class="empty-day">
              <p></p>
            </div>
            <div v-else class="selected-day-places">
              <!-- 선택된 장소 목록 - 순서 번호와 삭제 버튼이 있는 레이아웃 -->
              <div
                v-for="(place, index) in selectedPlacesByDay[dayIndex - 1]"
                :key="place.attractionId"
                class="selected-place"
                draggable="true"
                @dragstart="dragStartSelected($event, place, dayIndex - 1, index)"
                @dragover.prevent
                @drop.stop="onDropReorder($event, dayIndex - 1, index)"
              >
                <div class="order-number">{{ index + 1 }}</div>
                <div class="image-container">
                  <img :src="getImageUrl(place.image1)" :alt="place.title" />
                </div>
                <div class="place-info">
                  <div class="flex gap-2 mb-2">
                    <span
                      v-if="place.contentTypeName"
                      class="px-2 py-1 rounded-lg text-xs text-white tag"
                      :style="{
                        backgroundColor: getContentTypeColor(place.contentTypeId),
                      }"
                    >
                      {{ place.contentTypeName }}
                    </span>
                  </div>
                  <h3>{{ place.title }}</h3>
                  <p class="place-address">{{ place.addr1 }}</p>
                </div>
                <button @click="removePlace(dayIndex - 1, place)" class="delete-button">
                  <i class="fas fa-trash"></i>
                </button>
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
      pageSize: 50,
      isLastPage: false,
      isFetching: false,
      localSelectedPlaces: {},
      draggedItem: null,
      draggedDay: null,
      draggedIndex: null,
      isSelectedPlaceDrag: false,
      scrollTimeout: null,
      middleSectionRef: null,
      placeListRef: null,
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
    formatFullDate(date) {
      if (!date) return "";
      const days = ["일", "월", "화", "수", "목", "금", "토"];
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const dayOfWeek = days[date.getDay()];
      return `${year}.${month}.${day}(${dayOfWeek})`;
    },

    async fetchAttractions(page = 0, searchQuery = "") {
      console.log("Fetching attractions:", { page, searchQuery });

      if (!this.areaCode || this.isFetching || (page > 0 && this.isLastPage)) {
        console.log("Fetch prevented:", {
          noAreaCode: !this.areaCode,
          isFetching: this.isFetching,
          isLastPage: page > 0 && this.isLastPage,
        });
        return;
      }

      this.isFetching = true;
      if (page === 0) {
        this.isLoading = true;
        this.error = null;
      }

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
          const newPlaces = response.data.content.filter(
            (newPlace) =>
              !this.places.some(
                (existingPlace) => existingPlace.attractionId === newPlace.attractionId
              )
          );
          this.places = [...this.places, ...newPlaces];
        }

        console.log("Fetch success:", {
          page: response.data.number,
          totalPages: response.data.totalPages,
          isLast: response.data.last,
          newItemsCount: response.data.content.length,
        });

        this.currentPage = response.data.number;
        this.totalPages = response.data.totalPages;
        this.isLastPage = response.data.last;
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

    handleScroll(event) {
      if (this.scrollTimeout) {
        clearTimeout(this.scrollTimeout);
      }

      this.scrollTimeout = setTimeout(() => {
        const target = this.placeListRef;
        if (!target) return;

        const { scrollTop, clientHeight, scrollHeight } = target;
        const threshold = 50; // 스크롤 바닥에서 50px 전에 로딩 시작

        console.log("Scroll metrics:", {
          scrollTop,
          clientHeight,
          scrollHeight,
          remaining: scrollHeight - (scrollTop + clientHeight),
        });

        if (
          !this.isLastPage &&
          !this.isFetching &&
          scrollHeight - (scrollTop + clientHeight) <= threshold
        ) {
          console.log("Fetching next page...");
          this.fetchAttractions(this.currentPage + 1, this.searchQuery.trim());
        }
      }, 200);
    },

    getContentTypeColor(contentType) {
      const colorMap = {
        12: "#ecb27b",
        14: "#6E6156",
        15: "#433629",
        25: "#332417",
        28: "#988D82",
        32: "#C3A386",
        38: "#ecb27b",
        39: "#6E6156",
      };
      return colorMap[contentType] || "#ecb27b";
    },

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
      this.draggedDay = dayIndex;
      this.draggedIndex = placeIndex;

      event.dataTransfer.setData(
        "text/plain",
        JSON.stringify({
          place,
          fromDay: dayIndex,
          fromIndex: placeIndex,
          isNew: false,
        })
      );

      event.target.classList.add("dragging");
    },

    onDragEnd(event) {
      event.target.classList.remove("dragging");
      this.resetDragState();
    },

    onDrop(event, dayIndex) {
      event.preventDefault();

      // 이미 선택된 장소의 드래그 앤 드롭인 경우 처리하지 않음
      if (this.isSelectedPlaceDrag) {
        return;
      }

      try {
        const data = JSON.parse(event.dataTransfer.getData("text/plain"));
        const place = data.place;

        // 새로운 장소 추가인 경우에만 실행
        if (data.isNew) {
          const newPlacesByDay = { ...this.selectedPlacesByDay };

          if (!newPlacesByDay[dayIndex]) {
            newPlacesByDay[dayIndex] = [];
          }

          if (!place.latitude && place.mapy) {
            place.latitude = place.mapy;
          }
          if (!place.longitude && place.mapx) {
            place.longitude = place.mapx;
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
        }
      } catch (error) {
        console.error("Error in onDrop:", error);
      }
    },

    onDropReorder(event, dayIndex, targetIndex) {
      event.preventDefault();
      event.stopPropagation();

      if (!this.isSelectedPlaceDrag) {
        return;
      }

      try {
        const data = JSON.parse(event.dataTransfer.getData("text/plain"));
        const { fromDay, fromIndex, place } = data;

        if (fromDay === dayIndex) {
          // 같은 날짜 내에서의 순서 변경
          const places = [...(this.selectedPlacesByDay[dayIndex] || [])];
          places.splice(fromIndex, 1);
          places.splice(targetIndex, 0, place);

          this.selectedPlacesByDay = {
            ...this.selectedPlacesByDay,
            [dayIndex]: places,
          };
        } else {
          // 다른 날짜로의 이동
          const fromPlaces = [...(this.selectedPlacesByDay[fromDay] || [])];
          const toPlaces = [...(this.selectedPlacesByDay[dayIndex] || [])];

          fromPlaces.splice(fromIndex, 1);
          toPlaces.splice(targetIndex, 0, place);

          this.selectedPlacesByDay = {
            ...this.selectedPlacesByDay,
            [fromDay]: fromPlaces,
            [dayIndex]: toPlaces,
          };
        }
      } catch (error) {
        console.error("Error in onDropReorder:", error);
      }

      this.resetDragState();
    },

    resetDragState() {
      this.draggedItem = null;
      this.draggedDay = null;
      this.draggedIndex = null;
      this.isSelectedPlaceDrag = false;

      document.querySelectorAll(".dragging").forEach((el) => {
        el.classList.remove("dragging");
      });
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
      this.currentPage = 0;
      this.isLastPage = false;
      this.places = [];
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

  mounted() {
    this.fetchAttractions();
    this.placeListRef = this.$el.querySelector(".places-list");
    if (this.placeListRef) {
      this.placeListRef.addEventListener("scroll", this.handleScroll);
    }
  },
  beforeUnmount() {
    if (this.placeListRef) {
      this.placeListRef.removeEventListener("scroll", this.handleScroll);
    }
    if (this.scrollTimeout) {
      clearTimeout(this.scrollTimeout);
    }
  },

  watch: {
    // areaCode가 변경될 때 데이터 초기화 및 새로운 데이터 로드
    areaCode: {
      handler(newVal) {
        if (newVal) {
          this.currentPage = 0;
          this.isLastPage = false;
          this.places = [];
          this.fetchAttractions();
        }
      },
      immediate: true,
    },
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
  grid-template-columns: 107px 440px 440px minmax(0, 1fr);
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
  padding: 10px;
}

.middle-section,
.right-section {
  overflow-y: auto;
  transition: all 0.3s ease;
}

/* 섹션 레이아웃 */
.middle-section,
.right-section {
  display: flex;
  flex-direction: column;
}

.right-section .header {
  flex-shrink: 0;
}

.selected-places {
  flex-grow: 1;
  overflow-y: auto;
}

.right-section .save-button {
  flex-shrink: 0;
  margin-top: 20px;
}

.middle-section .header,
.middle-section .search-section {
  flex-shrink: 0;
}

.places-list {
  flex-grow: 1;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding: 0 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 가로/세로 스크롤바 스타일 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #ffffff;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #ecb27b;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #c3a386;
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
  font-family: "EliceDigitalBaeum_bold";
  color: #ecb27b;
  font-size: 14px;
}

/* 네비게이션 스타일 */
.steps-nav {
  text-align: center;
  font-family: "EliceDigitalBaeum_regular";
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
  color: #b4b4b4;
}

.step-number {
  margin-bottom: 4px;
}

.step:hover .step-number,
.step:hover .step-title,
.step.active .step-number,
.step.active .step-title {
  color: #ecb27b;
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
  font-family: "Pretendard-Regular";
}

.search-box i {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  cursor: pointer;
}

/* 장소 아이템 및 선택된 장소 공통 스타일 */
.place-item,
.selected-place {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 7px;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
  cursor: grab;
  position: relative;
  transition: transform 0.2s ease;
}

.selected-place {
  width: 400px;
  margin-bottom: 1rem;
  padding-left: 48px;
}

.place-item:hover,
.selected-place:hover {
  transform: translateY(-2px);
}

/* 순서 번호 스타일 */
.spot-number,
.order-number {
  font-family: "Pretendard-SemiBold";
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6e6156;
  font-size: 1.125rem;
}

.order-number {
  position: absolute;
  left: 12px;
  width: 24px;
  height: 24px;
  background-color: #ecb27b;
  color: white;
  border-radius: 50%;
  font-weight: bold;
  font-size: 14px;
}

/* 이미지 컨테이너 스타일 */
.image-container,
.place-image {
  flex-shrink: 0;
  width: 145px;
  height: 6rem;
  margin-right: 1rem;
  border-radius: 0.5rem;
  overflow: hidden;
}

.image-container img,
.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0.5rem;
}

/* 장소 정보 스타일 */
.place-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.place-info h3,
.place-info h4 {
  font-family: "Pretendard-Medium";
  font-weight: bold;
  font-size: 1.125rem;
  margin-bottom: 4px;
}

.place-info p,
.place-address {
  font-family: "Pretendard-Regular";
  font-size: 14px;
  color: #b4b4b4;
}

/* 태그 스타일 */
.tag {
  font-family: "Pretendard-SemiBold";
  font-size: 12px;
}

/* 삭제 버튼 스타일 */
.delete-button,
.remove-button {
  color: #b4b4b4;
  padding: 0.5rem;
  transition: color 0.2s;
  background: none;
  border: none;
  cursor: pointer;
}

.delete-button:hover,
.remove-button:hover {
  color: #666;
}

/* 드래그 상태 */
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
  font-family: "Pretendard-Medium";
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.day-header h3 {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.day-header h3 .day {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 20px;
  margin-right: 5px;
}
.day-header h3 .date {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 12px;
  padding-top: 5px;
}
.day-header h3:hover {
  background-color: #f0f0f0;
}
.day-header-buttons {
  display: flex;
  gap: 8px;
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
  color: #ecb27b;
}

/* 기타 버튼 스타일 */
.view-all-button {
  margin-left: 10px;
  border-radius: 10px;
  cursor: pointer;
  padding: 6px 12px;
  background-color: #ecb27b;
  color: white;
  font-family: "Pretendard-Regular";
}

.clear-button {
  border: none;
  border-radius: 4px;
  cursor: pointer;
  padding: 6px 12px;
}
.clear-button:hover {
  color: #f44336;
}

.save-button {
  width: 100%;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 500;
  background: #2b2b2b;
  color: white;
  border: none;
  border-radius: 4px;
  font-family: "Pretendard-Medium";
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
  color: #ecb27b;
}

.loading-more {
  padding: 16px;
  font-size: 14px;
  color: #666;
}

/* 기타 */
.empty-day {
  height: 100px;
}

/* 콘텐츠 접힘 상태 */
.content-wrapper.collapsed .middle-section,
.content-wrapper.right-collapsed .right-section {
  width: 0;
  padding: 0;
  overflow: hidden;
  opacity: 0;
}

/* 드롭 영역 스타일 */
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
</style>
