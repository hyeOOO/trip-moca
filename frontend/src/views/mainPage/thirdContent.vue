<template>
  <div class="TitleContainer">
    <h1 class="Title">시간은 아끼고 추억은 더하고 ⚡️</h1>
  </div>
  <div class="Container">
    <!-- 기존 갤러리 코드 -->
    <div
      v-for="(picture, index) in pictures"
      :key="index"
      class="Picture"
      :style="picture.style"
      @mousedown="startDrag($event, index)"
      @touchstart="startDrag($event, index)"
    >
      <img class="Picture-img" :src="picture.src" :alt="picture.alt" />
      <div class="Picture-note">
        <span>
          {{ picture.note }}
          <template v-if="picture.social">
            <a
              v-for="(link, platform) in picture.social"
              :key="platform"
              class="Network"
              :href="link.url"
              target="_top"
            >
              <img :src="link.icon" :alt="platform" />
            </a>
          </template>
        </span>
      </div>
    </div>
  </div>
  <a class="GitLink" href="https://github.com/bamtol2/enjoy-trip" target="_top"
    >Hyewon & Seungwoo</a
  >
</template>

<script setup>
import { ref, onMounted } from "vue";

// 이전 pictures 데이터 동일...
const pictures = ref([
  {
    src: "https://media.licdn.com/dms/image/C4D03AQHjPuY9oi3Www/profile-displayphoto-shrink_800_800/0/1579768452204?e=2147483647&v=beta&t=4ZgTgOkSEu2eKRoLWEVtgL8s2zYu80YMxI_0018U9Dk",
    note: "@DeyJordan - ",
    social: {
      codepen: {
        url: "https://codepen.io/DeyJordan",
        icon: "https://cdn-icons-png.flaticon.com/512/2111/2111501.png",
      },
      twitter: {
        url: "https://twitter.com/DeyJordan",
        icon: "https://www.freepnglogos.com/uploads/twitter-logo-png/twitter-logo-vector-png-clipart-1.png",
      },
    },
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/aPalaceOnTheWater.jpg",
    note: "Over the clouds",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/bundo-kim-hipcPLJE4uk-unsplash.jpg",
    note: "Golden Gate Bridge - San Francisco",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/clark-gu-5fa8Z9NXLqI-unsplash.jpg",
    note: "Cat nose",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/damyang-3381419_1280.jpg",
    note: "Cat nose",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/damyang-3396598_1280.jpg",
    note: "Cat nose",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/eveningPalace.jpg",
    note: "Cat nose",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gangneung-7111058_1280.jpg",
    note: "Cat nose",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gangneung.png",
    note: "Cat nose",
    style: {},
  },
  {
    src: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gwanganBridge.jpg",
    note: "Cat nose",
    style: {},
  },
]);

let previousTouch = null;

const updateElementPosition = (element, event, index) => {
  let movementX, movementY;

  if (event.type === "touchmove") {
    const touch = event.touches[0];
    movementX = previousTouch ? touch.clientX - previousTouch.clientX : 0;
    movementY = previousTouch ? touch.clientY - previousTouch.clientY : 0;
    previousTouch = touch;
  } else {
    movementX = event.movementX;
    movementY = event.movementY;
  }

  const currentTop = parseInt(pictures.value[index].style.top || 0);
  const currentLeft = parseInt(pictures.value[index].style.left || 0);

  pictures.value[index].style = {
    ...pictures.value[index].style,
    top: `${currentTop + movementY}px`,
    left: `${currentLeft + movementX}px`,
  };
};

const startDrag = (event, index) => {
  const updateFunc = (e) => updateElementPosition(event.target, e, index);
  const stopFunc = () => stopDrag({ update: updateFunc, stop: stopFunc });

  document.addEventListener("mousemove", updateFunc);
  document.addEventListener("touchmove", updateFunc);
  document.addEventListener("mouseup", stopFunc);
  document.addEventListener("touchend", stopFunc);
};

const stopDrag = (functions) => {
  previousTouch = null;
  document.removeEventListener("mousemove", functions.update);
  document.removeEventListener("touchmove", functions.update);
  document.removeEventListener("mouseup", functions.stop);
  document.removeEventListener("touchend", functions.stop);
};

onMounted(() => {
  const screenWidth = window.innerWidth;
  const screenHeight = window.innerHeight;
  const titleHeight = 120;
  const minDistance = 400; // 사진 간 최소 거리

  const usedPositions = [];

  pictures.value.forEach((picture, index) => {
    let validPosition = false;
    let attempts = 0;
    let randomX, randomY;

    while (!validPosition && attempts < 50) {
      const maxX = screenWidth * 0.3;
      const maxY = (screenHeight - titleHeight) * 0.4;

      randomX = (Math.random() * 2 - 1) * maxX;
      randomY = (Math.random() * 2 - 1) * maxY + titleHeight / 2;

      // 다른 사진들과의 거리 체크
      validPosition = usedPositions.every((pos) => {
        const distance = Math.sqrt(
          Math.pow(pos.x - randomX, 2) + Math.pow(pos.y - randomY, 2)
        );
        return distance >= minDistance;
      });

      attempts++;
    }

    usedPositions.push({ x: randomX, y: randomY });

    const randomRotate = Math.random() * 30 - 15;

    picture.style = {
      top: `${randomY}px`,
      left: `${randomX}px`,
      transform: `translate(-50%, -50%) rotate(${randomRotate}deg)`,
      zIndex: Math.floor(Math.random() * 10),
    };
  });
});
</script>

<style scoped>
.TitleContainer {
  position: fixed;
  top: 40px;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 20;
}

.Title {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 48px;
  color: #000000;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  margin: 0;
  padding: 20px;
}

.Container {
  position: absolute;
  top: 50%;
  left: 50%;
}

.Picture {
  display: inline-block;
  position: absolute;
  top: 0;
  left: 0;
  border: 5px solid #fff;
  border-radius: 3px;
  background: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  transform: translate(-50%, -50%);
  user-select: none;
  cursor: pointer;
}

.Picture-img {
  display: block;
  width: 300px;
  height: 300px;
  pointer-events: none;
}

.Picture-note {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 300px;
  height: 70px;
  padding: 12px 24px;
  font-size: 1.5rem;
  text-align: center;
}

.Network {
  display: inline-block;
  padding: 0 5px;
}

.Network img {
  width: 1.5rem;
  aspect-ratio: 1 / 1;
  vertical-align: middle;
}

.GitLink {
 position: fixed;
 z-index: 15;
 bottom: 20px;
 right: 20px; /* left: 50%와 transform: translateX(-50%) 대신 right: 20px 사용 */
 color: #111;
 font-weight: 700;
 text-align: right;
 opacity: 0.7;
}

* {
  box-sizing: border-box;
}
</style>
