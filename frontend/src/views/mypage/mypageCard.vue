<template>
  <div class="mypage-card">
    <div v-if="!mypageStore?.userCards?.length" class="empty-state">
      현재 보유중인 카드가 없습니다.
    </div>
    <div v-else class="grid-container">
      <div class="grid-item" v-for="card in mypageStore.totalCards" :key="card.cardId">
        <div class="card-image" :class="{ 'grayscale': !isUserCard(card.cardId) }"
          :style="{ backgroundImage: `url(${card.image1})` }">
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useMypageStore } from '@/store/mypageStore'

const mypageStore = useMypageStore()

const isUserCard = (cardId) => {
  return mypageStore.userCards.some(userCard => userCard.cardId === cardId)
}
</script>

<style scoped>
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
  aspect-ratio: 1;
  /* 1:1 비율 유지 */
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

.grayscale {
  filter: brightness(40%) grayscale(80%);
}

.card-image:hover {
  transform: scale(1.05);
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-family: "Pretendard-Medium";
  font-size: 20px;
  color: #777777;
  text-align: center;
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