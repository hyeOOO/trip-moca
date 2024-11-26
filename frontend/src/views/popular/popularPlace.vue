xed Popular Place Component

<template>
  <div class="popular-background">
    <div class="popular-place-background">
      <navBar />
      <div class="title">
        <h2>즐겨봐!</h2>
        <h1>AI</h1>
        <h2>인기여행</h2>
      </div>

      <!-- 현재 계절 -->
      <div>
        <div
          class="video-container current-season"
          :class="{
            'full-screen': isFullScreen,
            'fade-out': showNextSeason,
          }"
        >
          <video :src="seasonalVideo" preload="auto" autoplay loop muted></video>
        </div>
        <section id="section1">
          <h1
            class="season-title cursor-pointer hover:scale-110 transition-transform"
            :class="{
              show: showSeasonTitle && !showNextSeasonTitle,
              'fade-out': showNextSeason,
            }"
            @click="handleSeasonClick(currentSeason)"
          >
            {{ currentSeason }}
          </h1>
        </section>
      </div>

      <!-- 다음 계절 -->
      <div
        class="next-season"
        :class="{
          show: showNextSeason,
          'fade-out': showThirdSeason,
        }"
      >
        <div
          class="video-container full-height"
          :class="{ 'full-screen': isFullScreen && showNextSeason }"
        >
          <video :src="nextSeasonVideo" preload="auto" autoplay loop muted></video>
        </div>
        <section id="section2">
          <h1
            class="season-title cursor-pointer hover:scale-110 transition-transform"
            :class="{
              show: showNextSeasonTitle && !showThirdSeasonTitle,
              'fade-out': showThirdSeason,
            }"
            @click="handleSeasonClick(nextSeason)"
          >
            {{ nextSeason }}
          </h1>
        </section>
      </div>

      <!-- 다다음 계절 -->
      <div
        class="next-next-season"
        :class="{
          show: showThirdSeason,
          'fade-out': showFourthSeason,
        }"
      >
        <div
          class="video-container full-height"
          :class="{ 'full-screen': isFullScreen && showThirdSeason }"
        >
          <video :src="thirdSeasonVideo" preload="auto" autoplay loop muted></video>
        </div>
        <section id="section3">
          <h1
            class="season-title cursor-pointer hover:scale-110 transition-transform"
            :class="{
              show: showThirdSeasonTitle && !showFourthSeasonTitle,
              'fade-out': showFourthSeason,
            }"
            @click="handleSeasonClick(thirdSeason)"
          >
            {{ thirdSeason }}
          </h1>
        </section>
      </div>

      <!-- 다다다음 계절 -->
      <div
        class="fourth-season"
        :class="{
          show: showFourthSeason,
          'fade-out': showCarousel,
        }"
      >
        <div
          class="video-container full-height"
          :class="{ 'full-screen': isFullScreen && showFourthSeason }"
        >
          <video :src="fourthSeasonVideo" preload="auto" autoplay loop muted></video>
        </div>
        <section id="section4">
          <h1
            class="season-title cursor-pointer hover:scale-110 transition-transform"
            :class="{
              show: showFourthSeasonTitle,
              'fade-out': showCarousel,
            }"
            @click="handleSeasonClick(fourthSeason)"
          >
            {{ fourthSeason }}
          </h1>
        </section>
      </div>

      <div class="scroll-container" :class="{ hide: showCarousel }">
        <div class="field">
          <div class="scroll"></div>
        </div>
      </div>
    </div>

    <!-- 카드 캐러셀 섹션 -->
    <div class="carousel-section" :class="{ 'show-carousel': showCarousel }" ref="carouselSection">
      <div class="popular-attraction-title">
        <p class="normal-text">유저들의</p>
        <p class="pick-text">PICK!</p>
        <p class="hot-text">HOT</p>
        <p class="normal-text">관광지🔥</p>
      </div>
      <div class="carousel-wrapper">
        <CarouselComponent />
      </div>
    </div>
  </div>
</template>

<script>
import navBar from "@/components/navBar.vue";
import CarouselComponent from "./CarouselComponent.vue";

export default {
  name: "popularPlace",
  components: {
    navBar,
    CarouselComponent,
  },
  data() {
    return {
      videos: {
        spring: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/spring-video.mp4",
        summer: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/summer-video.mp4",
        autumn: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/autumn-video.mp4",
        winter: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/winter-video.mp4",
      },
      isFullScreen: false,
      showSeasonTitle: false,
      showNextSeason: false,
      showNextSeasonTitle: false,
      showThirdSeason: false,
      showThirdSeasonTitle: false,
      showFourthSeason: false,
      showFourthSeasonTitle: false,
      showCarousel: false,
      observer: null,
      lastScrollY: 0,
    };
  },
  mounted() {
    window.addEventListener("scroll", this.handleScroll);
    this.setupIntersectionObserver();
  },
  beforeDestroy() {
    window.removeEventListener("scroll", this.handleScroll);
    if (this.observer) {
      this.observer.disconnect();
    }
  },
  methods: {
    handleScroll() {
      const scrollPosition = window.scrollY;
      const windowHeight = window.innerHeight;
      const maxScroll = windowHeight * 4; // 전체 스크롤 높이

      // 스크롤 비율 계산 (0 ~ 1)
      const scrollRatio = Math.min(scrollPosition / maxScroll, 1);

      this.isFullScreen = scrollPosition > 100;
      this.showSeasonTitle = scrollPosition > 200;
      this.showNextSeason = scrollPosition > windowHeight - 200;
      this.showNextSeasonTitle = scrollPosition > windowHeight;
      this.showThirdSeason = scrollPosition > windowHeight * 2 - 200;
      this.showThirdSeasonTitle = scrollPosition > windowHeight * 2;
      this.showFourthSeason = scrollPosition > windowHeight * 3 - 200;
      this.showFourthSeasonTitle = scrollPosition > windowHeight * 3;

      // 캐러셀 표시 조건 수정
      if (scrollPosition > windowHeight * 3.5) {
        if (!this.showCarousel) {
          // 캐러셀이 처음 나타날 때만 transition 적용
          this.showCarousel = true;
        }
      } else {
        this.showCarousel = false;
      }

      // 스크롤 방향 감지 및 처리
      const currentScrollY = window.scrollY;
      const scrollingUp = this.lastScrollY > currentScrollY;
      this.lastScrollY = currentScrollY;

      if (scrollingUp) {
        // 스크롤 업일 때는 fourth season show 상태 유지
        this.showFourthSeason = scrollPosition > windowHeight * 3 - 200;
      }
    },

    setupIntersectionObserver() {
      const options = {
        rootMargin: "0px",
        threshold: 0.5,
      };

      this.observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting && this.showFourthSeasonTitle) {
            this.showCarousel = true;
          }
        });
      }, options);

      this.$nextTick(() => {
        if (this.$refs.carouselSection) {
          this.observer.observe(this.$refs.carouselSection);
        }
      });
    },
    async handleSeasonClick(season) {
      try {
        // 계절에 따른 한글->영어 변환
        const seasonMap = {
          봄: "spring",
          여름: "summer",
          가을: "autumn",
          겨울: "winter",
        };

        // router를 통해 새로운 페이지로 이동
        this.$router.push({
          name: "seasonPlan",
          params: { season: seasonMap[season] },
        });
      } catch (error) {
        console.error("Error handling season click:", error);
      }
    },
  },
  computed: {
    seasonalVideo() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return this.videos.spring;
      if (month >= 5 && month <= 7) return this.videos.summer;
      if (month >= 8 && month <= 10) return this.videos.autumn;
      return this.videos.winter;
    },
    nextSeasonVideo() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return this.videos.summer;
      if (month >= 5 && month <= 7) return this.videos.autumn;
      if (month >= 8 && month <= 10) return this.videos.winter;
      return this.videos.spring;
    },
    thirdSeasonVideo() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return this.videos.autumn;
      if (month >= 5 && month <= 7) return this.videos.winter;
      if (month >= 8 && month <= 10) return this.videos.spring;
      return this.videos.summer;
    },
    fourthSeasonVideo() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return this.videos.winter;
      if (month >= 5 && month <= 7) return this.videos.spring;
      if (month >= 8 && month <= 10) return this.videos.summer;
      return this.videos.autumn;
    },
    currentSeason() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return "봄";
      if (month >= 5 && month <= 7) return "여름";
      if (month >= 8 && month <= 10) return "가을";
      return "겨울";
    },
    nextSeason() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return "여름";
      if (month >= 5 && month <= 7) return "가을";
      if (month >= 8 && month <= 10) return "겨울";
      return "봄";
    },
    thirdSeason() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return "가을";
      if (month >= 5 && month <= 7) return "겨울";
      if (month >= 8 && month <= 10) return "봄";
      return "여름";
    },
    fourthSeason() {
      const month = new Date().getMonth() + 1;
      if (month >= 2 && month <= 4) return "겨울";
      if (month >= 5 && month <= 7) return "봄";
      if (month >= 8 && month <= 10) return "여름";
      return "가을";
    },
  },
};
</script>

<style scoped>
.popular-background {
  background-color: #433629;
}
.popular-place-background {
  background-color: #433629;
  min-height: 400vh;
  position: relative;
  z-index: 1;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-left: 150px;
}

.title h1 {
  color: #ecb27b;
  font-family: "Black Han Sans";
  font-size: 96px;
  font-style: normal;
}

.title h2 {
  color: white;
  font-family: "Pretendard-Bold";
  font-size: 75px;
  margin: 0px 10px;
}

.video-container {
  width: 100%;
  height: 50vh;
  overflow: hidden;
  transition: all 0.8s ease;
  position: relative;
  opacity: 1;
}

.video-container video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scroll-container {
  position: fixed;
  bottom: 40px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  z-index: 30;
  pointer-events: none;
}

.scroll-container.hide {
  opacity: 0;
}

.scroll {
  width: 60px;
  height: 60px;
  border: 2px solid white;
  border-radius: 50%;
  position: relative;
  animation: down 1.5s infinite;
  -webkit-animation: down 1.5s infinite;
}

.scroll::before {
  content: "";
  position: absolute;
  top: 15px;
  left: 18px;
  width: 18px;
  height: 18px;
  border-left: 2px solid white;
  border-bottom: 2px solid white;
  transform: rotate(-45deg);
}

.video-container.full-screen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 10;
}

.video-container.fade-out {
  opacity: 0;
  transform: translateY(-20px);
}

.season-title {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 90px;
  font-family: "Pretendard-Bold";
  font-style: normal;
  opacity: 0;
  transition: all 0.8s ease;
  z-index: 20;
}

.season-title.show {
  opacity: 1;
  transform: translate(-50%, -50%);
}

.season-title.fade-out {
  opacity: 0;
  transform: translate(-50%, -100%);
  transition: all 0.8s ease;
}

.current-season {
  width: 90%;
  margin: 0 auto;
  border-radius: 20px;
}

.current-season video {
  border-radius: 20px;
}

.current-season.full-screen {
  width: 100vw;
  margin: 0;
  border-radius: 0;
}

.current-season.full-screen video {
  border-radius: 0;
}

.next-season,
.next-next-season,
.fourth-season {
  position: absolute;
  left: 0;
  width: 100%;
  height: 100vh;
  opacity: 0;
  transition: all 0.8s ease;
  background-color: #433629;
}

.next-season {
  top: 100vh;
}

.next-next-season {
  top: 200vh;
}

.fourth-season {
  top: 300vh;
}

.next-season.show,
.next-next-season.show,
.fourth-season.show {
  opacity: 1;
}

.next-season.fade-out,
.next-next-season.fade-out,
.fourth-season.fade-out {
  opacity: 0;
  transform: translateY(-20px);
}

/* 페이드 효과 수정 */
.fourth-season.fade-out {
  opacity: 0;
  transform: translateY(-50px);
  transition: opacity 0.8s ease, transform 0.8s ease;
}

.full-height {
  height: 100vh !important;
}

#section1,
#section2,
#section3,
#section4 {
  height: 100vh;
  position: relative;
}

.carousel-section {
  min-height: 100vh;
  opacity: 0;
  transform: translateY(100px);
  transition: all 1s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 2;
  pointer-events: none;
  background-color: #433629;
}

/* 캐러셀 컨테이너 스타일 수정 */
.carousel-container {
  opacity: 0;
  transform: scale(0.95);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.show-carousel .carousel-container {
  opacity: 1;
  transform: scale(1);
}

.show-carousel {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.carousel-wrapper {
  position: relative;
  width: 100%;
  height: 100vh;
  background-color: #433629;
}

.popular-attraction-title {
  display: flex;
  justify-content: center;
  font-family: "Pretendard-Bold";
  font-size: 64px;
}

.normal-text {
  color: white;
  margin: 0px 10px;
}

.pick-text {
  color: #988d82;
  margin: 0px 10px;
}

.hot-text {
  color: #ecb27b;
  margin: 0px 10px;
}

@keyframes down {
  0% {
    transform: translate(0);
  }

  20% {
    transform: translateY(15px);
  }

  40% {
    transform: translate(0);
  }
}

@-webkit-keyframes down {
  0% {
    transform: translate(0);
  }

  20% {
    transform: translateY(15px);
  }

  40% {
    transform: translate(0);
  }
}
</style>
