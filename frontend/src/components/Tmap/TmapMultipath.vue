숫자핑이랑 경로 찍어주는 Tmap
<template>
  <div class="map-wrapper">
    <div id="map_div"></div>
  </div>
</template>

<script>
export default {
  name: "TmapMultipath",

  props: {
    selectedPlacesByDay: {
      type: Object,
      default: () => ({}),
    },
    latitude: {
      type: Number,
      required: true,
    },
    longitude: {
      type: Number,
      required: true,
    },
    selectedDay: {
      type: [Number, String],
      default: "all",
    },
    showAllDays: {
      type: Boolean,
      default: true,
    },
  },

  data() {
    return {
      map: null,
      markersByDay: {},
      polylinesByDay: {},
      defaultZoom: 11,
      currentInfoWindow: null,
      routePolylines: {},
      tData: null,
      activeMarkers: [], // 현재 활성화된 마커들을 추적
      activeRoutes: [], // 현재 활성화된 경로들을 추적
    };
  },

  watch: {
    selectedPlacesByDay: {
      handler(newPlaces) {
        try {
          this.clearAllVisuals();
          this.updateMarkers(newPlaces);
          this.updateRoutes(newPlaces);
          this.visualizeForDay(this.selectedDay);
        } catch (err) {
          console.warn("Error updating places:", err);
        }
      },
      deep: true,
    },
    selectedDay: {
      handler(newDay) {
        try {
          this.clearAllVisuals();
          this.visualizeForDay(newDay);
        } catch (err) {
          console.warn("Error handling day change:", err);
        }
      },
      immediate: true,
    },
    showAllDays: {
      handler(show) {
        try {
          if (show) {
            this.visualizeForDay('all');
          }
        } catch (err) {
          console.warn("Error handling show all:", err);
        }
      }
    },
  },

  mounted() {
    this.initializeMap();
    if (Object.keys(this.selectedPlacesByDay).length > 0) {
      this.updateMarkers(this.selectedPlacesByDay);
      this.updateRoutes(this.selectedPlacesByDay);
      this.visualizeForDay(this.selectedDay);
    }
  },

  methods: {
    initializeMap() {
      if (this.map) {
        this.map = null;
      }

      this.map = new Tmapv2.Map("map_div", {
        center: new Tmapv2.LatLng(this.latitude, this.longitude),
        width: "100%",
        height: "100%",
        zoom: this.defaultZoom,
        httpsMode: true,
        zoomControl: true,
        scrollwheel: true,
        draggable: true,
      });

      this.map.addListener("bounds_changed", () => {
        console.log("Map bounds changed");
      });

      this.tData = new Tmapv2.extension.TData();
    },

    clearAllVisuals() {
      if (this.currentInfoWindow) {
        this.currentInfoWindow.setMap(null);
        this.currentInfoWindow = null;
      }

      // 활성화된 마커들 제거
      this.activeMarkers.forEach(marker => {
        if (marker && marker.setMap) {
          marker.setMap(null);
        }
      });
      this.activeMarkers = [];

      // 활성화된 경로들 제거
      this.activeRoutes.forEach(route => {
        if (route && route.setMap) {
          route.setMap(null);
        }
      });
      this.activeRoutes = [];
    },

    visualizeForDay(dayIndex) {
      this.clearAllVisuals();

      if (dayIndex === 'all') {
        // 모든 날짜 표시
        Object.entries(this.markersByDay).forEach(([day, markers]) => {
          markers.forEach(marker => {
            marker.setMap(this.map);
            this.activeMarkers.push(marker);
          });
        });
        Object.values(this.routePolylines).forEach(routes => {
          routes.forEach(route => {
            route.setMap(this.map);
            this.activeRoutes.push(route);
          });
        });
      } else if (dayIndex && this.markersByDay[dayIndex]) {
        // 특정 날짜 표시
        this.markersByDay[dayIndex].forEach(marker => {
          marker.setMap(this.map);
          this.activeMarkers.push(marker);
        });
        if (this.routePolylines[dayIndex]) {
          this.routePolylines[dayIndex].forEach(route => {
            route.setMap(this.map);
            this.activeRoutes.push(route);
          });
        }
      }
    },

    async calculateRoute(places, day) {
      if (places.length < 2) return;

      const routeColors = [
        "#FF6B6B", "#6BCB77", "#B983FF", "#FF9F45", "#4CACBC",
        "#FF8FB1", "#95CD41", "#7B2869", "#FFD93D"
      ];

      const validPlaces = places.filter(place =>
        place.latitude && place.longitude &&
        !isNaN(Number(place.latitude)) && !isNaN(Number(place.longitude))
      );

      if (validPlaces.length < 2) return;

      // TMap API 제한을 고려한 경로 분할 처리
      const MAX_WAYPOINTS = 5;
      const segments = [];
      for (let i = 0; i < validPlaces.length - 1; i += MAX_WAYPOINTS) {
        const segmentPlaces = validPlaces.slice(i, Math.min(i + MAX_WAYPOINTS + 1, validPlaces.length));
        if (segmentPlaces.length >= 2) {
          segments.push(segmentPlaces);
        }
      }

      const dayRoutes = [];

      for (const segment of segments) {
        const start = segment[0];
        const end = segment[segment.length - 1];
        const waypoints = segment.slice(1, -1);

        const passList = waypoints
          .map(place => `${place.longitude},${place.latitude}`)
          .join("_");

        try {
          const response = await new Promise((resolve, reject) => {
            this.tData.getRoutePlanJson(
              new Tmapv2.LatLng(start.latitude, start.longitude),
              new Tmapv2.LatLng(end.latitude, end.longitude),
              {
                reqCoordType: "WGS84GEO",
                resCoordType: "WGS84GEO",
                passList: passList,
                trafficInfo: "Y",
              },
              {
                onComplete: resolve,
                onProgress: () => {},
                onError: reject,
              }
            );
          });

          if (response._responseData?.features) {
            response._responseData.features.forEach(feature => {
              if (feature?.geometry?.type === "LineString" && feature.geometry.coordinates) {
                const path = feature.geometry.coordinates.map(
                  coord => new Tmapv2.LatLng(coord[1], coord[0])
                );

                const polyline = new Tmapv2.Polyline({
                  path: path,
                  strokeColor: routeColors[(day - 1) % routeColors.length],
                  strokeWeight: 5,
                  strokeStyle: "solid",
                  map: null,
                });

                dayRoutes.push(polyline);
              }
            });
          }
        } catch (err) {
          console.warn("Route segment calculation error:", err);
        }
      }

      this.routePolylines[day] = dayRoutes;
    },

    async updateRoutes(places) {
      const currentRoutes = { ...this.routePolylines };
      this.routePolylines = {};

      for (const [day, dayPlaces] of Object.entries(places)) {
        if (dayPlaces.length >= 2) {
          await this.calculateRoute(dayPlaces, day);
        }
      }

      // 이전 경로들 제거
      Object.values(currentRoutes).forEach(routes => {
        routes.forEach(route => route?.setMap(null));
      });
    },

    createMarker(place, dayIndex, placeIndex) {
      if (!place.latitude || !place.longitude || 
          isNaN(Number(place.latitude)) || isNaN(Number(place.longitude))) {
        return null;
      }

      const position = new Tmapv2.LatLng(
        Number(place.latitude),
        Number(place.longitude)
      );

      const colors = [
        "red", "green", "purple", "orange", "mint",
        "pink", "lime", "magenta", "yello"
      ];
      const adjustedDayIndex = dayIndex - 1;
      const currentColor = colors[adjustedDayIndex % colors.length];
      const markerNumber = (placeIndex % 9) + 1;
      const markerImageUrl = `https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/${currentColor}${markerNumber}.png`;

      const marker = new Tmapv2.Marker({
        position: position,
        icon: markerImageUrl,
        iconSize: new Tmapv2.Size(24, 24),
        map: null,
        title: place.title,
        zIndex: dayIndex + 1,
      });

      marker.addListener("click", () => {
        if (this.currentInfoWindow) {
          this.currentInfoWindow.setMap(null);
        }

        this.currentInfoWindow = new Tmapv2.InfoWindow({
          position: position,
          content: `
            <div style="padding:10px;min-width:150px;background-color:white;border-radius:5px;">
              <div style="font-weight:bold;margin-bottom:5px;">${place.title}</div>
              <div style="font-size:12px;">${dayIndex}일차</div>
            </div>
          `,
          type: 2,
          map: this.map,
          border: "0px solid #FF0000",
          background: false,
        });
      });

      return marker;
    },

    updateMarkers(places) {
      const currentMarkers = { ...this.markersByDay };
      this.markersByDay = {};

      let centerPosition = null;

      Object.entries(places).forEach(([day, dayPlaces]) => {
        this.markersByDay[day] = [];

        dayPlaces.forEach((place, placeIndex) => {
          const marker = this.createMarker(place, parseInt(day), placeIndex);
          if (marker) {
            this.markersByDay[day].push(marker);
            if (!centerPosition) {
              centerPosition = marker.getPosition();
            }
          }
        });
      });

      // 이전 마커들 제거
      Object.values(currentMarkers).forEach(markers => {
        markers.forEach(marker => marker?.setMap(null));
      });

      if (centerPosition) {
        this.map.setCenter(centerPosition);
        this.map.setZoom(this.defaultZoom);
      }
    },
  },

  beforeUnmount() {
    this.clearAllVisuals();
    this.map = null;
  },
};
</script>

<style scoped>
.map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

#map_div {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
}
</style>
