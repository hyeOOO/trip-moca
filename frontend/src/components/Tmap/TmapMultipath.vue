숫자핑이랑 경로 찍어주는 Tmap
<template>
  <div class="map-wrapper">
    <div id="map_div"></div>
  </div>
</template>

<script>
export default {
  name: "TMapComponent",

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
      type: [Number, String], // String 타입 추가
      default: null,
    },
    showAllDays: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      map: null,
      markersByDay: {},
      polylinesByDay: {},
      defaultZoom: 11,
      currentInfoWindow: null,
      routePolylines: [],
      tData: null,
    };
  },

  watch: {
    selectedPlacesByDay: {
      handler(newPlaces) {
        this.updateMarkers(newPlaces);
        this.updateRoutes(newPlaces);
      },
      deep: true,
    },
    selectedDay(newDay) {
      if (this.currentInfoWindow) {
        this.currentInfoWindow.setMap(null);
        this.currentInfoWindow = null;
      }
      this.toggleMarkersByDay(newDay);
      this.toggleRoutes(newDay);
    },
    showAllDays(show) {
      if (this.currentInfoWindow) {
        this.currentInfoWindow.setMap(null);
        this.currentInfoWindow = null;
      }
      if (show) {
        this.showAllMarkers();
        this.showAllRoutes();
      }
    },
  },

  mounted() {
    this.initializeMap();
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
      });

      this.tData = new Tmapv2.extension.TData();
    },

    calculateDistance(lat1, lon1, lat2, lon2) {
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
    },

    async calculateRoute(places, day) {
      if (places.length < 2) return;

      const routeColors = [
        "#FF6B6B",
        "#6BCB77",
        "#B983FF",
        "#FF9F45",
        "#4CACBC",
        "#FF8FB1",
        "#95CD41",
        "#7B2869",
        "#FFD93D",
      ];

      const start = places[0];
      const end = places[places.length - 1];
      const waypoints = places.slice(1, -1);

      // 좌표 유효성 검사 추가
      if (
        !start.latitude ||
        !start.longitude ||
        !end.latitude ||
        !end.longitude
      ) {
        console.error("Invalid coordinates for route calculation");
        return;
      }

      // 경유지 목록 생성
      const passList = waypoints
        .filter((place) => place.latitude && place.longitude)
        .map((place) => `${place.longitude},${place.latitude}`)
        .join("_");

      const startLatLng = new Tmapv2.LatLng(start.latitude, start.longitude);
      const endLatLng = new Tmapv2.LatLng(end.latitude, end.longitude);

      const optionObj = {
        reqCoordType: "WGS84GEO",
        resCoordType: "WGS84GEO",
        passList: passList,
        trafficInfo: "Y",
      };

      return new Promise((resolve) => {
        const params = {
          onComplete: (response) => {
            try {
              const resultData = response._responseData?.features;
              if (!resultData) {
                console.error("No route data received");
                resolve();
                return;
              }

              const dayRoutes = [];

              resultData.forEach((feature) => {
                if (
                  feature?.geometry?.type === "LineString" &&
                  feature.geometry.coordinates
                ) {
                  try {
                    const path = feature.geometry.coordinates.map(
                      (coord) => new Tmapv2.LatLng(coord[1], coord[0])
                    );

                    const polyline = new Tmapv2.Polyline({
                      path: path,
                      strokeColor: routeColors[(day - 1) % routeColors.length],
                      strokeWeight: 5,
                      strokeStyle: "solid",
                      map: this.map,
                    });

                    dayRoutes.push(polyline);
                  } catch (err) {
                    console.error("Error creating polyline:", err);
                  }
                }
              });

              this.routePolylines[day] = dayRoutes;
              resolve();
            } catch (err) {
              console.error("Error processing route response:", err);
              resolve();
            }
          },
          onProgress: () => {},
          onError: (error) => {
            console.error("경로 검색 중 오류가 발생했습니다.", error);
            resolve();
          },
        };

        try {
          this.tData.getRoutePlanJson(
            startLatLng,
            endLatLng,
            optionObj,
            params
          );
        } catch (err) {
          console.error("Error calling getRoutePlanJson:", err);
          resolve();
        }
      });
    },
    clearRoutes() {
      Object.values(this.routePolylines).forEach((routes) => {
        routes.forEach((route) => {
          if (route) route.setMap(null);
        });
      });
      this.routePolylines = {};
    },

    async updateRoutes(places) {
      this.clearRoutes();

      for (const [day, dayPlaces] of Object.entries(places)) {
        if (dayPlaces.length >= 2) {
          await this.calculateRoute(dayPlaces, day);
        }
      }
    },

    toggleRoutes(dayIndex) {
      // 모든 경로 숨기기
      Object.values(this.routePolylines).forEach((routes) => {
        routes.forEach((route) => route.setMap(null));
      });

      // 선택된 날짜의 경로만 표시
      if (dayIndex && this.routePolylines[dayIndex]) {
        this.routePolylines[dayIndex].forEach((route) =>
          route.setMap(this.map)
        );
      }
    },

    showAllRoutes() {
      Object.values(this.routePolylines).forEach((routes) => {
        routes.forEach((route) => route.setMap(this.map));
      });
    },

    getMap() {
      return this.map;
    },

    createMarker(place, dayIndex, placeIndex) {
      const position = new Tmapv2.LatLng(
        Number(place.latitude),
        Number(place.longitude)
      );

      const colors = [
        "red",
        "green",
        "purple",
        "orange",
        "mint",
        "pink",
        "lime",
        "magenta",
        "yello",
      ];
      const adjustedDayIndex = dayIndex - 1;
      const currentColor = colors[adjustedDayIndex % colors.length];
      const markerNumber = (placeIndex % 9) + 1;
      const markerImageUrl = `https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/${currentColor}${markerNumber}.png`;

      const marker = new Tmapv2.Marker({
        position: position,
        icon: markerImageUrl,
        iconSize: new Tmapv2.Size(24, 24),
        map: this.map,
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

    createPolyline(positions, color) {
      return new Tmapv2.Polyline({
        path: positions,
        strokeColor: color,
        strokeWeight: 3,
        strokeStyle: "solid",
        map: this.map,
      });
    },

    clearAllMarkers() {
      Object.values(this.markersByDay).forEach((markers) => {
        markers.forEach((marker) => {
          if (marker) {
            marker.setMap(null);
            marker = null;
          }
        });
      });
      this.markersByDay = {};
    },

    clearAllPolylines() {
      Object.values(this.polylinesByDay).forEach((polyline) => {
        if (polyline) {
          polyline.setMap(null);
          polyline = null;
        }
      });
      this.polylinesByDay = {};
    },

    updateMarkers(places) {
      this.clearAllMarkers();
      this.clearAllPolylines();

      if (this.currentInfoWindow) {
        this.currentInfoWindow.setMap(null);
        this.currentInfoWindow = null;
      }

      let lastPosition = null;

      Object.entries(places).forEach(([day, dayPlaces]) => {
        if (!this.markersByDay[day]) {
          this.markersByDay[day] = [];
        }

        const dayPositions = [];

        dayPlaces.forEach((place, placeIndex) => {
          try {
            const marker = this.createMarker(place, parseInt(day), placeIndex);
            this.markersByDay[day].push(marker);
            lastPosition = marker.getPosition();
            dayPositions.push(lastPosition);
          } catch (error) {
            console.error("Error creating marker:", error);
          }
        });
      });

      if (lastPosition) {
        this.map.setCenter(lastPosition);
      }

      this.map.setZoom(this.defaultZoom);
    },

    toggleMarkersByDay(dayIndex) {
      Object.entries(this.markersByDay).forEach(([day, markers]) => {
        markers.forEach((marker) => {
          if (marker) {
            marker.setMap(null);
          }
        });
      });

      if (dayIndex) {
        const markers = this.markersByDay[dayIndex];
        if (markers) {
          markers.forEach((marker) => {
            if (marker) {
              marker.setMap(this.map);
            }
          });
        }
      }
    },

    showAllMarkers() {
      Object.values(this.markersByDay).forEach((markers) => {
        markers.forEach((marker) => {
          if (marker) {
            marker.setMap(this.map);
          }
        });
      });
    },
  },

  beforeUnmount() {
    if (this.currentInfoWindow) {
      this.currentInfoWindow.setMap(null);
      this.currentInfoWindow = null;
    }

    this.clearAllMarkers();
    this.clearAllPolylines();
    this.clearRoutes();

    if (this.map) {
      this.map = null;
    }
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