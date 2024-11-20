<template>
  <div ref="mapContainer" class="map-container">
    <div id="map_div" style="width: 100%; height: 100%"></div>
    <button
      class="current-location-btn"
      @click="getCurrentLocation"
      :disabled="isLoadingLocation"
    >
      <i class="fa-solid fa-location-crosshairs"></i>
    </button>
    <button
      class="traffic-btn"
      @click="toggleTraffic"
      :class="{ active: isTrafficVisible }"
    >
      <i class="fa-solid fa-car"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

const mapContainer = ref(null);
const map = ref(null);
const tData = ref(null);
const markerStart = ref(null);
const markerEnd = ref(null);
const lineArr = ref([]);
const labelArr = ref([]);
const isLoadingLocation = ref(false);
const trafficLayer = ref(null);
const isTrafficVisible = ref(false);

const startCoords = ref({ lat: null, lng: null });
const endCoords = ref({ lat: null, lng: null });

const waitForTmap = () => {
  return new Promise((resolve) => {
    const checkTmap = () => {
      if (window.Tmapv2) {
        resolve();
      } else {
        setTimeout(checkTmap, 100);
      }
    };
    checkTmap();
  });
};

onMounted(async () => {
  try {
    await waitForTmap();
    initializeMap();
  } catch (error) {
    console.error("지도 초기화 중 오류 발생:", error);
  }
});

const initializeMap = () => {
  map.value = new window.Tmapv2.Map("map_div", {
    center: new window.Tmapv2.LatLng(37.566481622437934, 126.98502302169841),
    width: "100%",
    height: "100%",
    zoom: 15,
    httpsMode: true,
  });

  tData.value = new window.Tmapv2.extension.TData();
  trafficLayer.value = new window.Tmapv2.extension.TrafficInfo({
    map: map.value,
  });
  trafficLayer.value.hide();
};

const toggleTraffic = () => {
  if (!trafficLayer.value) return;
  isTrafficVisible.value ? trafficLayer.value.hide() : trafficLayer.value.show();
  isTrafficVisible.value = !isTrafficVisible.value;
};

const emit = defineEmits(["locationSelected"]);

const getCurrentLocation = async () => {
  try {
    isLoadingLocation.value = true;
    const samsungLocation = {
      name: "삼성전기 부산사업장 주차장",
      address: "부산광역시 강서구 녹산산업중로 333",
      lat: 35.0936583,
      lng: 128.8526144,
    };

    alert("현재 위치로 이동합니다.");
    map.value.setCenter(
      new window.Tmapv2.LatLng(samsungLocation.lat, samsungLocation.lng)
    );
    map.value.setZoom(15);
    setLocationMarker(samsungLocation, "start");
    emit("locationSelected", samsungLocation);
  } catch (error) {
    console.error("Error setting location:", error);
    alert("위치 설정 중 오류가 발생했습니다.");
  } finally {
    isLoadingLocation.value = false;
  }
};

const searchLocationSuggestions = async (keyword) => {
  if (!tData.value || !keyword || keyword.length < 2) return [];

  return new Promise((resolve) => {
    const optionObj = {
      resCoordType: "WGS84GEO",
      reqCoordType: "WGS84GEO",
      count: 10,
    };

    const params = {
      onComplete: function (result) {
        try {
          const resultpoisData = result._responseData.searchPoiInfo.pois.poi;
          if (resultpoisData) {
            const suggestions = resultpoisData.map((poi) => ({
              name: poi.name,
              address: poi.fullAddressRoad,
              lat: poi.noorLat,
              lng: poi.noorLon,
            }));
            resolve(suggestions);
          } else {
            resolve([]);
          }
        } catch (error) {
          console.error("POI 데이터 파싱 중 오류:", error);
          resolve([]);
        }
      },
      onProgress: function () {},
      onError: function (error) {
        console.error("POI 검색 중 오류 발생:", error);
        resolve([]);
      },
    };

    try {
      tData.value.getPOIDataFromSearchJson(keyword, optionObj, params);
    } catch (error) {
      console.error("POI API 호출 중 오류 발생:", error);
      resolve([]);
    }
  });
};

const setLocationMarker = (location, type) => {
  try {
    if (type === "start" && markerStart.value) {
      markerStart.value.setMap(null);
    } else if (type === "end" && markerEnd.value) {
      markerEnd.value.setMap(null);
    }

    if (!location || !location.lat || !location.lng) {
      console.error("유효하지 않은 위치 정보:", location);
      return;
    }

    const marker = new window.Tmapv2.Marker({
      position: new window.Tmapv2.LatLng(location.lat, location.lng),
      iconHTML: `<div class='_t_marker' style="position:relative;">
                  <i class="fa-solid fa-location-dot" style="color: ${
                    type === "start" ? "#4D96FF" : "#FF6B6B"
                  }; font-size: 38px;"></i>
                </div>`,
      offset: new window.Tmapv2.Point(12, 38),
      map: map.value,
    });

    if (type === "start") {
      markerStart.value = marker;
      startCoords.value = location;
    } else {
      markerEnd.value = marker;
      endCoords.value = location;
    }

    map.value.setCenter(new window.Tmapv2.LatLng(location.lat, location.lng));
  } catch (error) {
    console.error("마커 설정 중 오류 발생:", error);
  }
};

const calculateRoute = async (routeType = "car") => {
  try {
    if (!startCoords.value.lat || !endCoords.value.lat) {
      console.error("출발지와 도착지를 모두 선택해주세요.");
      return null;
    }

    clearMap();

    const startLatLng = new window.Tmapv2.LatLng(
      startCoords.value.lat,
      startCoords.value.lng
    );
    const endLatLng = new window.Tmapv2.LatLng(
      endCoords.value.lat,
      endCoords.value.lng
    );

    const distance = calculateDistance(
      startCoords.value.lat,
      startCoords.value.lng,
      endCoords.value.lat,
      endCoords.value.lng
    );

    if (routeType === "pedestrian" && distance > 10) {
      alert("도보 경로는 10km 이내만 검색 가능합니다.");
      return null;
    }

    const optionObj = {
      reqCoordType: "WGS84GEO",
      resCoordType: "WGS84GEO",
      trafficInfo: routeType === "car" ? "Y" : "N",
    };

    return new Promise((resolve) => {
      const callbacks = {
        onComplete: function (result) {
          if (
            !result ||
            !result._responseData ||
            !result._responseData.features
          ) {
            alert("경로를 찾을 수 없습니다.");
            resolve(null);
            return;
          }

          const resultData = result._responseData.features;

          if (resultData.length === 0) {
            alert("경로를 찾을 수 없습니다.");
            resolve(null);
            return;
          }

          drawRoute(resultData);
          setMarkers();

          const details = {
            totalTime: resultData[0].properties.totalTime,
            totalDistance: resultData[0].properties.totalDistance,
            taxiFare:
              routeType === "car" ? resultData[0].properties.taxiFare : null,
          };
          resolve(details);
        },
        onProgress: function () {},
        onError: function (error) {
          console.error(
            `${routeType} 경로 검색 중 오류가 발생했습니다:`,
            error
          );
          alert("경로 검색 중 오류가 발생했습니다.");
          resolve(null);
        },
      };

      try {
        if (routeType === "pedestrian") {
          tData.value.getRoutePlanForPeopleJson(
            startLatLng,
            endLatLng,
            "출발지",
            "도착지",
            optionObj,
            callbacks
          );
        } else {
          tData.value.getRoutePlanJson(
            startLatLng,
            endLatLng,
            optionObj,
            callbacks
          );
        }
      } catch (error) {
        console.error("경로 계산 중 오류 발생:", error);
        alert("경로 계산 중 오류가 발생했습니다.");
        resolve(null);
      }
    });
  } catch (error) {
    console.error("경로 계산 중 오류 발생:", error);
    alert("경로 계산 중 오류가 발생했습니다.");
    return null;
  }
};

const drawRoute = (resultData) => {
  if (!resultData || !Array.isArray(resultData)) {
    console.error("유효하지 않은 경로 데이터입니다.");
    return;
  }

  const positionBounds = new window.Tmapv2.LatLngBounds();
  const allPoints = [];

  // 경로 데이터에서 좌표 추출
  resultData.forEach((feature) => {
    if (feature.geometry.type === "LineString") {
      const points = feature.geometry.coordinates.map(
        (coord) => new window.Tmapv2.LatLng(coord[1], coord[0])
      );
      allPoints.push(...points);
      points.forEach((point) => positionBounds.extend(point));
    }
  });

  // 단일 폴리라인으로 경로 그리기
  const polyline = new window.Tmapv2.Polyline({
    path: allPoints,
    strokeColor: "#6BCB77",
    strokeWeight: 7,
    strokeOpacity: 1,
    map: map.value,
    strokeStyle: 'solid'
  });

  lineArr.value.push(polyline);

  // 경로 애니메이션
  let startLength = 0;
  const animationDuration = 4000; // 4초
  const startTime = performance.now();

  const animate = (currentTime) => {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / animationDuration, 1);
    
    // 현재까지의 경로 길이 계산
    const currentLength = Math.floor(allPoints.length * progress);
    
    if (progress < 1) {
      polyline.setPath(allPoints.slice(0, currentLength));
      requestAnimationFrame(animate);
    } else {
      polyline.setPath(allPoints);
    }
  };

  requestAnimationFrame(animate);
  map.value.panToBounds(positionBounds);
  map.value.zoomOut();
};

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
};

const setMarkers = () => {
  if (startCoords.value.lat) {
    setLocationMarker(startCoords.value, "start");
  }
  if (endCoords.value.lat) {
    setLocationMarker(endCoords.value, "end");
  }
};

const clearMap = () => {
  lineArr.value.forEach((line) => {
    if (line) line.setMap(null);
  });
  lineArr.value = [];

  labelArr.value.forEach((label) => {
    if (label) label.setMap(null);
  });
  labelArr.value = [];
};

defineExpose({
  searchLocationSuggestions,
  setLocationMarker,
  calculateRoute,
  getCurrentLocation,
  toggleTraffic,
});
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.current-location-btn,
.traffic-btn {
  position: absolute;
  left: 10px;
  width: 40px;
  height: 40px;
  background-color: white;
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  transition: all 0.2s ease;
}

.current-location-btn {
  bottom: 10px;
}

.traffic-btn {
  bottom: 60px;
}

.current-location-btn:hover,
.traffic-btn:hover {
  background-color: #f5f5f5;
}

.current-location-btn:disabled,
.traffic-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.current-location-btn i,
.traffic-btn i {
  font-size: 20px;
  color: #333;
}

.traffic-btn.active {
  background-color: #4d96ff;
}

.traffic-btn.active i {
  color: white;
}
</style>