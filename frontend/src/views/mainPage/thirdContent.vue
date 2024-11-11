<template>
  <section class="third-section">
    <h1 class="third-title">시간은 아끼고 추억은 더하고⚡️</h1>
    <p>
      나는 무엇인지 그리워 이 많은 별빛이 내린 언덕 위에 내 이름자를 써보고
      흙으로 덮어 버리었습니다. 딴은 밤을 세워<br />
      우는 벌레는 부끄러운 이름을 슬퍼하는 까닭입니다. 어머님, 그리고 당신은
      멀리 북간도에 계십니다. 계절이 지나가는 하늘에<br />
      는 가을로 가득 차 있습니다.
    </p>
    <div class="map-container">
      <img src="@/assets/image/map.png" alt="지도 이미지" class="map-image" />
    </div>
  </section>
</template>

<script>
export default {};
</script>

<style>
.third-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  min-height: 100vh;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
}


.third-title {
   display: flex;
  text-align: center;
  font-size: 40px;
  margin-bottom: 0px;
}

.third-section p {
  font-family: "EliceDigitalBaeum_Regular";
  text-align: center;
  width: 100%;
  padding: 10px;
}
.map-imgae {
  align-items: center;
}
/* 지도 섹션 */
.map-container {
  height: 400px;
  background: #f0f0f0;
  border-radius: 10px;
  margin-top: 40px;
}

</style>
