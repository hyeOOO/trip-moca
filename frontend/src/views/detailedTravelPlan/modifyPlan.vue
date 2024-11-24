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
            :latitude="33.3846"
            :longitude="126.5534"
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
                <h1>{{ planData.planTitle }}</h1>
                <!-- 여기 여행계획 이름으로 받아와야해-->
                <p>
                  {{ formatDateRange(planData.startDate, planData.endDate) }}
                </p>
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
                        :key="spot.planDetailId"
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
                          <p>{{ spot.memo }}</p>
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
                    :key="spot.planDetailId"
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
                      <p>{{ spot.memo }}</p>
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from "vue";
import { useRouter } from "vue-router";
import { usePlanStore } from "@/store/editPlanStore";
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";

const router = useRouter();
const planStore = usePlanStore();

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
let animationFrame = null;

// --- Computed Properties ---
const planData = computed(() => planStore.getPlanData);

const gridStyle = computed(() => ({
  display: "flex",
  gap: "2.2rem",
  width: "max-content",
}));

// 총 일수 계산
const totalDays = computed(() => {
  if (!planData.value || !planData.value.dayPlans) return 0;
  return planData.value.dayPlans.length;
});

// 지도에 표시할 장소들
const getSelectedPlaces = computed(() => {
  if (!planData.value || !planData.value.dayPlans) return {};

  if (selectedDay.value === "all") {
    return planData.value.dayPlans.reduce((acc, day) => {
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
    (day) => day.day === selectedDay.value
  );
  return dayPlan
    ? {
        [selectedDay.value]: dayPlan.details.map((spot) => ({
          id: spot.attractionId,
          title: spot.attractionTitle,
          latitude: Number(spot.latitude),
          longitude: Number(spot.longitude),
        })),
      }
    : {};
});

// 장바구니 아이템
const cartItems = computed(() => {
  return planStore.getCartItems.map((item) => ({
    ...item,
    image: item.image1 || item.image || item.firstimage || "",
    title: item.title || item.attractionTitle,
    addr1: item.addr1 || item.address,
  }));
});

// --- 유틸리티 함수들 ---
// 현재 날짜의 장소들 가져오기
const getCurrentDaySpots = () => {
  const dayPlan = planData.value.dayPlans.find(
    (day) => day.day === selectedDay.value
  );
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

// 날짜 포맷팅
const formatDateRange = (start, end) => {
  if (!start || !end || isLoading.value) return "";
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
  if (!planData.value?.startDate || isLoading.value) return "";
  const start = new Date(planData.value.startDate);
  start.setDate(start.getDate() + day - 1);
  return formatDate(start);
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

  // 이전 애니메이션 프레임 취소
  if (animationFrame) cancelAnimationFrame(animationFrame);

  // 새로운 애니메이션 프레임 요청
  animationFrame = requestAnimationFrame(() => {
    // 베이스 너비와 최대 너비 계산
    const baseWidth = 464;
    const maxWidth = Math.min(window.innerWidth - 100, 1350);

    // 새로운 너비 계산 (부드러운 제한)
    const targetWidth = baseWidth + delta;
    const smoothWidth = Math.max(baseWidth, Math.min(targetWidth, maxWidth));

    // 변화량에 따른 부드러운 전환
    const smoothingFactor = 0.1;
    const smoothedWidth =
      currentWidth.value + (smoothWidth - currentWidth.value) * smoothingFactor;

    // 상태 업데이트
    currentWidth.value = smoothedWidth;
    isExpanded.value = smoothedWidth > baseWidth;

    // 패널 요소에 transform 적용
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

// 드래그 시작 - 일정에서
const dragStart = (event, place, index, fromDay) => {
  draggedItem.value = { place, type: "day", index, fromDay };
  event.dataTransfer.effectAllowed = "move";
  // 드래그 데이터 설정 추가
  event.dataTransfer.setData('text/plain', JSON.stringify({
    type: 'day',
    index,
    fromDay
  }));
};

// 드래그 시작 - 장바구니에서
const dragStartCart = (event, item, index) => {
  // 드래그 아이템 데이터 정규화
  const normalizedItem = {
    ...item,
    attractionId: item.attractionId || item.id,
    title: item.title || item.attractionTitle,
    attractionTitle: item.title || item.attractionTitle,
    latitude: Number(item.latitude || item.mapy),
    longitude: Number(item.longitude || item.mapx),
  };

  draggedItem.value = { item: normalizedItem, type: "cart", index };
  event.dataTransfer.effectAllowed = "move";
  // 드래그 데이터 설정 추가
  event.dataTransfer.setData('text/plain', JSON.stringify({
    type: 'cart',
    index
  }));
};

// 드롭 처리
const handleDrop = (event, targetDay, targetIndex) => {
  event.preventDefault();
  if (!draggedItem.value) return;

  const newPlan = JSON.parse(JSON.stringify(planData.value));
  const targetDayPlan = newPlan.dayPlans.find((plan) => plan.day === targetDay);
  if (!targetDayPlan) return;

  // 같은 날짜 내에서의 이동인 경우
  if (draggedItem.value.type === "day" && draggedItem.value.fromDay === targetDay) {
    const details = targetDayPlan.details;
    const [movedItem] = details.splice(draggedItem.value.index, 1);
    
    // targetIndex가 원래 인덱스보다 크면 하나 감소 (항목이 제거되었으므로)
    const adjustedTargetIndex = targetIndex > draggedItem.value.index ? targetIndex - 1 : targetIndex;
    details.splice(adjustedTargetIndex, 0, movedItem);
  }
  // 다른 날짜로의 이동이거나 장바구니에서의 추가
  else {
    if (draggedItem.value.type === "cart") {
      const newPlace = {
        attractionId: draggedItem.value.item.attractionId,
        attractionTitle: draggedItem.value.item.title || draggedItem.value.item.attractionTitle,
        image: draggedItem.value.item.image || draggedItem.value.item.image1 || draggedItem.value.item.firstimage,
        latitude: Number(draggedItem.value.item.latitude || draggedItem.value.item.mapy),
        longitude: Number(draggedItem.value.item.longitude || draggedItem.value.item.mapx),
        addr1: draggedItem.value.item.addr1 || draggedItem.value.item.address,
        memo: "",
      };

      if (typeof targetIndex === 'number') {
        targetDayPlan.details.splice(targetIndex, 0, newPlace);
      } else {
        targetDayPlan.details.push(newPlace);
      }
    } else if (draggedItem.value.type === "day") {
      const sourceDayPlan = newPlan.dayPlans.find(
        (plan) => plan.day === draggedItem.value.fromDay
      );
      if (sourceDayPlan) {
        const [movedPlace] = sourceDayPlan.details.splice(draggedItem.value.index, 1);
        if (typeof targetIndex === 'number') {
          targetDayPlan.details.splice(targetIndex, 0, movedPlace);
        } else {
          targetDayPlan.details.push(movedPlace);
        }
      }
    }
  }

  planStore.updateEditingPlan(newPlan);
  draggedItem.value = null;
  clearDragOverStyles();
};

// 드래그 오버 처리
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

const removeFromCart = (item) => {
  cartItems.value = cartItems.value.filter(
    (p) => p.attractionId !== item.attractionId
  );
};

const onDrop = async (event, targetIndex) => {
  event.preventDefault();
  
  try {
    const data = event.dataTransfer.getData('text/plain');
    if (!data || !draggedItem.value) return;

    const parsed = JSON.parse(data);
    
    // 장바구니 내에서의 순서 변경은 처리하지 않음
    if (parsed.type === 'cart' && draggedItem.value.type === 'cart') {
      return;
    }

  } catch (error) {
    console.error('Drop handling error:', error);
  }
  draggedItem.value = null;
};

// --- 기타 액션 핸들러 ---
const selectDay = (day) => {
  selectedDay.value = day;
};

const removePlace = (day, index) => {
  const newPlan = JSON.parse(JSON.stringify(planData.value));
  newPlan.dayPlans[day - 1].details.splice(index, 1);
  planStore.updateEditingPlan(newPlan);
};

const saveModifiedPlan = () => {
  planStore.savePlan();
  router.push(`/plan/${planData.value.planId}`);
};

const goToTravelCart = () => {
  router.push(`/TravelCart/${planData.value.planId}`);
};

const cancelModify = () => {
  planStore.setCartItems(cartItems.value);
  router.push(`/plan/${planData.value.planId}`);
};

// --- 생명주기 훅 ---
onMounted(async () => {
  try {
    isLoading.value = true;
    await planStore.initializePlan();
  } catch (error) {
    console.error("여행 계획 데이터 로드 실패:", error);
  } finally {
    isLoading.value = false;
  }
});

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
  background: #6e6156;
}
</style>