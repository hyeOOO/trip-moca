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
        <div class="nav-content">
          <router-link to="/" class="logo" v-if="!isDarkRoute">
            <img
              src="@/assets/image/HW&SW.png"
              alt="HW&SW Logo"
              class="hwsw-logo-image"
            />
          </router-link>
          <router-link to="/" class="logo" v-else>
            <img
              src="@/assets/image/HW&SW-dark.png"
              alt="HW&SW Logo"
              class="hwsw-logo-image"
            />
          </router-link>
          <!-- 메뉴바 -->
          <div class="menu">
            <router-link
              v-for="(item, i) in menuItems"
              :key="i"
              :to="item.path"
              :class="{ 'dark-text': isDarkRoute }"
            >
              {{ item.name }}
            </router-link>
          </div>
          <!-- 검색바 -->
          <div class="search-bar">
            <input
              type="text"
              placeholder="Search..."
              class="search-input"
              v-model="searchQuery"
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">
              <i class="fa-solid fa-magnifying-glass"></i>
            </button>
          </div>
        </div>
      </nav>
    </div>
  </div>
</template>

<script>
import navLogin from "@/components/navLogin.vue";

export default {
  name: "Menu",
  data() {
    return {
      menuItems: [
        { name: "여행계획", path: "/travelPlan" },
        { name: "경로검색", path: "/" },
        { name: "추천관광", path: "/" },
        { name: "카드도감", path: "/" },
        { name: "마이페이지", path: "/" },
      ],
      searchQuery: "",
    };
  },
  computed: {
  isDarkRoute() {
    // 메인 페이지('/')가 아닌 모든 경로에서 dark 모드 (검은색) 적용
    return this.$route.path !== '/';
  },
},
  components: {
    navLogin: navLogin,
  },
  methods: {
    handleSearch() {
      // 검색 로직
    },
  },
};
</script>

<style>
:root {
  --nav-text-color: #ffffff;
}

.menu {
  color: var(--nav-text-color);
  display: flex;
  gap: 80px;
  z-index: 2;
  font-family: "EliceDigitalBaeum_BOLD";
  font-size: 18px;
  font-weight: 300;
  padding: 15px;
}

.menu a {
  color: var(--nav-text-color);
  text-decoration: none;
  transition: color 0.3s ease;
  cursor: pointer;
}

.menu a.dark-text {
  color: black;
}

.nav-theme .menu a:hover {
  color: #ffdd57;
}

.nav-dark .menu a:hover {
  color: #ecb27b;
}

.nav-top {
  height: 36px;
  color: var(--nav-text-color);
}

.info-top {
  margin-left: 170px;
  grid-column: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-top-text {
  color: var(--nav-text-color);
  opacity: 1;
  z-index: 2;
  font-family: "Pretendard-Light";
  font-size: 15px;
}

.nav-dark .info-top-text {
  color: black;
}

.info-top-text:hover {
  transform: scale(1.05);
}

/* 구분선 */
.top-division-line {
  border-top: 1.5px solid rgba(255, 255, 255, 1);
  margin-top: 10px;
  margin-bottom: 10px;
  position: relative;
  z-index: 2;
}

.top-division-line.dark-line {
  border-top: 1.5px solid rgba(0, 0, 0, 1);
}

/* 구분선 밑 네비바 */
.nav-bottom {
  padding: 25px;
  z-index: 2;
  position: relative;
  margin-top: 0px;
}

.main-nav {
  width: 100%;
  background-color: transparent;
  position: absolute;
  top: 0;
  left: 0;
}

.nav-content {
  max-width: 100%;
  margin: -5px 50px;
  padding: 0px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  position: relative;
  opacity: 0.5;
  left: -95px;
}

.logo {
  position: relative;
  display: flex;
  align-items: center;
  text-decoration: none;
  left: 130px;
}

.hwsw-logo-image {
  height: 40px;
  width: 100px;
  object-fit: contain;
  transition: transform 0.2s ease;
}

.hwsw-logo-image:hover {
  transform: scale(1.05);
}

.search-input {
  border: 1px solid black;
  padding: 8px 40px 8px 16px;
  border-radius: 20px;
  background-color: rgba(255, 255, 255, 0.99);
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

.top-info {
  background-color: transparent;
  padding: 8px 0;
  position: relative;
  z-index: 2;
}
</style>
