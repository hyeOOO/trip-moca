<template>
  <div class="scroll-container">
    <!-- 첫 번째 섹션 -->
    <section class="background first-section" id="section1">
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
          <searchForm />
        </div>
      </div>
      <div class="scroll-indicator">
        <div class="arrow arrow-first"></div>
        <div class="arrow arrow-second"></div>
      </div>
    </section>

    <!-- 두 번째 섹션 -->
    <section v-intersect class="fade-in-section" id="section2">
      <firstContent />
      <div class="scroll-indicator">
        <div class="arrow arrow-first"></div>
        <div class="arrow arrow-second"></div>
      </div>
    </section>

    <!-- 세 번째 섹션 -->
    <section v-intersect class="fade-in-section" id="section3">
      <secondContent />
      <div class="scroll-indicator">
        <div class="arrow arrow-first"></div>
        <div class="arrow arrow-second"></div>
      </div>
    </section>

    <!-- 네 번째 섹션 -->
    <section v-intersect class="fade-in-section" id="section4">
      <thirdContent />
      <div class="scroll-indicator">
        <div class="arrow arrow-first"></div>
        <div class="arrow arrow-second"></div>
      </div>
    </section>

    <!-- 다섯 번째 섹션 -->
    <section v-intersect class="fade-in-section" id="section5">
      <fourthContent />
      <div class="scroll-indicator">
        <div class="arrow arrow-first"></div>
        <div class="arrow arrow-second"></div>
      </div>
    </section>

    <!-- Footer -->
    <section v-intersect class="fade-in-section" id="section6">
      <footInfo />
    </section>
  </div>
</template>

<script>
import firstContent from "@/views/mainPage/firstContent.vue";
import secondContent from "@/views/mainPage/secondContent.vue";
import thirdContent from "@/views/mainPage/thirdContent.vue";
import fourthContent from "@/views/mainPage/fourthContent.vue";
import searchForm from "@/views/mainPage/searchForm.vue";
import footInfo from "@/components/footInfo.vue";
import region from "@/assets/data/region.js";
import navBar from "@/components/navBar.vue";

export default {
  name: "FirstSearch",
  data() {
    return {
      지역: region,
      searchQuery: "",
      searchResults: [],
      currentSection: 1,
      isAnimating: false,
      totalSections: 6,
    };
  },
  components: {
    navBar,
    firstContent,
    secondContent,
    thirdContent,
    fourthContent,
    footInfo,
    searchForm,
  },
  mounted() {
    window.addEventListener("wheel", this.handleScroll, { passive: false });
    this.initializeSections();
  },
  beforeUnmount() {
    window.removeEventListener("wheel", this.handleScroll);
  },
  methods: {
    initializeSections() {
      document.getElementById("section1").classList.add("active");
      for (let i = 2; i <= this.totalSections; i++) {
        document.getElementById(`section${i}`).classList.add("next");
      }
      this.isAnimating = false;
    },

    handleScroll(event) {
      if (this.isAnimating) return;
      event.preventDefault();

      const delta = Math.sign(event.deltaY);

      if (delta > 0 && this.currentSection < this.totalSections) {
        this.isAnimating = true;
        const currentEl = document.getElementById(
          `section${this.currentSection}`
        );
        const nextEl = document.getElementById(
          `section${this.currentSection + 1}`
        );

        currentEl.classList.remove("active");
        currentEl.classList.add("prev");
        nextEl.classList.remove("next");
        nextEl.classList.add("active");

        this.currentSection++;

        setTimeout(() => {
          this.isAnimating = false;
        }, 1000);
      } else if (delta < 0 && this.currentSection > 1) {
        this.isAnimating = true;
        const currentEl = document.getElementById(
          `section${this.currentSection}`
        );
        const prevEl = document.getElementById(
          `section${this.currentSection - 1}`
        );

        currentEl.classList.remove("active");
        currentEl.classList.add("next");
        prevEl.classList.remove("prev");
        prevEl.classList.add("active");

        this.currentSection--;

        setTimeout(() => {
          this.isAnimating = false;
        }, 1000);
      }
      // Footer에 도달했을 때 스크롤 인디케이터 숨기기
      if (this.currentSection === this.totalSections) {
        document.querySelector(".scroll-indicator").style.display = "none";
      } else {
        document.querySelector(".scroll-indicator").style.display = "block";
      }
    },

    scrollToSection(sectionNumber) {
      if (this.isAnimating || this.currentSection === sectionNumber) return;

      this.isAnimating = true;
      const currentEl = document.getElementById(
        `section${this.currentSection}`
      );
      const targetEl = document.getElementById(`section${sectionNumber}`);

      currentEl.classList.remove("active");
      targetEl.classList.add("active");

      if (sectionNumber > this.currentSection) {
        currentEl.classList.add("prev");
        targetEl.classList.remove("next");
      } else {
        currentEl.classList.add("next");
        targetEl.classList.remove("prev");
      }

      this.currentSection = sectionNumber;

      setTimeout(() => {
        this.isAnimating = false;
      }, 1000);
    },
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

<style scoped>
.first-section {
  background-color: rgba(0, 0, 0, 0.3);
  animation: carousel 30s infinite;
}

.fade-in-section {
  opacity: 1 !important;
  background-color: white !important;
  z-index: 2;
}

.fade-in-section.visible {
  opacity: 1;
}
.scroll-container {
  height: 100vh;
  overflow: hidden;
  position: relative;
  background-color: white;  /* 배경색 추가 */
}

section {
  position: absolute;
  width: 100%;
  height: 100vh;
  transition: all 1s cubic-bezier(0.645, 0.045, 0.355, 1);
  overflow: visible;
  background-color: white;  /* 배경색 추가 */
}

section.active {
  transform: translateY(0);
  opacity: 1;
  z-index: 3;  /* fade-in-section보다 높게 설정 */
  background-color: white;
}

section:not(.active):not(.fade-in-section) {
  opacity: 0;
  pointer-events: none;
}

section.next {
  transform: translateY(100%);
  z-index: 1;
}

section.prev {
  transform: translateY(-100%);
  z-index: 1;
}

.background {
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  background-color: rgba(0, 0, 0, 0.3);
  animation: carousel 30s infinite;
}

.first-section::before {
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
  padding: 100px;
  align-items: center;
  color: white;
  z-index: 1;
}

.box1 {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 64px;
  text-align: left;
  padding-left: 45px;
}

.box2 {
  margin-left: 115px;
  align-self: center;
  background: rgba(0, 0, 0, 0.4);
  height: 600px;
  width: 475px;
  padding: 45px;
  border-radius: 20px;
}

.scroll-indicator {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  cursor: pointer;
}

.arrow {
  opacity: 0;
  position: relative;
  transform-origin: 50% 50%;
}

.arrow-first {
  animation: arrow-movement 2s ease-in-out infinite;
}

.arrow-second {
  animation: arrow-movement 2s 1s ease-in-out infinite;
}

.arrow:before,
.arrow:after {
  background: #ecb27b;
  content: "";
  display: block;
  height: 3px;
  position: absolute;
  top: 0;
  left: 0;
  width: 30px;
}

.arrow:before {
  transform: rotate(45deg) translateX(-23%);
  transform-origin: top left;
}

.arrow:after {
  transform: rotate(-45deg) translateX(23%);
  transform-origin: top right;
}

@keyframes arrow-movement {
  0% {
    opacity: 0;
    transform: translateY(-10px);
  }
  70% {
    opacity: 1;
  }
  100% {
    opacity: 0;
    transform: translateY(10px);
  }
}

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
