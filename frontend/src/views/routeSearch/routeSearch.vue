<template>
  <div class="route-search-page">
    <navBar />
    <div class="route-search-container">
      <!-- 왼쪽 사이드바 -->
      <div class="sidebar">
        <h2 class="sidebar-title">장소 입력</h2>

        <!-- 출발지 입력 -->
        <div class="location-input-section">
          <h3>출발지</h3>
          <div class="input-wrapper">
            <input
              type="text"
              v-model="startLocation"
              @input="handleStartLocationInput"
              placeholder="출발지를 입력해주세요!"
              class="location-input"
            />
            <!-- 출발지 검색어 추천 목록 -->
            <ul
              v-if="showStartSuggestions && startSuggestions.length > 0"
              class="suggestions-list"
            >
              <li
                v-for="suggestion in startSuggestions"
                :key="suggestion.name"
                @click="selectStartLocation(suggestion)"
                class="suggestion-item"
              >
                {{ suggestion.name }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 도착지 입력 -->
        <div class="location-input-section">
          <h3>도착지</h3>
          <div class="input-wrapper">
            <input
              type="text"
              v-model="endLocation"
              @input="handleEndLocationInput"
              placeholder="도착지를 입력해 주세요!"
              class="location-input"
            />
            <!-- 도착지 검색어 추천 목록 -->
            <ul
              v-if="showEndSuggestions && endSuggestions.length > 0"
              class="suggestions-list"
            >
              <li
                v-for="suggestion in endSuggestions"
                :key="suggestion.name"
                @click="selectEndLocation(suggestion)"
                class="suggestion-item"
              >
                {{ suggestion.name }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 경로 정보 표시 -->
        <div v-if="showRouteInfo" class="route-info">
          <div class="route-type-selector">
            <button
              :class="[
                'route-type-btn',
                { active: selectedRouteType === 'pedestrian' },
              ]"
              @click="selectRouteType('pedestrian')"
            >
              보행자 경로
            </button>
            <button
              :class="[
                'route-type-btn',
                { active: selectedRouteType === 'car' },
              ]"
              @click="selectRouteType('car')"
            >
              자동차 경로
            </button>
          </div>
          <div class="time-info">
            <div v-if="routeDetails" class="route-details">
              <div class="total-time">
                {{ Math.floor(routeDetails.totalTime / 60) }}분
                {{ routeDetails.totalTime % 60 }}초
              </div>
              <div class="distance">
                총 {{ (routeDetails.totalDistance / 1000).toFixed(1) }}km
              </div>
              <div v-if="selectedRouteType === 'car'" class="fare">
                택시 요금: {{ routeDetails.taxiFare }}원
              </div>
            </div>
          </div>
        </div>

        <!-- 경로 찾기 버튼 -->
        <button
          @click="searchRoute"
          :disabled="!selectedStart || !selectedEnd"
          class="search-button"
        >
          경로 찾기
        </button>
      </div>

      <!-- 지도 영역 -->
      <div class="map-container">
        <TmapRoute
          ref="tmapRoute"
          :selected-start="selectedStart"
          :selected-end="selectedEnd"
          :route-type="selectedRouteType"
          @route-calculated="handleRouteCalculated"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import navBar from "@/components/navBar.vue";
import TmapRoute from "@/components/Tmap/TmapRoute.vue";

// 상태 관리
const startLocation = ref("");
const endLocation = ref("");
const selectedStart = ref(null);
const selectedEnd = ref(null);
const startSuggestions = ref([]);
const endSuggestions = ref([]);
const showStartSuggestions = ref(false);
const showEndSuggestions = ref(false);
const showRouteInfo = ref(false);
const selectedRouteType = ref("pedestrian");
const routeDetails = ref(null);
const tmapRoute = ref(null);

// 입력 핸들러 - 디바운스 처리
let startDebounceTimer;
const handleStartLocationInput = () => {
  clearTimeout(startDebounceTimer);
  startDebounceTimer = setTimeout(async () => {
    if (startLocation.value.length >= 2) {
      const results = await tmapRoute.value.searchLocationSuggestions(
        startLocation.value
      );
      startSuggestions.value = results;
      showStartSuggestions.value = true;
    } else {
      startSuggestions.value = [];
      showStartSuggestions.value = false;
    }
  }, 300);
};

let endDebounceTimer;
const handleEndLocationInput = () => {
  clearTimeout(endDebounceTimer);
  endDebounceTimer = setTimeout(async () => {
    if (endLocation.value.length >= 2) {
      const results = await tmapRoute.value.searchLocationSuggestions(
        endLocation.value
      );
      endSuggestions.value = results;
      showEndSuggestions.value = true;
    } else {
      endSuggestions.value = [];
      showEndSuggestions.value = false;
    }
  }, 300);
};

// 장소 선택 핸들러
const selectStartLocation = (suggestion) => {
  startLocation.value = suggestion.name;
  selectedStart.value = suggestion;
  tmapRoute.value.setLocationMarker(suggestion, "start"); // 마커 표시 추가
  showStartSuggestions.value = false;
};

const selectEndLocation = (suggestion) => {
  endLocation.value = suggestion.name;
  selectedEnd.value = suggestion;
  tmapRoute.value.setLocationMarker(suggestion, "end"); // 마커 표시 추가
  showEndSuggestions.value = false;
};

// 경로 타입 선택
const selectRouteType = (type) => {
  selectedRouteType.value = type;
  if (showRouteInfo.value) {
    searchRoute();
  }
};

// 경로 검색
const searchRoute = async () => {
  if (selectedStart.value && selectedEnd.value) {
    try {
      showRouteInfo.value = true;
      const result = await tmapRoute.value.calculateRoute(
        selectedRouteType.value
      );
      if (result) {
        routeDetails.value = result;
      } else {
        console.error("경로 계산 실패");
      }
    } catch (error) {
      console.error("경로 검색 중 오류:", error);
    }
  }
};

const handleRouteCalculated = (details) => {
  if (details) {
    routeDetails.value = details;
    showRouteInfo.value = true;  // 여기서도 showRouteInfo를 true로 설정
  }
};

// 클릭 이벤트 리스너 (외부 클릭 시 suggestion 닫기)
watch(
  () => [showStartSuggestions.value, showEndSuggestions.value],
  () => {
    if (showStartSuggestions.value || showEndSuggestions.value) {
      setTimeout(() => {
        const handleClickOutside = (event) => {
          const suggestionsLists =
            document.querySelectorAll(".suggestions-list");
          let clickedInside = false;

          suggestionsLists.forEach((list) => {
            if (list.contains(event.target)) {
              clickedInside = true;
            }
          });

          if (!clickedInside) {
            showStartSuggestions.value = false;
            showEndSuggestions.value = false;
            document.removeEventListener("click", handleClickOutside);
          }
        };

        document.addEventListener("click", handleClickOutside);
      });
    }
  }
);
</script>

<style scoped>
.route-search-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.route-search-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 360px;
  padding: 20px;
  background-color: white;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
}

.sidebar-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
  font-family: "EliceDigitalBaeum_Bold";
}

.location-input-section {
  margin-bottom: 20px;
  position: relative;
}

.location-input-section h3 {
  font-size: 16px;
  margin-bottom: 8px;
  font-family: "Pretendard-Medium";
}

.input-wrapper {
  position: relative;
}

.location-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  font-family: "Pretendard-Regular";
}

.suggestions-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
  margin: 0;
  padding: 0;
  list-style: none;
}

.suggestion-item {
  padding: 10px 12px;
  cursor: pointer;
  font-family: "Pretendard-Regular";
  font-size: 14px;
}

.suggestion-item:hover {
  background-color: #f5f5f5;
}

.route-info {
  margin: 20px 0;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #f8f9fa;
}

.time-info {
  text-align: center;
  margin-bottom: 15px;
}

.total-time {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
  font-family: "EliceDigitalBaeum_Bold";
}

.time-unit {
  font-size: 18px;
  margin: 0 4px;
}

.distance {
  color: #666;
  font-size: 14px;
  font-family: "Pretendard-Regular";
}

.search-button {
  width: 100%;
  padding: 12px;
  background-color: #ecb27b;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-family: "EliceDigitalBaeum_Bold";
}

.search-button:hover:not(:disabled) {
  background-color: #e5a165;
}

.search-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.map-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  margin: 0;
  flex: 1;
}

.route-type-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.route-type-btn {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-family: "Pretendard-Medium";
}

.route-type-btn.active {
  background: #ecb27b;
  color: white;
  border-color: #ecb27b;
}

.route-details {
  text-align: center;
}

.search-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.search-button:hover:not(:disabled) {
  background-color: #e5a165;
}
</style>
