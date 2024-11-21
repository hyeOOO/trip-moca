<template>
  <div class="DestinationBackground">
    <navBar />
    <div class="container mx-auto px-4 py-8">
      <h1 class="text-3xl font-bold text-center mb-8">지금 떠나고 싶은 곳은?</h1>
      <div class="relative max-w-2xl mx-auto mb-12">
        <input
          type="text"
          class="w-full px-4 py-3 rounded-full border border-gray-300 focus:outline-none focus:border-gray-500"
          placeholder="관광지를 입력해주세요"
          v-model="searchQuery"
          @keyup.enter="handleSearch"
        />
        <button class="absolute right-4 top-1/2 transform -translate-y-1/2" @click="handleSearch">
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

      <!-- 검색 결과가 없을 때 -->
      <div v-if="searchResult.length === 0 && hasSearched" class="text-center text-gray-500 my-8">
        검색 결과가 없습니다.
      </div>

      <!-- 도시 그리드 -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="attraction in searchResult"
          :key="attraction.attractionId"
          class="group cursor-pointer"
          @click="handleAttractionClick(attraction)"
        >
          <div class="relative overflow-hidden rounded-lg shadow-lg">
            <img
              :src="
                attraction.image1 ||
                'https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png'
              "
              :alt="attraction.title"
              class="w-full h-64 object-cover transition-transform duration-300 group-hover:scale-110"
              @error="handleImageError"
            />
            <div
              class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-4"
            >
              <!-- 태그 컨테이너 추가 -->
              <div class="flex gap-2 mb-2">
                <span
                  v-if="attraction.sidoName"
                  class="px-2 py-1 rounded-lg text-xs text-white"
                  style="background-color: #433629"
                >
                  {{ attraction.sidoName }}
                </span>
                <span
                  v-if="attraction.gugunName"
                  class="px-2 py-1 rounded-lg text-xs text-white"
                  style="background-color: #6e6156"
                >
                  {{ attraction.gugunName }}
                </span>
                <span
                  v-if="attraction.contentTypeName"
                  class="px-2 py-1 rounded-lg text-xs text-white"
                  style="background-color: #ecb27b"
                >
                  {{ attraction.contentTypeName }}
                </span>
              </div>
              <h3 class="text-white text-xl font-bold">
                {{ attraction.title }}
              </h3>
              <p class="text-white/80">{{ attraction.addr1 }} {{ attraction.addr2 }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import navBar from "@/components/navBar.vue";
import api from "@/plugins/axios";

export default {
  name: "searchPlace",
  components: {
    navBar,
  },
  data() {
    return {
      searchQuery: "",
      searchResult: [],
      hasSearched: false,
    };
  },
  methods: {
    async handleSearch() {
      if (!this.searchQuery.trim()) return;

      try {
        const response = await api.get("/domain/attraction/search/no-page", {
          params: {
            title: this.searchQuery,
          },
        });
        this.searchResult = response.data;
        this.hasSearched = true;
      } catch (error) {
        console.error("검색 실패:", error);
      }
    },
    handleImageError(e) {
      e.target.src = "/default-image.jpg"; // 기본 이미지로 대체
    },
    handleAttractionClick(attraction) {
      // 관광지 클릭 시 상세 페이지로 이동하거나 모달 표시
      console.log("Selected attraction:", attraction);
    },
  },
  // URL의 검색어 파라미터로 초기 검색 수행
  async created() {
    const queryKeyword = this.$route.query.keyword;
    if (queryKeyword) {
      this.searchQuery = queryKeyword;
      await this.handleSearch();
    }
  },
  // URL 변경 감지하여 검색 수행
  watch: {
    "$route.query.keyword": {
      handler(newKeyword) {
        if (newKeyword) {
          this.searchQuery = newKeyword;
          this.handleSearch();
        }
      },
      immediate: true,
    },
  },
};
</script>

<style scoped>
.DestinationBackground {
  min-height: 100vh;
  background-color: #f8f9fa;
}

/* 이미지 로딩 중 스켈레톤 효과 */
.image-skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.rounded-lg {
  transition: transform 0.2s ease;
}

.rounded-lg:hover {
  transform: translateY(-2px);
}

h1 {
  font-family: "Pretendard-Bold";
}

body {
  font-family: "Pretendard-Regular";
}

input {
  font-family: "Pretendard-Light";
}

span {
  font-family: "Pretendard-SemiBold";
  font-size: 12px;
}

h3 {
  font-family: "Pretendard-Bold";
  font-size: 20px;
}

p {
  font-family: "Pretendard-Regular";
}
</style>
