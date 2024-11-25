<template>
  <div class="menu-container" ref="menuContainer" @click.stop>
    <button class="menu-button" @click.stop="toggleMenu">
      <i :class="['fi fi-rr-menu-burger icon', { 'dark-icon': isDarkRoute }]"></i>
    </button>
    <button class="user-button" @click="handleUserButtonClick">
      <i :class="['fi fi-rr-user icon', { 'dark-icon': isDarkRoute }]"></i>
    </button>

    <div v-show="showMenu" class="dropdown-menu">
      <!-- 비로그인 상태일 때 메뉴 -->
      <template v-if="!isAuthenticated">
        <button @click="handleOpenLoginModal">로그인</button>
      </template>
      <!-- 로그인 상태일 때 메뉴 -->
      <template v-else>
        <div class="user-info">{{ memberId }}님</div>
        <button @click="goToMyPage">마이페이지</button>
        <button @click="handleLogout">로그아웃</button>
      </template>
    </div>
  </div>
</template>

<script>
import { inject } from "vue";
import { useAuthStore } from "@/store/auth";
import { storeToRefs } from "pinia";
export default {
  props: {
    isDarkRoute: {
      type: Boolean,
      default: false,
    },
  },
  setup() {
    const { openLoginModal } = inject("modalControl");
    const authStore = useAuthStore();
    // storeToRefs를 사용하여 반응형 상태 가져오기
    const { isAuthenticated, memberId } = storeToRefs(authStore);

    return {
      openLoginModal,
      authStore,
      isAuthenticated,
      memberId,
    };
  },
  data() {
    return {
      showMenu: false,
    };
  },
  mounted() {
    window.addEventListener("click", this.closeMenu);
  },
  beforeUnmount() {
    window.removeEventListener("click", this.closeMenu);
  },
  methods: {
    closeMenu() {
      this.showMenu = false;
    },
    toggleMenu() {
      this.showMenu = !this.showMenu;
    },
    handleOpenLoginModal() {
      this.openLoginModal(); // inject된 함수 사용
      this.showMenu = false;
    },
    async handleLogout() {
      await this.authStore.logout();
      this.showMenu = false;
      this.$router.push("/");
    },
    handleUserButtonClick() {
      if (this.isAuthenticated) {
        this.goToMyPage();
      } else {
        this.handleOpenLoginModal();
      }
    },
    goToMyPage() {
      if (this.isAuthenticated) {
        this.$router.push("/mypage");
      } else {
        alert("로그인이 필요한 서비스입니다.");
        this.handleOpenLoginModal();
      }
      this.showMenu = false;
    },
  },
};
</script>

<style scoped>
.menu-container {
  margin-right: 114px;
  grid-column: 2;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  position: relative;
  z-index: 1000;
}

.menu-button,
.user-button {
  padding: 8px;
  cursor: pointer !important;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.menu-button:hover,
.user-button:hover {
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
}

.icon {
  font-size: 20px;
  color: white;
}

.dark-icon {
  color: black; /* Dark route에서 아이콘 색상을 검정색으로 설정 */
}

.dropdown-menu {
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 100%;
  right: 0;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 8px 0;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  min-width: 150px;
}

.dropdown-menu button {
  background: none;
  border: none;
  padding: 12px 16px;
  text-align: left;
  cursor: pointer;
  white-space: nowrap;
  color: #333;
  width: 100%;
  transition: background-color 0.2s ease;
}

.dropdown-menu button:hover {
  background-color: #f5f5f5;
}

.user-info {
  padding: 12px 16px;
  color: #333;
  font-weight: bold;
  border-bottom: 1px solid #eee;
}

/* 반응형 스타일 */
@media screen and (max-width: 768px) {
  .menu-container {
    margin-right: 20px;
  }

  .dropdown-menu {
    right: -10px;
  }
}
</style>
