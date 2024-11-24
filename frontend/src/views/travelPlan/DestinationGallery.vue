<template>
  <div class="destination-background">
    <navBar />
    <div class="container">
      <h1 class="title">지금 당장 생각나는 곳은?</h1>
      <div class="search-container">
        <input
          type="text"
          class="search-input"
          placeholder="지역명을 입력해주세요"
          v-model="searchQuery"
        />
        <button class="search-button">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="search-icon"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </button>
      </div>

      <!-- 도시 그리드 -->
      <div class="destination-grid">
        <div
          v-for="destination in filteredDestinations"
          :key="destination.id"
          class="destination-item"
          @click="selectDestination(destination)"
        >
          <div class="destination-image-container">
            <img
              :src="destination.image"
              :alt="destination.nameKo"
              class="destination-image"
            />
            <div class="destination-overlay">
              <h3 class="destination-name-ko">{{ destination.nameKo }}</h3>
              <p class="destination-name-en">{{ destination.nameEn }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from "vue";
import navBar from "@/components/navBar.vue";
import DestinationGalleryData from "@/assets/data/DestinationGalleryData.js";
import { usePlanStore } from "@/store/planStore";

export default defineComponent({
  name: "DestinationGallery",
  components: {
    navBar,
  },

  setup() {
    const planStore = usePlanStore();
    return { planStore };
  },

  data() {
    return {
      destinations: DestinationGalleryData,
      searchQuery: "",
    };
  },

  computed: {
    filteredDestinations() {
      const search = this.searchQuery.toLowerCase().trim();

      if (!search) return this.destinations;

      return this.destinations.filter((destination) => {
        // 정확한 도시 이름 매칭 (가중치 높음)
        if (destination.nameKo === search ||
          destination.nameEn.toLowerCase() === search) {
          return true;
        }

        // 부분 도시 이름 매칭
        if (destination.nameKo.toLowerCase().includes(search) ||
          destination.nameEn.toLowerCase().includes(search)) {
          return true;
        }

        // 정확한 시/군/구 매칭
        if (destination.districts && destination.districts.some(district => {
          const districtLower = district.toLowerCase();
          // 정확한 매칭 ("강남구" === "강남구")
          if (districtLower === search) return true;
          // 부분 매칭 중 구/시/군 단위 확인
          if (search.endsWith('구') || search.endsWith('시') || search.endsWith('군')) {
            return districtLower.includes(search);
          }
          // 일반 부분 매칭
          return districtLower.includes(search);
        })) {
          return true;
        }

        return false;
      });
    }
  },

  methods: {
    selectDestination(destination) {
      // Pinia store의 selectedDestination을 직접 업데이트
      this.planStore.$patch({
        selectedDestination: {
          id: destination.id,
          areaCode: destination.id,
          areaName: destination.nameKo,
          title: destination.nameKo,
          image: destination.image,
        },
      });

      // 페이지 이동
      this.$router.push({
        name: "chooseDate",
        params: { name: destination.nameKo },
        query: { id: destination.id },
      });
    },
  },
});
</script>

<style scoped>
.destination-background {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.container {
  max-width: 1650px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.title {
  font-family: "EliceDigitalBaeum_Bold";
  font-size: 40px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 2rem;
}

.search-container {
  font-family: "Pretendard-Light";
  position: relative;
  max-width: 768px;
  margin: 0 auto 3rem;
}

.search-input {
  font-family: "Pretendard-Light";
  width: 100%;
  padding: 0.75rem 17px;
  border-radius: 9999px;
  border: 1px solid #d1d5db;
  outline: none;
}

.search-input:focus {
  border-color: #6b7280;
}

.search-button {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
}

.search-icon {
  height: 1.5rem;
  width: 1.5rem;
  color: #9ca3af;
}

.search-icon:hover {
  color: #6b7280;
}

.destination-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 30px;
}

@media (min-width: 768px) {
  .destination-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1024px) {
  .destination-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.destination-item {
  cursor: pointer;
}

.destination-image-container {
  position: relative;
  overflow: hidden;
  border-radius: 0.5rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1),
    0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.destination-image {
  width: 100%;
  height: 16rem;
  object-fit: cover;
  transition: transform 0.3s ease-in-out;
}

.destination-item:hover .destination-image {
  transform: scale(1.1);
}

.destination-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
  padding: 1rem;
}

.destination-name-ko {
  color: white;
  font-size: 1.25rem;
  font-weight: bold;
}

.destination-name-en {
  color: rgba(255, 255, 255, 0.8);
}
</style>
