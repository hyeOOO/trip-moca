<template>
  <div>
    <!-- 네비게이션 바 -->
    <router-view />
    <!-- 인생샷 명소부터 숨은 맛집까지, 우리만의 트래블 큐레이션 -->
    <firstContent />
    <!-- 발도장 찍고 캐릭터 GET! 내 손안의 귀여운 여행 컬렉션 -->
    <secondContent />
    <!-- 시간은 아끼고 추억은 더하고 -->
    <thirdContent />
    <!-- 우리가 찾은 국내 여행 방곳 모음.zip 지금 바로 확인하기 -->
    <fourthContent />

    <!-- 푸터 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-logo">
          <img
            src="@/assets/image/HW&SW.png"
            alt="HW&SW Logo"
            class="footer-logo-image"
          />
        </div>
        <div class="footer-info">
          <h3>개발자 정보</h3>
          <p>Email: hyewon5342@nate.com</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import firstContent from "@/views/mainPage/firstContent.vue";
import secondContent from "@/views/mainPage/secondContent.vue";
import thirdContent from "@/views/mainPage/thirdContent.vue";
import fourthContent from "./views/mainPage/fourthContent.vue";

export default {
  name: "App",
  data() {},
  components: {
    firstContent: firstContent,
    secondContent: secondContent,
    thirdContent: thirdContent,
    fourthContent: fourthContent,
  },
};
</script>

<style lang="scss">
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
  /* 폰트 굵기 */
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

.form-input {
  padding: 10px;
  border: none;
  border-radius: 5px;
  background: white;
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
/* 푸터 */
.footer {
  background: #f8f9fa;
  padding: 40px 20px;
  margin-top: 80px;
}
.footer-content {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-logo-image {
  height: 30px;
}
.footer-info h3 {
  margin-bottom: 10px;
}
</style>
