<template>
  <div class="mypage-card">
    <div class="grid-container">
      <div class="grid-item" v-for="(item, index) in items" :key="index">
        <div class="card-image" :style="{ backgroundImage: `url(${item.imgPath})` }"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "mypageCard",
  data() {
    return {
      items: [
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/hamo.png",
          location: "제주",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/boogie.png",
          location: "서울",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/boogie.png",
          location: "부산",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/hamo.png",
          location: "제주",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/hamo.png",
          location: "제주",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/hamo.png",
          location: "제주",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/hamo.png",
          location: "제주",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/hamo.png",
          location: "제주",
        },
      ],
    };
  },
};
</script>

<style scoped>
.mypage-card {
  padding: 20px 0;
}

/* 스크롤바 스타일링 */
.mypage-card::-webkit-scrollbar {
  width: 8px;
}

.mypage-card::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.mypage-card::-webkit-scrollbar-thumb {
  background: #ecb27b;
  border-radius: 4px;
}

.mypage-card::-webkit-scrollbar-thumb:hover {
  background: #dca06a;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 0 10px;
}

.grid-item {
  aspect-ratio: 1; /* 1:1 비율 유지 */
  width: 100%;
}

.card-image {
  width: 100%;
  height: 100%;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  transition: transform 0.2s;
  cursor: pointer;
}

.card-image:hover {
  transform: scale(1.05);
}

/* 반응형 그리드 */
@media (max-width: 1400px) {
  .grid-container {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1100px) {
  .grid-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .grid-container {
    grid-template-columns: 1fr;
  }
}
</style>
