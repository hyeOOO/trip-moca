<template>
  <div class="detailed-travel-plan">
    <nav-bar />
    <div class="main-content flex relative h-[calc(100vh-64px)]">
      <!-- Left Sidebar -->
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

      <!-- Main Content Area -->
      <div class="flex-1 relative">
        <!-- Map Section -->
        <div class="absolute inset-0">
          <tmap-multipath
            :selected-places-by-day="getSelectedPlaces"
            :latitude="33.3846"
            :longitude="126.5534"
            :selected-day="selectedDay === 'all' ? null : selectedDay"
            :show-all-days="selectedDay === 'all'"
          />
        </div>

        <!-- Middle Section (Sliding Panel) -->
        <div
          ref="middleSection"
          class="middle-section absolute left-0 h-full bg-white shadow-lg transition-width"
          :class="{ expanded: isExpanded }"
          :style="{ width: `${currentWidth}px` }"
        >
          <div class="content-wrapper h-full bg-white">
            <div class="p-6 h-full overflow-y-auto">
              <div class="mb-6">
                <h1 class="text-2xl font-bold mb-2">{{ planData.planTitle }}</h1>
                <p class="text-gray-600">
                  {{ formatDateRange(planData.startDate, planData.endDate) }}
                </p>
              </div>

              <div v-if="selectedDay === 'all'" class="days-grid" :style="gridStyle">
                <div
                  v-for="day in planData.dayPlans"
                  :key="day.day"
                  class="day-container"
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

          <!-- Drag Handle -->
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

// Width calculation
const currentWidth = computed(() => {
  const baseWidth = 400;
  const maxWidth = Math.min(window.innerWidth - 192, 1200);
  return Math.min(baseWidth + panelOffset.value, maxWidth);
});

// Grid layout computation
const gridStyle = computed(() => {
  const isPanelWide = currentWidth.value > 800;
  const columns = isPanelWide ? Math.floor(currentWidth.value / 400) : 1;
  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${columns}, minmax(300px, 1fr))`,
    gap: '1.5rem',
    height: 'fit-content'
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
.detailed-travel-plan {
  height: 100vh;
  overflow: hidden;
}

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

.content-wrapper {
  position: relative;
  height: 100%;
  overflow: hidden;
  user-select: none;
}

.p-6 {
  height: 100%;
  overflow-y: auto;
  padding-right: 24px;
}

/* Scrollbar styles */
.content-wrapper::-webkit-scrollbar,
.days-grid::-webkit-scrollbar {
  width: 8px;
  height: 8px;
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

/* Days grid layout */
.days-grid {
  display: grid;
  gap: 1.5rem;
  height: fit-content;
  transition: grid-template-columns 0.3s ease;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 12px;
  scroll-behavior: smooth;
}

.day-container {
  min-width: 300px;
  background-color: #fff;
}

/* Spot card styles */
.spot-card {
  transition: transform 0.2s ease;
  background-color: #fff;
}

.spot-card:hover {
  transform: translateY(-2px);
}

/* Drag handle styles */
.drag-handle {
  opacity: 0.8;
  transition: opacity 0.2s ease, transform 0.2s ease;
  touch-action: none;
  right: 24px;
  z-index: 30;
  cursor: grab;
}

.drag-handle:hover {
  opacity: 1;
}

.drag-handle:active {
  cursor: grabbing;
}

@media (max-width: 1024px) {
  .middle-section {
    width: 350px;
  }
}
</style>