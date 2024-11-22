숫자 핑 찍어주는 Tmap 컴포넌트
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
      type: [Number, String],
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
      defaultZoom: 11,
    };
  },

  watch: {
    selectedPlacesByDay: {
      handler(newPlaces) {
        this.updateMarkers(newPlaces);
      },
      deep: true,
    },
    selectedDay(newDay) {
      this.toggleMarkersByDay(newDay);
    },
    showAllDays(show) {
      if (show) {
        this.showAllMarkers();
      }
    },
  },

  mounted() {
    this.initializeMap();
  },

  methods: {
    initializeMap() {
      this.map = new Tmapv2.Map("map_div", {
        center: new Tmapv2.LatLng(this.latitude, this.longitude),
        width: "100%",
        height: "100%",
        zoom: this.defaultZoom,
        httpsMode: true,
      });
    },

    getMap() {
      return this.map;
    },

    createMarker(place, index, dayIndex) {
      const position = new Tmapv2.LatLng(
        Number(place.latitude),
        Number(place.longitude)
      );

      // 마커 번호 설정 (장바구니 아이템의 순서)
      const markerNumber = index + 1;

      // S3에서 마커 이미지 URL
      const markerImageUrl = `https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/red${markerNumber}.png`;

      const marker = new Tmapv2.Marker({
        position: position,
        icon: markerImageUrl,
        iconSize: new Tmapv2.Size(24, 24),
        map: this.map,
        title: place.title,
        zIndex: markerNumber,
      });

      marker.addListener("click", () => {
        new Tmapv2.InfoWindow({
          position: position,
          content: `
              <div style="padding:10px;min-width:150px;background-color:white;border-radius:5px;">
                <div style="font-weight:bold;margin-bottom:5px;">${place.title}</div>
                <div style="font-size:12px;color:#666;">장소 ${markerNumber}</div>
              </div>
            `,
          type: 2,
          map: this.map,
          border: "0px solid #FF0000",
          background: false,
        });
      });

      this.map.setCenter(position);
      return marker;
    },

    updateMarkers(places) {
      // 기존 마커 제거
      Object.values(this.markersByDay).forEach((markers) => {
        markers.forEach((marker) => marker.setMap(null));
      });
      this.markersByDay = {};

      let lastPosition = null;

      // 새로운 마커 생성
      Object.entries(places).forEach(([day, dayPlaces]) => {
        if (!this.markersByDay[day]) {
          this.markersByDay[day] = [];
        }

        dayPlaces.forEach((place, index) => {
          try {
            const marker = this.createMarker(place, index, parseInt(day));
            this.markersByDay[day].push(marker);
            lastPosition = marker.getPosition();
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
          marker.setMap(
            dayIndex === "cart" || parseInt(day) === dayIndex ? this.map : null
          );
        });
      });
    },

    showAllMarkers() {
      Object.values(this.markersByDay).forEach((markers) => {
        markers.forEach((marker) => marker.setMap(this.map));
      });
    },
  },

  beforeUnmount() {
    Object.values(this.markersByDay).forEach((markers) => {
      markers.forEach((marker) => marker.setMap(null));
    });
    this.markersByDay = {};
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