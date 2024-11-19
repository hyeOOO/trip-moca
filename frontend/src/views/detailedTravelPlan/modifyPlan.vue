<template>
  <div class="h-screen flex flex-col">
    <nav-bar />
    <div class="flex-1 flex">
      <!-- Left Sidebar -->
      <div class="w-60 bg-white border-r border-gray-200 flex flex-col">
        <div class="p-4 border-b border-gray-200">
          <div class="font-bold text-lg">{{ planData.planTitle }}</div>
          <div class="text-sm text-gray-500">
            {{ formatDateRange(planData.startDate, planData.endDate) }}
          </div>
        </div>

        <div class="flex-1 overflow-y-auto">
          <div
            v-for="day in planData.dayPlans"
            :key="day.day"
            class="p-4 border-b border-gray-200"
          >
            <button
              @click="selectDay(day.day)"
              class="w-full text-left"
              :class="{ 'text-blue-600': selectedDay === day.day }"
            >
              <div class="font-medium">DAY {{ day.day }}</div>
              <div class="text-sm text-gray-500">{{ getDayDate(day.day) }}</div>
            </button>
          </div>
        </div>

        <div class="p-4 border-t border-gray-200 bg-gray-50">
          <button
            @click="savePlan"
            class="w-full bg-blue-500 text-white rounded-md py-2 mb-2"
          >
            저장
          </button>
          <button
            @click="cancelEdit"
            class="w-full bg-gray-200 text-gray-700 rounded-md py-2"
          >
            취소
          </button>
        </div>
      </div>

      <!-- Main Content -->
      <div class="flex-1 flex relative">
        <!-- Map Area -->
        <div class="absolute inset-0">
          <tmap-multipath
            :selected-places-by-day="getSelectedPlaces"
            :latitude="33.3846"
            :longitude="126.5534"
            :selected-day="selectedDay"
          />
        </div>

        <!-- Right Panel -->
        <div
          class="absolute right-0 w-[460px] h-full bg-white shadow-lg flex flex-col"
        >
          <div class="p-4 border-b border-gray-200">
            <h2 class="text-lg font-bold">DAY {{ selectedDay }} 일정</h2>
            <div class="text-sm text-gray-500">
              {{ getDayDate(selectedDay) }}
            </div>
          </div>

          <div class="flex-1 overflow-y-auto p-4">
            <VueDraggableNext
              v-model="currentDaySpots"
              handle=".drag-handle"
              item-key="planDetailId"
              class="space-y-3"
            >
              <template #item="{ element, index }">
                <div
                  class="bg-white rounded-lg border border-gray-200 p-4 relative group"
                >
                  <div class="flex items-center gap-3">
                    <div
                      class="drag-handle cursor-move text-gray-400 hover:text-gray-600"
                    >
                      <i class="fas fa-grip-vertical"></i>
                    </div>
                    <div
                      class="w-8 h-8 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center font-medium"
                    >
                      {{ index + 1 }}
                    </div>
                    <div class="flex-1">
                      <div class="font-medium">
                        {{ element.attractionTitle }}
                      </div>
                      <input
                        v-model="element.memo"
                        class="mt-2 w-full p-2 bg-gray-50 rounded border border-gray-200"
                        placeholder="메모를 입력하세요..."
                      />
                    </div>
                    <button
                      @click="removeSpot(index)"
                      class="text-gray-400 hover:text-red-500"
                    >
                      <i class="fas fa-times"></i>
                    </button>
                  </div>
                </div>
              </template>
            </VueDraggableNext>

            <button
              @click="openSpotSearch"
              class="mt-4 w-full py-3 bg-gray-50 text-gray-600 rounded-lg border border-gray-200 hover:bg-gray-100"
            >
              + 장소 추가하기
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { usePlanStore } from "@/store/planStore";
import { VueDraggableNext } from "vue-draggable-next";
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";

const router = useRouter();
onMounted(() => {
  const id = router.currentRoute.value.params.id;
  planStore.initializePlan();
});
const planStore = usePlanStore();
const selectedDay = ref(1);

const planData = computed(() => planStore.getPlanData);

const currentDaySpots = computed({
  get: () => {
    const dayPlan = planData.value.dayPlans.find(
      (day) => day.day === selectedDay.value
    );
    return dayPlan ? [...dayPlan.details] : [];
  },
  set: (newSpots) => {
    const newPlanData = { ...planData.value };
    const dayPlanIndex = newPlanData.dayPlans.findIndex(
      (day) => day.day === selectedDay.value
    );
    if (dayPlanIndex !== -1) {
      newPlanData.dayPlans[dayPlanIndex].details = newSpots;
      planStore.updateEditingPlan(newPlanData);
    }
  },
});

const getSelectedPlaces = computed(() => {
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

function selectDay(day) {
  selectedDay.value = day;
}

function removeSpot(index) {
  const newPlanData = { ...planData.value };
  const dayPlan = newPlanData.dayPlans.find(
    (day) => day.day === selectedDay.value
  );
  if (dayPlan) {
    dayPlan.details.splice(index, 1);
    planStore.updateEditingPlan(newPlanData);
  }
}

function savePlan() {
  planStore.savePlan();
  router.push(`/plan/${planData.value.planId}`);
}

function cancelEdit() {
  const id = router.currentRoute.value.params.id;
  planStore.resetToOriginal();
  router.push(`/plan/${id}`);
}

function openSpotSearch() {
  // 장소 검색 모달 열기 로직
}

function formatDateRange(start, end) {
  if (!start || !end) return "";
  return `${formatDate(new Date(start))} ~ ${formatDate(new Date(end))}`;
}

function formatDate(date) {
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}.${String(date.getDate()).padStart(2, "0")}(${days[date.getDay()]})`;
}

function getDayDate(day) {
  if (!planData.value.startDate) return "";
  const date = new Date(planData.value.startDate);
  date.setDate(date.getDate() + day - 1);
  return formatDate(date);
}
</script>

<style scoped>
.drag-handle {
  cursor: move;
  padding: 8px;
}

::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background-color: #f5f5f5;
}

::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: #ccc;
}
</style>
