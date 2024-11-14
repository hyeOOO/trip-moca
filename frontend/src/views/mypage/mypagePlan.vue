<template>
  <div class="mypage-plan">
    <div class="grid-container">
      <div class="grid-item" v-for="(item, index) in items" :key="index">
        <div class="card shadow-sm">
          <span class="img" :style="{ backgroundImage: `url(${item.imgPath})` }"></span>
          <div class="card-body">
            <font-awesome-icon :icon="['fas', 'location-dot']" :style="{ color: '#ECB27B' }" />
            <span class="location">{{ item.location }}</span>
            <p class="card-text">
              <span class="title">{{ item.title }}</span>
            </p>
            <p class="dates">{{ item.dates }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "mypagePlan",
  data() {
    return {
      items: [
        {
          imgPath:
            "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/jeju-island-sunset.jpg",
          location: "제주",
          title: "귤 조 아",
          dates: "2024.12.05~2024.12.08",
        },
        {
          imgPath:
            "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gyeongbok-palace-5771324_1280.jpg",
          location: "서울",
          title: "서울 여행",
          dates: "2024.12.10~2024.12.12",
        },
        {
          imgPath: "https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/no-image.png",
          location: "부산",
          title: "부산 여행",
          dates: "2024.12.15~2024.12.17",
        },
      ],
    };
  },
};
</script>

<style scoped>
.mypage-plan {
  padding: 20px 0;
}

/* 스크롤바 스타일링 */
.mypage-plan::-webkit-scrollbar {
  width: 8px;
}

.mypage-plan::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.mypage-plan::-webkit-scrollbar-thumb {
  background: #ecb27b;
  border-radius: 4px;
}

.mypage-plan::-webkit-scrollbar-thumb:hover {
  background: #dca06a;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 0 10px;
}

.grid-item {
  width: 100%;
}

.card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
  height: 100%;
  border: 1px solid #eee;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.card .img {
  display: block;
  width: 100%;
  height: 250px;
  background-size: cover;
  background-position: center;
}

.card-body {
  padding: 15px;
}

.card-text {
  margin: 0;
}

.location {
  font-family: "Pretendard-Medium";
  font-size: 18px;
  color: #000;
  margin: 10px 10px;
}

.card-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.title {
  font-size: 28px;
  font-family: "Pretendard-SemiBold";
  margin: 0;
  color: #000;
}

.dates {
  font-size: 15px;
  font-family: "Pretendard-Regular";
  margin: 0 0 30px 0;
  color: #777777;
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
