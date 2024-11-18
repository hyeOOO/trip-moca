<template>
  <div class="layout-container">
    <navBar />
    <div class="content-wrapper" :class="{ collapsed: isCollapsed }">
      <div class="steps-sidebar">
        <div class="steps-nav">
          <div
            class="step"
            :class="{ active: isStep1Active }"
            @click="toggleStep1"
          >
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
        <Tmap
          ref="tmap"
          :latitude="latitude"
          :longitude="longitude"
          :selectedPlaces="[]"
        />
      </div>
    </div>
  </div>
</template>

<script>
import navBar from "@/components/navBar.vue";
import Tmap from "@/components/Tmap.vue"; // Tmap 컴포넌트 import

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
  data() {
    return {
      startDate: "",
      endDate: "",
      map: null,
      markers: [],
      polylines: [],
      formattedDateRange: "",
      isCollapsed: false,
      isStep1Active: true,
    };
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
          selectedPlaces: this.selectedPlaces
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
      if (!this.startDate || !this.endDate) {
        alert("날짜를 먼저 선택해 주세요!");
        return;
      }

      const formattedStart = this.formatDate(this.startDate);
      const formattedEnd = this.formatDate(this.endDate);
      this.formattedDateRange = `${formattedStart} - ${formattedEnd}`;

      this.$router.push({
        name: "choosePlace",
        params: {
          name: this.name,
        },
        query: {
          startDate: this.startDate,
          endDate: this.endDate,
          formattedDateRange: this.formattedDateRange,
          id: this.id,
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
      if (!this.startDate || !this.endDate) {
        alert("출발 일자와 도착 일자를 모두 선택해주세요.");
        return;
      }

      const formattedStart = this.formatDate(this.startDate);
      const formattedEnd = this.formatDate(this.endDate);
      this.formattedDateRange = `${formattedStart} - ${formattedEnd}`;

      this.$router.push({
        path: `/choosePlace/${this.name}`,
        query: {
          startDate: this.startDate,
          endDate: this.endDate,
          formattedDateRange: this.formattedDateRange,
          id: this.id,
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
/* Base layout */
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 200px 320px 1fr;
  grid-template-rows: 1fr;
  height: calc(100vh - 64px);
  overflow: hidden;
  transition: all 0.3s ease;
  gap: 0;
}

.content-wrapper.collapsed {
  grid-template-columns: 200px 0 1fr;
}

/* Common section styles */
.steps-sidebar,
.middle-section {
  background: white;
  padding: 24px;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-right: 1px solid #eee;
  height: 100%;
  overflow-y: auto;
}

/* Step styles */
.steps-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

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

.step:hover,
.step.active {
  background-color: #f5f5f5;
}

.step-number {
  font-size: 14px;
  font-weight: bold;
  color: #f57c00;
  margin-bottom: 4px;
}

.step-title {
  font-size: 16px;
}

/* Middle section styles */
.middle-section {
  position: relative;
  min-width: 0;
}

.content-wrapper.collapsed .middle-section {
  padding: 0;
  overflow: hidden;
  opacity: 0;
}

/* Toggle button styles */
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

.toggle-button:hover {
  transform: scale(1.1);
}

.toggle-button i {
  font-size: 18px;
  color: #666;
  transition: color 0.3s ease;
}

.toggle-button:hover i {
  color: #f57c00;
}

/* Header styles */
.header {
  margin-bottom: 24px;
}

.header h2 {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.date-range {
  color: #f57c00;
  font-size: 14px;
}

/* Form styles */
.input-group {
  margin-bottom: 16px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.date-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  background-color: #fff;
  transition: all 0.3s ease;
}

.date-input:focus {
  outline: none;
  border-color: #f57c00;
  box-shadow: 0 0 0 2px rgba(245, 124, 0, 0.1);
}

.date-input:hover {
  border-color: #bbb;
}

/* Button styles */
.save-button {
  width: 100%;
  padding: 12px;
  background-color: #333;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
}

.save-button:hover {
  background-color: #555;
}

/* Map container styles */
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

.map-container :deep(.map-wrapper) {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
}
</style>