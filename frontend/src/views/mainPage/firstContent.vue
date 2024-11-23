<template>
  <!-- 배경으로 사용할 갤러리 컨테이너 -->
  <div class="background-container">
    <div class="Container">
      <div
        v-for="(picture, index) in pictures"
        :key="index"
        class="Picture"
        :style="picture.style"
        @mousedown="startDrag($event, index)"
        @touchstart="startDrag($event, index)"
      >
        <img class="Picture-img" :src="picture.src" :alt="picture.alt" />
      </div>
    </div>
  </div>

  <!-- 메인 컨텐츠 -->
  <div class="main-container">
    <!-- 이미지 로더 -->
    <div class="imgLoader"></div>

    <!-- 메인 컨테이너 -->
    <div class="container">
      <div class="TitleContainer">
        <h1 class="Title">
          인생샷 명소부터 숨은 맛집까지,<br />트립모카와 함께 써내려가는
          우리만의 여행 이야기
        </h1>
      </div>
      <!-- 책 컴포넌트 -->
      <div class="book">
        <div class="gap"></div>
        <div class="pages">
          <div class="page" v-for="n in 6" :key="n"></div>
        </div>
        <div class="flips">
          <div class="flip flip1">
            <div class="flip flip2">
              <div class="flip flip3">
                <div class="flip flip4">
                  <div class="flip flip5">
                    <div class="flip flip6">
                      <div class="flip flip7"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";

const imageCache = new Map();

const preloadImage = (src) => {
  if (!imageCache.has(src)) {
    const img = new Image();
    img.src = src;
    imageCache.set(src, img);
  }
  return imageCache.get(src);
};

// 책 이미지 수정
const images = [
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/sunrise-6090933_1280.jpg",
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/Jinju.png",
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/jejuStare.png",
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gyeongju.png",
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gyeongbok-palace-5771324_1280.jpg",
  "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/sunrise-6090933_1280.jpg",
];

const pictures = ref([
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/aPalaceOnTheWater.jpg",
    note: "Over the clouds",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/bundo-kim-hipcPLJE4uk-unsplash.jpg",
    note: "Golden Gate Bridge",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/clark-gu-5fa8Z9NXLqI-unsplash.jpg",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/damyang-3381419_1280.jpg",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/damyang-3396598_1280.jpg",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/eveningPalace.jpg",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gangneung-7111058_1280.jpg",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gangneung.png",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gwanganBridge.jpg",
    style: {},
  },
]);

const calculatePositions = (() => {
  const cache = new Map();

  return (index, screenWidth, screenHeight, columns, rows) => {
    const cacheKey = `${index}-${screenWidth}-${screenHeight}`;
    if (cache.has(cacheKey)) return cache.get(cacheKey);

    const col = index % columns;
    const row = Math.floor(index / columns) % rows;
    const baseX = (screenWidth / columns) * (col + 0.5);
    const baseY = (screenHeight / rows) * (row + 0.5);
    const offsetX = (Math.random() - 0.5) * ((screenWidth / columns) * 0.5);
    const offsetY = (Math.random() - 0.5) * ((screenHeight / rows) * 0.5);

    const result = {
      x: baseX + offsetX,
      y: baseY + offsetY,
    };

    cache.set(cacheKey, result);
    return result;
  };
})();

function debounce(fn, delay) {
  let timeoutId;
  return function (...args) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn.apply(this, args), delay);
  };
}

onMounted(() => {
  // 모든 이미지 프리로드
  [...images, ...pictures.value.map((p) => p.src)].forEach(preloadImage);

  const updatePositions = () => {
    const screenWidth = window.innerWidth;
    const screenHeight = window.innerHeight;
    const columns = 3;
    const rows = 3;

    pictures.value.forEach((picture, index) => {
      const { x, y } = calculatePositions(
        index,
        screenWidth,
        screenHeight,
        columns,
        rows
      );
      const randomRotate = Math.random() * 30 - 15;

      picture.style = {
        top: `${y}px`,
        left: `${x}px`,
        transform: `translate(-50%, -50%) rotate(${randomRotate}deg)`,
        zIndex: Math.floor(Math.random() * 10),
        opacity: "0.8",
      };
    });
  };

  // 초기 위치 설정
  updatePositions();

  // 리사이즈 핸들러
  const handleResize = debounce(updatePositions, 150);
  window.addEventListener("resize", handleResize);

  // 컴포넌트 언마운트 시 이벤트 리스너 제거
  onUnmounted(() => {
    window.removeEventListener("resize", handleResize);
  });
});
</script>

<style lang="scss" scoped>
$bookAngle: 60deg;
$speed: 5s;
$borderColor: #555;

// 책 이미지 수정
$images: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/sunrise-6090933_1280.jpg"),
  url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/Jinju.png"),
  url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/jejuStare.png"),
  url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gyeongju.png"),
  url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gyeongbok-palace-5771324_1280.jpg"),
  url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/sunrise-6090933_1280.jpg");

// 믹스인
@mixin fixed-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
}

@mixin page-dimensions {
  width: 420px;
  height: 450px;
  background-size: 840px 450px;
}

@mixin transform-3d {
  transform-style: preserve-3d;
  perspective-origin: center;
}

// 기본 스타일 리셋
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

// 레이아웃 컴포넌트
.background-container {
  @include fixed-fullscreen;
  z-index: -1;
  pointer-events: none;
  overflow: hidden;
}

.main-container {
  position: relative;
  z-index: 1;
}

.container {
  position: relative;
  width: 840px;
  height: 630px;
  margin: 0 auto; // 가운데 정렬을 위해 추가
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

// 이미지 로더
.imgLoader {
  position: fixed;
  width: 1px;
  height: 1px;
  animation: preLoad 1s steps(1);
}

// 제목 스타일
.TitleContainer {
  position: fixed;
  top: 120px;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 20;
}

.Title {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 50px;
  color: #000;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  padding: 20px;
}

// 책 컴포넌트
.book {
  position: relative;
  perspective: 1260px;
  perspective-origin: center 75px;
  transform: scale(1);
  margin-top: 50px;
  filter: drop-shadow(0px 10px 5px rgba(0, 0, 0, 0.25));
  @include transform-3d;
}

// 페이지 베이스
%page-base {
  @include page-dimensions;
  position: absolute;
  top: 0;
}

// 페이지 스타일
.page {
  @extend %page-base;
  background-color: #fff;
  right: 50%;
  border: solid $borderColor 1px;
  transform-origin: 100% 100%;
  background-position: center;
  @include transform-3d;

  @for $i from 1 through 6 {
    &:nth-child(#{$i}) {
      @if $i == 3 {
        transform: rotateX($bookAngle) rotateY(6deg);
        animation: nextPage $speed * 5 infinite $speed * -4.8 steps(1);
        background-position: -2px -2px;
      } @else if $i == 4 {
        transform: rotateX($bookAngle) rotateY(177deg);
      } @else if $i == 5 {
        transform: rotateX($bookAngle) rotateY(175.5deg);
      } @else if $i == 6 {
        transform: rotateX($bookAngle) rotateY(174deg);
        overflow: hidden;

        &::after {
          @extend %page-base;
          content: "";
          right: 0;
          transform: rotateY(180deg);
          animation: nextPage $speed * 5 $speed * -4 infinite steps(1);
          background-position: 100% -2px;
        }
      } @else {
        transform: rotateX($bookAngle) rotateY(#{3 + ($i - 1) * 1.5}deg);
      }
    }
  }
}

// 갭 스타일
.gap {
  width: 10px;
  height: 450px;
  transform: rotateX($bookAngle);
  transform-origin: bottom;
  position: absolute;
  top: 0;
  left: calc(50% - 5px);

  &::after {
    content: "";
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translate(-50%, 50%);
    background-color: $borderColor;
    width: 10px;
    height: 5px;
    border-radius: 50%;
  }
}

// 플립 스타일
.pages,
.flips {
  @include transform-3d;
}

.flip {
  @extend %page-base;
  width: 64px;
  transform-origin: 100% 100%;
  right: 100%;
  border: solid $borderColor;
  border-width: 1px 0;
  perspective: 8400px;
  @include transform-3d;

  &::after {
    content: "";
    position: absolute;
    inset: 0;
    transform-origin: center;
    background-size: 840px 450px;
  }

  &.flip1 {
    right: 50%;
    animation: flip1 $speed infinite ease-in-out;
    border-width: 1px 1px 1px 0;

    &::after {
      animation: nextFlip1 $speed * 5 $speed * -4 infinite steps(1);
    }
  }

  @for $i from 2 through 7 {
    &.flip#{$i} {
      @if $i == 7 {
        width: 60px;
        border-width: 1px 0 1px 1px;
      } @else {
        right: calc(100% - 2px);
        top: -2px;
        transform-origin: right;
        animation: flip2 $speed ease-in-out infinite;
      }
      &::after {
        animation: nextFlip#{$i} $speed * 5 $speed * -4 infinite steps(1);
      }
    }
  }
}

// 갤러리 스타일
.Container {
  @include fixed-fullscreen;
}

.Picture {
  position: fixed;
  border: 5px solid #fff;
  border-radius: 3px;
  background: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
  transform: translate(-50%, -50%);
  user-select: none;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0.8;

  &-img {
    display: block;
    width: 350px; // 250px에서 350px로 수정
    height: 350px; // 250px에서 350px로 수정
    pointer-events: none;
    object-fit: cover;
  }
}

// 애니메이션
@keyframes preLoad {
  @for $i from 0 through 4 {
    #{$i * 10}% {
      background-image: nth($images, ($i + 1));
    }
  }
  100% {
    display: none;
  }
}

@keyframes nextPage {
  @for $i from 0 through 4 {
    #{$i * 20}% {
      background-image: nth($images, ($i + 1));
    }
  }
}

@keyframes flip1 {
  0%,
  20% {
    transform: rotateX($bookAngle) rotateY(6deg);
  }
  80%,
  100% {
    transform: rotateX($bookAngle) rotateY(174deg);
  }
}

@keyframes flip2 {
  0%,
  20% {
    transform: rotateY(0deg) translateY(0);
  }
  50% {
    transform: rotateY(-15deg) translateY(0);
  }
}

@mixin flip-animation($i) {
  @keyframes nextFlip#{$i} {
    @for $j from 0 through 4 {
      #{$j * 20}% {
        background-image: nth($images, ($j + 1));
        background-position: if(
          $i == 7,
          -2px -2px,
          #{-296 + (($i - 2) * 60)}px -2px
        );
        transform: rotateY(0deg);
      }
      #{if($i == 7, 13 + ($j * 20), (10 + ($j * 20)) + (($i - 1) * 0.5))}% {
        background-image: nth($images, ($j + 2));
        background-position: if(
          $i == 7,
          -776px -2px,
          #{-476 - (($i - 2) * 60)}px -2px
        );
        transform: rotateY(180deg);
      }
    }
  }
}

@for $i from 1 through 7 {
  @include flip-animation($i);
}
</style>
