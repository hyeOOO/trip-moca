<template>
  <div>
    <div id="map_div"></div>
    <div class="controls">
      <!-- Map Type Controls -->
      <div class="map-type">
        <button @click="MapType('ROAD')">기본지도</button>
        <button @click="MapType('SATELLITE')">위성지도</button>
        <button @click="MapType('HYBRID')">하이브리드</button>
        <button @click="console.log(this.selectedPlacesByDay);">test</button>
        <!-- selectedPlacesByDay 수정 -->
      </div>
      
      <!-- Search Controls -->
      <div class="search">
        <input type="text" v-model="searchKeyword" placeholder="장소 검색">
        <button @click="searchPois">검색</button>
      </div>
      
      <!-- Route Controls -->
      <div class="route">
        <input type="text" v-model="startPoint.x" placeholder="출발지 X">
        <input type="text" v-model="startPoint.y" placeholder="출발지 Y">
        <input type="text" v-model="endPoint.x" placeholder="도착지 X">
        <input type="text" v-model="endPoint.y" placeholder="도착지 Y">
        <button @click="routesPedestrian">보행자 경로</button>
        <button @click="routesCar(0)">자동차 경로</button>
      </div>
      
      <!-- Measurement Controls -->
      <div class="measure">
        <button @click="measureDistance">거리측정</button>
        <button @click="measureRadius">반경측정</button>
      </div>
      
      <!-- Traffic Control -->
      <button @click="autoTraffic">교통정보</button>
    </div>
    
    <!-- Results Display -->
    <div id="apiResult" v-html="searchResults"></div>
  </div>
</template>

<script>
export default {
  name: 'TMapComponent',

  props: {
    selectedPlacesByDay: Object
  },
  
  data() {
    return {
      map: null,
      tData: null,
      searchKeyword: '',
      searchResults: '',
      startPoint: { x: '', y: '' },
      endPoint: { x: '', y: '' },
      markerArr: [],
      lineArr: [],
      labelArr: [],
      drawMode: '',
      measureDistanceObject: null,
      measureRadiusObject: null,
      squareMapObject: null,
      drawingObject: null
    }
  },

  mounted() {
    this.initializeMap()
  },

  methods: {
    initializeMap() {
      // Initialize TMap
      this.map = new Tmapv2.Map("map_div", {
        center: new Tmapv2.LatLng(34.85838339789551, 128.69144439697305),
        width: "750px",
        height: "750px",
        zoom: 7,
        httpsMode: true,
      })

      this.initializeMarker()
      this.initializeEventListeners()
    },

    initializeMarker() {
      // Create default marker
      const marker = new Tmapv2.Marker({
        position: new Tmapv2.LatLng(37.51027052249463, 127.02392578125034),
        icon: "https://tmapapi.tmapmobility.com/resources/images/common/pin_car.png",
        iconSize: new Tmapv2.Size(35, 48),
        draggable: true,
        map: this.map
      })

      this.markerArr.push(marker)
      this.setupMarkerEvents(marker)
    },

    initializeEventListeners() {
      // Map event listeners
      this.map.addListener("click", (e) => {
        console.log('클릭한 위치의 좌표는' + e.latLng + '입니다.')
      })

      this.map.addListener("zoom_changed", (e) => {
        console.log('현재 zoom : ' + e.zoom + '입니다.')
      })

      this.map.addListener("drag", (e) => {
        console.log('드래그한 위치의 좌표는' + e.latLng + '입니다.')
      })
    },

    setupMarkerEvents(marker) {
      marker.addListener("click", () => {
        alert('마커를 클릭했습니다!')
      })

      marker.addListener("mouseenter", () => {
        console.log('마커에 mouseover 이벤트가 발생했습니다!')
      })

      marker.addListener("mouseleave", () => {
        console.log('마커에 mouseout 이벤트가 발생했습니다!')
      })
    },

    MapType(type) {
      switch(type) {
        case "ROAD":
          this.map.setMapType(Tmapv2.Map.MapType.ROAD)
          break
        case "SATELLITE":
          this.map.setMapType(Tmapv2.Map.MapType.SATELLITE)
          break
        case "HYBRID":
          this.map.setMapType(Tmapv2.Map.MapType.HYBRID)
          break
      }
    },

    async searchPois() {
      const optionObj = {
        resCoordType: "WGS84GEO",
        reqCoordType: "WGS84GEO",
        count: 10
      }

      try {
        const result = await this.tData.getPOIDataFromSearchJson(this.searchKeyword, optionObj)
        this.handleSearchResults(result)
      } catch (error) {
        console.error('POI 검색 오류:', error)
      }
    },

    handleSearchResults(result) {
      const resultpoisData = result._responseData.searchPoiInfo.pois.poi
      let innerHtml = ""
      const positionBounds = new Tmapv2.LatLngBounds()

      resultpoisData.forEach((poi, k) => {
        const markerPosition = new Tmapv2.LatLng(Number(poi.noorLat), Number(poi.noorLon))
        
        const marker = new Tmapv2.Marker({
          position: markerPosition,
          icon: `http://tmapapi.sktelecom.com/upload/tmap/marker/pin_b_m_${k}.png`,
          iconSize: new Tmapv2.Size(24, 38),
          title: poi.name,
          map: this.map
        })

        innerHtml += this.createSearchResultHtml(poi, k)
        this.markerArr.push(marker)
        positionBounds.extend(markerPosition)
      })

      this.searchResults = `<ul>${innerHtml}</ul>`
      this.map.panToBounds(positionBounds)
      this.map.zoomOut()
    },

    createSearchResultHtml(poi, index) {
      return `<li>
        <img src='http://tmapapi.sktelecom.com/upload/tmap/marker/pin_b_m_${index}.png' style='vertical-align:middle;'/>
        <span>${poi.name}</span><br>
        <span>${poi.newAddressList.newAddress[0].fullAddressRoad}</span><br>
        <span>중심점: ${poi.noorLat},${poi.noorLon}</span><br>
        <span>입구점: ${poi.frontLat},${poi.frontLon}</span>
      </li>`
    },

    async routesPedestrian() {
      this.drawMode = "apiRoutesPedestrian"
      const startLatLng = new Tmapv2.LatLng(this.startPoint.y, this.startPoint.x)
      const endLatLng = new Tmapv2.LatLng(this.endPoint.y, this.endPoint.x)

      const optionObj = {
        reqCoordType: "WGS84GEO",
        resCoordType: "WGS84GEO"
      }

      try {
        const result = await this.tData.getRoutePlanForPeopleJson(startLatLng, endLatLng, optionObj)
        this.handleRouteResults(result)
      } catch (error) {
        console.error('보행자 경로 검색 오류:', error)
      }
    },

    routesCar(mode) {
      this.drawMode = `apiRoutesCar_${mode}`
      // Implementation similar to routesPedestrian but for car routes
      // ... car routing implementation
    },

    measureDistance() {
      this.measureDistanceObject = new Tmapv2.extension.MeasureDistance({
        map: this.map
      })
    },

    measureRadius() {
      this.measureRadiusObject = new Tmapv2.extension.MeasureRadius({
        map: this.map
      })
    },

    autoTraffic() {
      this.tData.autoTraffic(this.map, { trafficOnOff: true })
    },

    clearMap() {
      this.markerArr.forEach(marker => marker.setMap(null))
      this.lineArr.forEach(line => line.setMap(null))
      this.labelArr.forEach(label => label.setMap(null))
      
      this.markerArr = []
      this.lineArr = []
      this.labelArr = []
    }
  },

  beforeUnmount() {
    this.clearMap()
  }
}
</script>

<style scoped>
#map_div {
  width: 750px;
  height: 750px;
  margin: 0 auto;
}

.controls {
  margin: 20px;
  padding: 10px;
  border: 1px solid #ccc;
}

.controls > div {
  margin: 10px 0;
}

button {
  margin: 0 5px;
  padding: 5px 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #45a049;
}

input {
  margin: 0 5px;
  padding: 5px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

#apiResult {
  margin: 20px;
  padding: 10px;
  border: 1px solid #ccc;
  max-height: 300px;
  overflow-y: auto;
}
</style>