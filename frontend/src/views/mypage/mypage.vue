<template>
  <div class="mypage">
    <navBar />
    <div class="profile-icon">
      <img src="@/assets/image/user-profile.png" alt="Profile" />
    </div>
    <div class="profile-container">
      <div class="mypage-contents">
        <div class="user-info">
          <h2>phw5883</h2>
          <div class="meter orange nostripes">
            <span ref="progressBar" :style="progressStyle"></span>
          </div>
          <div class="user-info-card">
            <span>8</span>
            <span>개 카드 수집</span>
          </div>
        </div>

        <div class="mypage-header">
          <button @click="setActiveTab('plan')">
            <span>나의 여행 계획 </span>
            <span>3</span>
          </button>
          <button @click="setActiveTab('card')">
            <span>보유 카드 </span>
            <span>8</span>
          </button>
          <hr :class="{ 'slide-card': activeTab === 'card' }" />
        </div>

        <div class="mypage-body">
          <div></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import navBar from "@/components/navBar.vue";

export default {
  components: {
    navBar: navBar,
  },
  data() {
    return {
      currentWidth: 0,
      targetWidth: 33.3, // 목표 너비 퍼센트
      animationStarted: false,
      activeTab: "plan", // 추가: 현재 활성화된 탭 상태
    };
  },
  computed: {
    progressStyle() {
      return {
        width: `${this.currentWidth}%`,
        transition: "width 1.2s ease-out",
      };
    },
  },
  methods: {
    // 추가: 탭 변경 메서드
    setActiveTab(tab) {
      this.activeTab = tab;
    },
  },
  mounted() {
    // mounted 훅에서 애니메이션 시작
    this.$nextTick(() => {
      if (!this.animationStarted) {
        this.animationStarted = true;
        // 시작시 너비 0으로 설정
        this.currentWidth = 0;
        // 약간의 지연 후 목표 너비로 애니메이션
        setTimeout(() => {
          this.currentWidth = this.targetWidth;
        }, 100);
      }
    });
  },
};
</script>

<style scoped>
.mypage {
  position: relative;
  height: 100vh;
  overflow: hidden;
  margin: 0;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  z-index: 0;
  background-image: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/enjoy-trip-main-photo.jpg");
}

.mypage::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: -1;
}

.profile-container {
  position: relative;
  background-color: white;
  padding: 6rem 2rem 2rem 2rem;
  height: 100vh;
  top: 110px;
  padding-left: 200px;
  padding-right: 200px;
}
.mypage-header {
  position: relative;
  /* 버튼들을 나란히 배치하기 위한 스타일 */
  display: flex;
  flex-wrap: wrap;
}
.mypage-header button {
  padding-left: 10px;
  text-align: left;
  width: 50%;
  font-family: "Pretendard-Medium";
  font-size: 32px;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 10px 0px;
}
.mypage-header button:hover {
  opacity: 0.8;
}
.mypage-header button span:nth-child(2) {
  font-family: "Pretendard-ExtraBold";
}

.mypage-header hr {
  border-width: 5px 0 0 0;
  border-color: #ecb27b;
  margin: 10px 0px;
  width: 50%;
  position: absolute;
  bottom: -10px;
  left: 0;
  transition: transform 0.3s ease-in-out;
}
/* hr 애니메이션을 위한 클래스 */
.mypage-header hr.slide-card {
  transform: translateX(100%);
}

/* 버튼 클릭 효과 */
.mypage-header button:active {
  transform: scale(0.98);
}
.white-content {
  margin-top: 3rem;
}

.profile-icon {
  position: relative;
  top: 200px;
  z-index: 1;
  padding-left: 200px;
}

.user-info {
  margin: 15px 0px;
}
.user-info > h2 {
  font-family: "Pretendard-Bold";
  font-size: 40px;
}
.user-info-card span:nth-child(1) {
  font-family: "Pretendard-Bold";
  font-size: 20px;
}
.user-info-card span:nth-child(2) {
  font-family: "Pretendard-SemiBold";
  font-size: 20px;
  color: #777777;
}

.meter {
  box-sizing: content-box;
  height: 20px;
  position: relative;
  margin: 20px 0 20px 0;
  background: #433629;
  border-radius: 25px;
  padding: 8px;
  box-shadow: inset 0 -1px 1px rgba(255, 255, 255, 0.3);
}

.meter > span {
  display: block;
  height: 100%;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
  border-top-left-radius: 20px;
  border-bottom-left-radius: 20px;
  background-color: #ecb27b;
  background-image: linear-gradient(center bottom, rgb(43, 194, 83) 37%, rgb(84, 240, 84) 69%);
  box-shadow: inset 0 2px 9px rgba(255, 255, 255, 0.3), inset 0 -2px 6px rgba(0, 0, 0, 0.4);
  position: relative;
  overflow: hidden;
}

.meter > span:after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background-image: linear-gradient(
    -45deg,
    rgba(255, 255, 255, 0.2) 25%,
    transparent 25%,
    transparent 50%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.2) 75%,
    transparent 75%,
    transparent
  );
  z-index: 1;
  background-size: 50px 50px;
  animation: move 2s linear infinite;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
}

@keyframes move {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 50px 50px;
  }
}
</style>
