\<template>
  <!-- 메인 콘텐츠 영역 -->
  <div class="background">
    <navBar />
    <div
      class="main-section"
      style="display: grid; grid-template-columns: 1fr 1fr"
    >
      <div class="box1">
        <h1 class="h1-text">
          평범한 여행은 가라,<br />우리만의 특별한 여행 스토리
        </h1>
      </div>
      <div class="box2">
        <div class="search-form">
          <div class="search-group">
            <div>
              <p class="text1">당신만의 힙한 여행,<br />여기서 시작해요✨</p>
            </div>
            <br />
            <div>
              <p class="text2">출발일자</p>
              <input
                type="date"
                v-model="startDate"
                @focus="showPlaceholder = false"
                @blur="showPlaceholder = !startDate"
                placeholder=" "
              />
              <span v-if="showPlaceholder" class="custom-placeholder"
                >선택하세요</span
              >
            </div>
            <div>
              <p class="text2">도착일자</p>
              <input
                type="date"
                v-model="startDate"
                @focus="showPlaceholder = false"
                @blur="showPlaceholder = !startDate"
                placeholder=" "
              />
              <span v-if="showPlaceholder" class="custom-placeholder"
                >선택하세요</span
              >
            </div>
            <div>
              <p class="text2">인원 수</p>
              <input type="number" class="form-input" min="0"/>
            </div>
            <div>
              <p class="text2">지역(선택)</p>
              <select
                v-model="result"
                style="text-align: center; width: 395px; height: 42px; border: 1px solid #FFFFFF;"
              >
                <option
                  v-for="(region, index) in 지역"
                  :key="index"
                  :value="region.value"
                >
                  {{ region.name }}
                </option>
              </select>
            </div>
          </div>
          <button class="search-submit">출 발 하 기</button>
        </div>
      </div>
    </div>
  </div>
  <div v-intersect class="fade-in-section">
    <firstContent />
  </div>
  <div v-intersect class="fade-in-section">
    <secondContent />
  </div>
  <div v-intersect class="fade-in-section">
    <thirdContent />
  </div>
  <div v-intersect class="fade-in-section">
    <fourthContent />
  </div>
</template>



<script>
import firstContent from "@/views/mainPage/firstContent.vue";
import secondContent from "@/views/mainPage/secondContent.vue";
import thirdContent from "@/views/mainPage/thirdContent.vue";
import fourthContent from "@/views/mainPage/fourthContent.vue";
import region from "@/assets/data/region.js";
import navBar from "@/components/navBar.vue";

export default {
  name: "FirstSearch",
  data() {
    return {
      지역: region,
      searchQuery: "", //검색어를 저장할 데이터 추가
      searchResults: [], //검색 결과를 저장할 배열
    };
  },
  components: {
    navBar: navBar,
    firstContent: firstContent,
    secondContent: secondContent,
    thirdContent: thirdContent,
    fourthContent: fourthContent,
  },
  directives: {
    intersect: {
      mounted(el) {
        const observer = new IntersectionObserver(
          ([entry]) => {
            if (entry.isIntersecting) {
              el.classList.add("visible");
              observer.unobserve(el);
            }
          },
          { threshold: 0.1 }
        );
        observer.observe(el);
      },
    },
  },
};
</script>

<style>
/* 배경 이미지 */
.background {
  position: relative;
  height: 100vh;
  overflow: hidden;
  margin: 0;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 0;
  animation: carousel 30s infinite; 
}
.background::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.2) 0%,
    rgba(0, 0, 0, 0.2) 50%,
    rgba(0, 0, 0, 0.2) 100%
  );
  z-index: 1;
}
.main-section {
  position: relative;
  height: 85%;
  padding: 100px;
  text-align: center;
  color: white;
  z-index: 1;
}

.box1 {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 30px;
  float: left;
  text-align: left;
  justify-self: center;
  margin-top: 20vh;
}

.box2 {
  margin-left: 140px;
  align-self: center;
  background: rgba(0, 0, 0, 0.5);
  height: 600px;
  width: 450px;
  padding: 30px;
  border-radius: 20px;
  display: inline-block;
}
.search-submit {
  width: 395px;
  height: 56px;
  border-radius: 8px;
  background: #ecb27b;
  font-weight: bold;
  cursor: pointer;
  margin-top: 40px;
}
.search-group {
  align-content: center;
  gap: 10px;
  margin-bottom: 10px;
}
.h1-text {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 56px;
}
.text1 {
  font-family: "EliceDigitalBaeum_Regular";
  text-align: left;
  font-size: 24px;
}

.text2 {
  font-family: "EliceDigitalBaeum_Regular";
  text-align: left;
}

/* 페이드 인 효과 */
.fade-in-section {
  margin-top: 120px;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 2s ease-out, transform 2s ease-out;
}
.fade-in-section.visible {
  opacity: 1;
  transform: translateY(0);
}
/* 출발일자, 도착일자 부분 년-월-일 없애기 */

/* 캐러셀 */
@keyframes carousel {
  0% {
    background-image: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/enjoy-trip-main-photo.jpg");
  }
  33% {
    background-image: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gwanganBridge.jpg");
  }
  66% {
    background-image: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/damyang-3381419_1280.jpg");
  }
  100% {
    background-image: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/enjoy-trip-main-photo.jpg");
  }
}
</style>
