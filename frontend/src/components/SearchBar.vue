<template>
  <div class="search-area">
    <div class="search-bar" @mouseover="showPopular = true" @mouseleave="handleMouseLeave">
      <input
        type="text"
        placeholder="Search..."
        class="search-input"
        v-model="searchQuery"
        @keyup.enter="handleSearch"
        @focus="showPopular = true"
        @blur="handleBlur"
        ref="searchInput"
      />
      <button class="search-btn" @click="handleSearch">
        <i class="fa-solid fa-magnifying-glass"></i>
      </button>

      <!-- 인기 검색어 드롭다운 -->
      <div v-if="showPopular" class="popular-keywords">
        <div class="popular-header">
          <span class="popular-title">인기 검색어 TOP 10</span>
          <div class="keyword-types">
            <span
              v-for="type in keywordTypes"
              :key="type.value"
              :class="['type-btn', { active: currentType === type.value }]"
              @click="changeType(type.value)"
              @mousedown.prevent
            >
              {{ type.label }}
            </span>
          </div>
        </div>
        <div class="keywords-list">
          <div
            v-for="(keyword, index) in currentKeywords"
            :key="index"
            class="keyword-item"
            @mousedown.prevent
            @click="selectKeyword(keyword.keyword)"
          >
            <span class="rank">{{ index + 1 }}</span>
            <span class="keyword">{{ keyword.keyword }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from "@/plugins/axios";

export default {
  name: "SearchBar",
  data() {
    return {
      searchQuery: "",
      showPopular: false,
      keywordPopular: [], // 키워드 타입 인기 검색어
      areaPopular: [], // 지역 타입 인기 검색어
      currentType: "KEYWORD",
      isMouseOver: false,
      keywordTypes: [
        { label: "키워드", value: "KEYWORD" },
        { label: "지역", value: "AREA" },
      ],
    };
  },
  computed: {
    currentKeywords() {
      return this.currentType === "KEYWORD" ? this.keywordPopular : this.areaPopular;
    },
  },
  methods: {
    handleSearch() {
      if (this.searchQuery.trim()) {
        this.$router.push({
          name: "searchPlace", // 라우터 name을 searchPlace로 변경
          query: { keyword: this.searchQuery },
        });
      }
    },
    async fetchPopularKeywords() {
      try {
        // 키워드 타입 인기 검색어 조회
        const keywordResponse = await api.get("/domain/attraction/popular/keywords", {
          params: { searchType: "KEYWORD" },
        });

        // 지역 타입 인기 검색어 조회
        const areaResponse = await api.get("/domain/attraction/popular/keywords", {
          params: { searchType: "AREA" },
        });

        if (keywordResponse.data) {
          this.keywordPopular = keywordResponse.data;
        }
        if (areaResponse.data) {
          this.areaPopular = areaResponse.data;
        }
      } catch (error) {
        console.error("인기 검색어 로딩 실패:", error);
      }
    },
    changeType(type) {
      this.currentType = type;
    },
    selectKeyword(keyword) {
      this.searchQuery = keyword;
      this.handleSearch();
    },
    handleMouseLeave() {
      this.isMouseOver = false;
      if (!this.$refs.searchInput.matches(":focus")) {
        this.showPopular = false;
      }
    },
    handleBlur() {
      if (!this.isMouseOver) {
        this.showPopular = false;
      }
    },
  },
  // 컴포넌트가 마운트될 때 한 번만 데이터 로드
  async mounted() {
    await this.fetchPopularKeywords();
  },
};
</script>

<style scoped>
.keywords-list::-webkit-scrollbar {
  width: 8px;
}

.keywords-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.keywords-list::-webkit-scrollbar-thumb {
  background: #ecb27b;
  border-radius: 4px;
}

.keywords-list::-webkit-scrollbar-thumb:hover {
  background: #dca06a;
}

.search-area {
  justify-self: center;
  position: relative;
}

.search-bar {
  position: relative;
}

.search-input {
  padding: 8px 40px 8px 16px;
  background-color: rgba(255, 255, 255, 0.99);
  width: 200px;
  font-size: 14px;
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

.search-input:focus {
  opacity: 1;
  outline: none;
  background-color: white;
}

/* 인기 검색어 드롭다운 스타일 */
.popular-keywords {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background-color: rgba(255, 255, 255, 1) !important; /* 완전 불투명하게 설정 */
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  opacity: 1 !important; /* 완전 불투명하게 설정 */
  font-family: "Pretendard-Reegular";
  font-size: 12px;
}

.popular-header {
  padding: 12px;
  border-bottom: 1px solid #eee;
  background-color: white;
  opacity: 1;
}

.popular-title {
  color: #333;
  display: block;
  margin-bottom: 8px;
  opacity: 1;
  font-family: "Pretendard-Bold";
  font-size: 16px;
}

.keyword-types {
  display: flex;
  gap: 8px;
  opacity: 1;
}

.type-btn {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  background: #f5f5f5;
  color: #666;
  opacity: 1;
}

.type-btn.active {
  background: #ecb27b;
  color: white;
}

.keywords-list {
  max-height: 300px;
  overflow-y: auto;
  background-color: white;
  opacity: 1;
}

.keyword-item {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
  background-color: white;
  opacity: 1;
}

.keyword-item:hover {
  background-color: #f5f5f5;
}

.rank {
  width: 24px;
  color: #ecb27b;
  font-weight: bold;
}

.keyword {
  flex: 1;
  margin: 0 8px;
  color: #333;
}

.search-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #666;
}
</style>
