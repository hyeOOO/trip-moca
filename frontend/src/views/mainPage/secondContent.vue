<template>
  <section class="second-section">
    <p class="second-title">
      발도장 찍고 캐릭터 GET! 내 손안의 귀여운 여행 컬렉션 📝 
      <button>카드 도감 가기</button>
    </p>
    <div class="card-grid">
      <div
        v-for="(card, index) in 5" 
        :key="index"
        class="character-card"
      >
        <img
          :src="require(`@/assets/image/character${(index % 2) + 1}.png`)"
          alt="캐릭터 카드"
          class="card-image"
        />
      </div>
    </div>
  </section>
</template>

<script>
export default {
  methods: {
    getImageUrl(index) {
      return require(`@/assets/image/character${(index % 2) + 1}.png`);
    }
  }
};
</script>


<style>
.second-section {
  font-family: "EliceDigitalBaeum_Regular";
  text-align: center;
  width: 100%;
  min-height: 100vh; /* 화면 전체 높이를 채우도록 설정 */
  padding: 20px 100px;
}

.second-title {
  display: flex;
  align-items: center;
  text-align: left;
  font-size: 40px;
  margin-bottom: 0px;
}

.second-section button{
  width: 150px;
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 10px 20px;
  margin-left: 500px;
  font-size: 16px;
  cursor: pointer;
}

.second-section button:hover{
  transform: scale(1.05);
  background-color: #ffdd57;
}

/* 카드 그리드 스타일 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); /* 카드가 화면 크기에 맞춰 늘어나도록 설정 */
  gap: 20px;
  width: 100%;
  height: 100vh; /* 뷰포트의 전체 높이를 차지하도록 설정 */
  padding: 10px;
  margin: 0;
  box-sizing: border-box;
}

/* 카드 크기를 설정 */
.character-card {
  background-color: white;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%; /* 그리드 셀의 너비에 맞게 조정 */
  height: 80%; /* 그리드 셀의 높이에 맞게 조정 */
}

.card-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

</style>
