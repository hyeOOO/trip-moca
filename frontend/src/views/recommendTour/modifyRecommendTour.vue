<template>
  <div class="travel-plan">
    <nav-bar />
    <div class="main-content">
      <!-- 좌측: 일정 선택 사이드바 -->
      <div class="sidebar">
        <div class="schedule-list">
          <!-- 전체 일정 버튼 -->
          <button class="schedule-btn" :class="{ active: selectedDay === 'all' }" @click="selectDay('all')">
            <i class="fa-regular fa-calendar"></i><br />
            전체일정
          </button>

          <!-- 일자별 버튼 -->
          <div v-for="day in totalDays" :key="day" class="day-item">
            <button class="schedule-btn" :class="{ active: selectedDay === day }" @click="selectDay(day)">
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

      <!-- 중앙: 메인 콘텐츠 영역 -->
      <div class="content-area">
        <!-- 지도 영역 -->
        <div class="map-container">
          <tmap-multipath :selected-places-by-day="getSelectedPlaces" :latitude="mapCoordinates.latitude"
            :longitude="mapCoordinates.longitude" :selected-day="selectedDay === 'all' ? null : selectedDay"
            :show-all-days="selectedDay === 'all'" />
        </div>

        <!-- 일정 패널 -->
        <div ref="middleSection" class="schedule-panel" :class="{ expanded: isExpanded }"
          :style="{ width: `${currentWidth}px` }">
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
                  <div v-for="dayPlan in planData.dayPlans" :key="dayPlan.day" class="day-container" @dragover.prevent
                    @drop="handleDrop($event, dayPlan.day)">
                    <h2>
                      <span class="day">{{ dayPlan.day }}일차</span>
                      <span class="date">{{ getDayDate(dayPlan.day) }}</span>
                    </h2>
                    <div class="schedule-items">
                      <div v-for="(spot, index) in dayPlan.details" :key="spot.attractionId" class="schedule-item"
                        draggable="true" @dragstart="dragStart($event, spot, index, dayPlan.day)" @dragover.prevent
                        @drop="handleDrop($event, dayPlan.day, index)">
                        <div class="spot-number">{{ index + 1 }}</div>
                        <div class="image-container">
                          <div class="place-image">
                            <img :src="getImageUrl(spot.image)" :alt="spot.attractionTitle" />
                          </div>
                        </div>
                        <div class="place-info">
                          <div class="flex gap-2 mb-2">
                            <span v-if="spot.contentTypeName" class="px-2 py-1 rounded-lg text-xs text-white tag"
                              :style="{ backgroundColor: getContentTypeColor(spot.contentTypeId) }">
                              {{ spot.contentTypeName }}
                            </span>
                          </div>

                          <h3>{{ spot.attractionTitle }}</h3>
                          <p class="attraction-addr">{{ spot.addr1 }}</p>
                        </div>
                        <div>
                          <button @click="removePlace(dayPlan.day, index)" class="delete-button">
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
                <div class="schedule-items">
                  <div v-for="(spot, index) in getCurrentDaySpots()" :key="spot.attractionId" class="schedule-item"
                    draggable="true" @dragstart="dragStart($event, spot, index, selectedDay)">
                    <div class="spot-number">{{ index + 1 }}</div>
                    <div class="place-image">
                      <img :src="getImageUrl(spot.image)" :alt="spot.attractionTitle" />
                    </div>
                    <div class="place-info">
                      <div class="flex gap-2 mb-2">
                        <span v-if="spot.contentTypeName" class="px-2 py-1 rounded-lg text-xs text-white tag"
                          :style="{ backgroundColor: getContentTypeColor(spot.contentTypeId) }">
                          {{ spot.contentTypeName }}
                        </span>
                      </div>
                      <h3>{{ spot.attractionTitle }}</h3>
                      <p class="attraction-addr">{{ spot.addr1 }}</p>
                    </div>
                    <div>
                      <button @click="removePlace(selectedDay, index)" class="delete-button">
                        <i class="fas fa-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 패널 크기 조절 핸들 -->
          <div ref="dragHandle" class="drag-handle" @mousedown="startDragExpand" @touchstart="startDragExpand">
            <i class="fa-solid fa-grip-lines-vertical"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 저장 모달 -->
    <SavePlanModal v-model="showSaveModal"
      :default-title="`${aiRecommendStore.selectedDestination?.areaName} ${aiRecommendStore.selectedDestination?.numberOfDays}일 여행`"
      @close="showSaveModal = false" @save="handleSavePlan" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { usePlanStore } from "@/store/planStore";
import { useAuthStore } from "@/store/auth";
import { showLoginModalFlag } from "@/eventBus";
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";
import SavePlanModal from '@/components/savePlanModal.vue';
import { useAiRecommendPlanStore } from "@/store/aiRecommendPlanStore";
import api from "@/plugins/axios";

// 라우터 및 스토어 설정
const router = useRouter();
const route = useRoute();
const aiRecommendStore = useAiRecommendPlanStore();
const planStore = usePlanStore();
const authStore = useAuthStore();

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
const isLoading = ref(true);
const showSaveModal = ref(false);
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
    if (!aiRecommendStore.getPlanData?.dayPlans?.length) {
      await aiRecommendStore.generateAiPlan().catch(error => {
        console.error("Failed to generate plan:", error);
        throw error;
      });
    }

    // 데이터 로드 후 상태 확인을 위한 로그
    console.log('Initial plan data:', aiRecommendStore.getPlanData);
    console.log('Selected places:', getSelectedPlaces.value);

    // 데이터 로드 후 약간의 지연을 주어 컴포넌트들이 제대로 마운트되도록 함
    await new Promise(resolve => setTimeout(resolve, 100));

    // 전체 일정 보기로 초기화
    selectedDay.value = 'all';

    // TmapMultipath 컴포넌트가 새로운 데이터로 업데이트되도록 강제로 트리거
    if (planData.value?.dayPlans?.length > 0) {
      const temp = JSON.parse(JSON.stringify(planData.value));
      aiRecommendStore.updatePlan(temp);
    }

  } catch (error) {
    console.error("Data initialization error:", error);
    alert("여행 계획을 불러오는데 실패했습니다. 다시 시도해주세요.");
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

const handleDrop = (event, targetDay, targetIndex) => {
  event.preventDefault();
  if (!draggedItem.value) return;

  const newPlan = JSON.parse(JSON.stringify(planData.value));
  const targetDayPlan = newPlan.dayPlans.find((plan) => plan.day === targetDay);
  if (!targetDayPlan) return;

  if (draggedItem.value.fromDay === targetDay) {
    const details = targetDayPlan.details;
    const [movedItem] = details.splice(draggedItem.value.index, 1);
    const adjustedTargetIndex =
      targetIndex > draggedItem.value.index ? targetIndex - 1 : targetIndex;
    details.splice(adjustedTargetIndex, 0, movedItem);
  } else {
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

  aiRecommendStore.updatePlan(newPlan);
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

// 기존 script setup 내부에 함수 추가
const getContentTypeColor = (contentType) => {
  const colorMap = {
    12: '#ecb27b',
    14: '#6E6156',
    15: '#433629',
    25: '#332417',
    28: '#988D82',
    32: '#C3A386',
    38: '#ecb27b',
    39: '#6E6156'
  };
  return colorMap[contentType] || '#ecb27b';  // 기본값으로 #ecb27b 반환
};

const saveModifiedPlan = () => {
  if (!authStore.isAuthenticated) {
    // 로그인 후 실행할 작업을 저장
    authStore.setPendingAction(() => {
      showSaveModal.value = true;
    });
    showLoginModalFlag.value = true;
    return;
  }
  showSaveModal.value = true;
};

const handleSavePlan = async ({ title, image }) => {
  try {
    // planStore에 데이터 설정
    planStore.setDestination({
      id: aiRecommendStore.selectedDestination.areaCode,
      areaCode: aiRecommendStore.selectedDestination.areaCode,
      areaName: aiRecommendStore.selectedDestination.areaName,
      title: title,
      image: aiRecommendStore.selectedDestination.image,
    });

    // 더미 날짜 설정
    const today = new Date();
    const endDate = new Date(today);
    endDate.setDate(today.getDate() + aiRecommendStore.selectedDestination.numberOfDays - 1);
    planStore.setDates(today.toISOString(), endDate.toISOString());

    // FormData 생성 및 전송
    const formData = new FormData();
    const planData = {
      planTitle: title,
      areaCode: planStore.selectedDestination.areaCode,
      startDate: planStore.dates.startDate,
      endDate: planStore.dates.endDate,
      dayPlans: aiRecommendStore.recommendPlan.dayPlans.map(dayPlan => ({
        day: dayPlan.day,
        details: dayPlan.details.map((detail, index) => ({
          attractionId: detail.attractionId,
          sequence: index + 1,
          memo: detail.memo || "",
        })),
      })),
    };

    formData.append(
      "plan",
      new Blob([JSON.stringify(planData)], { type: "application/json" })
    );

    if (image) {
      formData.append("image", image);
    }

    const response = await api.post("/domain/plans", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    if (response.status === 200) {
      alert("여행 계획이 저장되었습니다!");
      planStore.resetStore();
      aiRecommendStore.resetStore();
      router.push("/mypage");
    }
  } catch (error) {
    console.error("Error saving plan:", error);
    alert(error.response?.data?.message || "저장 중 오류가 발생했습니다.");
  } finally {
    showSaveModal.value = false;
  }
};

const cancelModify = () => {
  router.push("/recommendTour");
};

onMounted(async () => {
  if (!aiRecommendStore.selectedDestination?.areaCode) {
    router.push("/recommendTour");
    return;
  }

  // store에 이미 데이터가 있는지 확인
  if (!aiRecommendStore.getPlanData?.dayPlans?.length) {
    try {
      isLoading.value = true;
      await aiRecommendStore.generateAiPlan();
    } catch (error) {
      console.error("Failed to initialize data:", error);
      alert("여행 계획을 불러오는데 실패했습니다. 다시 시도해주세요.");
      router.push("/recommendTour");
    } finally {
      isLoading.value = false;
    }
  }
});

watch(
  () => route.params.id,
  async (newId) => {
    if (newId && aiRecommendStore.selectedDestination?.areaCode) {
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
.schedule-btn {
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

/* 일정 아이템 스타일 */
.schedule-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.schedule-item {
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

.schedule-item:hover {
  transform: translateY(-2px);
}

.place-image {
  margin-right: 1rem;
  width: 145px;
  height: 6rem;
  object-fit: cover;
  border-radius: 0.5rem;
}

.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.place-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.place-info h3 {
  font-family: "Pretendard-Medium";
  font-weight: bold;
  font-size: 1.125rem;
}

.place-info p {
  color: #b4b4b4;
  font-size: 13px;
}

.spot-number {
  font-family: "Pretendard-SemiBold";
  display: flex;
  align-items: center;
  color: #6e6156;
  font-size: 1.125rem;
}

/* 삭제 버튼 */
.delete-button {
  color: #b4b4b4;
  padding: 0.5rem;
  transition: color 0.2s;
}

.delete-button:hover {
  color: #b4b4b4;
}

/* 드래그 핸들 */
.drag-handle {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
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

/* 기타 스타일 */
.days-grid {
  display: flex;
  gap: 2.2rem;
  width: max-content;
}

.day-container {
  margin-bottom: 1.5rem;
}

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

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
}

.image-container {
  flex-shrink: 0;
  /* 컨테이너가 줄어들지 않도록 설정 */
  width: 145px;
  height: 6rem;
  margin-right: 1rem;
  border-radius: 0.5rem;
  overflow: hidden;
  /* 이미지가 컨테이너를 벗어나지 않도록 */
}

.image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0.5rem;
}

.text-container {
  flex: 1;
  min-width: 0;
  /* 텍스트가 넘칠 때 줄바꿈되도록 */
}

.modal-content h2 {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 24px;
  margin-bottom: 1.5rem;
  text-align: center;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.plan-title-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
}

.image-upload-container {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
}

.image-preview {
  width: 100%;
  height: 200px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

.image-preview.has-image {
  border: none;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  text-align: center;
  color: #666;
}

.upload-placeholder i {
  font-size: 48px;
  margin-bottom: 8px;
}

.tag {
  font-family: 'Pretendard-SemiBold';
  font-size: 12px;
}

.attraction-addr {
  font-family: 'Pretendard-Regular';
  font-size: 14px;
  color: #B4B4B4;
}

.hidden-input {
  display: none;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.save-button,
.cancel-button {
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  border: none;
}

.save-button {
  background: #ecb27b;
  color: white;
}

.cancel-button {
  background: #f3f4f6;
  color: #4b5563;
}

.save-button:hover {
  background: #6e6156;
}

.cancel-button:hover {
  background: #e5e7eb;
}
</style>