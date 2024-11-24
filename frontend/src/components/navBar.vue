<template>
  <div :class="{ 'nav-theme': true, 'nav-dark': isDarkRoute }">
    <!-- nav-top -->
    <div class="nav-top" style="display: grid; grid-template-columns: 1fr 1fr">
      <div class="info-top">
        <img src="@/assets/image/location_icon.png" />
        <p class="info-top-text">SSAFY 12 / BUSAN 03</p>
        <img src="@/assets/image/mail_icon.png" />
        <p class="info-top-text">HW&SW@ssafy.com</p>
      </div>
      <navLogin :is-dark-route="isDarkRoute" />
    </div>
    <!-- 구분선 -->
    <div :class="['top-division-line', { 'dark-line': isDarkRoute }]"></div>
    <!-- nav-bottom -->
    <div class="nav-bottom">
      <nav class="main-nav">
        <div class="nav-grid">
          <!-- 로고 영역 -->
          <div class="logo-area">
            <router-link to="/main" class="logo" v-if="!isDarkRoute">
              <img
                src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/logo-white-5.png"
                alt="HW&SW Logo"
                class="hwsw-logo-image"
              />
            </router-link>
            <router-link to="/main" class="logo" v-else>
              <img
                src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/logo-black-6.png"
                alt="HW&SW Logo"
                class="hwsw-logo-image"
              />
            </router-link>
          </div>
          <div class="menu">
            <router-link
              v-for="(item, i) in menuItems"
              :key="i"
              :to="item.path"
              :class="{ 'dark-text': isDarkRoute }"
              active-class="active"
            >
              {{ item.name }}
              <span class="dot"></span>
            </router-link>
          </div>

          <!-- 검색바 영역 -->
          <SearchBar />
        </div>
      </nav>
    </div>
  </div>
</template>

<script>
import navLogin from "@/components/navLogin.vue";
import SearchBar from "@/components/SearchBar.vue";

export default {
  name: "Menu",
  data() {
    return {
      menuItems: [
        { name: "여행계획", path: "/travelPlan", subPaths: ["/chooseDate", "/choosePlace"], active: false },
        { name: "경로검색", path: "/routeSearch", active: false },
        { name: "추천관광", path: "/recommendTour", active: false },
        { name: "인기여행", path: "/popularPlace", active: false },
        {
          name: "마이페이지",
          path: "/mypage",
          requiresAuth: true,
          active: false,
        },
      ],
      searchQuery: "",
    };
  },
  computed: {
    // 흰색 테마로 하고 싶은 페이지들 밑에 경로 추가하면댐
    isDarkRoute() {
      return (
        !["/", "/main", "/popularPlace"].includes(this.$route.path) &&
        !this.$route.path.startsWith("/main/") &&
        !this.$route.path.startsWith("/season-plan/") &&
        !this.$route.path.startsWith("/keyword-plan/") &&
        !this.$route.path.startsWith("/mypage")
      );
    },
  },
  components: {
    navLogin,
    SearchBar,
  },
  watch: {
    $route(to) {
      this.setActiveItemByRoute(to.path);
    },
  },
  methods: {
    setActiveItem(index) {
      this.menuItems.forEach((item, i) => {
        item.active = i === index;
      });
    },
    setActiveItemByRoute(path) {
      this.menuItems.forEach((item) => {
        item.active = path.startsWith(item.path) || (item.subPaths && item.subPaths.some(subPath => path.startsWith(subPath)));
      });
    },
    handleSearch() {
      if (this.searchQuery.trim()) {
        // 검색어를 쿼리 파라미터로 전달
        this.$router.push({
          name: "TmapSearch",
          query: {
            keyword: this.searchQuery,
          },
        });
      }
    },
  },
  created() {
    this.setActiveItemByRoute(this.$route.path);
  },
};
</script>

<style scoped>
:root {
  --nav-text-color: #ffffff;
}

/* Top Navigation Styles */
.nav-top {
  color: var(--nav-text-color);
}

.info-top {
  margin-left: 140px;
  grid-column: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-top-text {
  color: var(--nav-text-color);
  opacity: 1;
  z-index: 2;
  font-family: "Pretendard-Light";
  padding-right: 38px;
  font-size: 14px;
}

.nav-dark .info-top-text {
  color: var(--nav-text-color);
}

.info-top-text:hover {
  transform: scale(1.05);
}

/* Division Line */
.top-division-line {
  border-top: 0.1px solid #d9d9d9;
  position: relative;
  z-index: 2;
}

.top-division-line.dark-line {
  border-top: 0.1px solid rgba(0, 0, 0, 0.1);
}

/* Bottom Navigation Styles */
.nav-bottom {
  padding: 28px;
  z-index: 2;
  position: relative;
  margin-top: 0;
}

.main-nav {
  width: 100%;
  background-color: transparent;
  position: absolute;
  top: 0;
  left: 0;
}

.nav-grid {
  display: grid;
  grid-template-columns: 1fr 4fr 1fr;
  align-items: center;
  padding: 0;
}

/* Logo Styles */
.logo-area {
  margin-left: 144px;
}

.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.hwsw-logo-image {
  height: 35px;
  width: 100%;
  object-fit: contain;
  transition: transform 0.2s ease;
}

.hwsw-logo-image:hover {
  transform: scale(1.05);
}

/* Menu Styles */
.menu {
  justify-self: center;
  display: flex;
  gap: 80px;
  z-index: 2;
  font-family: "EliceDigitalBaeum_regular";
  font-size: 16px;
  font-weight: 300;
  padding: 15px;
  color: var(--nav-text-color);
}

.menu a {
  color: var(--nav-text-color);
  text-decoration: none;
  transition: color 0.3s ease;
  cursor: pointer;
}

.menu a.dark-text {
  color: var(--nav-text-color);
}

.nav-theme {
  --nav-text-color: #ffffff;
}

.nav-dark {
  --nav-text-color: #000000;
}

.nav-theme .menu a:hover {
  color: #ecb27b;
}

.nav-dark .menu a:hover {
  color: #ecb27b;
}
.nav-theme .menu a.active {
  color: #ecb27b;
}

.nav-dark .menu a.active {
  color: #ecb27b;
}

.menu a {
  position: relative;
}

.menu a .dot {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 3px;
  height: 3px;
  background-color: transparent;
  border-radius: 100%;
  transition: background-color 0.3s ease;
}

.nav-theme .menu a.active .dot {
  background-color: #ecb27b;
}

.nav-dark .menu a.active .dot {
  background-color: #ecb27b;
}
/* Search Bar Styles */
.search-area {
  justify-self: center;
  margin-right: 120px;
}

.search-bar {
  margin-right: 113px;
  position: relative;
  opacity: 0.5;
}

.search-input {
  border: 1px solid black;
  padding: 8px 40px 8px 16px;
  border-radius: 20px;
  width: 200px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  background-color: white;
}

.search-input::placeholder {
  color: #515151;
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
