<template>
  <div class="choose-date">
    <!-- Left panel with step instructions and date inputs -->
    <div class="panel">
      <h2>{{ name }}</h2>
      <p class="date-range">2024.12.05(목) - 2024.12.08(일)</p>

      <div class="step">
        <h3>STEP 1 날짜 선택</h3>
        <label>출발 일자</label>
        <input type="date" v-model="startDate" />
      </div>

      <div class="step">
        <h3>STEP 2 장소 선택</h3>
        <label>도착 일자</label>
        <input type="date" v-model="endDate" />
      </div>

      <div class="step">
        <h3>STEP 3 계획 생성</h3>
        <button @click="savePlan">저장</button>
      </div>
    </div>

    <!-- Right panel with TMap -->
    <div class="map-container" ref="tmap"></div>
  </div>
</template>
    
  <script>
export default {
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

  //   props: {
  //     name: String,
  //     id: String,
  //     latitude: {
  //       type: Number,
  //       default: 37.5666805, // 서울 기본값
  //     },
  //     longitude: {
  //       type: Number,
  //       default: 126.9784147, // 서울 기본값
  //     },
  //   },
  data() {
    return {
      startDate: "",
      endDate: "",
      map: null,
      markers: [], // 마커들을 저장할 배열
      polylines: [], // 폴리라인들을 저장할 배열
    };
  },
  watch: {
    // latitude나 longitude가 변경될 때 지도 중심 업데이트
    latitude: "updateMapCenter",
    longitude: "updateMapCenter",
  },
  methods: {
    initializeMap() {
      try {
        if (!window.Tmapv2) {
          console.error("TMap API가 로드되지 않았습니다.");
          return;
        }

        // TMap 초기화
        const option = {
          center: new window.Tmapv2.LatLng(this.latitude, this.longitude),
          width: "100%",
          height: "100%",
          zoom: 12,
        };

        // 기존 지도 제거
        if (this.map) {
          this.clearMap();
        }

        this.map = new window.Tmapv2.Map(this.$refs.tmap, option);
      } catch (error) {
        console.error("지도 초기화 중 오류 발생:", error);
      }
    },
    updateMapCenter() {
      if (this.map && window.Tmapv2) {
        try {
          const newCenter = new window.Tmapv2.LatLng(
            this.latitude,
            this.longitude
          );
          this.map.setCenter(newCenter);
          this.map.setZoom(12);
        } catch (error) {
          console.error("지도 중심 업데이트 중 오류 발생:", error);
        }
      }
    },
    clearMap() {
      // 기존 마커들 제거
      this.markers.forEach((marker) => marker.setMap(null));
      this.markers = [];

      // 기존 폴리라인들 제거
      this.polylines.forEach((polyline) => polyline.setMap(null));
      this.polylines = [];

      // 지도 객체 제거
      if (this.map) {
        this.map.destroy();
        this.map = null;
      }
    },
    savePlan() {
      alert(`Plan saved for dates: ${this.startDate} to ${this.endDate}`);
    },
    sendIdToBackend(id) {
      fetch("백엔드_URL", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ id }),
      })
        .then((response) => response.json())
        .then((data) => console.log(data))
        .catch((error) => console.error("Error:", error));
    },
  },
  mounted() {
    // 컴포넌트 마운트 시 지도 초기화
    this.$nextTick(() => {
      this.initializeMap();
    });
  },
  beforeUnmount() {
    // 컴포넌트 제거 시 지도 정리
    this.clearMap();
  },
};
</script>
  
  <style scoped>
.choose-date {
  display: flex;
  width: 100%;
  height: 100vh;
}

.panel {
  width: 300px;
  padding: 20px;
  background-color: #fff;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.panel h2 {
  font-size: 24px;
  margin-bottom: 10px;
}

.date-range {
  color: #f57c00;
  margin-bottom: 20px;
}

.step {
  margin-bottom: 15px;
}

.step h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 5px;
}

.step label {
  display: block;
  margin-bottom: 5px;
}

.step input[type="date"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  color: #fff;
  background-color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #555;
}

.map-container {
  flex-grow: 1;
  height: 100%;
  background-color: #e0f7fa;
}
</style>