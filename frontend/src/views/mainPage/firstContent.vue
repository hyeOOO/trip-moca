<template>
  <section class="first-section">
    <h1 class="first-title">
      인생샷 명소부터 숨은 맛집까지,<br />우리만의 트래블 큐레이션
    </h1>
    <br />
    <content-box class="photo-grid" >
      <div class="first-box1" style="grid-column: 1 / 2;
    grid-row: 1 / 2;
    padding: 40px 20px;">
        <p>
          나는 무엇인지 그리워 이 많은 별빛이 내린 언덕 위에 내 이름자를 써보고
          흙으로 덮어 버리었읍니다. 딴은 밤을 세워 우는 벌레는 부끄러운 이름을
          슬퍼하는 까닭입니다. 어머님, 그리고 당신은 멀리 북간도에 계십니다.
          계절이 지나가는 하늘에는 가을로 가득 차 있습니다.
        </p>
        <button>살펴보러가기</button>
      </div>
      <div class="first-box2" style="grid-row: 1/3; grid-column: 2/4">
        <img src="@/assets/image/photo1.png" />
      </div>
      <div class="first-box3" style="grid-row: 2/4; grid-column: 1/2">
        <img src="@/assets/image/photo2.png" />
      </div>
      <div class="first-box4" style="grid-row: 3/4; grid-column: 2/3">
        <img src="@/assets/image/photo3.png"  />
      </div>
    </content-box>
  </section>
</template>

<script>
export default {
  name: "firstContent",
};
</script>

<style>
.first-section {
  display: flex;
  flex-direction: column;
  font-family: "EliceDigitalBaeum_Regular";
  text-align: center;
  width: 100%;
  height: 100vh;
  margin-top: 120px;
}

.photo-grid {
  display: grid;
  grid-template-rows: 2fr 1fr 2fr;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 20px;
  width: 85%;
  height: 100%;
  margin: 0 auto;
}

.first-title {
  text-align: center;
  font-size: 40px;
  margin-bottom: 60px;
}

.first-box1 p {
  margin-left: 24px;
  font-size: 14px;
  text-align: left;
  margin-right: 140px;
  margin-top: 60px;
}

.first-box1 button {
  width: 150px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 10px 20px;
  margin: 40px 560px 10px 10px;
  font-size: 16px;
  cursor: pointer;
}

.first-box1 button:hover {
  transform: scale(1.05);
  background-color: #ffdd57;
}
.first-box4 {
  grid-column: 2 / 2;
  grid-row: 2 / 3;
}

.first-box2 img,
.first-box3 img,
.first-box4 img {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease, box-shadow 0.3s ease; /* 부드러운 전환 효과 */
}

.first-box2 img:hover,
.first-box3 img:hover,
.first-box4 img:hover {
  transform: scale(1.05); /* 이미지 크기 확대 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2); /* 그림자 효과 */
}
</style>