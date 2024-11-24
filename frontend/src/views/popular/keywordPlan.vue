<template>
  <div
    class="season-plan-container min-h-screen bg-gradient-to-b from-[#433629] to-[#2a1f15]"
  >
    <navBar />

    <!-- 상단 타이틀 섹션 -->
    <div class="text-center">
      <h1 class="season-title">{{ keyword }}</h1>
      <p class="sub-title">AI가 추천하는 최적의 여행 코스</p>
    </div>

    <!-- 지도 컨테이너 -->
    <div class="map-container" v-if="planData">
      <tmap-multipath
        ref="tmapComponent"
        :selected-places-by-day="getSelectedPlaces"
        :latitude="getCenterCoordinates.latitude"
        :longitude="getCenterCoordinates.longitude"
        :selected-day="selectedDay"
        :show-all-days="isShowingAllDays"
      />
    </div>

    <!-- 로딩 표시 -->
    <div v-if="loading" class="flex justify-center items-center min-h-[60vh]">
      <div
        class="animate-spin rounded-full h-32 w-32 border-t-2 border-b-2 border-[#ecb27b]"
      ></div>
    </div>

    <!-- 에러 메시지 -->
    <div v-else-if="error" class="text-center text-red-500 p-8">
      {{ error }}
    </div>

    <!-- 여행 계획 표시 -->
    <div v-else-if="planData" class="plan-container">
      <!-- 일자 선택 버튼 -->
      <div class="day-selector">
        <button
          @click="handleDaySelect('all')"
          :class="['day-button', selectedDay === 'all' ? 'active' : '']"
        >
          전체 보기
        </button>
        <button
          v-for="dayPlan in planData"
          :key="dayPlan.day"
          @click="handleDaySelect(dayPlan.day)"
          :class="['day-button', selectedDay === dayPlan.day ? 'active' : '']"
        >
          Day {{ dayPlan.day }}
        </button>
      </div>

      <div class="plans-grid">
        <div
          v-for="(dayPlan, dayIndex) in planData"
          :key="dayIndex"
          class="day-plan-card"
        >
          <!-- 일자 타이틀 -->
          <div class="days-container">
            <div class="ml-4 days-instance">
              <h2 class="days-title">DAY {{ dayPlan.day }}</h2>
            </div>
          </div>

          <!-- 캐러셀 섹션 -->
          <div class="gallery">
            <div class="carousel-container">
              <div
                class="carousel-track"
                :style="{
                  transform: `translateX(-${currentIndexes[dayIndex] * 100}%)`,
                }"
              >
                <div
                  v-for="(attraction, attrIndex) in dayPlan.attractionDetails"
                  :key="attrIndex"
                  class="carousel-slide"
                  :class="{ active: currentIndexes[dayIndex] === attrIndex }"
                >
                  <img
                    :src="
                      attraction.image1 ||
                      'https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png'
                    "
                    :alt="attraction.title"
                  />
                  <div class="slide-content">
                    <div class="tags-container">
                      <span
                        v-if="attraction.sidoName"
                        class="location-tag sido"
                      >
                        {{ attraction.sidoName }}
                      </span>
                      <span
                        v-if="attraction.gugunName"
                        class="location-tag gugun"
                      >
                        {{ attraction.gugunName }}
                      </span>
                      <span class="location-tag type">
                        {{ attraction.contentTypeName }}
                      </span>
                    </div>
                    <h3>{{ attraction.title }}</h3>
                    <p class="address">{{ attraction.addr1 }}</p>
                    <p v-if="attraction.tel" class="tel">
                      {{ attraction.tel }}
                    </p>
                  </div>
                </div>
              </div>

              <!-- 캐러셀 버튼 -->
              <button
                class="carousel-button prev"
                @click="prev(dayIndex)"
                :disabled="currentIndexes[dayIndex] === 0"
              >
                <span class="arrow">&lt;</span>
              </button>
              <button
                class="carousel-button next"
                @click="next(dayIndex)"
                :disabled="
                  currentIndexes[dayIndex] ===
                  dayPlan.attractionDetails.length - 1
                "
              >
                <span class="arrow">&gt;</span>
              </button>

              <!-- 인디케이터 -->
              <div class="carousel-indicators">
                <button
                  v-for="(_, index) in dayPlan.attractionDetails"
                  :key="index"
                  class="indicator"
                  :class="{ active: currentIndexes[dayIndex] === index }"
                  @click="goToSlide(dayIndex, index)"
                ></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 데이터가 없을 때 -->
    <div v-else class="text-center text-white p-8">
      여행 계획을 불러오는 중입니다...
    </div>
  </div>
</template>
<script>
import api from "@/plugins/axios";
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";

export default {
  name: "keywordPlan",
  components: {
    navBar,
    TmapMultipath,
  },
  data() {
    return {
      planData: null,
      loading: true,
      error: null,
      keyword: "",
      currentIndexes: {},
      isAnimating: false,
      selectedDay: 1,
    };
  },
  computed: {
    getSelectedPlaces() {
      if (!this.planData) return {};

      const selectedPlaces = {};
      this.planData.forEach((dayPlan) => {
        if (dayPlan.attractionDetails && dayPlan.attractionDetails.length > 0) {
          selectedPlaces[dayPlan.day] = dayPlan.attractionDetails;
        }
      });
      return selectedPlaces;
    },
    getCenterCoordinates() {
      if (!this.planData || this.planData.length === 0) {
        return { latitude: 33.3846, longitude: 126.5534 };
      }

      let totalLat = 0;
      let totalLong = 0;
      let count = 0;

      this.planData.forEach((dayPlan) => {
        if (dayPlan.attractionDetails) {
          dayPlan.attractionDetails.forEach((place) => {
            if (place.latitude && place.longitude) {
              totalLat += parseFloat(place.latitude);
              totalLong += parseFloat(place.longitude);
              count++;
            }
          });
        }
      });

      if (count === 0) {
        return { latitude: 33.3846, longitude: 126.5534 };
      }

      return {
        latitude: totalLat / count,
        longitude: totalLong / count,
      };
    },
    // 전체보기 상태를 확인하는 computed 속성 추가
    isShowingAllDays() {
      return this.selectedDay === "all";
    },
  },

  created() {
    this.keyword = this.$route.params.keyword;
    this.selectedDay = "all";
    this.fetchKeywordPlan(this.keyword);
  },

  methods: {
    async fetchKeywordPlan(keyword) {
      try {
        this.loading = true;
        const response = await api.get(
          `/api/attraction/ai/plan/keyword/${keyword}`
        );
        this.planData = response.data;
        // 각 일차별 현재 인덱스 초기화
        this.planData.forEach((_, index) => {
          this.currentIndexes[index] = 0;
        });
      } catch (error) {
        console.error("Error fetching keyword plan:", error);
        this.error = "여행 계획을 불러오는데 실패했습니다.";
      } finally {
        this.loading = false;
      }
    },

    // 위도/경도 관련 새로운 메서드들
    getCoordinates(attraction) {
      if (!attraction || !attraction.latitude || !attraction.longitude) {
        return null;
      }
      return {
        latitude: parseFloat(attraction.latitude),
        longitude: parseFloat(attraction.longitude),
      };
    },

    getDayCoordinates(day) {
      if (!this.planData) return [];

      const dayPlan = this.planData.find((plan) => plan.day === day);
      if (!dayPlan || !dayPlan.attractionDetails) return [];

      return dayPlan.attractionDetails
        .map((attraction) => this.getCoordinates(attraction))
        .filter((coord) => coord !== null);
    },

    getAllCoordinates() {
      if (!this.planData) return [];

      return this.planData.reduce((coords, dayPlan) => {
        if (dayPlan.attractionDetails) {
          const dayCoords = dayPlan.attractionDetails
            .map((attraction) => this.getCoordinates(attraction))
            .filter((coord) => coord !== null);
          return [...coords, ...dayCoords];
        }
        return coords;
      }, []);
    },

    // 캐러셀 관련 메서드들
    async next(dayIndex) {
      if (this.isAnimating) return;
      this.isAnimating = true;
      const maxIndex = this.planData[dayIndex].attractionDetails.length - 1;
      this.currentIndexes[dayIndex] = Math.min(
        this.currentIndexes[dayIndex] + 1,
        maxIndex
      );
      await this.waitForTransition();
      this.isAnimating = false;
    },
    async prev(dayIndex) {
      if (this.isAnimating) return;
      this.isAnimating = true;
      this.currentIndexes[dayIndex] = Math.max(
        this.currentIndexes[dayIndex] - 1,
        0
      );
      await this.waitForTransition();
      this.isAnimating = false;
    },
    async goToSlide(dayIndex, index) {
      if (this.isAnimating || this.currentIndexes[dayIndex] === index) return;
      this.isAnimating = true;
      this.currentIndexes[dayIndex] = index;
      await this.waitForTransition();
      this.isAnimating = false;
    },
    waitForTransition() {
      return new Promise((resolve) => {
        setTimeout(resolve, 500);
      });
    },
    getTypeClass(type) {
      const classes = {
        관광지: "bg-blue-500 text-white",
        레포츠: "bg-green-500 text-white",
        문화시설: "bg-purple-500 text-white",
        축제공연행사: "bg-yellow-500 text-white",
        음식점: "bg-red-500 text-white",
        숙박: "bg-indigo-500 text-white",
      };
      return classes[type] || "bg-gray-500 text-white";
    },
    handleDaySelect(day) {
      this.selectedDay = day;
    },
  },
};
</script>

<style scoped>
.text-center {
  margin-top: 44px;
  justify-content: center;
}

.season-title {
  font-family: "Black Han Sans";
  font-size: 96px;
  font-style: normal;
  color: #ecb27b;
}

.sub-title {
  color: white;
  font-family: "Pretendard-Regular";
  font-size: 18px;
}

.days-container {
  width: 100%;
  background-color: #ecb27b;
}

.days-instance {
  text-align: center;
}

.days-title {
  color: white;
  font-family: "Pretendard-Bold";
  font-size: 22px;
}

.plan-container {
  padding: 0 20px;
}

.gallery {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.day-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  justify-content: center;
}

.day-button {
  padding: 8px 16px;
  background-color: #6e6156;
  color: white;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.day-button.active {
  background-color: #ecb27b;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  width: 100%;
  height: 400px;
}

.day-plan-card {
  display: flex;
  flex-direction: column;
  background: #ecb27b;
  border-radius: 12px;
  overflow: hidden;
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 340px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.carousel-track {
  display: flex;
  height: 100%;
  transition: transform 0.5s ease-out;
}

.carousel-slide {
  flex: 0 0 100%;
  position: relative;
  overflow: hidden;
}

.carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.carousel-slide:hover img {
  transform: scale(1.05);
}

.slide-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 2rem;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
  color: white;
  transform: translateY(20%);
  opacity: 0;
  transition: all 0.5s ease;
}

.carousel-slide.active .slide-content {
  transform: translateY(0);
  opacity: 1;
}

.tags-container {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.location-tag {
  padding: 0.25rem 0.5rem;
  border-radius: 0.5rem;
  font-size: 12px;
  font-family: "Pretendard-SemiBold";
  color: white;
}

.sido {
  background-color: #433629;
}

.gugun {
  background-color: #6e6156;
}

.type {
  background-color: #ecb27b;
}

.slide-content h3 {
  font-size: 28px;
  margin: 0.5rem 0;
  font-family: "Pretendard-Bold";
}

.address {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.8);
  font-family: "Pretendard-Regular";
}

.carousel-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
}

.carousel-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.carousel-button:not(:disabled):hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-50%) scale(1.1);
}

.carousel-button.prev {
  left: 20px;
}

.carousel-button.next {
  right: 20px;
}

.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  background: transparent;
  cursor: pointer;
  padding: 0;
  transition: all 0.3s ease;
}

.indicator.active {
  background: white;
  transform: scale(1.2);
}

@media (max-width: 768px) {
  .carousel-container {
    width: 95%;
  }

  .slide-content h3 {
    font-size: 1.5rem;
  }

  .carousel-button {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
}
.map-container {
  width: 100%;
  height: 500px;
  margin: 20px 0;
  padding: 0 20px;
}

@media (max-width: 1200px) {
  .plans-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .plans-grid {
    grid-template-columns: 1fr;
  }
}
</style>
