<template>
  <div class="layout-container">
    <navBar />
    <div class="content-wrapper" :class="{ collapsed: isCollapsed }">
      <div class="steps-sidebar">
        <div class="steps-nav">
          <div class="step" :class="{ active: isStep1Active }" @click="toggleStep1">
            <div class="step-number">STEP 1</div>
            <div class="step-title">날짜 선택</div>
          </div>

          <div class="step" @click="checkDateAndNavigate">
            <div class="step-number">STEP 2</div>
            <div class="step-title">장소 선택</div>
          </div>

          <div class="step" @click="checkAndNavigateToSavePlan">
            <div class="step-number">STEP 3</div>
            <div class="step-title">계획 생성</div>
          </div>
        </div>
      </div>

      <div class="middle-section">
        <div class="toggle-button" @click="toggleCollapse">
          <i class="fa-solid fa-arrow-left"></i>
        </div>

        <div class="header">
          <h2>{{ name }}</h2>
          <p v-if="formattedDateRange" class="date-range">
            {{ formattedDateRange }}
          </p>
        </div>

        <div class="date-form">
          <div class="input-group">
            <label>출발 일자</label>
            <input type="date" v-model="startDate" class="date-input" />
          </div>

          <div class="input-group">
            <label>도착 일자</label>
            <input type="date" v-model="endDate" class="date-input" />
          </div>

          <button @click="savePlan" class="save-button">저장</button>
        </div>
      </div>

      <div class="map-container">
        <Tmap ref="tmap" :latitude="latitude" :longitude="longitude" :selectedPlaces="[]" />
      </div>
    </div>
  </div>
</template>

<script>
import navBar from "@/components/navBar.vue";
import Tmap from "@/components/Tmap/Tmap.vue"; // Tmap 컴포넌트 import
import { usePlanStore } from "@/store/planStore";

export default {
  name: "ChooseDate",
  components: {
    navBar,
    Tmap,
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    id: {
      type: String,
      required: true,
    },
    latitude: {
      type: Number,
      required: true,
    },
    longitude: {
      type: Number,
      required: true,
    },
  },

  setup() {
    const planStore = usePlanStore();
    return { planStore };
  },

  data() {
    return {
      map: null,
      markers: [],
      polylines: [],
      isCollapsed: false,
      isStep1Active: true,
    };
  },

  // 계산된 속성 정의
  computed: {
    // 시작 날짜의 getter와 setter
    startDate: {
      get() {
        return this.planStore.dates.startDate;
      },
      set(value) {
        this.planStore.setDates(value, this.planStore.dates.endDate);
      },
    },
    // 종료 날짜의 getter와 setter
    endDate: {
      get() {
        return this.planStore.dates.endDate;
      },
      set(value) {
        this.planStore.setDates(this.planStore.dates.startDate, value);
      },
    },
    // 형식화된 날짜 범위를 반환하는 computed 속성
    formattedDateRange() {
      return this.planStore.dates.formattedDateRange;
    },
  },

  watch: {
    startDate: {
      handler: "updateFormattedDateRange",
      immediate: true,
    },
    endDate: {
      handler: "updateFormattedDateRange",
      immediate: true,
    },
  },
  methods: {
    updateFormattedDateRange() {
      if (this.startDate && this.endDate) {
        const formattedStart = this.formatDate(this.startDate);
        const formattedEnd = this.formatDate(this.endDate);
        this.formattedDateRange = `${formattedStart} - ${formattedEnd}`;
      } else {
        this.formattedDateRange = "";
      }
    },
    // 장소 선택 후 계획 생성 페이지로 이동
    checkAndNavigateToSavePlan() {
      if (!this.startDate || !this.endDate) {
        alert("날짜를 먼저 선택해 주세요!");
        return;
      }
      if (!this.selectedPlaces || this.selectedPlaces.length === 0) {
        alert("장소를 먼저 선택해주세요!");
        return;
      }

      this.$router.push({
        name: "savePlan",
        params: {
          name: this.name,
          selectedPlaces: this.selectedPlaces,
        },
        query: {
          startDate: this.startDate,
          endDate: this.endDate,
          formattedDateRange: this.formattedDateRange,
          id: this.id,
        },
      });
    },
    // 날짜 선택 후 장소 선택 페이지로 이동
    checkDateAndNavigate() {
      // store에서 날짜 정보 가져오기
      const startDate = this.planStore.dates.startDate;
      const endDate = this.planStore.dates.endDate;

      if (!startDate || !endDate) {
        alert("날짜를 먼저 선택해 주세요!");
        return;
      }

      // 장소 선택 페이지로 이동
      this.$router.push({
        name: "choosePlace",
        params: {
          name: this.name,
        },
        query: {
          id: this.planStore.selectedDestination.areaCode,
        },
      });
    },

    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
      setTimeout(() => {
        this.updateMapSize();
      }, 300);
    },

    toggleStep1() {
      this.isStep1Active = !this.isStep1Active;
      if (!this.isStep1Active) {
        this.isCollapsed = true;
      } else {
        this.isCollapsed = false;
      }
      setTimeout(() => {
        this.updateMapSize();
      }, 300);
    },

    formatDate(dateString) {
      const date = new Date(dateString);
      const days = ["일", "월", "화", "수", "목", "금", "토"];
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const dayOfWeek = days[date.getDay()];

      return `${year}.${month}.${day}(${dayOfWeek})`;
    },

    savePlan() {
      // store에서 날짜 정보 가져오기
      const startDate = this.planStore.dates.startDate;
      const endDate = this.planStore.dates.endDate;

      if (!startDate || !endDate) {
        alert("출발 일자와 도착 일자를 모두 선택해주세요.");
        return;
      }

      // 장소 선택 페이지로 이동
      this.$router.push({
        name: "choosePlace",
        params: {
          name: this.name,
        },
        query: {
          id: this.planStore.selectedDestination.areaCode,
        },
      });
    },

    updateMapSize() {
      if (this.$refs.tmap) {
        this.$refs.tmap.getMap().resize();
      }
    },
  },
  mounted() {
    // updateMapSize 이벤트 리스너 추가
    window.addEventListener("resize", this.updateMapSize);

    // 초기 맵 사이즈 설정
    setTimeout(() => {
      this.updateMapSize();
    }, 100);
  },

  beforeUnmount() {
    window.removeEventListener("resize", this.updateMapSize);
  },
};
</script>

<style scoped>
/* 기본 레이아웃 컨테이너 - 전체 페이지 래퍼 */
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: none;
  overflow: hidden;
}

/* 메인 컨텐츠 영역 - 사이드바, 중앙 섹션, 지도 컨테이너를 포함하는 그리드 */
.content-wrapper {
  display: grid;
  grid-template-columns: 107px 370px minmax(0, 1fr); /* 왼쪽 사이드바, 중앙 섹션, 오른쪽 지도 영역 */
  grid-template-rows: 1fr;
  height: calc(100vh - 64px); /* 네비게이션 바 높이 제외 */
  overflow: hidden;
  transition: all 0.3s ease;
  gap: 0;
}

/* 중앙 섹션이 접혔을 때의 그리드 레이아웃 */
.content-wrapper.collapsed {
  grid-template-columns: 200px 0 1fr;
}

/* 사이드바와 중앙 섹션의 공통 스타일 */
.steps-sidebar,
.middle-section {
  background: white;
  padding: 10px;
  height: 100%;
}

/* 중앙 섹션 기본 스타일 */
.middle-section {
  position: relative;
  min-width: 0;
}

/* 중앙 섹션이 접혔을 때의 스타일 */
.content-wrapper.collapsed .middle-section {
  padding: 0;
  overflow: hidden;
  opacity: 0;
}

/* 단계 네비게이션 영역 스타일 */
.steps-nav {
  text-align: center;
  font-family: "EliceDigitalBaeum_Regular";
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 각 단계 버튼 스타일 */
.step {
  display: flex;
  flex-direction: column;
  padding: 16px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  border-radius: 8px;
  transition: all 0.3s ease;
}

/* 단계 번호와 제목의 공통 스타일 */
.step-number,
.step-title {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 14px;
  color: #B4B4B4;
}

.step-number {
  margin-bottom: 4px;
}

/* 단계 버튼 호버/활성화 상태 스타일 */
.step:hover .step-number,
.step:hover .step-title,
.step.active .step-number,
.step.active .step-title {
  color: #ECB27B;
}

/* 접기/펼치기 토글 버튼 스타일 */
.toggle-button {
  position: absolute;
  top: 20px;
  right: 10px;
  width: 32px;
  height: 32px;
  background-color: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  transition: transform 0.3s ease;
}

.toggle-button i {
  font-size: 18px;
  color: #666;
  transition: color 0.3s ease;
}

.toggle-button:hover i {
  color: #ECB27B;
}

/* 헤더 영역 스타일 */
.header {
  margin-bottom: 24px;
}

.header h2 {
  font-family: "EliceDigitalBaeum_bold";
  font-size: 32px;
  margin-bottom: 8px;
}

.date-range {
  font-family: "EliceDigitalBaeum_regular";
  color: #ECB27B;
  font-size: 14px;
}

/* 날짜 입력 폼 스타일 */
.input-group {
  color: #000000;
  margin-bottom: 16px;
}

/* 입력 필드 레이블 스타일 */
.input-group label {
  font-family: "EliceDigitalBaeum_regular";
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
}

/* 날짜 입력 필드 스타일 */
.date-input {
  width: 100%;
  padding: 11px;
  border: 1px solid #ddd;
  border-radius: 15px;
  font-family: "EliceDigitalBaeum_regular";
  font-size: 14px;
  color: #B4B4B4;
  background-color: #fff;
  transition: all 0.3s ease;
  text-align: center;
}

/* 날짜 입력 필드 포커스/호버 상태 */
.date-input:focus {
  outline: none;
  border-color: #ECB27B;
  box-shadow: 0 0 0 2px rgba(245, 124, 0, 0.1);
}

.date-input:hover {
  border-color: #ECB27B;
}

/* 저장 버튼 스타일 */
.save-button {
  width: 100%;
  padding: 12px;
  background-color: #ECB27B;
  color: white;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
  margin-top: 40px;
}

.save-button:hover {
  background-color: #6e6156;
}

/* 지도 컨테이너 스타일 */
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
}

/* 지도 래퍼 컴포넌트 스타일 (하위 컴포넌트 스타일 설정) */
.map-container :deep(.map-wrapper) {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
}
</style>
