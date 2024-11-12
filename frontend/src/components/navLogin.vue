<template>
  <div class="menu-container" ref="menuContainer" @click.stop>  
    <button class="menu-button" @click.stop="toggleMenu">   
      <i class="fi fi-rr-menu-burger icon"></i>
    </button>
    <button class="user-button" @click="goToMyPage">
      <i class="fi fi-rr-user icon"></i>
    </button>

    <div v-show="showMenu" class="dropdown-menu">  
      <button @click="signin">로그인</button>
      <button @click="signup">회원가입</button>
      <button @click="logout">로그아웃</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      showMenu: false,
    };
  },
  mounted() {
    // 이벤트 리스너 수정
    window.addEventListener('click', this.closeMenu);
  },
  beforeUnmount() {
    window.removeEventListener('click', this.closeMenu);
  },
  methods: {
    toggleMenu() {
      console.log('toggleMenu called'); // 디버깅용
      this.showMenu = !this.showMenu;
    },
    // closeMenu(event) {
    //   if (this.$refs.menuContainer && !this.$refs.menuContainer.contains(event.target)) {
    //     this.showMenu = false;
    //   }
    //   const menuContainer = this.$el.querySelector('.menu-container');
    //   if (!menuContainer.contains(event.target)) {
    //     this.showMenu = false;
    //   }
    // },
    goToMyPage() {
      this.$router.push({ name: "MyPage" });
    },
    login() {
      console.log("로그인 클릭됨");
    },
    signUp() {
      console.log("회원가입 클릭됨");
    },
    logout() {
      console.log("로그아웃 클릭됨");
    },
    onClickMoveLoginPage() {
      this.$router.push("/LoginPage");
    },
  },
};
</script>

<style scoped>
.menu-container {
  margin-right: 155px;
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
  color: #333;
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
  transition: background-color 0.2s ease;
}

.dropdown-menu button:hover {
  background-color: #f5f5f5;
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

.icon {
  color: white;
}

</style>
