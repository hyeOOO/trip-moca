<template>
  <div class="third-section">
    <div class="TitleContainer">
      <h1 class="Title">
        인생샷 명소부터 숨은 맛집까지,<br />우리만의 트래블 큐레이션
      </h1>
    </div>
    <div class="content-container">
      <div class="photo-grid">
        <div class="first-box1">
          <p>
            나는 무엇인지 그리워 이 많은 별빛이 내린 언덕 위에 내 이름자를 써보고
            흙으로 덮어 버리었읍니다. 딴은 밤을 세워 우는 벌레는 부끄러운 이름을
            슬퍼하는 까닭입니다. 어머님, 그리고 당신은 멀리 북간도에 계십니다.
            계절이 지나가는 하늘에는 가을로 가득 차 있습니다.
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
  font-size: 48px;
  color: #000;
  margin: 0;
  padding: 20px;
}

.content-container {
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
  background-color: #ffdd57;
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