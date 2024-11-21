<template>
  <div class="detailed-travel-plan h-screen flex flex-col">
    <nav-bar />
    <div class="main-content flex-1 flex relative">
      <!-- 왼쪽 사이드바 -->
      <div class="w-48 bg-white border-r border-gray-200 flex flex-col h-full">
        <div class="flex-1 overflow-y-auto divide-y divide-gray-200">
          <!-- 전체 일정 보기 버튼 -->
          <button
            class="w-full p-4 text-left hover:bg-gray-100 font-medium"
            :class="{ 'bg-blue-50 text-blue-600': selectedDay === 'all' }"
            @click="selectDay('all')"
          >
            전체일정
          </button>

          <!-- 일자별 버튼 목록 -->
          <div v-for="day in totalDays" :key="day" class="day-item">
            <button
              class="w-full p-4 text-left hover:bg-gray-100"
              :class="{ 'bg-blue-50 text-blue-600': selectedDay === day }"
              @click="selectDay(day)"
            >
              <div class="font-medium">DAY {{ day }}</div>
              <div class="text-sm text-gray-500">{{ getDayDate(day) }}</div>
            </button>
          </div>
        </div>

        <div
          class="sticky bottom-0 bg-white p-4 border-t border-gray-200 space-y-2"
        >
          <button
            @click="saveModifiedPlan"
            class="w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition"
          >
            저장
          </button>
          <button
            @click="goToTravelCart"
            class="w-full p-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition"
          >
            추가로 담기
          </button>
          <button
            @click="cancelModify"
            class="w-full p-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 transition"
          >
            취소
          </button>
        </div>
      </div>

      <!-- 장바구니 영역 -->
      <div class="right-section">
        <div class="p-4 border-b border-gray-200">
          <h2 class="text-lg font-bold">장바구니</h2>
        </div>
        <div class="cart-items">
          <div
            v-for="(item, index) in cartItems"
            :key="item.attractionId"
            class="cart-item"
            draggable="true"
            @dragstart="dragStartCart($event, item, index)"
          >
            <div class="place-image">
              <img
                :src="getImageUrl(item.image)"
                :alt="item.title"
                class="cart-image"
                @error="handleImageError"
              />
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.addr1 }}</p>
          </div>
        </div>
      </div>

      <!-- 메인 콘텐츠 영역 -->
      <div class="flex-1 relative">
        <div class="absolute inset-0">
          <tmap-multipath
            :selected-places-by-day="getSelectedPlaces"
            :latitude="33.3846"
            :longitude="126.5534"
            :selected-day="selectedDay === 'all' ? null : selectedDay"
            :show-all-days="selectedDay === 'all'"
          />
        </div>

        <!-- 드래그 가능한 일정 상세 패널 -->
        <div
          ref="middleSection"
          class="middle-section absolute left-0 h-full bg-white shadow-lg transition-width overflow-hidden"
          :style="{
            width: `${currentWidth}px`,
            transform: `translateX(${panelOffset}px)`,
          }"
        >
          <div class="content-wrapper h-full">
            <div class="p-6 h-full overflow-y-auto">
              <!-- 여행 제목 및 날짜 정보 -->
              <div class="mb-6">
                <h1 class="text-2xl font-bold mb-2">
                  {{ planData.planTitle }}
                </h1>
                <p class="text-gray-600">
                  {{ formatDateRange(planData.startDate, planData.endDate) }}
                </p>
              </div>

              <!-- 선택된 날짜의 장소 목록 -->
              <div>
                <!-- 전체 일정 뷰 -->
                <template v-if="selectedDay === 'all'">
                  <div
                    class="flex overflow-x-auto gap-6 pb-4 snap-x snap-mandatory"
                  >
                    <div
                      v-for="dayPlan in planData.dayPlans"
                      :key="dayPlan.day"
                      class="flex-none w-[calc(100%-2rem)] min-w-[300px] max-w-[400px] bg-gray-50 rounded-lg p-6 snap-start"
                      @dragover.prevent
                      @drop="handleDrop($event, dayPlan.day)"
                    >
                      <h2
                        class="text-xl font-bold mb-4 sticky top-0 bg-gray-50 py-2 z-10"
                      >
                        {{ dayPlan.day }}일차 {{ getDayDate(dayPlan.day) }}
                      </h2>
                      <div class="space-y-4">
                        <div
                          v-for="(place, index) in dayPlan.details"
                          :key="place.attractionId"
                          class="place-card bg-white rounded-lg shadow p-4"
                          draggable="true"
                          @dragstart="
                            dragStart($event, place, index, dayPlan.day)
                          "
                          @dragover.prevent="
                            handleDragOver($event, dayPlan.day, index)
                          "
                          @drop="handleDrop($event, dayPlan.day, index)"
                        >
                          <div class="flex gap-4">
                            <div
                              class="place-number font-bold text-blue-500 text-lg"
                            >
                              {{ index + 1 }}
                            </div>
                            <div class="flex-1">
                              <img
                                :src="getImageUrl(place.image)"
                                :alt="place.title || place.attractionTitle"
                                @error="handleImageError"
                              />
                              <h3 class="font-bold text-lg mb-2">
                                {{ place.title || place.attractionTitle }}
                              </h3>
                              <p class="text-gray-600">{{ place.memo }}</p>
                              <div class="flex justify-end mt-2">
                                <button
                                  @click="removePlace(dayPlan.day, index)"
                                  class="text-red-500 hover:text-red-700"
                                >
                                  <i class="fas fa-trash"></i>
                                </button>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

                <!-- 개별 일정 뷰 -->
                <template v-else>
                  <div
                    v-for="(place, index) in getDayPlaces(selectedDay)"
                    :key="place.attractionId"
                    class="place-card bg-white rounded-lg shadow p-4 mb-4"
                    draggable="true"
                    @dragstart="dragStart($event, place, index, selectedDay)"
                  >
                    <div class="flex gap-4">
                      <div class="place-number font-bold text-blue-500 text-lg">
                        {{ index + 1 }}
                      </div>
                      <div class="flex-1">
                        <img
                          :src="getImageUrl(place.image)"
                          :alt="place.title || place.attractionTitle"
                          @error="handleImageError"
                        />
                        <h3 class="font-bold text-lg mb-2">
                          {{ place.title || place.attractionTitle }}
                        </h3>
                        <p class="text-gray-600">{{ place.memo }}</p>
                        <div class="flex justify-end mt-2">
                          <button
                            @click="removePlace(selectedDay, index)"
                            class="text-red-500 hover:text-red-700"
                          >
                            <i class="fas fa-trash"></i>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <!-- 드래그 핸들 -->
          <div
            ref="dragHandle"
            class="drag-handle absolute right-0 top-1/2 -translate-y-1/2 w-6 h-12 bg-transparent flex items-center justify-center hover:bg-transparent transition-colors z-20"
            @mousedown="startDragResize"
            @touchstart="startDragResize"
          >
            <i class="fas fa-grip-lines-vertical"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { usePlanStore } from "@/store/planStore";
import navBar from "@/components/navBar.vue";

const router = useRouter();
const planStore = usePlanStore();

// --- 상태 관리 변수들 ---
const selectedDay = ref("all"); // 현재 선택된 일자
const middleSection = ref(null); // 중앙 섹션 참조
const dragHandle = ref(null); // 드래그 핸들 참조
const isDragging = ref(false); // 드래그 상태
const startX = ref(0); // 드래그 시작 X 좌표
const startWidth = ref(400); // 시작 너비
const currentWidth = ref(400); // 현재 너비
const panelOffset = ref(0); // 패널 오프셋
const draggedItem = ref(null); // 현재 드래그 중인 아이템

// --- Computed Properties ---
// Store에서 계획 데이터 가져오기
const planData = computed(() => planStore.getPlanData);

// 장바구니 아이템 데이터 정규화
const cartItems = computed(() => {
  return planStore.getCartItems.map((item) => ({
    ...item,
    image: item.image1 || item.image || item.firstimage || "",
    title: item.title || item.attractionTitle,
    addr1: item.addr1 || item.address,
  }));
});

// 총 일수 계산
const totalDays = computed(() => {
  if (!planData.value || !planData.value.dayPlans) return 0;
  return planData.value.dayPlans.length;
});

// 선택된 장소들 정보 가져오기
const getSelectedPlaces = computed(() => {
  if (selectedDay.value === "all") {
    return planData.value.dayPlans.reduce((acc, dayPlan) => {
      acc[dayPlan.day] = dayPlan.details.map((place) => ({
        id: place.attractionId,
        title: place.title || place.attractionTitle,
        latitude: place.latitude || place.mapy,
        longitude: place.longitude || place.mapx,
      }));
      return acc;
    }, {});
  }

  const dayPlaces = getDayPlaces(selectedDay.value);
  if (!Array.isArray(dayPlaces)) return {};

  return {
    [selectedDay.value]: dayPlaces.map((place) => ({
      id: place.attractionId,
      title: place.title || place.attractionTitle,
      latitude: place.latitude || place.mapy,
      longitude: place.longitude || place.mapx,
    })),
  };
});

// --- 유틸리티 함수들 ---
// 특정 일자의 장소 목록 가져오기
const getDayPlaces = (day) => {
  if (!planData.value || !planData.value.dayPlans) return [];
  if (day === "all") return planData.value.dayPlans;
  const dayPlan = planData.value.dayPlans.find((plan) => plan.day === day);
  return dayPlan ? dayPlan.details : [];
};

// 이미지 URL 처리
const getImageUrl = (imageUrl) => {
  if (!imageUrl)
    return "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png";
  if (typeof imageUrl === "object") {
    return (
      imageUrl.image1 ||
      imageUrl.firstimage ||
      "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png"
    );
  }
  return imageUrl;
};

// 날짜 포맷팅 함수들
const formatDateRange = (start, end) => {
  if (!start || !end) return "";
  return `${formatDate(new Date(start))} ~ ${formatDate(new Date(end))}`;
};

const formatDate = (date) => {
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}.${String(date.getDate()).padStart(2, "0")}(${days[date.getDay()]})`;
};

const getDayDate = (day) => {
  if (!planData.value?.startDate) return "";
  const start = new Date(planData.value.startDate);
  start.setDate(start.getDate() + day - 1);
  return formatDate(start);
};

// --- 드래그 리사이즈 관련 함수들 ---
// 드래그 리사이즈 시작
const startDragResize = (e) => {
  e.preventDefault();
  isDragging.value = true;
  startX.value = e.type === "mousedown" ? e.clientX : e.touches[0].clientX;
  startWidth.value = currentWidth.value;

  if (e.type === "mousedown") {
    document.addEventListener("mousemove", handleDragMove);
    document.addEventListener("mouseup", stopDragResize);
  } else {
    document.addEventListener("touchmove", handleDragMove);
    document.addEventListener("touchend", stopDragResize);
  }
};
// 드래그 오버 처리
const handleDragOver = (event, targetDay, targetIndex) => {
  event.preventDefault();
  const dragCard = event.target.closest(".place-card");
  if (!dragCard) return;

  const cards = Array.from(dragCard.parentNode.children);
  const cardRect = dragCard.getBoundingClientRect();
  const midPoint = cardRect.top + cardRect.height / 2;

  // 마우스 위치에 따라 위/아래 드롭 위치 표시
  if (event.clientY < midPoint) {
    dragCard.classList.add("drag-over-top");
    dragCard.classList.remove("drag-over-bottom");
  } else {
    dragCard.classList.add("drag-over-bottom");
    dragCard.classList.remove("drag-over-top");
  }
};

// 드래그 중 처리
const handleDragMove = (e) => {
  if (!isDragging.value) return;
  const clientX = e.type === "mousemove" ? e.clientX : e.touches[0].clientX;
  const delta = clientX - startX.value;
  const newWidth = Math.max(
    300,
    Math.min(startWidth.value + delta, window.innerWidth - 192)
  );

  requestAnimationFrame(() => {
    currentWidth.value = newWidth;
  });
};

// 드래그 종료
const stopDragResize = () => {
  isDragging.value = false;
  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragResize);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragResize);
};

// --- 드래그 앤 드롭 관련 함수들 ---
// 드롭 처리
const handleDrop = (event, targetDay) => {
  event.preventDefault();
  if (!draggedItem.value) return;

  const newPlan = JSON.parse(JSON.stringify(planData.value));
  const targetDayPlan = newPlan.dayPlans.find((plan) => plan.day === targetDay);

  if (!targetDayPlan) return;

  if (draggedItem.value.type === "cart") {
    const newPlace = {
      attractionId: draggedItem.value.item.attractionId,
      title: draggedItem.value.item.title,
      attractionTitle: draggedItem.value.item.title,
      image: draggedItem.value.item.image,
      latitude: draggedItem.value.item.latitude || draggedItem.value.item.mapy,
      longitude:
        draggedItem.value.item.longitude || draggedItem.value.item.mapx,
      addr1: draggedItem.value.item.addr1,
      memo: "",
    };
    targetDayPlan.details.push(newPlace);
  } else if (draggedItem.value.type === "day") {
    const sourceDayPlan = newPlan.dayPlans.find(
      (plan) => plan.day === draggedItem.value.fromDay
    );
    if (sourceDayPlan) {
      const [movedPlace] = sourceDayPlan.details.splice(
        draggedItem.value.index,
        1
      );
      targetDayPlan.details.push(movedPlace);
    }
  }

  planStore.updateEditingPlan(newPlan);
  draggedItem.value = null;
};

// 드래그 종료 시 스타일 정리
const dragEnd = (event) => {
  clearDragOverStyles();
  draggedItem.value = null;
};

// 드래그 시작 - 일정에서
const dragStart = (event, place, index, fromDay) => {
  draggedItem.value = { place, type: "day", index, fromDay };
  event.dataTransfer.effectAllowed = "move";
};

// 드래그 시작 - 장바구니에서
const dragStartCart = (event, item, index) => {
  draggedItem.value = { item, type: "cart", index };
  event.dataTransfer.effectAllowed = "move";
};

// --- 기타 액션 핸들러 ---
// 일자 선택
const selectDay = (day) => {
  selectedDay.value = day;
};

// 장소 제거
const removePlace = (day, index) => {
  const newPlan = JSON.parse(JSON.stringify(planData.value));
  newPlan.dayPlans[day - 1].details.splice(index, 1);
  planStore.updateEditingPlan(newPlan);
};

// 수정된 계획 저장
const saveModifiedPlan = () => {
  planStore.savePlan();
  router.push(`/plan/${planData.value.planId}`);
};

// 장바구니로 이동
const goToTravelCart = () => {
  router.push(`/TravelCart/${planData.value.planId}`);
};

// 수정 취소
const cancelModify = () => {
  planStore.setCartItems(cartItems.value);
  router.push(`/plan/${planData.value.planId}`);
};

// --- 라이프사이클 훅 ---
onMounted(() => {
  const id = router.currentRoute.value.params.id;
  planStore.initializePlan();
});

onBeforeUnmount(() => {
  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragResize);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragResize);
});
</script>

<style scoped>
.cart-image-container {
  width: 100%;
  height: 160px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 8px;
}

.cart-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.place-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.place-image {
  width: 80px;
  height: 80px;
  margin-right: 12px;
}

.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.place-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

/* 기본 레이아웃 스타일 */
.detailed-travel-plan {
  height: 100vh;
  overflow: hidden;
}

/* 중앙 섹션 스타일 */
.middle-section {
  width: 400px;
  max-width: calc(100vw - 192px);
  background-color: #fff;
  z-index: 10;
  will-change: width;
  touch-action: none;
  transition: width 0.1s ease;
  height: calc(100vh - 64px);
}

/* 컨텐츠 래퍼 스타일 */
.content-wrapper {
  position: relative;
  height: 100%;
  overflow: hidden;
  user-select: none;
  background-color: rgba(255, 255, 255, 0.95);
}

/* 드래그 핸들 스타일 */
.drag-handle {
  opacity: 0.8;
  transition: opacity 0.2s ease;
  touch-action: none;
  z-index: 30;
  cursor: ew-resize;
}

.drag-handle:hover {
  opacity: 1;
}

/* 드래그 앤 드롭 관련 스타일 */
.cart-item,
.place-card {
  cursor: grab;
  transition: transform 0.2s ease;
}

.cart-item:active,
.place-card:active {
  cursor: grabbing;
}

.cart-item:hover,
.place-card:hover {
  transform: translateY(-2px);
}

/* 드롭 영역 스타일 */
.flex-none {
  transition: background-color 0.2s ease;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.flex-none.drag-over {
  background-color: rgba(59, 130, 246, 0.1);
}

/* 스크롤바 스타일 */
::-webkit-scrollbar {
  height: 6px;
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 가로 스크롤 컨테이너 */
.flex.overflow-x-auto {
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
}

/* 반응형 스타일 */
@media (max-width: 1024px) {
  .middle-section {
    width: 350px;
  }
}

.cart-item {
  display: grid;
  grid-template-columns: 30px 80px 1fr;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.right-section {
  width: 380px;
  height: 100%;
  background-color: white;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
  padding: 20px;
  display: flex;
  flex-direction: column;
  position: relative;
}
.cart-items {
  flex: 1;
  overflow-y: auto;
  height: calc(100vh - 250px); /* nav-bar + header + button 영역 높이를 뺀 값 */
}
</style>