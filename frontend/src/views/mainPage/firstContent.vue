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

<style scoped>
.first-section {
  display: flex;
  flex-direction: column;
  font-family: "EliceDigitalBaeum_Regular";
  text-align: center;
  width: 100%;
  height: 100vh; /* 전체 높이를 100vh로 제한 */
  overflow: hidden; /* 초과되는 콘텐츠를 숨김 */
}

.photo-grid {
  display: grid;
  grid-template-rows: 2fr 1fr 2fr;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 20px;
  width: 85%;
  height: calc(100% - 60px); /* 제목과 여백을 제외한 높이 */
  margin: 0 auto;
}

.first-title {
  text-align: center;
  font-size: 40px;
  margin-bottom: 20px; /* 간격 축소 */
}

.first-box1 p {
  margin: 24px 140px 0 24px;
  font-size: 14px;
  text-align: left;
}

.first-box1 button {
  width: 150px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 10px 20px;
  margin: 20px auto 10px; /* 버튼을 중앙에 정렬 */
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
  object-fit: cover; /* 이미지 비율 유지하며 박스에 맞춤 */
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.first-box2 img:hover,
.first-box3 img:hover,
.first-box4 img:hover {
  transform: scale(1.05); /* 이미지 크기 확대 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2); /* 그림자 효과 */
}
</style>
