<template>
  <div class="gallery">
    <div class="carousel-container">
      <div class="carousel-track" :style="trackStyle">
        <div
          v-for="(attraction, index) in attractions"
          :key="attraction.attractionId"
          class="carousel-slide"
          :class="{ active: currentIndex === index }"
          @click="handleAttractionClick(attraction.title)"
        >
          <img
            :src="
              attraction.image1 ||
              'https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png'
            "
            :alt="attraction.title"
          />
          <div class="slide-content">
            <!-- 태그 컨테이너 추가 -->
            <div class="tags-container">
              <span v-if="attraction.sidoName" class="location-tag sido">
                {{ attraction.sidoName }}
              </span>
              <span v-if="attraction.gugunName" class="location-tag gugun">
                {{ attraction.gugunName }}
              </span>
              <span v-if="attraction.contentTypeName" class="location-tag type">
                {{ attraction.contentTypeName }}
              </span>
            </div>
            <h3>{{ attraction.title }}</h3>
            <p class="address">{{ attraction.addr1 }} {{ attraction.addr2 }}</p>
          </div>
        </div>
      </div>

      <button class="carousel-button prev" @click="prev" aria-label="Previous slide">
        <span class="arrow">&lt;</span>
      </button>
      <button class="carousel-button next" @click="next" aria-label="Next slide">
        <span class="arrow">&gt;</span>
      </button>

      <div class="carousel-indicators">
        <button
          v-for="(_, index) in attractions"
          :key="index"
          class="indicator"
          :class="{ active: currentIndex === index }"
          @click="goToSlide(index)"
          :aria-label="'Go to slide ' + (index + 1)"
        ></button>
      </div>
    </div>
  </div>
</template>

<script>
import api from "@/plugins/axios";

export default {
  name: "CarouselComponent",
  data() {
    return {
      currentIndex: 0,
      attractions: [],
      isAnimating: false,
      autoplayInterval: null,
      isLoading: false,
      error: null,
    };
  },
  computed: {
    trackStyle() {
      return {
        transform: `translateX(-${this.currentIndex * 100}%)`,
      };
    },
  },
  async created() {
    await this.fetchPopularAttractions();
  },
  mounted() {
    this.startAutoplay();
  },
  beforeDestroy() {
    this.stopAutoplay();
  },
  methods: {
    async handleAttractionClick(title) {
      try {
        this.$router.push({
          name: "keywordPlan",
          params: { keyword: title },
        });
      } catch (error) {
        console.error("Error handling attraction click:", error);
      }
    },
    async fetchPopularAttractions() {
      try {
        this.isLoading = true;
        const response = await api.get("/domain/attraction/popular/attractions");
        this.attractions = response.data;
      } catch (error) {
        console.error("Failed to fetch popular attractions:", error);
        this.error = "인기 관광지를 불러오는데 실패했습니다.";
      } finally {
        this.isLoading = false;
      }
    },
    truncateText(text) {
      if (!text) return "";
      return text.length > 100 ? text.substring(0, 100) + "..." : text;
    },
    async next() {
      if (this.isAnimating || !this.attractions.length) return;
      this.isAnimating = true;
      this.currentIndex = (this.currentIndex + 1) % this.attractions.length;
      await this.waitForTransition();
      this.isAnimating = false;
    },
    async prev() {
      if (this.isAnimating || !this.attractions.length) return;
      this.isAnimating = true;
      this.currentIndex =
        (this.currentIndex - 1 + this.attractions.length) % this.attractions.length;
      await this.waitForTransition();
      this.isAnimating = false;
    },
    async goToSlide(index) {
      if (this.isAnimating || this.currentIndex === index) return;
      this.isAnimating = true;
      this.currentIndex = index;
      await this.waitForTransition();
      this.isAnimating = false;
    },
    waitForTransition() {
      return new Promise((resolve) => {
        setTimeout(resolve, 500);
      });
    },
    startAutoplay() {
      this.autoplayInterval = setInterval(this.next, 5000);
    },
    stopAutoplay() {
      if (this.autoplayInterval) {
        clearInterval(this.autoplayInterval);
      }
    },
  },
};
</script>

<style scoped>
.gallery {
  position: relative;
  width: 100%;
  height: 80vh;
  background: #433629;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}

.carousel-container {
  position: relative;
  width: 70%;
  height: 60vh;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.carousel-track {
  display: flex;
  height: 100%;
  transition: transform 0.5s ease-out;
}

.carousel-slide {
  flex: 0 0 100%;
  position: relative;
  overflow: hidden;
}

.carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.carousel-slide:hover img {
  transform: scale(1.05);
}

.slide-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 2rem;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
  color: white;
  transform: translateY(20%);
  opacity: 0;
  transition: all 0.5s ease;
}

.carousel-slide.active .slide-content {
  transform: translateY(0);
  opacity: 1;
}

/* 태그 스타일 */
.tags-container {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.location-tag {
  padding: 0.25rem 0.5rem;
  border-radius: 0.5rem;
  font-size: 12px;
  font-family: "Pretendard-SemiBold";
  color: white;
}

.sido {
  background-color: #433629;
}

.gugun {
  background-color: #6e6156;
}

.type {
  background-color: #ecb27b;
}

.slide-content h3 {
  font-size: 28px;
  margin: 0.5rem 0;
  font-family: "Pretendard-Bold";
}

.address {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.8);
  font-family: "Pretendard-Regular";
}

/* 기존 버튼 및 인디케이터 스타일 유지 */
.carousel-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
}

.carousel-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-50%) scale(1.1);
}

.carousel-button.prev {
  left: 20px;
}

.carousel-button.next {
  right: 20px;
}

.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid white;
  background: transparent;
  cursor: pointer;
  padding: 0;
  transition: all 0.3s ease;
}

.indicator.active {
  background: white;
  transform: scale(1.2);
}

@media (max-width: 768px) {
  .carousel-container {
    width: 95%;
    height: 60vh;
  }

  .slide-content h3 {
    font-size: 1.5rem;
  }

  .carousel-button {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
}

@media (max-width: 480px) {
  .carousel-container {
    height: 50vh;
  }

  .slide-content {
    padding: 1rem;
  }

  .slide-content h3 {
    font-size: 1.2rem;
  }
}
</style>
