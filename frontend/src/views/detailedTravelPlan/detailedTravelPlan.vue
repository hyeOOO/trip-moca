<template>
  <!-- 전체 페이지 레이아웃 컨테이너 -->
  <div class="detailed-travel-plan h-screen flex flex-col">
    <nav-bar />
    <!-- 메인 콘텐츠 영역 -->
    <div class="main-content flex-1 flex relative">
      <!-- 왼쪽 사이드바: 일정 목록 및 버튼 -->
      <div class="w-48 bg-white border-r border-gray-200 flex flex-col h-full">
        <!-- 일정 목록 스크롤 영역 -->
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

        <!-- 하단 고정 버튼 영역 -->
        <div class="sticky bottom-0 bg-white p-4 border-t border-gray-200">
          <button
            class="w-full mb-2 p-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition"
            @click="goToModifyPlan"
          >
            편집
          </button>
        </div>
      </div>

      <!-- 메인 콘텐츠 영역: 지도와 일정 상세 정보 -->
      <div class="flex-1 relative">
        <!-- 지도 표시 영역 -->
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
          :class="{ expanded: isExpanded }"
          :style="{ width: `${currentWidth}px` }"
        >
          <!-- 패널 내용 -->
          <div class="content-wrapper h-full">
            <div class="p-6 h-full overflow-y-auto">
              <!-- 여행 제목 및 날짜 정보 -->
              <div class="mb-6" v-if="!isLoading">
                <h1 class="text-2xl font-bold mb-2">
                  {{ planData.planTitle }}
                </h1>
                <p class="text-gray-600">
                  {{ formatDateRange(planData.startDate, planData.endDate) }}
                </p>
              </div>

              <!-- 전체 일정 뷰 -->
              <div
                v-if="selectedDay === 'all'"
                class="days-grid-container overflow-x-auto"
              >
                <!-- 가로 스크롤 가능한 일정 그리드 -->
                <div class="days-grid" :style="gridStyle">
                  <!-- 일자별 컨테이너 -->
                  <div
                    v-for="day in planData.dayPlans"
                    :key="day.day"
                    class="day-container min-w-[380px]"
                  >
                    <h2 class="text-xl font-bold mb-4">
                      {{ day.day }}일차 {{ formatDate(new Date(day.date)) }}
                    </h2>
                    <!-- 장소 카드 목록 -->
                    <div class="space-y-4 overflow-y-auto max-h-[calc(100vh-300px)]">
                      <div
                        v-for="(spot, index) in day.details"
                        :key="spot.planDetailId"
                        class="spot-card bg-white rounded-lg shadow p-4"
                      >
                        <div class="flex gap-4">
                          <div class="spot-number font-bold text-blue-500 text-lg">
                            {{ index + 1 }}
                          </div>
                          <div class="flex-1">
                            <img
                              :src="spot.image"
                              :alt="spot.attractionTitle"
                              class="w-full h-48 object-cover rounded mb-3"
                            />
                            <h3 class="font-bold text-lg mb-2">
                              {{ spot.attractionTitle }}
                            </h3>
                            <p class="text-gray-600">{{ spot.memo }}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 개별 일정 뷰 -->
              <div v-else class="space-y-4 h-[calc(100%-120px)] overflow-y-auto">
                <!-- 선택된 날짜의 장소 카드 목록 -->
                <div
                  v-for="(spot, index) in getCurrentDaySpots()"
                  :key="spot.planDetailId"
                  class="spot-card bg-white rounded-lg shadow p-4"
                >
                  <div class="flex gap-4">
                    <div class="spot-number font-bold text-blue-500 text-lg">
                      {{ index + 1 }}
                    </div>
                    <div class="flex-1">
                      <img
                        :src="spot.image"
                        :alt="spot.attractionTitle"
                        class="w-full h-48 object-cover rounded mb-3"
                      />
                      <h3 class="font-bold text-lg mb-2">
                        {{ spot.attractionTitle }}
                      </h3>
                      <p class="text-gray-600">{{ spot.memo }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 패널 크기 조절을 위한 드래그 핸들 -->
          <div
            ref="dragHandle"
            class="drag-handle absolute right-0 top-1/2 -translate-y-1/2 w-6 h-12 bg-transparent flex items-center justify-center hover:bg-transparent transition-colors z-20"
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
// 필요한 Vue 컴포넌트와 기능들을 임포트
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { usePlanStore } from '@/store/planStore';
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";

// 라우터와 상태관리 스토어 초기화
const router = useRouter();
const planStore = usePlanStore();

// 상태 변수들 정의
const selectedDay = ref("all"); // 선택된 일자
const isExpanded = ref(false); // 패널 확장 상태
const middleSection = ref(null); // 중간 섹션 참조
const dragHandle = ref(null); // 드래그 핸들 참조
const isLoading = ref(true); // 로딩 상태

// 패널 드래그 관련 상태
const panelOffset = ref(0); // 패널 오프셋
const isDragging = ref(false); // 드래그 중 여부
const startX = ref(0); // 드래그 시작 X 좌표
const currentX = ref(0); // 현재 X 좌표
const initialPanelOffset = ref(0); // 초기 패널 오프셋

// 현재 패널 너비 계산
const currentWidth = computed(() => {
  const baseWidth = 400;
  const maxWidth = Math.min(window.innerWidth - 192, 1200);
  return Math.min(baseWidth + panelOffset.value, maxWidth);
});

// 그리드 스타일 계산
const gridStyle = computed(() => ({
  display: 'flex',
  gap: '1.5rem',
  width: 'max-content'
}));

// 여행 계획 데이터
const planData = computed(() => planStore.getPlanData);

let animationFrame = null;

// 총 일수 계산
const totalDays = computed(() => planData.value?.dayPlans?.length || 0);

// 선택된 장소들 데이터 가공
const getSelectedPlaces = computed(() => {
  if (selectedDay.value === "all") {
    return planData.value.dayPlans.reduce((acc, day) => {
      acc[day.day] = day.details.map((spot) => ({
        id: spot.attractionId,
        title: spot.attractionTitle,
        latitude: spot.latitude,
        longitude: spot.longitude,
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
          latitude: spot.latitude,
          longitude: spot.longitude,
        })),
      }
    : {};
});

// 드래그 시작 핸들러
const startDragExpand = (e) => {
  if (!dragHandle.value) return;
  if (e.type === "mousedown" && e.button !== 0) return;
  e.preventDefault();

  isDragging.value = true;
  startX.value = e.type === "mousedown" ? e.clientX : e.touches[0].clientX;
  initialPanelOffset.value = panelOffset.value;

  if (dragHandle.value) {
    dragHandle.value.style.cursor = "grabbing";
  }

  if (e.type === "mousedown") {
    document.addEventListener("mousemove", handleDragMove);
    document.addEventListener("mouseup", stopDragExpand);
  } else {
    document.addEventListener("touchmove", handleDragMove);
    document.addEventListener("touchend", stopDragExpand);
  }
};

// 드래그 중 핸들러
const handleDragMove = (e) => {
  if (!isDragging.value) return;

  const clientX = e.type === "mousemove" ? e.clientX : e.touches[0].clientX;
  const delta = clientX - startX.value;

  if (animationFrame) {
    cancelAnimationFrame(animationFrame);
  }

  animationFrame = requestAnimationFrame(() => {
    const newOffset = initialPanelOffset.value + delta;
    const maxOffset = Math.min(window.innerWidth - 192, 1200) - 400;
    panelOffset.value = Math.max(Math.min(newOffset, maxOffset), 0);
    isExpanded.value = panelOffset.value > 100;
  });
};

// 드래그 종료 핸들러
const stopDragExpand = () => {
  isDragging.value = false;
  if (dragHandle.value) {
    dragHandle.value.style.cursor = "grab";
  }

  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragExpand);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragExpand);

  if (animationFrame) {
    cancelAnimationFrame(animationFrame);
  }
};

// 여행 계획 데이터 가져오기
const fetchPlanData = async () => {
  try {
    isLoading.value = true;
    await new Promise((resolve) => setTimeout(resolve, 500));
    planStore.initializePlan();
  } catch (error) {
    console.error("Failed to fetch plan data:", error);
  } finally {
    isLoading.value = false;
  }
};

// 날짜 범위 포맷팅
const formatDateRange = (start, end) => {
  if (!start || !end || isLoading.value) return "";
  const startDate = new Date(start);
  const endDate = new Date(end);
  return `${formatDate(startDate)} ~ ${formatDate(endDate)}`;
};

// 날짜 포맷팅
const formatDate = (date) => {
  if (!date || !(date instanceof Date) || isNaN(date)) return "";
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}.${String(date.getDate()).padStart(2, "0")}(${days[date.getDay()]})`;
};

// 일자별 날짜 가져오기
const getDayDate = (day) => {
  if (!planData.value?.startDate || isLoading.value) return "";
  const start = new Date(planData.value.startDate);
  if (isNaN(start.getTime())) return "";
  start.setDate(start.getDate() + day - 1);
  return formatDate(start);
};

// 현재 선택된 날짜의 장소 목록 가져오기
const getCurrentDaySpots = () => {
  const dayPlan = planData.value.dayPlans.find(
    (day) => day.day === selectedDay.value
  );
  return dayPlan ? dayPlan.details : [];
};

// 날짜 선택 핸들러
const selectDay = (day) => {
  selectedDay.value = day;
};

// 일정 수정 페이지로 이동
const goToModifyPlan = () => {
  router.push(`/modify-plan/${planData.value.planId}`);
};

// 계획 저장 핸들러
const savePlan = async () => {
  try {
    await new Promise((resolve) => setTimeout(resolve, 500));
    planStore.savePlan();
    alert("저장되었습니다.");
  } catch (error) {
    console.error("Failed to save plan:", error);
    alert("저장에 실패했습니다.");
  }
};

// 컴포넌트 언마운트 시 이벤트 리스너 정리
onBeforeUnmount(() => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame);
  }
  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragExpand);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragExpand);
});

// 컴포넌트 마운트 시 초기 데이터 로드
onMounted(() => {
  fetchPlanData();
});
</script>

<style scoped>
/* 전체 레이아웃 - 스크롤 방지 */
.detailed-travel-plan {
  height: 100vh;
  overflow: hidden;
}

/* 중간 섹션 - 지도와 콘텐츠 영역 */
.middle-section {
  width: 400px;
  max-width: calc(100vw - 192px);
  background-color: #fff;
  z-index: 10;
  will-change: transform;
  touch-action: none;
  transition: width 0.3s ease;
  height: calc(100vh - 64px);
}

/* 콘텐츠 래퍼 - 드래그 방지 */
.content-wrapper {
  position: relative;
  height: 100%;
  overflow: hidden;
  user-select: none;
  background-color: rgba(255, 255, 255, 0.5);
}

/* 패딩 영역 - 하단 버튼 공간 확보 */
.p-6 {
  height: calc 100%;
  padding-right: 24px;
  display: flex;
  flex-direction: column;
}

/* 일정 그리드 컨테이너 - 가로 스크롤 허용 */
.days-grid-container {
  width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  margin-bottom: 20px;
  min-height: 0;
  flex-grow: 1;
}

/* 일정 그리드 - 일자별 카드 배치 */
.days-grid {
  display: inline-flex;
  gap: 1.5rem;
  min-height: 100%;
  padding-bottom: 16px;
  padding-right: 24px;
}

/* 일자별 컨테이너 스타일링 */
.day-container {
  flex: 0 0 380px;
  max-height: calc(100vh - 280px);
  background-color: #fff;
}

/* 일자별 콘텐츠 영역 스크롤바 스타일링 */
.day-container .space-y-4 {
  height: 100%;
  overflow-y: auto;
  max-height: inherit;
}

/* 스크롤바 스타일링 */
.day-container .space-y-4::-webkit-scrollbar {
  width: 6px;
}

.day-container .space-y-4::-webkit-scrollbar-track {
  background-color: #f5f5f5;
  border-radius: 3px;
}

.day-container .space-y-4::-webkit-scrollbar-thumb {
  background-color: #888;
  border-radius: 3px;
}

.day-container .space-y-4::-webkit-scrollbar-thumb:hover {
  background-color: #666;
}

/* 가로 스크롤바 스타일링 */
.days-grid-container::-webkit-scrollbar {
  height: 6px;
  background-color: #f5f5f5;
}

.days-grid-container::-webkit-scrollbar-track {
  border-radius: 3px;
  background-color: #f5f5f5;
  margin: 0 24px;
}

.days-grid-container::-webkit-scrollbar-thumb {
  border-radius: 3px;
  background-color: #888;
}

.days-grid-container::-webkit-scrollbar-thumb:hover {
  background-color: #666;
}

/* 장소 카드 호버 효과 */
.spot-card {
  transition: transform 0.2s ease;
  background-color: #fff;
}

.spot-card:hover {
  transform: translateY(-2px);
}

/* 드래그 핸들 스타일링 */
.drag-handle {
  opacity: 0.8;
  transition: opacity 0.2s ease, transform 0.2s ease;
  touch-action: none;
  z-index: 30;
  cursor: grab;
}

.drag-handle:active {
  cursor: grabbing;
}

/* 왼쪽 사이드바 레이아웃 */
.left-sidebar {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
}

/* 반응형 스타일링 */
@media (max-width: 1024px) {
  .middle-section {
    width: 350px;
  }
  .day-container {
    width: 330px;
  }
}
</style>