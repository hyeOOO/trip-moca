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
            <Datepicker
              v-model="startDate"
              :min-date="new Date()"
              :enable-time-picker="false"
              :format="formatDate"
              :auto-apply="true"
              locale="ko"
              :clearable="false"
              @update:model-value="updateStartDate"
              menuClassName="custom-datepicker-menu"
              :dark="true"
            >
              <template #trigger>
                <div class="date-trigger" :class="{ 'has-value': startDate }">
                  {{ startDate ? formatDate(startDate) : "" }}
                </div>
              </template>
            </Datepicker>
          </div>

          <div class="input-group">
            <label>도착 일자</label>
            <Datepicker
              v-model="endDate"
              :min-date="startDate || new Date()"
              :enable-time-picker="false"
              :format="formatDate"
              :auto-apply="true"
              locale="ko"
              :clearable="false"
              menuClassName="custom-datepicker-menu"
              :dark="true"
            >
              <template #trigger>
                <div class="date-trigger" :class="{ 'has-value': endDate }">
                  {{ endDate ? formatDate(endDate) : "" }}
                </div>
              </template>
            </Datepicker>
          </div>

          <button @click="savePlan" class="save-button" :disabled="!isFormValid">저장</button>
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
import Tmap from "@/components/Tmap/Tmap.vue";
import { usePlanStore } from "@/store/planStore";
import Datepicker from "@vuepic/vue-datepicker";
import "@vuepic/vue-datepicker/dist/main.css";

export default {
  name: "ChooseDate",
  components: {
    navBar,
    Tmap,
    Datepicker,
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
        return this.planStore.dates.startDate ? new Date(this.planStore.dates.startDate) : null;
      },
      set(value) {
        this.planStore.setDates(
          value ? this.formatDate(value) : null,
          this.planStore.dates.endDate
        );
      },
    },
    // 종료 날짜의 getter와 setter
    endDate: {
      get() {
        return this.planStore.dates.endDate ? new Date(this.planStore.dates.endDate) : null;
      },
      set(value) {
        this.planStore.setDates(
          this.planStore.dates.startDate,
          value ? this.formatDate(value) : null
        );
      },
    },
    // 형식화된 날짜 범위를 반환하는 computed 속성
    formattedDateRange() {
      return this.planStore.dates.formattedDateRange;
    },

    isFormValid() {
      return this.startDate && this.endDate && new Date(this.endDate) >= new Date(this.startDate);
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

    formatDate(date) {
      if (!date) return "";
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },

    updateStartDate(newDate) {
      if (this.endDate && new Date(this.endDate) < new Date(newDate)) {
        this.endDate = null;
      }
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
  color: #b4b4b4;
}

.step-number {
  margin-bottom: 4px;
}

/* 단계 버튼 호버/활성화 상태 스타일 */
.step:hover .step-number,
.step:hover .step-title,
.step.active .step-number,
.step.active .step-title {
  color: #ecb27b;
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
  color: #ecb27b;
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
  font-family: "EliceDigitalBaeum_bold";
  color: #ecb27b;
  font-size: 14px;
}

/* 날짜 입력 폼 스타일 */
.input-group {
  color: #000000;
  margin-bottom: 16px;
}

/* 입력 필드 레이블 스타일 */
.input-group label {
  font-family: "Pretendard-Medium";
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
  color: #b4b4b4;
  background-color: #fff;
  transition: all 0.3s ease;
  text-align: center;
}

/* 날짜 입력 필드 포커스/호버 상태 */
.date-input:focus {
  outline: none;
  border-color: #ecb27b;
  box-shadow: 0 0 0 2px rgba(245, 124, 0, 0.1);
}

.date-input:hover {
  border-color: #ecb27b;
}

/* 저장 버튼 스타일 */
.save-button {
  width: 100%;
  padding: 12px;
  background-color: #ecb27b;
  color: white;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
  margin-top: 40px;
  font-family: "Pretendard-Medium";
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

:deep(.custom-datepicker-menu) {
  background-color: #2a2a2a !important;
  border: 1px solid #3a3a3a !important;
  border-radius: 6px !important;
  font-family: "EliceDigitalBaeum_Regular" !important;
}

:deep(.dp__main) {
  font-family: var(--dp-font-family);
  user-select: none;
  box-sizing: border-box;
  position: relative;
  width: 100%;
  margin-bottom: 20px;
}

:deep(.dp__active_date) {
  background-color: #ecb27b !important;
  color: #ffffff !important;
}

:deep(.dp__date_hover) {
  background-color: rgba(236, 178, 123, 0.2) !important;
}

:deep(.dp__today) {
  border: 1px solid #ecb27b !important;
}

:deep(.dp__arrow_bottom) {
  border-top: 2px solid #ffffff !important;
  border-right: 2px solid #ffffff !important;
}

:deep(.dp__month_year_select) {
  color: #ffffff !important;
}

:deep(.dp__menu_index) {
  font-family: "Pretendard-Light";
}

:deep(.dp__calendar_header) {
  color: #ffffff !important;
}

:deep(.dp__cell_inner) {
  color: #ffffff !important;
}

:deep(.dp__disabled) {
  color: #666666 !important;
}

.date-trigger {
  width: 100%;
  height: 48px;
  background-color: transparent;
  border: 1px solid #ddd;
  border-radius: 15px;
  color: #b4b4b4;
  font-family: "EliceDigitalBaeum_Regular";
  padding: 0 12px;
  font-size: 14px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  justify-content: center;
}

.date-trigger.has-value {
  color: #000000;
}

.date-trigger:hover {
  border-color: #ecb27b;
}

:deep(.dp__input) {
  color: #000000 !important;
}

:deep(.dp__input_icon) {
  color: #000000 !important;
}

:deep(.dp__overlay) {
  background-color: rgba(0, 0, 0, 0.7) !important;
}
</style>
