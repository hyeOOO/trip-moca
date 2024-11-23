<template>
  <!-- 전체 페이지 레이아웃 -->
  <div class="travel-cart">
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

        <!-- 하단 취소 버튼 -->
        <div class="edit-button-container">
          <button @click="cancelEdit" class="edit-button">취소</button>
        </div>
      </div>

      <!-- 메인 영역: 장소선택, 장바구니, 지도 -->
      <div class="content-wrapper">
        <!-- 왼쪽 섹션: 장소 검색 및 목록 -->
        <div class="places-section" :class="{ collapsed: isCollapsed }">
          <div class="trip-header">
            <h1>{{ planData.planTitle }}</h1>
            <p class="date-range">
              {{ formatDateRange(planData.startDate, planData.endDate) }}
            </p>
          </div>
          <!-- 검색창 -->
          <div class="search-box">
            <input type="text" v-model="searchQuery" placeholder="장소를 입력해 주세요." @keyup.enter="handleSearch" />
            <i class="fa-solid fa-search" @click="handleSearch"></i>
          </div>
          <!-- 장소 목록 -->
          <div class="places-list" @scroll="handleScroll">
            <div v-for="place in paginatedPlaces" :key="place.attractionId" class="place-item" draggable="true"
              @dragstart="dragStart($event, place)">
              <div class="place-image">
                <img :src="getImageUrl(place.image1)" :alt="place.title" />
              </div>
              <div class="place-info">
                <div class="flex gap-2 mb-2">
                  <span v-if="place.contentTypeName" class="px-2 py-1 rounded-lg text-xs text-white tag"
                    :style="{ backgroundColor: getContentTypeColor(place.contentTypeId) }">
                    {{ place.contentTypeName }}
                  </span>
                </div>
                <h3>{{ place.title }}</h3>
                <p>{{ place.addr1 }}</p>
              </div>
            </div>
            <!-- 로딩 표시 -->
            <div v-if="loading" class="loading">
              <i class="fa-solid fa-spinner fa-spin"></i> 로딩중...
            </div>
            <!-- 데이터 없음 표시 -->
            <div v-if="!hasMore && paginatedPlaces.length > 0" class="no-more">
              모든 장소를 불러왔습니다
            </div>
          </div>
        </div>

        <!-- 오른쪽 섹션: 장바구니 -->
        <div class="cart-section" :class="{ collapsed: isRightCollapsed }" @dragover.prevent @drop="onDrop($event)">
          <div class="section-header">
            <h2 class="section-title">장바구니</h2>
            <button @click="clearCart" class="clear-cart">초기화</button>
          </div>
          <!-- 선택된 장소 목록 -->
          <div class="cart-items" @dragover.prevent @drop="onDrop($event)">
            <div v-for="(place, index) in cartItems" :key="place.attractionId" class="cart-item" draggable="true"
              @dragstart="dragStartSelected($event, place, index)" @dragover.prevent @drop="onDrop($event, index)">
              <div class="order-number">{{ index + 1 }}</div>
              <div class="place-image">
                <img :src="getImageUrl(place.image1)" :alt="place.title" />
              </div>
              <div class="place-info">
                <div class="flex gap-2 mb-2">
                  <span v-if="place.contentTypeName" class="px-2 py-1 rounded-lg text-xs text-white tag"
                    :style="{ backgroundColor: getContentTypeColor(place.contentTypeId) }">
                    {{ place.contentTypeName }}
                  </span>
                </div>
                <h3>{{ place.title }}</h3>
                <p>{{ place.addr1 }}</p>
              </div>
              <div>
                <button @click="removePlace(place)" class="remove-btn">
                  <i class="fa-solid fa-times"></i>
                </button>
              </div>
            </div>
          </div>
          <!-- 장바구니 저장 버튼 -->
          <div class="cart-actions">
            <button @click="assignToDay" class="save-cart" :disabled="!cartItems.length">
              장바구니 저장
            </button>
          </div>
        </div>

        <!-- 지도 표시 영역 -->
        <div class="map-container">
          <t-map-component ref="tmap" :selected-places-by-day="getSelectedPlaces" :latitude="33.3846"
            :longitude="126.5534" :selected-day="selectedDay" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";
import { usePlanStore } from "@/store/editPlanStore";
import navBar from "@/components/navBar.vue";
import TMapComponent from "@/components/Tmap/TmapNumPing.vue";
import api from "@/plugins/axios";

const router = useRouter();
const route = useRoute();
const planStore = usePlanStore();
const draggedItem = ref(null);

const cartData = computed(() => planStore.getCartItems);
const selectedDay = ref("cart");
const cartItems = ref([]);
const isCollapsed = ref(false);
const isRightCollapsed = ref(false);
const searchQuery = ref("");
const places = ref([]);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const hasMore = ref(true);
const tmap = ref(null);

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
      0: cartItems.value.map((place) => ({
        id: place.attractionId,
        title: place.title,
        latitude: place.latitude,
        longitude: place.longitude,
      })),
    };
  }
  return {};
});

const fetchAttractions = async () => {
  try {
    loading.value = true;
    const response = await api.get('/domain/attraction/search', {
      params: {
        areaCode: planData.value.areaCode,
        page: page.value - 1,
        size: pageSize.value,
        sort: "title,asc"  // 배열이 아닌 문자열로 변경
        // title: searchQuery.value  // 필요한 경우 추가
      }
    });

    console.log('API Response:', response.data);  // 응답 데이터 확인

    const { content, last } = response.data;
    if (page.value === 1) {
      places.value = content;
    } else {
      places.value = [...places.value, ...content];
    }
    hasMore.value = !last;
  } catch (error) {
    console.error('관광지 데이터 로드 실패:', error);
    console.error('Error details:', error.response?.data);  // 상세 에러 확인
  } finally {
    loading.value = false;
  }
};

const handleScroll = async (e) => {
  if (loading.value || !hasMore.value) return;

  const element = e.target;
  const scrollBottom = element.scrollHeight - element.scrollTop - element.clientHeight;

  if (scrollBottom < 50) {
    page.value++;
    await fetchAttractions();
  }
};

const handleSearch = async () => {
  page.value = 1;
  hasMore.value = true;
  try {
    loading.value = true;
    // URL이 잘못되었습니다: attractions -> attraction
    const response = await api.get('/domain/attraction/search', {  // attractions를 attraction으로 수정
      params: {
        areaCode: planData.value.areaCode,
        title: searchQuery.value,  // keyword -> title로 수정
        page: 0,
        size: pageSize.value,
        sort: "title,asc"  // 문자열로 변경
      }
    });

    const { content, last } = response.data;
    places.value = content;
    hasMore.value = !last;
  } catch (error) {
    console.error('관광지 검색 실패:', error);
  } finally {
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

  if (!draggedItem.value) return;

  try {
    const parsed = JSON.parse(data);
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
      const newItem = draggedItem.value;
      if (newItem) {
        await nextTick(() => {
          if (targetIndex !== undefined) {
            cartItems.value.splice(targetIndex, 0, newItem);
          } else {
            cartItems.value.push(newItem);
          }
        });
      }
    }
  } catch {
    const newItem = draggedItem.value;
    if (newItem) {
      await nextTick(() => {
        if (targetIndex !== undefined) {
          cartItems.value.splice(targetIndex, 0, newItem);
        } else {
          cartItems.value.push(newItem);
        }
      });
    }
  }
  draggedItem.value = null;
};

const selectDay = (day) => {
  selectedDay.value = day;
};

const removePlace = (place) => {
  cartItems.value = cartItems.value.filter(
    (p) => p.attractionId !== place.attractionId
  );
};

const clearCart = () => {
  planStore.clearCartItems();
  cartItems.value = [];
  localStorage.removeItem('cartItems');
};

const assignToDay = () => {
  if (!cartItems.value.length) return;
  planStore.setCartItems(cartItems.value);
  localStorage.setItem('cartItems', JSON.stringify(cartItems.value));
  router.push(`/modify/${planData.value.planId}/${planData.value.areaCode}`);
};

const cancelEdit = () => {
  const id = route.params.id;
  planStore.resetToOriginal();
  router.push(`/plan/${id}/${planData.value.areaCode}`);
  planStore.clearCartItems();
  cartItems.value = [];
  localStorage.removeItem('cartItems');
};

const getImageUrl = (imageUrl) =>
  imageUrl ||
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png";

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

watch(searchQuery, () => {
  page.value = 1;
  hasMore.value = true;
  if (searchQuery.value.trim()) {
    handleSearch();
  } else {
    fetchAttractions();
  }
});

watch(
  cartItems,
  () => {
    selectedDay.value = "cart";
    localStorage.setItem('cartItems', JSON.stringify(cartItems.value));
  },
  { deep: true }
);

onMounted(async () => {
  const id = route.params.id;
  await planStore.initializePlan(id);

  const savedCartItems = localStorage.getItem('cartItems');
  if (savedCartItems) {
    cartItems.value = JSON.parse(savedCartItems);
  } else {
    cartItems.value = cartData.value;
  }

  await fetchAttractions();
});
</script>

<style scoped>
/* 전체 레이아웃 */
.travel-cart {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
  /* 추가 */
}

/* 사이드바 */
.sidebar {
  width: 100px;
  display: flex;
  flex-direction: column;
  background: white;
  min-height: 0;
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

/* 메인 컨텐츠 영역 */
.content-wrapper {
  flex: 1;
  display: flex;
  position: relative;
  min-height: 0;
}

/* 장소 검색/목록 섹션 */
.places-section {
  width: 464px;
  height: calc(100vh - 64px);
  background: white;
  z-index: 10;
  padding: 0 24px 24px 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.trip-header {
  font-family: "EliceDigitalBaeum_Bold";
}

.trip-header h1 {
  font-size: 32px;
}

.trip-header p {
  color: #ecb27b;
}

/* 검색창 */
.search-box {
  position: relative;
  margin: 10px 0px 20px 0px;
}

.search-box input {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 1px solid #ddd;
  border-radius: 25px;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
}

.search-box input::placeholder {
  font-size: 16px;
  font-family: "Pretendard-Light";
  color: #b4b4b4;
}

.search-box i {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #b4b4b4;
  cursor: pointer;
}

/* 장소 목록 */
.places-list {
  max-height: 72vh;
  overflow-y: auto;
}

.place-item {
  display: flex;
  align-items: center;
  width: 400px;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
  margin-bottom: 1rem;
  transition: transform 0.5s;
  padding: 10px 16px;
  cursor: grab;
}

.place-item:hover {
  transform: translateY(-2px);
}

.place-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.place-image {
  margin-right: 1rem;
  /* 이미지와 텍스트 사이 간격 */
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
  font-family: 'Pretendard-Regular';
  font-size: 13px;
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

/* 장바구니 상단 문구 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

/* 장바구니 문구 */
.section-title {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 30px;
}

/* 초기화버튼 */
.clear-cart {
  font-family: "EliceDigitalBaeum_regular";
  color: #ff6365;
  cursor: pointer;
  padding: 15px 10px 5px 0px;
}

.clear-cart:hover {
  color: #f44336;
}

/* 장바구니 여행지 속성 */
.cart-items {
  max-height: 76vh;
  overflow-y: auto;
}

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

/* 버튼 스타일 */
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

/* 여행지 순서 번호 */
.order-number {
  width: 24px;
  height: 24px;
  background: #ecb27b;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

/* 지도 컨테이너 */
.map-container {
  flex: 1;
  height: calc(100vh - 64px);
  position: relative;
  min-height: 0;
  z-index: 1;
}

/* 상태 표시 */
.loading,
.no-more {
  text-align: center;
  padding: 1rem;
  color: #666;
}

.loading i {
  margin-right: 0.5rem;
}

.tag {
  font-family: 'Pretendard-SemiBold';
  font-size: 12px;
}

/* 패널 상태 */
.collapsed {
  width: 0;
  padding: 0;
  overflow: hidden;
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
