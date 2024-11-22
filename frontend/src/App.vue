<template>
  <div class="app-container">
    <div class="main-content">
      <router-view />
      <login-modal v-model="showLoginModal" @card-acquired="handleCardAcquired" />
      <card-modal v-if="showCardModal" :card="acquiredCard" @close="closeCardModal" />
    </div>
    <footInfo />
  </div>
</template>

<script>
import { ref, provide, watch } from "vue";
import { showLoginModalFlag } from "@/eventBus";
import LoginModal from "@/components/loginModal.vue";
import CardModal from "@/components/cardModal.vue";

export default {
  name: "App",
  data() {},
  components: {
    LoginModal,
    CardModal,
  },
  setup() {
    const showLoginModal = ref(false);
    const showCardModal = ref(false);
    const acquiredCard = ref(null);

    watch(showLoginModalFlag, (newValue) => {
      showLoginModal.value = newValue;
    });

    const openLoginModal = () => {
      showLoginModal.value = true;
      showLoginModalFlag.value = true;
    };

    const handleCardAcquired = (card) => {
      acquiredCard.value = card;
      showCardModal.value = true;
    };

    const closeCardModal = () => {
      showCardModal.value = false;
      acquiredCard.value = null;
    };

    // provide를 통해 모달 컨트롤 함수 제공
    provide("modalControl", {
      openLoginModal,
    });

    return {
      showLoginModal,
      showCardModal,
      acquiredCard,
      handleCardAcquired,
      closeCardModal,
    };
  },
};
</script>

<style lang="scss">

#app {
  display: flex;
  flex-direction: column;
  height: 100vh; /* 전체 뷰포트 높이 */
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  position: relative;
}

/* 폰트 */
@font-face {
  font-family: "EliceDigitalBaeum_Bold";
  /* font-family:'다른컴포넌트가서 불일 이름'; */
  src: url("assets/fonts/EliceDigitalBaeum_Bold.ttf");
  /* src: url('폰트가 저장 되어 있는 공간') format('truetype');*/
}

@font-face {
  font-family: "EliceDigitalBaeum_Regular";
  src: url("assets/fonts/EliceDigitalBaeum_Regular.ttf");
  font-weight: 100;
}

@font-face {
  font-family: "Pretendard-Light";
  src: url("assets/fonts/Pretendard-Light.ttf");
}

@font-face {
  font-family: "Pretendard-Black";
  src: url("assets/fonts/Pretendard-Black.ttf");
}

@font-face {
  font-family: "Pretendard-Bold";
  src: url("assets/fonts/Pretendard-Bold.ttf");
}

@font-face {
  font-family: "Pretendard-ExtraBold";
  src: url("assets/fonts/Pretendard-ExtraBold.ttf");
}

@font-face {
  font-family: "Pretendard-ExtraLight";
  src: url("assets/fonts/Pretendard-ExtraLight.ttf");
}

@font-face {
  font-family: "Pretendard-Medium";
  src: url("assets/fonts/Pretendard-Medium.ttf");
}

@font-face {
  font-family: "Pretendard-Regular";
  src: url("assets/fonts/Pretendard-Regular.ttf");
}

@font-face {
  font-family: "Pretendard-SemiBold";
  src: url("assets/fonts/Pretendard-SemiBold.ttf");
}

@font-face {
  font-family: "Pretendard-Thin";
  src: url("assets/fonts/Pretendard-Thin.ttf");
}

// .form-input {
//   padding: 10px;
//   border: none;
//   border-radius: 5px;
//   background: white;
// }
body.modal-open {
  overflow: hidden;
  padding-right: 17px;
  position: fixed;
  width: 100%;
  height: 100%;
}

.search-submit {
  width: 401px;
  height: 56px;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: #ecb27b;
  color: white;
  font-weight: bold;
  cursor: pointer;
}
</style>
