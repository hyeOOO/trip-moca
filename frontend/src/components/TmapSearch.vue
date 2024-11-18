<template>
  <div class="tmap-search-container">
    <div class="search-results-panel">
      <h2>검색 결과: "{{ searchKeyword }}"</h2>
      <div class="search-results-list" v-if="searchResults.length">
        <div 
          v-for="(result, index) in searchResults" 
          :key="index"
          class="search-result-item"
          @click="selectLocation(result)"
        >
          <h3>{{ result.name }}</h3>
          <p>{{ result.address }}</p>
        </div>
      </div>
      <div v-else class="no-results">
        <p>검색 결과가 없습니다.</p>
      </div>
    </div>
    
    <!-- Tmap 컴포넌트 -->
    <div class="map-container">
      <Tmap 
        ref="tmap"
        :center="mapCenter"
        :markers="markers"
      />
    </div>
  </div>
</template>

<script>
import Tmap from './Tmap.vue';

export default {
  name: 'TmapSearch',
  components: {
    Tmap
  },
  data() {
    return {
      searchKeyword: '',
      searchResults: [],
      mapCenter: {
        lat: 35.1795543,
        lng: 129.0756416
      },
      markers: []
    };
  },
  created() {
    // URL의 쿼리 파라미터에서 검색어 가져오기
    this.searchKeyword = this.$route.query.keyword;
    if (this.searchKeyword) {
      this.performSearch();
    }
  },
  methods: {
    async performSearch() {
      try {
        // T map API를 사용한 검색 로직
        // 실제 구현시 적절한 API 키와 함께 사용해야 합니다
        const response = await fetch(`YOUR_TMAP_API_ENDPOINT?keyword=${this.searchKeyword}`);
        const data = await response.json();
        
        this.searchResults = data.map(item => ({
          name: item.name,
          address: item.address,
          lat: item.latitude,
          lng: item.longitude
        }));
        
        // 검색 결과를 마커로 표시
        this.updateMarkers();
        
        // 검색 결과의 중심으로 지도 이동
        if (this.searchResults.length > 0) {
          this.mapCenter = {
            lat: this.searchResults[0].lat,
            lng: this.searchResults[0].lng
          };
        }
      } catch (error) {
        console.error('Search failed:', error);
      }
    },
    
    selectLocation(location) {
      this.mapCenter = {
        lat: location.lat,
        lng: location.lng
      };
      // 선택된 위치로 지도 센터 이동
      if (this.$refs.tmap) {
        this.$refs.tmap.setCenter(location.lat, location.lng);
      }
    },
    
    updateMarkers() {
      this.markers = this.searchResults.map(result => ({
        position: {
          lat: result.lat,
          lng: result.lng
        },
        title: result.name
      }));
    }
  }
};
</script>

<style scoped>
.tmap-search-container {
  display: grid;
  grid-template-columns: 350px 1fr;
  height: calc(100vh - 120px); /* Adjust based on your navbar height */
  width: 100%;
}

.search-results-panel {
  padding: 20px;
  background-color: #fff;
  border-right: 1px solid #eee;
  overflow-y: auto;
}

.search-results-list {
  margin-top: 20px;
}

.search-result-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-result-item:hover {
  background-color: #f5f5f5;
}

.search-result-item h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.search-result-item p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.map-container {
  width: 100%;
  height: 100%;
}

.no-results {
  padding: 20px;
  text-align: center;
  color: #666;
}
</style>