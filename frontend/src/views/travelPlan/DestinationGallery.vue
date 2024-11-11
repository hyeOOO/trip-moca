<!-- DestinationGallery.vue -->
<template>
  <div class="planBackground">
    <navBar />
    <div class="container mx-auto px-4 py-8">
      <!-- 상단 검색 섹션 -->
      <h1 class="text-3xl font-bold text-center mb-8">
        지금 당장 생각나는 곳은?
      </h1>
      <div class="relative max-w-2xl mx-auto mb-12">
        <input
          type="text"
          class="w-full px-4 py-3 rounded-full border border-gray-300 focus:outline-none focus:border-gray-500"
          placeholder="지역명을 입력해주세요"
          v-model="searchQuery"
        />
        <button class="absolute right-4 top-1/2 transform -translate-y-1/2">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-6 w-6 text-gray-400"
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
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="destination in destinations"
          :key="destination.id"
          class="group cursor-pointer"
        >
          <div class="relative overflow-hidden rounded-lg shadow-lg">
            <img
              :src="destination.image"
              :alt="destination.name"
              class="w-full h-64 object-cover transition-transform duration-300 group-hover:scale-110"
            />
            <div
              class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-4"
            >
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
import navBar from "@/components/navBar.vue";
export default {
  name: "DestinationGallery",
  data() {
    return {
      searchQuery: "",
      destinations: [
        {
          id: 1,
          nameKo: "제주",
          nameEn: "Jeju",
          image: require('@/assets/image/jeju.png')
        },
        {
          id: 2,
          nameKo: "부산",
          nameEn: "Busan",
          image: require('@/assets/image/busan.png')
        },
        {
          id: 3,
          nameKo: "강릉",
          nameEn: "Gangneung",
          image: require('@/assets/image/gangneung.png')
        },
        {
          id: 4,
          nameKo: "경주",
          nameEn: "Gyeongju",
          image: require('@/assets/image/gyeongju.png')
        },
      ],
    };
  },
  components: {
    navBar: navBar,
  },
};
</script>

<style scoped>
/* 추가적인 스타일링이 필요한 경우 여기에 작성 */
</style>
