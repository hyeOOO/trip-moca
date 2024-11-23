<template>
  <div class="destination-background">
    <navBar />
    <div class="container">
      <h1 class="title">
        여행계획을 짜기가 어려우시다고요?! 여행지만 선택해주세요!!
      </h1>
      <div class="search-section">
        <form autocomplete="off" @submit="handleSubmit">
          <div
            class="finder"
            :class="{ active: isActive, processing: isProcessing }"
          >
            <div class="finder__outer">
              <div class="finder__inner">
                <div class="finder__icon" ref="icon"></div>
                <input
                  class="finder__input"
                  type="text"
                  name="q"
                  ref="searchInput"
                  v-model="searchQuery"
                  @focus="handleFocus"
                  @blur="handleBlur"
                  :disabled="isProcessing"
                  placeholder="혹시 찾는 곳이 있으면 검색해주세요!"
                />
              </div>
            </div>
          </div>
        </form>
      </div>

      <!-- 도시 그리드 -->
      <div class="destination-grid">
        <div
          v-for="(destination, index) in filteredDestinations"
          :key="destination.id"
          class="destination-item"
          @click="selectDestination(destination)"
        >
          <div :class="['flip', { 'flip-vertical': index % 2 === 1 }]">
            <div
              class="front"
              :style="{ backgroundImage: `url(${destination.image1})` }"
            >
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

      <!-- 모달 컴포넌트 -->
      <travelModal
        v-if="isModalOpen"
        v-model:isOpen="isModalOpen"
        :destinationImage="selectedDestination?.image1"
        :destinationName="selectedDestination?.nameKo"
        :destinationInfo="selectedDestination?.info"
        @confirm="handleModalConfirm"
      />
    </div>
    <welcomeAnimation
      v-if="showAnimation"
      @animation-complete="handleAnimationComplete"
    />
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
    };
  },

  computed: {
    filteredDestinations() {
      return this.destinations.filter((destination) => {
        const search = this.searchQuery.toLowerCase();
        return (
          destination.nameKo.toLowerCase().includes(search) ||
          destination.nameEn.toLowerCase().includes(search)
        );
      });
    },
  },

  methods: {
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
    async handleModalConfirm({ days }) {
      if (this.selectedDestination) {
        try {
          // 선택된 목적지 정보 저장 - sidoCode 사용
          this.aiRecommendStore.$patch({
            selectedDestination: {
              id: this.selectedDestination.id,                  // router용으로 유지
              areaCode: this.selectedDestination.sidoCode,      // sidoCode로 변경
              areaName: this.selectedDestination.nameKo,
              title: this.selectedDestination.nameKo,
              image: this.selectedDestination.image1,
              numberOfDays: days,
              info: this.selectedDestination.info,
            },
          });

          // 제주도인 경우에만 더미 데이터 불러오기
          if (this.selectedDestination.sidoCode === "39") {
            const aiRecommendData = (
              await import("@/assets/data/aiRecommendData.js")
            ).default;

            // 데이터 형식 변환 및 저장
            const formattedData = aiRecommendData
              .slice(0, days)
              .map((dayPlan, index) => ({
                day: index + 1,
                details: dayPlan.attractionDetails.map((detail) => ({
                  attractionId: detail.attractionId,
                  attractionTitle: detail.title,
                  image: detail.image1,
                  latitude: detail.latitude,
                  longitude: detail.longitude,
                  addr1: detail.addr1,
                  memo: "",
                })),
              }));

            // 스토어에 데이터 저장
            await this.aiRecommendStore.setRecommendPlan(formattedData);
          }

          // 애니메이션 및 페이지 이동
          this.showAnimation = true;
          await new Promise((resolve) => setTimeout(resolve, 7000));
          await this.router.push({
            name: "modifyRecommendTour",
            params: { id: this.selectedDestination.id.toString() },
          });
        } catch (error) {
          console.error("Error during plan creation:", error);
          alert("여행 계획을 생성하는 중 오류가 발생했습니다.");
        }
      }
    },
  },
});
</script>

<style scoped>
/* 기본 레이아웃 스타일 */
.destination-background {
  min-height: 100vh;
  background-color: #f8f9fa;
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

/* 검색 아이콘 애니메이션 효과 */
.finder__icon:after,
.finder__icon:before {
  display: block;
  content: "";
  position: absolute;
  transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.finder__icon:after {
  width: 12px;
  height: 12px;
  background-color: #292929;
  border: 3px solid #f6f5f0;
  top: 50%;
  position: absolute;
  transform: translateY(-50%);
  left: 0px;
  right: 0;
  margin: auto;
  border-radius: 50%;
}

.active .finder__icon:after {
  border-width: 10px;
  background-color: #f6f5f0;
}

.finder__icon:before {
  width: 3px;
  height: 13px;
  background-color: #f6f5f0;
  top: 44%;
  left: 17px;
  transform: rotateZ(45deg) translate(-50%, 0);
  transform-origin: 0 0;
  border-radius: 4px;
}

.active .finder__icon:before {
  background-color: #292929;
  width: 6px;
  transform: rotateZ(45deg) translate(-50%, 25px);
}

/* 검색 처리중 애니메이션 */
.processing .finder__icon {
  transform-origin: 50%;
  animation: spinner 0.3s linear infinite;
  animation-delay: 0.5s;
}

.active .finder__icon {
  transform: translateY(-5px);
}

@keyframes spinner {
  0% {
    transform: rotateZ(45deg);
  }
  100% {
    transform: rotateZ(405deg);
  }
}

/* 여행지 그리드 레이아웃 */
.destination-grid {
  display: grid;
  grid-template-columns: repeat(3, 400px); /* 3개 열로 수정, 각 400px 너비 */
  gap: 30px; /* 카드 사이 간격 2px로 설정 */
  padding: 1em;
  justify-content: center; /* 중앙 정렬 */
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
  border-radius: 10px; /* 추가 */
}

/* 카드 앞면과 뒷면 공통 스타일 */
.flip > .front,
.flip > .back {
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
.flip > .front {
  transform: rotateY(0deg);
  background-color: #313131;
}

.flip > .back {
  position: absolute;
  opacity: 0;
  top: 0px;
  left: 0px;
  width: 100%;
  height: 100%;
  transform: rotateY(-180deg);
}

.flip:hover > .front {
  transform: rotateY(180deg);
}

.flip:hover > .back {
  opacity: 1;
  transform: rotateY(0deg);
}

/* 수직 플립 효과 */
.flip.flip-vertical > .back {
  transform: rotateX(-180deg);
}

.flip.flip-vertical:hover > .front {
  transform: rotateX(180deg);
}

.flip.flip-vertical:hover > .back {
  transform: rotateX(0deg);
}

/* 카드 내부 텍스트 스타일 */
.flip > .front h1 {
  font-size: 2em;
  margin: 0;
  font-family: "Roboto Mono";
}

.flip > .back h2 {
  font-size: 1.3em;
  margin-bottom: 1em;
}

.flip > .back p {
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
  z-index: 9999; /* 최상위에 표시되도록 높은 z-index 설정 */
  background-color: white; /* 흰색 배경 설정 */
}
</style>
