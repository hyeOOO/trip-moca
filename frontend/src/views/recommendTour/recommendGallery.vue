<template>
  <div class="destination-background">
    <navBar />
    <div class="container">
      <h1 class="title">
        여행계획을 짜기가 어려우시다고요?! 여행지만 선택해주세요!!
      </h1>
      <div class="search-section">
        <form autocomplete="off" @submit="handleSubmit">
          <div class="finder" :class="{ active: isActive, processing: isProcessing }">
            <div class="finder__outer">
              <div class="finder__inner">
                <div class="finder__icon" ref="icon"></div>
                <input class="finder__input" type="text" name="q" ref="searchInput" v-model="searchQuery"
                  @focus="handleFocus" @blur="handleBlur" :disabled="isProcessing" placeholder="혹시 찾는 곳이 있으면 검색해주세요!" />
              </div>
            </div>
          </div>
        </form>
      </div>

      <!-- 도시 그리드 -->
      <div class="destination-grid">
        <div v-for="(destination, index) in filteredDestinations" :key="destination.id" class="destination-item"
          @click="selectDestination(destination)">
          <div :class="['flip', { 'flip-vertical': index % 2 === 1 }]">
            <div class="front" :style="{ backgroundImage: `url(${destination.image1})` }">
              <h1 class="text-shadow">{{ destination.nameEn }}</h1>
            </div>
            <div class="back">
              <h2>{{ destination.nameKo }}</h2>
              <p>{{ destination.nameEn }}</p>
              <p>{{ destination.info }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 로딩 오버레이 -->
      <div v-if="isLoading" class="loading-overlay">
        <div class="loading-content">
          <div class="spinner"></div>
          <h2 class="loading-text">AI가 최적의 여행 계획을 만들고 있습니다</h2>
          <p class="loading-description">잠시만 기다려주세요...</p>
          <p class="loading-timer">{{ formattedLoadingTime }}</p>
        </div>
      </div>

      <!-- 모달 컴포넌트 -->
      <travelModal v-if="isModalOpen" v-model:isOpen="isModalOpen" :destinationImage="selectedDestination?.image1"
        :destinationName="selectedDestination?.nameKo" :destinationInfo="selectedDestination?.info"
        @confirm="handleModalConfirm" />
    </div>
    <welcomeAnimation v-if="showAnimation" @animation-complete="handleAnimationComplete" />
  </div>
</template>

<script>
import { defineComponent } from "vue";
import navBar from "@/components/navBar.vue";
import travelModal from "@/components/travelModal.vue";
import DestinationGalleryData from "@/assets/data/DestinationGalleryData.js";
import { useAiRecommendPlanStore } from "@/store/aiRecommendPlanStore";
import welcomeAnimation from "@/views/animationView/welcomeAnimation.vue";
import { useRouter } from "vue-router";

export default defineComponent({
  name: "DestinationGallery",
  components: {
    navBar,
    travelModal,
    welcomeAnimation,
  },

  setup() {
    const aiRecommendStore = useAiRecommendPlanStore();
    const router = useRouter();
    return { aiRecommendStore, router };
  },

  data() {
    return {
      destinations: DestinationGalleryData,
      searchQuery: "",
      isActive: false,
      isProcessing: false,
      isModalOpen: false,
      selectedDestination: null,
      showAnimation: false,
      isLoading: false,
      loadingStartTime: null,
      loadingTimer: null,
      elapsedSeconds: 0,
    };
  },

  computed: {
    filteredDestinations() {
      const search = this.searchQuery.toLowerCase().trim();

      if (!search) return this.destinations;

      return this.destinations.filter((destination) => {
        // 정확한 도시 이름 매칭 (가중치 높음)
        if (destination.nameKo === search ||
          destination.nameEn.toLowerCase() === search) {
          return true;
        }

        // 부분 도시 이름 매칭
        if (destination.nameKo.toLowerCase().includes(search) ||
          destination.nameEn.toLowerCase().includes(search)) {
          return true;
        }

        // 정확한 시/군/구 매칭
        if (destination.districts && destination.districts.some(district => {
          const districtLower = district.toLowerCase();
          // 정확한 매칭 ("강남구" === "강남구")
          if (districtLower === search) return true;
          // 부분 매칭 중 구/시/군 단위 확인
          if (search.endsWith('구') || search.endsWith('시') || search.endsWith('군')) {
            return districtLower.includes(search);
          }
          // 일반 부분 매칭
          return districtLower.includes(search);
        })) {
          return true;
        }

        return false;
      });
    },
    formattedLoadingTime() {
      const minutes = Math.floor(this.elapsedSeconds / 60);
      const seconds = this.elapsedSeconds % 60;
      return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    }
  },

  methods: {
    startLoadingTimer() {
      this.loadingStartTime = new Date();
      this.elapsedSeconds = 0;
      this.loadingTimer = setInterval(() => {
        this.elapsedSeconds++;
      }, 1000);
    },
    stopLoadingTimer() {
      if (this.loadingTimer) {
        clearInterval(this.loadingTimer);
        this.loadingTimer = null;
      }
    },
    handleFocus() {
      this.isActive = true;
    },

    handleBlur() {
      if (this.searchQuery.length === 0) {
        this.isActive = false;
      }
    },

    handleSubmit(event) {
      event.preventDefault();
      this.isProcessing = true;
      this.isActive = false;

      setTimeout(() => {
        this.isProcessing = false;
        if (this.searchQuery.length > 0) {
          this.isActive = true;
        }
      }, 1000);
    },

    // 여행지 선택시 모달 오픈
    selectDestination(destination) {
      this.selectedDestination = destination;
      this.isModalOpen = true;
    },

    // 모달에서 선택 완료시 처리
    // 모달에서 선택 완료시 처리
    async handleModalConfirm({ days }) {
      if (this.selectedDestination) {
        try {
          this.isLoading = true;
          this.startLoadingTimer();

          // Store 초기화
          this.aiRecommendStore.resetStore();

          // 선택된 목적지 정보 저장
          this.aiRecommendStore.$patch({
            selectedDestination: {
              id: this.selectedDestination.id,
              areaCode: this.selectedDestination.sidoCode,
              areaName: this.selectedDestination.nameKo,
              title: this.selectedDestination.nameKo,
              image: this.selectedDestination.image1,
              numberOfDays: days,
              info: this.selectedDestination.info,
            },
          });

          // API 호출 실행
          await this.aiRecommendStore.generateAiPlan();

          // 로딩이 너무 빨리 끝나면 최소 2초는 로딩 표시
          const endTime = new Date();
          const loadingDuration = endTime - this.loadingStartTime;
          if (loadingDuration < 2000) {
            await new Promise(resolve => setTimeout(resolve, 2000 - loadingDuration));
          }

          // 페이지 이동은 API 호출이 완료된 후에 실행
          await this.router.push({
            name: "modifyRecommendTour",
            params: { id: this.selectedDestination.id.toString() },
          });

        } catch (error) {
          console.error("Error during plan creation:", error);
          alert("여행 계획을 생성하는 중 오류가 발생했습니다.");
        } finally {
          this.stopLoadingTimer();
          this.isLoading = false;
        }
      }
    }
  },
});
</script>

<style scoped>
/* 기본 레이아웃 스타일 */
.destination-background {
  min-height: 100vh;
  background-color: #f8f9fa;
  position: relative;
  /* 로딩 오버레이를 위해 추가 */
}

.container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

/* 타이틀 스타일 */
.title {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 1.875rem;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

/* 검색 섹션 스타일 */
.search-section {
  width: 1130px;
  margin: 15px auto 15px;
}

/* 검색 폼 스타일 */
form {
  transition: all 0.5s;
  position: relative;
}

/* 검색창 컨테이너 스타일 */
.finder {
  background-color: #f6f5f0;
  border-radius: 15px;
  padding: 4px;
  box-shadow: 9px 9px 16px rgba(189, 189, 189, 0.6),
    -9px -9px 16px rgba(255, 255, 255, 0.5);
}

.finder__outer {
  display: flex;
  width: 100%;
  padding: 10px 15px;
  border-radius: 10px;
  box-shadow: inset 10px 10px 15px -10px #c3c3c3,
    inset -10px -10px 15px -10px #ffffff;
}

.finder__inner {
  display: flex;
  align-items: center;
  position: relative;
  flex: 1;
}

.finder__input {
  font-family: "Pretendard-Light";
  width: 100%;
  border: none;
  background-color: transparent;
  outline: none;
  font-size: 1.5rem;
  letter-spacing: 0.75px;
}

/* 검색 아이콘 스타일 */
.finder__icon {
  width: 30px;
  height: 30px;
  margin-right: 1rem;
  transition: all 0.2s;
  box-shadow: inset 0 0 0 20px #292929;
  border-radius: 50%;
  position: relative;
}

/* 로딩 오버레이 스타일 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-content {
  text-align: center;
  color: white;
  padding: 2rem;
}

.loading-text {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 1.5rem;
  margin: 1rem 0;
}

.loading-description {
  font-family: "EliceDigitalBaeum_Regular";
  margin-bottom: 0.5rem;
}

.loading-timer {
  font-family: "Roboto Mono";
  font-size: 1.2rem;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ecb27b;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* 여행지 그리드 레이아웃 */
.destination-grid {
  display: grid;
  grid-template-columns: repeat(3, 400px);
  gap: 30px;
  padding: 1em;
  justify-content: center;
}

/* 반응형 그리드 설정 */
@media (max-width: 1240px) {
  .destination-grid {
    grid-template-columns: repeat(2, 400px);
  }
}

@media (max-width: 820px) {
  .destination-grid {
    grid-template-columns: repeat(1, 400px);
  }
}

/* 플립 카드 스타일 */
.flip {
  position: relative;
  display: inline-block;
  width: 400px;
  margin-bottom: 1em;
  border-radius: 10px;
}

/* 카드 앞면과 뒷면 공통 스타일 */
.flip>.front,
.flip>.back {
  display: block;
  transition-timing-function: cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transition-duration: 0.5s;
  transition-property: transform, opacity;
  color: white;
  width: inherit;
  background-size: cover !important;
  background-position: center !important;
  height: 220px;
  padding: 1em 2em;
  background: #6e6156;
  border-radius: 10px;
}

/* 카드 플립 애니메이션 */
.flip>.front {
  transform: rotateY(0deg);
  background-color: #313131;
}

.flip>.back {
  position: absolute;
  opacity: 0;
  top: 0px;
  left: 0px;
  width: 100%;
  height: 100%;
  transform: rotateY(-180deg);
}

.flip:hover>.front {
  transform: rotateY(180deg);
}

.flip:hover>.back {
  opacity: 1;
  transform: rotateY(0deg);
}

/* 수직 플립 효과 */
.flip.flip-vertical>.back {
  transform: rotateX(-180deg);
}

.flip.flip-vertical:hover>.front {
  transform: rotateX(180deg);
}

.flip.flip-vertical:hover>.back {
  transform: rotateX(0deg);
}

/* 카드 내부 텍스트 스타일 */
.flip>.front h1 {
  font-size: 2em;
  margin: 0;
  font-family: "Roboto Mono";
}

.flip>.back h2 {
  font-size: 1.3em;
  margin-bottom: 1em;
}

.flip>.back p {
  font-size: 0.9125rem;
  line-height: 160%;
  color: #ffffff;
}

/* 텍스트 그림자 효과 */
.text-shadow {
  text-shadow: 1px 1px rgba(0, 0, 0, 0.04), 2px 2px rgba(0, 0, 0, 0.04),
    3px 3px rgba(0, 0, 0, 0.04), 4px 4px rgba(0, 0, 0, 0.04),
    0.125rem 0.125rem rgba(0, 0, 0, 0.04), 6px 6px rgba(0, 0, 0, 0.04),
    7px 7px rgba(0, 0, 0, 0.04), 8px 8px rgba(0, 0, 0, 0.04),
    9px 9px rgba(0, 0, 0, 0.04), 0.3125rem 0.3125rem rgba(0, 0, 0, 0.04),
    11px 11px rgba(0, 0, 0, 0.04), 12px 12px rgba(0, 0, 0, 0.04),
    13px 13px rgba(0, 0, 0, 0.04), 14px 14px rgba(0, 0, 0, 0.04),
    0.625rem 0.625rem rgba(0, 0, 0, 0.04), 16px 16px rgba(0, 0, 0, 0.04),
    17px 17px rgba(0, 0, 0, 0.04), 18px 18px rgba(0, 0, 0, 0.04),
    19px 19px rgba(0, 0, 0, 0.04), 1.25rem 1.25rem rgba(0, 0, 0, 0.04);
}

.fullscreen-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  background-color: white;
}
</style>