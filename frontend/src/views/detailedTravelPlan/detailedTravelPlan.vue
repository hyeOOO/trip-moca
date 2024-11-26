<template>
  <!-- 전체 페이지 레이아웃 -->
  <div class="travel-plan">
    <nav-bar />
    <div class="main-content">
      <!-- 왼쪽 사이드바: 일자별 일정 목록 -->
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

        <!-- 하단 편집 버튼 -->
        <div class="edit-button-container">
          <button class="edit-button" @click="goToTravelCart">편집</button>
        </div>
      </div>

      <!-- 메인 영역: 지도와 일정 상세 -->
      <div class="content-area">
        <!-- 지도 영역 -->
        <div class="map-container">
          <tmap-multipath :selected-places-by-day="getSelectedPlaces" :latitude="33.3846" :longitude="126.5534"
            :selected-day="selectedDay === 'all' ? null : selectedDay" :show-all-days="selectedDay === 'all'" />
        </div>

        <!-- 드래그 가능한 일정 패널 -->
        <div ref="middleSection" class="schedule-panel" :class="{ expanded: isExpanded }"
          :style="{ width: `${currentWidth}px` }">
          <div class="panel-content">
            <div class="schedule-detail">
              <!-- 여행 제목과 기간 -->
              <div class="trip-header" v-if="!isLoading">
                <h1>{{ planData.planTitle }}</h1>
                <p>
                  {{ formatDateRange(planData.startDate, planData.endDate) }}
                </p>
              </div>

              <!-- 전체 일정 뷰 -->
              <div v-if="selectedDay === 'all'" class="all-schedules">
                <div class="days-grid" :style="gridStyle">
                  <div v-for="day in planData.dayPlans" :key="day.day" class="day-container">
                    <h2>
                      <span class="day">{{ day.day }}일차</span>
                      <span class="date">{{
                        formatDate(new Date(day.date))
                      }}</span>
                    </h2>
                    <div class="spots-container">
                      <div v-for="(spot, index) in day.details" :key="spot.planDetailId" class="spot-card">
                        <div class="spot-content">
                          <div class="spot-number">{{ index + 1 }}</div>
                          <div class="spot-info">
                            <div class="image-container">
                              <img :src="spot.image" :alt="spot.attractionTitle" />
                            </div>
                            <div class="text-container">
                              <!-- 태그 컨테이너 추가 -->
                              <div class="flex gap-2 mb-2">
                                <span v-if="spot.contentTypeName" class="px-2 py-1 rounded-lg text-xs text-white tag"
                                  :style="{ backgroundColor: getContentTypeColor(spot.contentTypeId) }">
                                  {{ spot.contentTypeName }}
                                </span>
                              </div>

                              <h3>{{ spot.attractionTitle }}</h3>
                              <span class="attraction-addr">{{ spot.addr1 }}</span>
                              <span class="attraction-addr">{{ spot.addr2 }}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 개별 일정 뷰 -->
              <div v-else class="day-schedule">
                <div v-for="(spot, index) in getCurrentDaySpots()" :key="spot.planDetailId" class="spot-card">
                  <div class="spot-content">
                    <div class="spot-number">{{ index + 1 }}</div>
                    <div class="spot-info">
                      <div class="image-container">
                        <img :src="spot.image" :alt="spot.attractionTitle" />
                      </div>
                      <div class="text-container">
                        <!-- 태그 컨테이너 추가 -->
                        <div class="flex gap-2 mb-2">
                          <span v-if="spot.contentTypeName" class="px-2 py-1 rounded-lg text-xs text-white tag"
                            :style="{ backgroundColor: getContentTypeColor(spot.contentTypeId) }">
                            {{ spot.contentTypeName }}
                          </span>
                        </div>

                        <h3>{{ spot.attractionTitle }}</h3>
                        <span class="attraction-addr">{{ spot.addr1 }}</span>
                        <span class="attraction-addr">{{ spot.addr2 }}</span>
                      </div>
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
  </div>
</template>

<script setup>
// Vue 관련 임포트
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from 'vue-router';
import { usePlanStore } from "@/store/editPlanStore.js";
import navBar from "@/components/navBar.vue";
import TmapMultipath from "@/components/Tmap/TmapMultipath.vue";

const router = useRouter(); // 라우터 조작(페이지 이동 등)
const route = useRoute();   // 현재 라우트 정보 접근(params, query 등)
const planStore = usePlanStore();

// 상태 관리 변수들
const selectedDay = ref("all"); // 현재 선택된 일자 (전체 or 특정 일자)
const isExpanded = ref(false); // 패널 확장 상태
const middleSection = ref(null); // 패널 DOM 참조
const dragHandle = ref(null); // 드래그 핸들 DOM 참조
const isLoading = ref(true); // 데이터 로딩 상태

// 패널 드래그 관련 상태
const panelOffset = ref(0); // 패널 이동 거리
const isDragging = ref(false); // 드래그 진행 중 여부
const startX = ref(0); // 드래그 시작 X 좌표
const initialPanelOffset = ref(0); // 초기 패널 위치
let animationFrame = null; // 애니메이션 프레임 참조

// 패널 너비 계산
const currentWidth = computed(() => {
  const baseWidth = 464; // 처음 시작 너비
  const maxWidth = Math.min(window.innerWidth - 100, 1382); // 화면의 너비에서 192를 뺀 값과 화면의 너비 중 작은 값
  return Math.min(baseWidth + panelOffset.value, maxWidth);
});

// 그리드 레이아웃 스타일
const gridStyle = computed(() => ({
  display: "flex",
  gap: "2.2rem",
  width: "max-content",
}));

// 여행 계획 데이터 관련
const planData = computed(() => {
  const data = planStore.getPlanData;
  console.log('Computed planData:', data);
  return data;
});
const totalDays = computed(() => planData.value?.dayPlans?.length || 0);

// 지도에 표시할 장소 데이터 처리
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

// 드래그 이벤트 핸들러
const startDragExpand = (e) => {
  if (!dragHandle.value || (e.type === "mousedown" && e.button !== 0)) return;
  e.preventDefault();

  isDragging.value = true;
  startX.value = e.type === "mousedown" ? e.clientX : e.touches[0].clientX;
  initialPanelOffset.value = panelOffset.value;
  dragHandle.value.style.cursor = "grabbing";

  if (e.type === "mousedown") {
    document.addEventListener("mousemove", handleDragMove);
    document.addEventListener("mouseup", stopDragExpand);
  } else {
    document.addEventListener("touchmove", handleDragMove);
    document.addEventListener("touchend", stopDragExpand);
  }
};

//화면 크기 조절 이벤트 핸들러
const handleDragMove = (e) => {
  if (!isDragging.value) return;

  const clientX = e.type === "mousemove" ? e.clientX : e.touches[0].clientX;
  const delta = clientX - startX.value;

  if (animationFrame) cancelAnimationFrame(animationFrame);

  animationFrame = requestAnimationFrame(() => {
    const newOffset = initialPanelOffset.value + delta;
    const maxOffset = Math.min(window.innerWidth - 100, 1382) - 464;
    panelOffset.value = Math.max(Math.min(newOffset, maxOffset), 0);
    isExpanded.value = panelOffset.value > 100;
  });
};

const stopDragExpand = () => {
  isDragging.value = false;
  if (dragHandle.value) dragHandle.value.style.cursor = "grab";

  document.removeEventListener("mousemove", handleDragMove);
  document.removeEventListener("mouseup", stopDragExpand);
  document.removeEventListener("touchmove", handleDragMove);
  document.removeEventListener("touchend", stopDragExpand);

  if (animationFrame) cancelAnimationFrame(animationFrame);
};

// 데이터 관련 함수들
const formatDateRange = (start, end) => {
  if (!start || !end || isLoading.value) return "";
  return `${formatDate(new Date(start))} ~ ${formatDate(new Date(end))}`;
};

const formatDate = (date) => {
  if (!date || !(date instanceof Date) || isNaN(date)) return "";
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}.${String(date.getDate()).padStart(2, "0")}(${days[date.getDay()]})`;
};

const getDayDate = (day) => {
  if (!planData.value?.startDate || isLoading.value) return "";
  const start = new Date(planData.value.startDate);
  if (isNaN(start.getTime())) return "";
  start.setDate(start.getDate() + day - 1);
  return formatDate(start);
};

// 현재 선택된 날짜의 장소 목록
const getCurrentDaySpots = () => {
  const dayPlan = planData.value.dayPlans.find(
    (day) => day.day === selectedDay.value
  );
  return dayPlan ? dayPlan.details : [];
};

// 데이터 초기화 및 저장
const fetchPlanData = async () => {
  try {
    isLoading.value = true;
    const planId = route.params.id;
    const areaCode = route.params.areaCode;

    console.log('Fetching plan details:', { planId, areaCode });

    await planStore.initializePlan(planId);

  } catch (error) {
    console.error("여행 계획 데이터 로드 실패:", error);
  } finally {
    isLoading.value = false;
  }
};

// 이벤트 핸들러
const selectDay = (day) => {
  selectedDay.value = day;
};

const goToTravelCart = () => {
  router.push(`/TravelCart/${planData.value.planId}/${planData.value.areaCode}`);
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

// 생명주기 훅
onMounted(() => {
  fetchPlanData();
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
  /* 절대 위치 */
  left: 0;
  height: 100%;
  background: white;
  z-index: 10;
  transition: width 0.3s;
  /* 너비 변경 시 애니메이션 */
  overflow: hidden;
  /* 내용이 넘칠 때 숨김 */
}

/* 드래그 패널 */
.panel-content {
  height: 100%;
  overflow: hidden;
}

.schedule-detail {
  padding: 0 24px 24px 24px;
  height: 100%;
  overflow-y: auto;
}

/* 패널 안 여행 제목과 기간  */
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

/* 몇일차 하고 xxxx.xx.xx(요일) */
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

/* 여행 스케줄 카드 */
.spot-card {
  width: 410px;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
  margin-bottom: 1rem;
  transition: transform 0.5s;
  padding: 10px 16px;
}

.spot-card:hover {
  transform: translateY(-2px);
}

.spot-content {
  display: flex;
  gap: 1rem;
}

.spot-number {
  font-family: "Pretendard-SemiBold";
  display: flex;
  align-items: center;
  color: #6e6156;
  font-size: 1.125rem;
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

.spot-info {
  display: flex;
  align-items: flex-start;
  flex: 1;
}

/* 카드여행지 이미지 */
.spot-info img {
  margin-right: 1rem;
  /* 이미지와 텍스트 사이 간격 */
  width: 145px;
  height: 6rem;
  object-fit: cover;
  border-radius: 0.5rem;
}

/* 카드여행지 이름, 설명 */
.spot-info h3 {
  font-family: "Pretendard-Bold";
  font-size: 1.125rem;
}

.spot-info p {
  color: #b4b4b4;
  font-size: 14px;
  font-family: 'Pretendard-Regular';
}

/* text-container 스타일 추가 */
.text-container {
  flex: 1;
  min-width: 0;
  /* 텍스트가 넘칠 때 줄바꿈되도록 */
}

/* 드래그 핸들 */
.drag-handle {
  position: absolute;
  /* 절대 위치 */
  right: 0;
  /* 오른쪽 정렬 */
  top: 50%;
  /* 세로 중앙 정렬 */
  transform: translateY(-50%);
  /* 세로 중앙 정렬 */
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

/* 가로/세로 스크롤바 공통 스타일 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #ffffff;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #ECB27B;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #C3A386;
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
</style>
