<template>
  <div class="search-form">
    <div class="search-group">
      <div>
        <p class="text1">당신만의 힙한 여행,<br />여기서 시작해요✨</p>
      </div>
      <br />
      <div>
        <p class="text2">출발일자</p>
        <input type="date" v-model="startDate" @focus="showStartPlaceholder = false"
          @blur="showStartPlaceholder = !startDate" placeholder=" " :min="today" />
        <span v-if="showStartPlaceholder" class="custom-placeholder">선택하세요</span>
      </div>
      <div>
        <p class="text2">도착일자</p>
        <input type="date" v-model="endDate" @focus="showEndPlaceholder = false" @blur="showEndPlaceholder = !endDate"
          placeholder=" " :min="startDate || today" />
        <span v-if="showEndPlaceholder" class="custom-placeholder">선택하세요</span>
      </div>
      <div>
        <p class="text2">지역</p>
        <select v-model="selectedArea"
          style="text-align: center; width: 395px; height: 42px; border: 1px solid #ffffff;">
          <option value="">지역을 선택하세요</option>
          <option v-for="region in regions" :key="region.code" :value="region.code">
            {{ region.name }}
          </option>
        </select>
      </div>

      <!-- 버튼을 search-group 안으로 이동 -->
      <button class="search-submit" @click="handleSubmit" :disabled="!isFormValid">출 발 하 기
        <div class="star-1">
          <svg xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 784.11 815.53"
            style="shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd"
            version="1.1" xml:space="preserve" xmlns="http://www.w3.org/2000/svg">
            <defs></defs>
            <g id="Layer_x0020_1">
              <metadata id="CorelCorpID_0Corel-Layer"></metadata>
              <path
                d="M392.05 0c-20.9,210.08 -184.06,378.41 -392.05,407.78 207.96,29.37 371.12,197.68 392.05,407.74 20.93,-210.06 184.09,-378.37 392.05,-407.74 -207.98,-29.38 -371.16,-197.69 -392.06,-407.78z"
                class="fil0"></path>
            </g>
          </svg>
        </div>
        <!-- 나머지 star divs... -->
      </button>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="spinner"></div>
        <h2 class="loading-text">AI가 최적의 여행 계획을 만들고 있습니다</h2>
        <p class="loading-description">잠시만 기다려주세요...</p>
        <p class="loading-timer">{{ formattedTime }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAiRecommendPlanStore } from "@/store/aiRecommendPlanStore";

const router = useRouter();
const aiRecommendStore = useAiRecommendPlanStore();

const startDate = ref('');
const endDate = ref('');
const selectedArea = ref('');
const showStartPlaceholder = ref(true);
const showEndPlaceholder = ref(true);
const isLoading = ref(false);
const elapsedSeconds = ref(0);
let timer = null;

const today = new Date().toISOString().split('T')[0];

const regions = [
  { code: "1", name: "서울" },
  { code: "2", name: "인천" },
  { code: "3", name: "대전" },
  { code: "4", name: "대구" },
  { code: "5", name: "광주" },
  { code: "6", name: "부산" },
  { code: "7", name: "울산" },
  { code: "8", name: "세종" },
  { code: "31", name: "경기도" },
  { code: "32", name: "강원도" },
  { code: "33", name: "충청북도" },
  { code: "34", name: "충청남도" },
  { code: "35", name: "경상북도" },
  { code: "36", name: "경상남도" },
  { code: "37", name: "전라북도" },
  { code: "38", name: "전라남도" },
  { code: "39", name: "제주도" }
];

const isFormValid = computed(() => {
  return startDate.value &&
    endDate.value &&
    selectedArea.value &&
    new Date(endDate.value) >= new Date(startDate.value);
});

const formattedTime = computed(() => {
  const minutes = Math.floor(elapsedSeconds.value / 60);
  const seconds = elapsedSeconds.value % 60;
  return `${minutes}:${seconds.toString().padStart(2, '0')}`;
});

const startTimer = () => {
  elapsedSeconds.value = 0;
  timer = setInterval(() => {
    elapsedSeconds.value++;
  }, 1000);
};

const stopTimer = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

const handleSubmit = async () => {
  try {
    if (!isFormValid.value) {
      alert("모든 항목을 올바르게 입력해주세요.");
      return;
    }

    isLoading.value = true;
    startTimer();

    const start = new Date(startDate.value);
    const end = new Date(endDate.value);
    const daysDiff = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;

    // aiRecommendStore 초기화
    aiRecommendStore.resetStore();

    // 선택된 목적지 정보 저장
    const selectedRegion = regions.find(r => r.code === selectedArea.value);
    aiRecommendStore.$patch({
      selectedDestination: {
        id: null,
        areaCode: selectedArea.value,
        areaName: selectedRegion.name,
        title: selectedRegion.name,
        numberOfDays: daysDiff,
        info: '',
      },
    });

    // AI 여행 계획 생성
    await aiRecommendStore.generateAiPlan();

    // 최소 로딩 시간 보장
    const minLoadingTime = new Promise(resolve => setTimeout(resolve, 2000));
    await minLoadingTime;

    // 계획 수정 페이지로 이동
    await router.push({
      name: "modifyRecommendTour",
      params: { id: selectedArea.value }
    });

  } catch (error) {
    console.error("Error creating plan:", error);
    alert("여행 계획 생성 중 오류가 발생했습니다.");
  } finally {
    stopTimer();
    isLoading.value = false;
  }
};
</script>

<style>
.search-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;
}



.h1-text {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 56px;
  text-align: left;
}

.text1 {
  font-family: "EliceDigitalBaeum_Regular";
  text-align: left;
  font-size: 24px;
}

.text2 {
  font-family: "EliceDigitalBaeum_Regular";
  text-align: left;
}

.search-submit {
  position: relative;
  padding: 12px 35px;
  background: var(--color);
  font-size: 17px;
  font-weight: 500;
  color: #ffffff;
  border: 1px solid #ffffff;
  border-radius: 8px;
  box-shadow: 0 0 0 #fec1958c;
  transition: all .3s ease-in-out;
  cursor: pointer;
}

button {
  position: relative;
  padding: 12px 35px;
  background: var(--color);
  font-size: 17px;
  font-weight: 500;
  color: #181818;
  border: 3px solid var(--color);
  border-radius: 8px;
  box-shadow: 0 0 0 #ff6a008c;
  transition: all .3s ease-in-out;
  cursor: pointer;
}

.star-1 {
  position: absolute;
  top: 20%;
  left: 20%;
  width: 25px;
  height: auto;
  filter: drop-shadow(0 0 0 #ECB27B);
  z-index: -5;
  transition: all 1s cubic-bezier(0.05, 0.83, 0.43, 0.96);
}

.star-2 {
  position: absolute;
  top: 45%;
  left: 45%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 0 #ECB27B);
  z-index: -5;
  transition: all 1s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-3 {
  position: absolute;
  top: 40%;
  left: 40%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 0 #ECB27B);
  z-index: -5;
  transition: all 1s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-4 {
  position: absolute;
  top: 20%;
  left: 40%;
  width: 8px;
  height: auto;
  filter: drop-shadow(0 0 0 #ECB27B);
  z-index: -5;
  transition: all .8s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-5 {
  position: absolute;
  top: 25%;
  left: 45%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 0 #ECB27B);
  z-index: -5;
  transition: all .6s cubic-bezier(0, 0.4, 0, 1.01);
}

.star-6 {
  position: absolute;
  top: 5%;
  left: 50%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 0 #ECB27B);
  z-index: -5;
  transition: all .8s ease;
}

button:hover {
  background: transparent;
  color: var(--color);
  box-shadow: 0 0 25px #fec1958c;
}

button:hover .star-1 {
  position: absolute;
  top: -80%;
  left: -30%;
  width: 25px;
  height: auto;
  filter: drop-shadow(0 0 10px #ECB27B);
  z-index: 2;
}

button:hover .star-2 {
  position: absolute;
  top: -25%;
  left: 10%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 10px #ECB27B);
  z-index: 2;
}

button:hover .star-3 {
  position: absolute;
  top: 55%;
  left: 25%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 10px #ECB27B);
  z-index: 2;
}

button:hover .star-4 {
  position: absolute;
  top: 30%;
  left: 80%;
  width: 8px;
  height: auto;
  filter: drop-shadow(0 0 10px #ECB27B);
  z-index: 2;
}

button:hover .star-5 {
  position: absolute;
  top: 25%;
  left: 115%;
  width: 15px;
  height: auto;
  filter: drop-shadow(0 0 10px #ECB27B);
  z-index: 2;
}

button:hover .star-6 {
  position: absolute;
  top: 5%;
  left: 60%;
  width: 5px;
  height: auto;
  filter: drop-shadow(0 0 10px #ECB27B);
  z-index: 2;
}

.fil0 {
  fill: #ECB27B
}

.search-submit:disabled {
  background: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.search-submit:disabled:hover {
  background: #cccccc;
  color: #ffffff;
  box-shadow: none;
}

.search-submit:disabled .star-1,
.search-submit:disabled .star-2,
.search-submit:disabled .star-3,
.search-submit:disabled .star-4,
.search-submit:disabled .star-5,
.search-submit:disabled .star-6 {
  display: none;
}

input[type="date"],
select {
  background-color: transparent;
  color: #ffffff;
  font-family: "EliceDigitalBaeum_Regular";
}

input[type="date"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}

select option {
  background-color: #333;
  color: #ffffff;
}

.custom-placeholder {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  pointer-events: none;
}
</style>
