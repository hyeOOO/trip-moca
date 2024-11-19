<template>
  <div class="detailed-travel-plan">
    <nav-bar />
    <div class="main-content flex relative h-[calc(100vh-64px)]">
      <!-- 왼쪽 사이드바 -->
      <div class="left-sidebar w-48 bg-white border-r border-gray-200 flex flex-col">
        <div class="overflow-y-auto flex-1 divide-y divide-gray-200">
          <button
            class="w-full p-4 text-left hover:bg-gray-100 font-medium"
            :class="{ 'bg-blue-50 text-blue-600': selectedDay === 'all' }"
            @click="selectDay('all')"
          >
            전체일정
          </button>

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

        <div class="p-4 border-t border-gray-200">
          <button
            class="w-full mb-2 p-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition"
            @click="goToModifyPlan"
          >
            편집
          </button>
          <button
            class="w-full p-2 bg-green-500 text-white rounded hover:bg-green-600 transition"
            @click="savePlan"
          >
            저장
          </button>
        </div>
      </div>

      <!-- 메인 콘텐츠 영역 -->
      <div class="flex-1 relative">
        <!-- 지도 섹션 -->
        <div class="absolute inset-0">
          <tmap-multipath
            :selected-places-by-day="getSelectedPlaces"
            :latitude="33.3846"
            :longitude="126.5534"
            :selected-day="selectedDay === 'all' ? null : selectedDay"
            :show-all-days="selectedDay === 'all'"
          />
        </div>

        <!-- 중간섹션 -->
        <div
          ref="middleSection"
          class="middle-section absolute left-0 h-full bg-white shadow-lg transition-width"
          :class="{ expanded: isExpanded }"
          :style="{ width: `${currentWidth}px` }"
        >
          <div class="content-wrapper">
            <div class="p-6 h-full overflow-y-auto">
              <div class="mb-6">
                <h1 class="text-2xl font-bold mb-2">{{ planData.planTitle }}</h1>
                <p class="text-gray-600">
                  {{ formatDateRange(planData.startDate, planData.endDate) }}
                </p>
              </div>

              <!-- 전체 일정 뷰 -->
              <div v-if="selectedDay === 'all'" class="days-grid-container overflow-x-auto">
                <div class="days-grid" :style="gridStyle">
                  <div
                    v-for="day in planData.dayPlans"
                    :key="day.day"
                    class="day-container min-w-[380px]"
                  >
                    <h2 class="text-xl font-bold mb-4">
                      {{ day.day }}일차 ({{ formatDate(day.date) }})
                    </h2>
                    <div class="space-y-4">
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
              <div v-else class="space-y-4">
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

          <!-- 드래그 핸들 -->
          <div
            ref="dragHandle"
            class="drag-handle absolute right-0 top-1/2 -translate-y-1/2 w-6 h-12 
            bg-white rounded-r-lg shadow flex items-center justify-center cursor-grab
            hover:bg-gray-100 transition-colors z-20"
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
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";
import detailTestData from "@/assets/data/detailTestData.js";

const router = useRouter();
const selectedDay = ref("all");
const isExpanded = ref(false);
const middleSection = ref(null);
const dragHandle = ref(null);

// Panel state
const panelOffset = ref(0);
const isDragging = ref(false);
const startX = ref(0);
const currentX = ref(0);
const initialPanelOffset = ref(0);

const currentWidth = computed(() => {
  const baseWidth = 400;
  const maxWidth = Math.min(window.innerWidth - 192, 1200);
  return Math.min(baseWidth + panelOffset.value, maxWidth);
});

const gridStyle = computed(() => {
  const columnWidth = 380;
  const gap = 24;
  const availableWidth = currentWidth.value - 48;
  let columns = Math.floor((availableWidth + gap) / (columnWidth + gap));
  columns = Math.min(3, Math.max(1, columns));

  const totalDayPlans = planData.value.dayPlans ? planData.value.dayPlans.length : 0;

  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${columns}, ${columnWidth}px)`,
    gap: '1.5rem',
    height: 'fit-content',
    minWidth: totalDayPlans > columns 
      ? `${(columnWidth + gap) * totalDayPlans - gap}px`
      : '100%'
  };
});

const planData = ref({
  planId: null,
  planTitle: "",
  startDate: "",
  endDate: "",
  dayPlans: [],
});

let animationFrame = null;

const totalDays = computed(() => planData.value.totalDays || 0);

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

const fetchPlanData = async () => {
  try {
    await new Promise((resolve) => setTimeout(resolve, 500));
    planData.value = detailTestData;
  } catch (error) {
    console.error("Failed to fetch plan data:", error);
  }
};

const formatDateRange = (start, end) => {
  const startDate = new Date(start);
  const endDate = new Date(end);
  return `${formatDate(startDate)} ~ ${formatDate(endDate)}`;
};

const formatDate = (date) => {
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(
    2,
    "0"
  )}-${String(d.getDate()).padStart(2, "0")}(${days[d.getDay()]})`;
};

const getDayDate = (day) => {
  const start = new Date(planData.value.startDate);
  start.setDate(start.getDate() + day - 1);
  return formatDate(start);
};

const getCurrentDaySpots = () => {
  const dayPlan = planData.value.dayPlans.find(
    (day) => day.day === selectedDay.value
  );
  return dayPlan ? dayPlan.details : [];
};

const selectDay = (day) => {
  selectedDay.value = day;
};

const goToModifyPlan = () => {
  router.push(`/modify-plan/${planData.value.planId}`);
};

const savePlan = async () => {
  try {
    await new Promise((resolve) => setTimeout(resolve, 500));
    console.log("Plan saved:", planData.value);
    alert("저장되었습니다.");
  } catch (error) {
    console.error("Failed to save plan:", error);
    alert("저장에 실패했습니다.");
  }
};

onBeforeUnmount(() => {
  if (animationFrame) {
    cancelAnimationFrame(animationFrame);
  }
  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragExpand);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragExpand);
});

onMounted(() => {
  fetchPlanData();
});
</script>

<style scoped>
/* Layout */
.detailed-travel-plan {
  height: 100vh;
  overflow: hidden;
}

.days-grid-container {
  width: 100%;
  overflow-x: auto;
  padding-bottom: 16px;
  /* 부드러운 스크롤 효과 추가 */
  scroll-behavior: smooth;
  /* 가로 스크롤 터치 동작 활성화 */
  touch-action: pan-x;
}

.days-grid-container::-webkit-scrollbar {
  height: 8px;
  background-color: #f5f5f5;
}

.days-grid-container::-webkit-scrollbar-track {
  border-radius: 4px;
  background-color: #f5f5f5;
  margin: 0 24px;
}

.days-grid-container::-webkit-scrollbar-thumb {
  border-radius: 4px;
  background-color: #888;
}

.days-grid-container::-webkit-scrollbar-thumb:hover {
  background-color: #666;
}

/* Middle section styles */
.middle-section {
  width: 400px;
  max-width: calc(100vw - 192px);
  background-color: #fff;
  z-index: 10;
  will-change: transform;
  touch-action: none;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
}

/* Content wrapper */
.content-wrapper {
  position: relative;
  height: 100%;
  overflow: hidden; /* 변경 없음 */
  user-select: none;
  background-color: rgba(255, 255, 255, 0.5);
}

/* Scrollable content area */
.p-6 {
  height: 100%;
  overflow-y: hidden; /* auto에서 hidden으로 변경 */
  padding-right: 24px;
}

/* Scrollbar styles - unified for both content-wrapper and days-grid */
.content-wrapper::-webkit-scrollbar,
.days-grid::-webkit-scrollbar {
  display: none;
}

.content-wrapper::-webkit-scrollbar-track,
.days-grid::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
  margin: 4px 0;
}

.content-wrapper::-webkit-scrollbar-thumb,
.days-grid::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.content-wrapper::-webkit-scrollbar-thumb:hover,
.days-grid::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* days-grid 스타일 수정 */
.days-grid {
  display: grid;
  gap: 1.5rem;
  height: fit-content;
  padding-right: 24px;
  /* 최소 너비 설정을 gridStyle computed에서 동적으로 처리 */
}

/* Day container에 세로 스크롤 추가 */
.day-container {
  width: 380px;
  background-color: #fff;
  flex-shrink: 0;
  height: calc(100vh - 200px); /* 상단 여백과 패딩을 고려한 높이 */
  overflow-y: auto; /* 세로 스크롤 추가 */
}

.day-container::-webkit-scrollbar {
  width: 8px;
}

.day-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.day-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.day-container::-webkit-scrollbar-thumb:hover {
  background: #666;
}

/* Spot card */
.spot-card {
  transition: transform 0.2s ease;
  background-color: #fff;
}

.spot-card:hover {
  transform: translateY(-2px);
}

/* Drag handle */
.drag-handle {
  opacity: 0.8;
  transition: opacity 0.2s ease, transform 0.2s ease;
  touch-action: none;
  right: 24px;
  z-index: 30;
  cursor: grab;
}

.drag-handle:active {
  cursor: grabbing;
}

@media (max-width: 1024px) {
  .middle-section {
    width: 350px;
  }
  
  .day-container {
    width: 330px;
  }
  
  .days-grid {
    gap: 1rem;
  }
}
</style>