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
      type: Number,
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
      markerColors: [
        "red",
        "blue",
        "green",
        "yellow",
        "purple",
        "orange",
        "pink",
      ],
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

    createMarker(place, dayIndex) {
      console.log("Creating marker for place:", place);

      const position = new Tmapv2.LatLng(
        Number(place.latitude),
        Number(place.longitude)
      );

      const colors = [
        "#FF6B6B",
        "#4D96FF",
        "#6BCB77",
        "#FFD93D",
        "#B983FF",
        "#FF9F45",
        "#4CACBC",
        "#FF8FB1",
        "#95CD41",
        "#7B2869",
      ];

      const markerHtml = `
    <div style="width: 24px; height: 24px;">
      <i class="fa-solid fa-location-dot" style="color: ${
        colors[dayIndex % colors.length]
      }; font-size: 24px;"></i>
    </div>
  `;

      const marker = new Tmapv2.Marker({
        position: position,
        icon:
          "data:image/svg+xml;charset=utf-8," + encodeURIComponent(markerHtml),
        iconHTML: markerHtml,
        iconSize: new Tmapv2.Size(24, 24),
        map: this.map,
        title: place.title,
        zIndex: dayIndex + 1,
      });

      marker.addListener("click", () => {
        new Tmapv2.InfoWindow({
          position: position,
          content: `
        <div style="padding:10px;min-width:150px;background-color:white;border-radius:5px;">
          <div style="font-weight:bold;margin-bottom:5px;">${place.title}</div>
          <div style="font-size:12px;color:${
            colors[dayIndex % colors.length]
          };">${dayIndex + 1}일차</div>
        </div>
      `,
          type: 2,
          map: this.map,
          border: "0px solid #FF0000",
          background: false,
        });
      });

      // 마커가 생성될 때마다 해당 위치로 지도 중심 이동
      this.map.setCenter(position);

      console.log(
        "Marker created at position:",
        position.lat(),
        position.lng()
      );
      return marker;
    },

    updateMarkers(places) {
      // 기존 마커 제거
      Object.values(this.markersByDay).forEach((markers) => {
        markers.forEach((marker) => marker.setMap(null));
      });
      this.markersByDay = {};

      // 새로운 마커들의 위치를 저장할 배열
      let lastPosition = null;

      // 새로운 마커 생성
      Object.entries(places).forEach(([day, dayPlaces]) => {
        if (!this.markersByDay[day]) {
          this.markersByDay[day] = [];
        }

        dayPlaces.forEach((place) => {
          try {
            const marker = this.createMarker(place, parseInt(day));
            this.markersByDay[day].push(marker);
            // 마지막 마커의 위치 저장
            lastPosition = marker.getPosition();
          } catch (error) {
            console.error("Error creating marker:", error);
          }
        });
      });

      // 마지막으로 추가된 마커의 위치로 지도 중심 이동
      if (lastPosition) {
        this.map.setCenter(lastPosition);
      }

      // 줌 레벨 유지
      this.map.setZoom(this.defaultZoom);
    },

    toggleMarkersByDay(dayIndex) {
      Object.entries(this.markersByDay).forEach(([day, markers]) => {
        markers.forEach((marker) => {
          marker.setMap(parseInt(day) === dayIndex ? this.map : null);
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
