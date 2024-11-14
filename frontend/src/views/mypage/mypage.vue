<template>
  <div class="mypage">
    <navBar />
    <div class="profile-icon">
      <img src="@/assets/image/user-profile.png" alt="Profile" />
    </div>
    <div class="profile-container">
      <div class="mypage-contents">
        <div class="user-info">
          <h2>{{ memberId }}</h2>
          <div class="meter orange nostripes">
            <span ref="progressBar" :style="mypageStore.progressStyle"></span>
          </div>
          <div class="user-info-card">
            <span>{{ mypageStore.userCards.length }}</span>
            <span>개 카드 수집({{ mypageStore.userCards.length }} / {{ mypageStore.totalCards.length }})</span>
          </div>
        </div>

        <div class="mypage-header">
          <router-link to="/mypage/plan" custom v-slot="{ navigate }">
            <button @click="navigate">
              <span>나의 여행 계획 </span>
              <span>{{ mypageStore.userPlans.length }}</span>
            </button>
          </router-link>
          <router-link to="/mypage/card" custom v-slot="{ navigate }">
            <button @click="navigate">
              <span>보유 카드 </span>
              <span>{{ mypageStore.userCards.length }}</span>
            </button>
          </router-link>
          <hr :class="{ 'slide-card': $route.path === '/mypage/card' }" />
        </div>

        <div class="mypage-body">
          <router-view></router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import navBar from "@/components/navBar.vue"
import { useAuthStore } from "@/store/auth"
import { useMypageStore } from '@/store/mypageStore';

const authStore = useAuthStore()
const mypageStore = useMypageStore()
const memberId = ref('')

const initUserData = () => {
  memberId.value = authStore.memberId
}

onMounted(async () => {
  initUserData()
  await mypageStore.fetchCardData()
  await mypageStore.fetchPlanData()
})
</script>

<style scoped>
.mypage {
  position: relative;
  min-height: 100vh;
  margin: 0;
  padding-bottom: 100px;
  /* footer 높이만큼 여백 추가 */
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  background-attachment: fixed;
  /* 이 속성을 추가하여 배경 이미지 고정 */
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
  min-height: calc(100vh - 210px);
  /* footer 높이를 고려하여 수정 */
  max-height: calc(100vh - 110px);
  /* 최대 높이 설정 */
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

/* mypage-contents도 수정 */
.mypage-contents {
  height: auto;
  /* 내용물 크기에 맞춰서 자동으로 크기 조절 */
  min-height: 100%;
}

.mypage-body {
  height: calc(100vh - 500px);
  /* 상단 여백과 footer를 고려한 높이 */
  overflow-y: auto;
  /* 스크롤 추가 */
  margin-top: 20px;
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

.user-info>h2 {
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

.meter>span {
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

.meter>span:after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background-image: linear-gradient(-45deg,
      rgba(255, 255, 255, 0.2) 25%,
      transparent 25%,
      transparent 50%,
      rgba(255, 255, 255, 0.2) 50%,
      rgba(255, 255, 255, 0.2) 75%,
      transparent 75%,
      transparent);
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

/* 스크롤바 스타일링 - mypage-body에 적용 */
.mypage-body::-webkit-scrollbar {
  width: 8px;
}

.mypage-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.mypage-body::-webkit-scrollbar-thumb {
  background: #ecb27b;
  border-radius: 4px;
}

.mypage-body::-webkit-scrollbar-thumb:hover {
  background: #dca06a;
}
</style>
