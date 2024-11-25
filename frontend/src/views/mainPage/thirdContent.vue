<template>
  <div class="third-section">
    <div class="TitleContainer">
      <h1 class="Title">
        인생샷 명소부터 숨은 맛집까지🥹,<br />우리만의 트래블 큐레이션
      </h1>
    </div>
    <div class="content-container">
      <div class="photo-grid">
        <div class="first-box1">
          <p>
            여행이 항상 예쁘지만은 않아요. 편하지만도 않고요. 때로 여행은 아프고, 심지어 당신의 마음을 부숴놓기도 합니다. <br/>괜찮아요. 여행은 당신을 변화시키거든요. <br/>여행은 당신의 기억과 의식, 마음, 그리고 신체에 흔적을 남겨요. 그리고 당신은 뭔가를 가지고 돌아오죠. 바라건대, 무언가 가치 있는 것을 남겼을 겁니다.
          </p>
          <button class="explore-button">살펴보러가기</button>
        </div>
        <div class="first-box2">
          <img src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gyeongbok-palace-5771324_1280.jpg" alt="Photo 1"/>
        </div>
        <div class="first-box3">
          <img src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/aPalaceOnTheWater.jpg" alt="Photo 2"/>
        </div>
        <div class="first-box4">
          <img src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/sunrise-6090933_1280.jpg" alt="Photo 3"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "thirdContent",
};
</script>

<style scoped>
.third-section {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  position: relative;
  background-color: white;
}

.TitleContainer {
  text-align: center;
  height: 20vh; 
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 20px;
}

.Title {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 46px;
  color: #000;
  margin: 0;
  padding: 20px;
}

.content-container {
  font-family: "Pretendard-Medium";
  height: 77vh; /* 전체의 70% 차지 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.photo-grid {
  display: grid;
  grid-template-rows: 2fr 1fr 2fr;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 20px;
  width: 85%;
  max-width: 1400px;
  height: 70vh; /* content-container 내부에서의 높이 조정 */
  margin: 0 auto;
}

.first-box1 {
  grid-column: 1 / 2;
  grid-row: 1 / 2;
  padding: 40px;
  background-color: white;
  border-radius: 10px;
}

.first-box1 p {
  margin-bottom: 20px;
  font-size: 16px;
  line-height: 1.6;
  text-align: left;
}

.explore-button {
  width: 150px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.explore-button:hover {
  transform: scale(1.05);
  background-color: #ECB27B;
}

.first-box2 {
  grid-row: 1/3;
  grid-column: 2/4;
}

.first-box3 {
  grid-row: 2/4;
  grid-column: 1/2;
}

.first-box4 {
  grid-row: 3/4;
  grid-column: 2/3;
}

.first-box2,
.first-box3,
.first-box4 {
  overflow: hidden;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.first-box2 img,
.first-box3 img,
.first-box4 img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.first-box2 img:hover,
.first-box3 img:hover,
.first-box4 img:hover {
  transform: scale(1.05);
}
</style>