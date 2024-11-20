<template>
  <div class="root-content">
    <!-- 전체 레이아웃: 화면 전체 높이를 사용하는 flex 컨테이너 -->
    <div class="nav-theme nav-dark">
      <nav-bar />
    </div>
    <div class="main-content">
      <!-- 왼쪽 사이드바: 일정 개요 및 일자 선택 -->
      <div class="sidebar">
        <!-- 일정 목록 스크롤 영역 -->
        <div class="sidebar-content">
          <!-- 전체 일정 -->
          <div class="day-item">
            <div class="day-info">
              <div class="day-title">전체일정</div>
            </div>
          </div>
          <!-- 일자별 표시 -->
          <div v-for="day in totalDays" :key="day" class="day-static">
            <div>DAY {{ day }}</div>
            <div class="day-count">{{ day }}일차</div>
          </div>
        </div>

        <!-- 하단 저장/취소 버튼 영역 -->
        <div class="sticky-footer">
          <button @click="savePlan" class="button button-save">저장</button>
          <button @click="cancelEdit" class="button button-cancel">취소</button>
        </div>
      </div>

      <!-- 메인 콘텐츠 영역 -->
      <div class="content-wrapper">
        <!-- Middle Section -->
        <div
          class="middle-section relative"
          :class="{ collapsed: isCollapsed }"
        >
          <div class="toggle-button" @click="toggleCollapse">
            <i
              class="fa-solid"
              :class="{
                'fa-arrow-left': !isCollapsed,
                'fa-arrow-right': isCollapsed,
              }"
            ></i>
          </div>
          <div class="header">
            <h2>장소 선택</h2>
            <p class="text-gray-500 text-sm">
              {{ formatDateRange(planData.startDate, planData.endDate) }}
            </p>
          </div>
          <div class="search-section">
            <div class="search-box">
              <input
                type="text"
                v-model="searchQuery"
                placeholder="여행지를 검색하세요"
                @keyup.enter="handleSearch"
              />
              <i class="fa-solid fa-search" @click="handleSearch"></i>
            </div>
          </div>
          <div class="places-list" @scroll="handleScroll">
            <div
              v-for="place in paginatedPlaces"
              :key="place.attractionId"
              class="place-item"
              draggable="true"
              @dragstart="dragStart($event, place)"
            >
              <div class="place-image">
                <img :src="getImageUrl(place.image1)" :alt="place.title" />
              </div>
              <div class="place-info">
                <h3 class="place-title">{{ place.title }}</h3>
                <p class="place-address">{{ place.addr1 }}</p>
              </div>
            </div>
            <!-- 로딩 인디케이터 -->
            <div v-if="loading" class="loading-indicator">
              <i class="fa-solid fa-spinner fa-spin"></i>
              로딩중...
            </div>
            <!-- 더 이상 데이터가 없을 때 메시지 -->
            <div
              v-if="!hasMore && paginatedPlaces.length > 0"
              class="no-more-data"
            >
              모든 장소를 불러왔습니다
            </div>
          </div>
        </div>

        <!-- Right Section (장바구니) -->
        <div class="right-section" :class="{ collapsed: isRightCollapsed }">
          <div class="toggle-button" @click="toggleRightSection">
            <i
              class="fa-solid"
              :class="{
                'fa-arrow-left': !isRightCollapsed,
                'fa-arrow-right': isRightCollapsed,
              }"
            ></i>
          </div>
          <div class="header">
            <h2>장바구니</h2>
            <button @click="clearCart" class="clear-button">
              <i class="fa-solid fa-rotate-left"></i> 초기화
            </button>
          </div>
          <div class="selected-places" @dragover.prevent @drop="onDrop($event)">
            <div v-if="!cartItems.length" class="empty-cart">
            </div>
            <div v-else class="cart-items">
              <div
                v-for="(place, index) in cartItems"
                :key="place.attractionId"
                class="cart-item"
                draggable="true"
                @dragstart="dragStartSelected($event, place, index)"
                @dragover.prevent
                @drop="onDrop($event, index)"
              >
                <div class="order-number">{{ index + 1 }}</div>
                <div class="place-image">
                  <img :src="getImageUrl(place.image1)" :alt="place.title" />
                </div>
                <div class="place-info">
                  <h4>{{ place.title }}</h4>
                  <button @click="removePlace(place)" class="remove-button">
                    <i class="fa-solid fa-times"></i>
                  </button>
                </div>
              </div>
            </div>
            <div class="cart-actions">
              <button
                @click="assignToDay"
                class="assign-button"
                :disabled="!cartItems.length || !selectedDay"
              >
                장바구니 저장
              </button>
            </div>
          </div>
        </div>

        <!-- 지도 영역 -->
        <div class="map-container">
          <t-map-component
            ref="tmap"
            :selected-places-by-day="getSelectedPlaces"
            :latitude="33.3846"
            :longitude="126.5534"
            :selected-day="selectedDay"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import { usePlanStore } from "@/store/planStore";
import navBar from "@/components/navBar.vue";
import TMapComponent from "@/components/Tmap/Tmap.vue";
import testData from "@/assets/data/testData.js";

const router = useRouter();
const planStore = usePlanStore();
const draggedItem = ref(null);

// 상태 관리
const selectedDay = ref("cart");
const cartItems = ref([]);
const isCollapsed = ref(false);
const isRightCollapsed = ref(false);
const searchQuery = ref("");
const places = ref(testData);
const selectedPlacesByDay = ref({});
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const hasMore = ref(true);
const tmap = ref(null);

// Computed properties
const planData = computed(() => planStore.getPlanData);
const totalDays = computed(() => planData.value?.dayPlans?.length || 0);

const filteredPlaces = computed(() => {
  if (!searchQuery.value) return places.value;
  const query = searchQuery.value.toLowerCase();
  return places.value.filter(
    (place) =>
      place.title.toLowerCase().includes(query) ||
      place.addr1.toLowerCase().includes(query)
  );
});

const paginatedPlaces = computed(() => {
  const start = 0;
  const end = page.value * pageSize.value;
  return filteredPlaces.value.slice(start, end);
});

const getSelectedPlaces = computed(() => {
  if (selectedDay.value === "cart") {
    return {
      0: cartItems.value.map((place) => ({  // '#FF6B6B' 대신 0을 사용
        id: place.attractionId,
        title: place.title,
        latitude: place.latitude || place.mapy,
        longitude: place.longitude || place.mapx,
      }))
    };
  }
  return {};
});

// Methods
const handleScroll = async (e) => {
  const element = e.target;
  if (loading.value || !hasMore.value) return;

  const scrollBottom =
    element.scrollHeight - element.scrollTop - element.clientHeight;
  if (scrollBottom < 50) {
    loading.value = true;
    await new Promise((resolve) => setTimeout(resolve, 500));

    if (page.value * pageSize.value < filteredPlaces.value.length) {
      page.value++;
    } else {
      hasMore.value = false;
    }
    loading.value = false;
  }
};

const dragStart = (event, place) => {
  event.dataTransfer.effectAllowed = "move";
  event.dataTransfer.setData("text/plain", place.attractionId);
  draggedItem.value = place;
};

const dragStartSelected = (event, place, index) => {
  event.dataTransfer.effectAllowed = "move";
  event.dataTransfer.setData(
    "text/plain",
    JSON.stringify({ id: place.attractionId, index })
  );
  draggedItem.value = place;
};

const onDrop = async (event, targetIndex) => {
  event.preventDefault();
  const data = event.dataTransfer.getData("text/plain");

  try {
    const parsed = JSON.parse(data);
    // Item from cart
    if (parsed.index !== undefined) {
      const items = [...cartItems.value];
      items.splice(parsed.index, 1);
      if (targetIndex !== undefined) {
        items.splice(targetIndex, 0, draggedItem.value);
      } else {
        items.push(draggedItem.value);
      }
      await nextTick(() => {
        cartItems.value = items;
      });
    } else {
      // New item from places list
      await nextTick(() => {
        if (targetIndex !== undefined) {
          cartItems.value.splice(targetIndex, 0, draggedItem.value);
        } else {
          cartItems.value.push(draggedItem.value);
        }
      });
    }
  } catch {
    // Handle new item drop with just ID
    if (draggedItem.value) {
      await nextTick(() => {
        if (targetIndex !== undefined) {
          cartItems.value.splice(targetIndex, 0, draggedItem.value);
        } else {
          cartItems.value.push(draggedItem.value);
        }
      });
    }
  }
  draggedItem.value = null;
};

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
  setTimeout(() => tmap.value?.getMap().resize(), 300);
};

const toggleRightSection = () => {
  isRightCollapsed.value = !isRightCollapsed.value;
  setTimeout(() => tmap.value?.getMap().resize(), 300);
};

const handleSearch = () => {
  if (!searchQuery.value.trim()) return;
};

const selectDay = (day) => (selectedDay.value = day);

const savePlan = () => {
  planStore.savePlan();
  router.push(`/plan/${planData.value.planId}`);
};

const cancelEdit = () => {
  const id = router.currentRoute.value.params.id;
  planStore.resetToOriginal();
  router.push(`/plan/${id}`);
};

const getImageUrl = (imageUrl) =>
  imageUrl ||
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png";

const removePlace = (place) => {
  cartItems.value = cartItems.value.filter(
    (p) => p.attractionId !== place.attractionId
  );
};

const clearCart = () => (cartItems.value = []);

const assignToDay = () => {
  if (!selectedDay.value || !cartItems.value.length) return;
  selectedPlacesByDay.value[selectedDay.value] = [...cartItems.value];
  selectedPlacesByDay.value = { ...selectedPlacesByDay.value };
  clearCart();
};

const formatDate = (date) => {
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}.${String(date.getDate()).padStart(2, "0")}(${days[date.getDay()]})`;
};

const formatDateRange = (start, end) =>
  !start || !end
    ? ""
    : `${formatDate(new Date(start))} ~ ${formatDate(new Date(end))}`;

const getDayDate = (day) => {
  if (!planData.value.startDate) return "";
  const date = new Date(planData.value.startDate);
  date.setDate(date.getDate() + day - 1);
  return formatDate(date);
};

// Watchers
watch(searchQuery, () => {
  page.value = 1;
  hasMore.value = true;
});

watch(
  cartItems,
  () => {
    selectedDay.value = "cart";
  },
  { deep: true }
);

// Lifecycle
onMounted(() => {
  const id = router.currentRoute.value.params.id;
  planStore.initializePlan();
});
</script>

<style scoped>
.root-content {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}
/* Middle Section 스타일 */
.middle-section {
  width: 380px;
  height: 100%;
  background-color: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
  padding: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 토글 버튼 스타일 */
.toggle-button {
  position: absolute;
  top: 20px;
  right: 2px;
  width: 32px;
  height: 32px;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 20;
  transition: all 0.3s ease;
}

/* 검색 섹션 스타일 */
.search-section {
  margin: 20px 0;
}

.search-box {
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 1px solid #ddd;
  border-radius: 25px;
  font-size: 14px;
}

.search-box i {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
  cursor: pointer;
}

/* 장소 목록 스타일 */
.places-list {
  flex: 1;
  overflow-y: auto;
  height: calc(100vh - 200px); /* nav-bar + header + search box 높이를 뺀 값 */
  margin-right: -20px;
  padding-right: 20px;
  scrollbar-width: thin;
  scroll-behavior: smooth;
}

/* 장소 아이템 공통 스타일 */
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

.place-item:hover,
.cart-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
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

.place-info {
  flex: 1;
}

.place-title,
.cart-item h4 {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.place-address {
  font-size: 14px;
  color: #666;
}

/* Sidebar 스타일 */
.sidebar {
  width: 6rem;
  min-width: 6rem; /* 추가: 사이드바 최소 너비 고정 */
  background-color: white;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  height: 100%;
  z-index: 40; /* 추가: 사이드바가 항상 보이도록 z-index 설정 */
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  border-top: 1px solid #e5e7eb;
}

.sidebar-content > * {
  border-bottom: 1px solid #e5e7eb;
}

.day-static {
  padding: 1rem 0.5rem;
  text-align: center;
  font-size: 0.875rem;
  color: #374151;
}

.day-count {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

/* 장바구니 스타일 */
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

.empty-cart {
  height: 74vh;
  padding: 20px;
  border-radius: 8px;
  background-color: white;
  color: #666;
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

.order-number {
  position: static;
  width: 24px;
  height: 24px;
  background-color: #f57c00;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

/* 버튼 스타일 */
.button,
.assign-button {
  width: 100%;
  padding: 1rem;
  text-align: center;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s ease;
}

.button-save {
  background-color: #ecb27b;
  color: white;
}

.button-save:hover {
  background-color: #2563eb;
}

.button-cancel {
  background-color: #e5e7eb;
  color: #374151;
}

.button-cancel:hover {
  background-color: #ecb27b;
}

.assign-button {
  background-color: #4caf50;
  color: white;
}

.assign-button:hover {
  background-color: #45a049;
}

.assign-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.remove-button {
  border: none;
  background: none;
  color: #666;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.remove-button:hover {
  color: #f44336;
}

/* 지도 컨테이너 */
.map-container {
  position: relative;
  flex: 1;
  height: 100%;
  min-width: 0;
  z-index: 1;
}

/* Collapsed 상태 */
.middle-section.collapsed,
.right-section.collapsed {
  width: 0;
  padding: 0;
  overflow: hidden;
}

/* 로딩 관련 스타일 */
.loading-indicator {
  text-align: center;
  padding: 1rem;
  color: #666;
}

.loading-indicator i {
  margin-right: 0.5rem;
}

.no-more-data {
  text-align: center;
  padding: 1rem;
  color: #666;
  font-size: 0.875rem;
}

/* 스크롤바 스타일 */
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

.nav-theme.nav-dark {
  z-index: 50;
  display: flex;
  flex-direction: column;
}

.flex-1.flex {
  position: fixed;
  top: 64px; /* nav-bar 높이만큼 띄움 */
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.main-content {
  top: 64px; /* nav-bar 높이만큼 띄움 */
  display: flex;
  overflow: hidden;
}

.content-wrapper {
  flex: 1;
  display: flex;
  position: relative;
}
</style>
