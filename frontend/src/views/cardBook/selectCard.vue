<template>
  <div>
    <navBar />
    <div class="cards-container">
      <div class="cards">
        <div v-for="(card, index) in cards" :key="index" class="card">
          <div class="card-face">
            <img
              v-if="card.imageUrl"
              :src="card.imageUrl"
              :alt="`Card ${index + 1}`"
              class="card-image"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import navBar from '@/components/navBar.vue'

const cards = [
  {
    imageUrl:
      "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_incheon.png",
  },
  {
    imageUrl:
      "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_jeju.png",
  },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_seoul.png" },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_daejeon.png" },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_busan.png" },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_gyeonggi.png" },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_gyeongnam.png" },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_jeonnam.png" },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_ulsan.png" },
  { imageUrl: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_daegu.png" },
];
</script>

<style scoped>
.cards-container {
  flex: 1;
  position: relative;
}

body {
  background: radial-gradient(#333, #111);
  overflow: hidden;
}

.cards {
  bottom: 0;
  display: flex;
  height: 50vh;
  padding: 0 50px;
  justify-content: center;
  position: fixed;
  left: 0;
  right: 0;
}

.card {
  height: 290px;
  width: 210px;
  margin: 0 -25px;
  position: relative;
}

.card:after {
  bottom: 0;
  content: "";
  left: -60px;
  position: absolute;
  right: -60px;
  top: 0px;
  z-index: 10;
}

.card-face {
  bottom: 0;
  left: 0;
  pointer-events: none;
  position: absolute;
  right: 0;
  top: 0;
  transition: 800ms cubic-bezier(0.19, 1, 0.22, 1) transform;
}

.card-face:after {
  animation: none;
  bottom: 0;
  content: "";
  left: 0;
  opacity: 0;
  position: absolute;
  right: 0;
  top: 0;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.card:nth-child(1) .card-face {
  transform: translateY(45px) rotate(-28.125deg);
}

.card:nth-child(2) .card-face {
  transform: translateY(35px) rotate(-21.875deg);
}

.card:nth-child(3) .card-face {
  transform: translateY(25px) rotate(-15.625deg);
}

.card:nth-child(4) .card-face {
  transform: translateY(15px) rotate(-9.375deg);
}

.card:nth-child(5) .card-face {
  transform: translateY(5px) rotate(-3.125deg);
}

.card:nth-child(6) .card-face {
  transform: translateY(5px) rotate(3.125deg);
}

.card:nth-child(7) .card-face {
  transform: translateY(15px) rotate(9.375deg);
}

.card:nth-child(8) .card-face {
  transform: translateY(25px) rotate(15.625deg);
}

.card:nth-child(9) .card-face {
  transform: translateY(35px) rotate(21.875deg);
}

.card:nth-child(10) .card-face {
  transform: translateY(45px) rotate(28.125deg);
}

.card:hover .card-face {
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.4);
  transform: translateY(-100px) rotate(0deg) scale(2);
  transition-duration: 0ms;
  z-index: 5;
}

.card:hover .card-face:after {
  animation: fade 250ms ease-out forwards;
}

.card:hover:after {
  top: -175px;
}

@keyframes fade {
  0% {
    opacity: 0.9;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.15);
  }
}


</style>