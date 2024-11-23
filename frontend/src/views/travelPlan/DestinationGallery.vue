<template>
  <div class="DestinationBackground">
    <navBar />
    <div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold text-center mb-8">지금 당장 생각나는 곳은?</h1>
      <div class="relative max-w-2xl mx-auto mb-12">
        <input type="text"
          class="w-full px-4 py-3 rounded-full border border-gray-300 focus:outline-none focus:border-gray-500"
          placeholder="지역명 또는 시/군/구를 입력해주세요 (예: 강원, 강릉시)" v-model="searchQuery" />
        <button class="absolute right-4 top-1/2 transform -translate-y-1/2">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-400" fill="none" viewBox="0 0 24 24"
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </button>
      </div>

      <!-- 도시 그리드 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="destination in filteredDestinations" :key="destination.id" class="group cursor-pointer"
          @click="selectDestination(destination)">
          <div class="relative overflow-hidden rounded-lg shadow-lg">
            <img :src="destination.image" :alt="destination.nameKo"
              class="w-full h-64 object-cover transition-transform duration-300 group-hover:scale-110" />
            <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-4">
              <h3 class="text-white text-xl font-bold">
                {{ destination.nameKo }}
              </h3>
              <p class="text-white/80">{{ destination.nameEn }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue';
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
          image: destination.image
        }
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
.DestinationBackground {
  min-height: 100vh;
  background-color: #f8f9fa;
}
</style>