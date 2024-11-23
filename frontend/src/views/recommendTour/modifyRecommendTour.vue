<template>
  <div class="travel-plan">
    <nav-bar />
    <div class="main-content">
      <!-- 좌측: 일정 선택 사이드바 -->
      <div class="sidebar">
        <div class="schedule-list">
          <!-- 전체 일정 버튼 -->
          <button
            class="schedule-btn"
            :class="{ active: selectedDay === 'all' }"
            @click="selectDay('all')"
          >
            <i class="fa-regular fa-calendar"></i><br />
            전체일정
          </button>

          <!-- 일자별 버튼 -->
          <div v-for="day in totalDays" :key="day" class="day-item">
            <button
              class="schedule-btn"
              :class="{ active: selectedDay === day }"
              @click="selectDay(day)"
            >
              <div class="day-title">DAY {{ day }}</div>
              <div class="day-date">{{ day }}일차</div>
            </button>
          </div>
        </div>

        <!-- 하단 저장,취소 버튼 -->
        <div class="edit-button-container">
          <button @click="saveModifiedPlan" class="edit-button">저장</button>
          <button @click="cancelModify" class="edit-button">취소</button>
        </div>
      </div>

      <!-- 우측: 장바구니 영역 -->
      <div
        class="cart-section"
        :class="{ collapsed: isRightCollapsed }"
        @dragover.prevent
        @drop="onDrop($event)"
      >
        <div class="section-header">
          <h2 class="section-title">장바구니</h2>
        </div>

        <div class="cart-items" @dragover.prevent @drop="onDrop($event)">
          <div
            v-for="(item, index) in cartItems"
            :key="item.attractionId"
            class="cart-item"
            draggable="true"
            @dragstart="dragStartCart($event, item, index)"
            @dragover.prevent
            @drop="onDrop($event, index)"
          >
            <div class="order-number">{{ index + 1 }}</div>
            <div class="place-image">
              <img
                :src="getImageUrl(item.image)"
                :alt="item.title"
                @error="handleImageError"
              />
            </div>
            <div class="place-info">
              <h3>{{ item.title }}</h3>
              <p>{{ item.addr1 }}</p>
            </div>
          </div>
        </div>

        <div class="cart-actions">
          <button
            @click="goToTravelCart"
            class="save-cart"
            :disabled="!cartItems.length"
          >
            더 담으러 가기
          </button>
        </div>
      </div>

      <!-- 중앙: 메인 콘텐츠 영역 -->
      <div class="content-area">
        <!-- 지도 영역 -->
        <div class="map-container">
          <tmap-multipath
            :selected-places-by-day="getSelectedPlaces"
            :latitude="mapCoordinates.latitude"
            :longitude="mapCoordinates.longitude"
            :selected-day="selectedDay === 'all' ? null : selectedDay"
            :show-all-days="selectedDay === 'all'"
          />
        </div>

        <!-- 일정 패널 -->
        <div
          ref="middleSection"
          class="schedule-panel"
          :class="{ expanded: isExpanded }"
          :style="{ width: `${currentWidth}px` }"
        >
          <div class="panel-content">
            <div class="schedule-detail">
              <!-- 여행 제목과 기간 -->
              <div class="trip-header" v-if="!isLoading">
                <h1>{{ displayTitle }}</h1>
                <p>{{ tripDuration }}</p>
              </div>

              <!-- 전체 일정 뷰 -->
              <div v-if="selectedDay === 'all'" class="all-schedules">
                <div class="days-grid" :style="gridStyle">
                  <div
                    v-for="dayPlan in planData.dayPlans"
                    :key="dayPlan.day"
                    class="day-container"
                    @dragover.prevent
                    @drop="handleDrop($event, dayPlan.day)"
                  >
                    <h2>
                      <span class="day">{{ dayPlan.day }}일차</span>
                      <span class="date">{{ getDayDate(dayPlan.day) }}</span>
                    </h2>
                    <div class="cart-items">
                      <div
                        v-for="(spot, index) in dayPlan.details"
                        :key="spot.attractionId"
                        class="cart-item"
                        draggable="true"
                        @dragstart="dragStart($event, spot, index, dayPlan.day)"
                        @dragover.prevent="
                          handleDragOver($event, dayPlan.day, index)
                        "
                        @drop="handleDrop($event, dayPlan.day, index)"
                      >
                        <div class="spot-number">{{ index + 1 }}</div>
                        <div class="place-image">
                          <img
                            :src="getImageUrl(spot.image)"
                            :alt="spot.attractionTitle"
                          />
                        </div>
                        <div class="place-info">
                          <h3>{{ spot.attractionTitle }}</h3>
                          <p>{{ spot.addr1 }}</p>
                        </div>
                        <div>
                          <button
                            @click="removePlace(dayPlan.day, index)"
                            class="delete-button"
                          >
                            <i class="fas fa-trash"></i>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 개별 일정 뷰 -->
              <div v-else class="day-schedule">
                <h2>
                  <span class="day">{{ selectedDay }}일차</span>
                  <span class="date">{{ getDayDate(selectedDay) }}</span>
                </h2>
                <div class="cart-items">
                  <div
                    v-for="(spot, index) in getCurrentDaySpots()"
                    :key="spot.attractionId"
                    class="cart-item"
                    draggable="true"
                    @dragstart="dragStart($event, spot, index, selectedDay)"
                  >
                    <div class="spot-number">{{ index + 1 }}</div>
                    <div class="place-image">
                      <img
                        :src="getImageUrl(spot.image)"
                        :alt="spot.attractionTitle"
                      />
                    </div>
                    <div class="place-info">
                      <h3>{{ spot.attractionTitle }}</h3>
                      <p>{{ spot.addr1 }}</p>
                    </div>
                    <div>
                      <button
                        @click="removePlace(selectedDay, index)"
                        class="delete-button"
                      >
                        <i class="fas fa-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 패널 크기 조절 핸들 -->
          <div
            ref="dragHandle"
            class="drag-handle"
            @mousedown="startDragExpand"
            @touchstart="startDragExpand"
          >
            <i class="fa-solid fa-grip-lines-vertical"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";
import { useAiRecommendPlanStore } from "@/store/aiRecommendPlanStore";

// 라우터 및 스토어 설정
const router = useRouter();
const route = useRoute();
const aiRecommendStore = useAiRecommendPlanStore();

// --- 상태 관리 변수들 ---
const selectedDay = ref("all");
const middleSection = ref(null);
const dragHandle = ref(null);
const isDragging = ref(false);
const isExpanded = ref(false);
const startX = ref(0);
const initialPanelOffset = ref(0);
const currentWidth = ref(464);
const panelOffset = ref(0);
const draggedItem = ref(null);
const isRightCollapsed = ref(false);
const isLoading = ref(true);
const cartItems = ref([]); // 장바구니 아이템
let animationFrame = null;

// --- Computed Properties ---
const planData = computed(() => aiRecommendStore.getPlanData);

const totalDays = computed(() => aiRecommendStore.getTotalDays);

const displayTitle = computed(() => {
  return `${aiRecommendStore.selectedDestination?.areaName} 여행`;
});

const tripDuration = computed(() => {
  const days = aiRecommendStore.selectedDestination?.numberOfDays || 0;
  return `${days - 1}박 ${days}일`;
});

const mapCoordinates = computed(() => {
  return aiRecommendStore.getMapCoordinates;
});

const getSelectedPlaces = computed(() => {
  if (!planData.value || !Array.isArray(planData.value.dayPlans)) return {};

  if (selectedDay.value === "all") {
    return planData.value.dayPlans.reduce((acc, day) => {
      if (!day || !Array.isArray(day.details)) {
        acc[day?.day || 0] = [];
        return acc;
      }

      acc[day.day] = day.details.map((spot) => ({
        id: spot.attractionId,
        title: spot.attractionTitle,
        latitude: Number(spot.latitude),
        longitude: Number(spot.longitude),
      }));
      return acc;
    }, {});
  }

  const dayPlan = planData.value.dayPlans.find(
    (day) => day?.day === selectedDay.value
  );

  if (!dayPlan || !Array.isArray(dayPlan.details)) {
    return { [selectedDay.value]: [] };
  }

  return {
    [selectedDay.value]: dayPlan.details.map((spot) => ({
      id: spot.attractionId,
      title: spot.attractionTitle,
      latitude: Number(spot.latitude),
      longitude: Number(spot.longitude),
    })),
  };
});

// --- 초기화 함수 ---
const initializeData = async () => {
  try {
    isLoading.value = true;

    // 제주도(39)가 아니거나 3일이 아닌 경우 리다이렉트
    if (
      aiRecommendStore.selectedDestination?.areaCode !== "39" ||
      aiRecommendStore.selectedDestination?.numberOfDays !== 3
    ) {
      console.log("Currently only available for Jeju 3-day trips");
      router.push("/recommendTour");
      return;
    }

    // 더미데이터 로드
    const aiRecommendData = (await import("@/assets/data/aiRecommendData.js"))
      .default;

    // 장바구니에 추가할 장소들 (예: 각 날짜의 첫 번째 장소)
    cartItems.value = [];

    // 일정 데이터 설정
    if (!aiRecommendStore.getPlanData?.dayPlans?.length) {
      const formattedData = aiRecommendData.map((dayPlan, index) => ({
        day: dayPlan.day,
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

      await aiRecommendStore.setRecommendPlan(formattedData);
    }
  } catch (error) {
    console.error("Data initialization error:", error);
    router.push("/recommendTour");
  } finally {
    isLoading.value = false;
  }
};

// --- 유틸리티 함수들 ---
const getImageUrl = (imageUrl) => {
  if (!imageUrl)
    return "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png";
  return imageUrl;
};

const getDayDate = (day) => {
  return `${day}일차`;
};

const getCurrentDaySpots = () => {
  const dayPlan = planData.value.dayPlans.find(
    (day) => day.day === selectedDay.value
  );
  return dayPlan ? dayPlan.details : [];
};

// --- 패널 크기 조절 관련 ---
const startDragExpand = (e) => {
  if (!dragHandle.value || (e.type === "mousedown" && e.button !== 0)) return;
  e.preventDefault();

  isDragging.value = true;
  startX.value = e.type === "mousedown" ? e.clientX : e.touches[0].clientX;
  initialPanelOffset.value = panelOffset.value;

  if (e.type === "mousedown") {
    document.addEventListener("mousemove", handleDragMove);
    document.addEventListener("mouseup", stopDragExpand);
  } else {
    document.addEventListener("touchmove", handleDragMove);
    document.addEventListener("touchend", stopDragExpand);
  }
};

const handleDragMove = (e) => {
  if (!isDragging.value) return;

  const clientX = e.type === "mousemove" ? e.clientX : e.touches[0].clientX;
  const delta = clientX - startX.value;

  if (animationFrame) cancelAnimationFrame(animationFrame);

  animationFrame = requestAnimationFrame(() => {
    const baseWidth = 464;
    const maxWidth = Math.min(window.innerWidth - 100, 1350);
    const targetWidth = baseWidth + delta;
    const smoothWidth = Math.max(baseWidth, Math.min(targetWidth, maxWidth));
    const smoothingFactor = 0.1;
    const smoothedWidth =
      currentWidth.value + (smoothWidth - currentWidth.value) * smoothingFactor;

    currentWidth.value = smoothedWidth;
    isExpanded.value = smoothedWidth > baseWidth;

    if (middleSection.value) {
      middleSection.value.style.width = `${smoothedWidth}px`;
    }
  });
};

const stopDragExpand = () => {
  isDragging.value = false;
  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragExpand);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragExpand);
  if (animationFrame) cancelAnimationFrame(animationFrame);
};

// --- 드래그 앤 드롭 관련 ---
const clearDragOverStyles = () => {
  const cards = document.querySelectorAll(".place-card");
  cards.forEach((card) => {
    card.classList.remove("drag-over-top", "drag-over-bottom");
  });
};

const dragStart = (event, place, index, fromDay) => {
  draggedItem.value = { place, type: "day", index, fromDay };
  event.dataTransfer.effectAllowed = "move";
  event.dataTransfer.setData(
    "text/plain",
    JSON.stringify({
      type: "day",
      index,
      fromDay,
    })
  );
};

const dragStartCart = (event, item, index) => {
  const normalizedItem = {
    ...item,
    attractionId: item.attractionId,
    title: item.title || item.attractionTitle,
    attractionTitle: item.title || item.attractionTitle,
    latitude: Number(item.latitude),
    longitude: Number(item.longitude),
  };

  draggedItem.value = { item: normalizedItem, type: "cart", index };
  event.dataTransfer.effectAllowed = "move";
  event.dataTransfer.setData(
    "text/plain",
    JSON.stringify({
      type: "cart",
      index,
    })
  );
};

const handleDrop = (event, targetDay, targetIndex) => {
  event.preventDefault();
  if (!draggedItem.value) return;

  const newPlan = JSON.parse(JSON.stringify(planData.value));
  const targetDayPlan = newPlan.dayPlans.find((plan) => plan.day === targetDay);
  if (!targetDayPlan) return;

  if (
    draggedItem.value.type === "day" &&
    draggedItem.value.fromDay === targetDay
  ) {
    const details = targetDayPlan.details;
    const [movedItem] = details.splice(draggedItem.value.index, 1);
    const adjustedTargetIndex =
      targetIndex > draggedItem.value.index ? targetIndex - 1 : targetIndex;
    details.splice(adjustedTargetIndex, 0, movedItem);
  } else {
    if (draggedItem.value.type === "cart") {
      const newPlace = {
        attractionId: draggedItem.value.item.attractionId,
        attractionTitle:
          draggedItem.value.item.title ||
          draggedItem.value.item.attractionTitle,
        image: draggedItem.value.item.image,
        latitude: draggedItem.value.item.latitude,
        longitude: draggedItem.value.item.longitude,
        addr1: draggedItem.value.item.addr1,
        memo: "",
      };

      if (typeof targetIndex === "number") {
        targetDayPlan.details.splice(targetIndex, 0, newPlace);
      } else {
        targetDayPlan.details.push(newPlace);
      }
    } else if (draggedItem.value.type === "day") {
      const sourceDayPlan = newPlan.dayPlans.find(
        (plan) => plan.day === draggedItem.value.fromDay
      );
      if (sourceDayPlan) {
        const [movedPlace] = sourceDayPlan.details.splice(
          draggedItem.value.index,
          1
        );
        if (typeof targetIndex === "number") {
          targetDayPlan.details.splice(targetIndex, 0, movedPlace);
        } else {
          targetDayPlan.details.push(movedPlace);
        }
      }
    }
  }

  aiRecommendStore.updatePlan(newPlan);
  draggedItem.value = null;
  clearDragOverStyles();
};

const handleDragOver = (event, targetDay, targetIndex) => {
  event.preventDefault();
  const dragCard = event.target.closest(".place-card");
  if (!dragCard) return;

  const cardRect = dragCard.getBoundingClientRect();
  const mouseY = event.clientY;
  const threshold = cardRect.top + cardRect.height * 0.5;
  const isAbove = mouseY < threshold;

  dragCard.classList.remove("drag-over-top", "drag-over-bottom");
  dragCard.classList.add(isAbove ? "drag-over-top" : "drag-over-bottom");
};

const onDrop = async (event, targetIndex) => {
  event.preventDefault();
  try {
    const data = event.dataTransfer.getData("text/plain");
    if (!data || !draggedItem.value) return;

    const parsed = JSON.parse(data);
    if (parsed.type === "cart" && draggedItem.value.type === "cart") {
      return;
    }
  } catch (error) {
    console.error("Drop handling error:", error);
  }
  draggedItem.value = null;
};

// --- 기타 액션 핸들러 ---
const selectDay = (day) => {
  selectedDay.value = day;
};

const removePlace = (day, index) => {
  const newPlan = JSON.parse(JSON.stringify(planData.value));
  const dayPlanIndex = day - 1;
  if (newPlan.dayPlans[dayPlanIndex]) {
    newPlan.dayPlans[dayPlanIndex].details.splice(index, 1);
    aiRecommendStore.updatePlan(newPlan);
  }
};

const saveModifiedPlan = () => {
  router.push(`/plan/${route.params.id}`);
};

const goToTravelCart = () => {
  alert("준비 중인 기능입니다.");
};

const cancelModify = () => {
  router.push(`/plan/${route.params.id}`);
};

// --- 생명주기 훅 ---
onMounted(async () => {
  await initializeData();
});

watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      await initializeData();
    }
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  if (animationFrame) cancelAnimationFrame(animationFrame);
  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragExpand);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragExpand);
});
</script>

<style scoped>
/* 전체 레이아웃 */
.travel-plan {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  position: relative;
}

/* 사이드바 */
.sidebar {
  width: 100px;
  height: 100%;
  background: white;
  display: flex;
  flex-direction: column;
  color: #b4b4b4;
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 14px;
}

.schedule-list {
  flex: 1;
}

/* 전체일정버튼, 일차버튼 */
.schedule-btn,
.edit-button-container {
  width: 100%;
  padding: 15px;
  text-align: center;
  transition: background 0.5s;
}

.schedule-btn:hover {
  background: #6e6156;
}

.schedule-btn.active {
  color: #ecb27b;
}

/* 편집버튼 */
.edit-button {
  width: 100%;
  padding: 0.5rem;
  background: #ecb27b;
  color: white;
  border-radius: 0.9rem;
  height: 50px;
  margin-bottom: 10px;
}

.edit-button:hover {
  background: #6e6156;
  transition: background 0.5s;
}

.secondary-button {
  width: 100%;
  padding: 0.5rem;
  background: #f3f4f6;
  color: #374151;
  border-radius: 0.375rem;
  margin-bottom: 0.5rem;
}

/* 장바구니 섹션 */
.cart-section {
  width: 464px;
  height: calc(100vh - 90px);
  background: white;
  padding: 0 24px 24px 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.section-title {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 30px;
}

.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
.place-image {
  margin-right: 1rem; /* 이미지와 텍스트 사이 간격 */
  width: 145px;
  height: 6rem;
  object-fit: cover;
  border-radius: 0.5rem;
}

.place-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
/* 카드 여행지 이름, 설명 */
.place-info h3 {
  font-family: "Pretendard-Medium";
  font-weight: bold;
  font-size: 1.125rem;
}

.place-info p {
  color: #b4b4b4;
  font-size: 13px;
}

.clear-cart {
  font-family: "EliceDigitalBaeum_regular";
  color: #ff6365;
  cursor: pointer;
  padding: 15px 10px 5px 0px;
}

.clear-cart:hover {
  color: #f44336;
}

.cart-items {
  max-height: 76vh;
  height: 60vh;
  overflow-y: auto;
}

/*카드 스타일 cart-item */
.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 7px;
  margin-bottom: 1rem;
  background: white;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
  border-radius: 0.5rem;
  cursor: grab;
  width: 400px;
}

.spot-card:hover,
.cart-item:hover {
  transform: translateY(-2px);
}

/* 숫자 스타일 */

.order-number {
  font-family: "Pretendard-SemiBold";
  display: flex;
  align-items: center;
  color: #6e6156;
  font-size: 1.125rem;
}

/* 정보 영역 공통 스타일 */

/* 장바구니 저장 버튼 */
.save-cart {
  width: 100%;
  padding: 1rem;
  background: #ecb27b;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.save-cart:hover {
  background: #6e6156;
}

.save-cart:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.collapsed {
  width: 0;
  padding: 0;
  overflow: hidden;
}

/* 콘텐츠 영역 */
.content-area {
  flex: 1;
  position: relative;
}

.map-container {
  position: absolute;
  inset: 0;
}

/* 일정 패널 */
.schedule-panel {
  position: absolute;
  left: 0;
  height: 88.5vh;
  background: white;
  z-index: 10;
  transition: width 0.1s ease-out;
  overflow: hidden;
  will-change: width;
  transform: translateZ(0);
}

.panel-content {
  height: 100%;
  overflow: hidden;
  backface-visibility: hidden;
}

.schedule-detail {
  padding: 0 24px 24px 24px;
  height: 100%;
  overflow-y: auto;
}

.all-schedules {
  padding-bottom: 1rem;
}

.days-grid {
  display: flex;
  gap: 2.2rem;
  width: max-content;
}

.day-container {
  margin-bottom: 1.5rem;
}

.spots-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 패널 안 여행 제목과 기간 */
.trip-header {
  font-family: "EliceDigitalBaeum_Bold";
}

.trip-header h1 {
  font-size: 32px;
}

.trip-header p {
  margin-bottom: 30px;
  color: #ecb27b;
}

/* 일정 헤더 */
.day-container h2 {
  margin-bottom: 1.5rem;
}

.day-container .day {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 20px;
  margin-right: 5px;
}

.day-container .date {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 12px;
}

/* 드래그 핸들 */
.drag-handle {
  position: absolute; /* 절대 위치 */
  right: 0; /* 오른쪽 정렬 */
  top: 50%; /* 세로 중앙 정렬 */
  transform: translateY(-50%); /* 세로 중앙 정렬 */
  width: 1.5rem;
  height: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  z-index: 30;
}

.drag-handle:active {
  cursor: grabbing;
}

/* 삭제 버튼과 닫기 버튼 */
.delete-button {
  color: #b4b4b4;
  padding: 0.5rem;
  transition: color 0.2s;
}

.delete-button:hover {
  color: #b4b4b4;
}

/* 스크롤바 스타일 */
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
</style>
