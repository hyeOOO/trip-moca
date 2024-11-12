<template>
  <div class="app-container"> <!-- 클래스 추가 -->
    <router-view />
    <footInfo />
  </div>
</template>


<script>
import footInfo from "@/components/footInfo.vue";

export default {
  name: "App",
  data() {},
  components: {
    footInfo: footInfo,
  },
};
</script>

<style lang="scss">
// .app-container {
//   display: flex;
//   flex-direction: column;
//   min-height: 100vh;
// }
/* 전체 설정 */
body {
  margin: 0;
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
</style>
