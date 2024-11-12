<template>
  <div class="fourth-section">
    <h1 class="fourth-title">
      우리가 찾은 국내 여행 띵곳 모음.zip <br />지금 바로 확인하기 ✨
    </h1>
    <div class="grid-container">
      <div class="grid-row">
        <div class="grid-item">
          <img
            src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/clark-gu-5fa8Z9NXLqI-unsplash.jpg"
            alt="랜턴이 있는 산책로"
            class="w-full h-full object-cover rounded-lg"
          />
        </div>
        <div class="grid-item">
          <img
            src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/eveningPalace.jpg"
            alt="야간 전통 건물"
            class="w-full h-full object-cover rounded-lg"
          />
        </div>
        <div class="grid-item">
          <img
            src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/damyang-3396598_1280.jpg"
            alt="나무가 있는 길"
            class="w-full h-full object-cover rounded-lg"
          />
        </div>
      </div>
      <div class="grid-row">
        <div class="grid-item-wide">
          <img
            src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/gwanganBridge.jpg"
            alt="광안대교 야경"
            class="w-full h-full object-cover rounded-lg"
          />
          <div class="image-info">
            <p class="location-type">관광명소</p>
            <p class="location-name">광안대교</p>
            <p class="location-address">부산광역시 해운대구</p>
          </div>
        </div>
        <div class="grid-item">
          <img
            src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/sunrise-6090933_1280.jpg"
            alt="위를 올려다본 숲"
            class="w-full h-full object-cover rounded-lg"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "FourthContent",
};
</script>

<style scoped>
.fourth-section {
  align-items: center;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.fourth-title {
  font-family: "EliceDigitalBaeum_Regular";
  display: flex;
  text-align: left;
  font-size: 40px;
  margin-bottom: 50px;
  width: 100%;
  max-width: 1400px; /* grid-container와 같은 max-width 적용 */
}

.grid-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  max-width: 1400px;
}

.grid-row {
  display: grid;
  gap: 10px;
}

.grid-row:first-child {
  grid-template-columns: repeat(3, 1fr);
  height: 400px; /* 첫 번째 행 높이 증가 */
}

.grid-row:last-child {
  grid-template-columns: 2fr 1fr;
  height: 400px; /* 두 번째 행 높이 증가 */
}

.grid-item,
.grid-item-wide {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  border-radius: 0.5rem;
  height: 100%; /* 부모 컨테이너의 높이를 따르도록 변경 */
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.5s ease;
  transform-origin: center;
  border-radius: 0.5rem;
}

@keyframes float {
  0% {
    transform: translateY(0) scale(1.2);
  }
  50% {
    transform: translateY(-10px) scale(1.2);
  }
  100% {
    transform: translateY(0) scale(1.2);
  }
}

.grid-item:hover img,
.grid-item-wide:hover img {
  animation: float 6s ease-in-out infinite;
  filter: brightness(1.1);
}

/* 반응형 스타일 */
@media (max-width: 768px) {
  .grid-row:first-child,
  .grid-row:last-child {
    grid-template-columns: 1fr;
    height: auto; /* 모바일에서는 자동 높이 */
  }

  .grid-item,
  .grid-item-wide {
    height: 300px; /* 모바일에서 고정 높이 */
  }

  .fourth-title {
    font-size: 28px;
    margin-bottom: 15px;
  }

  .grid-container {
    gap: 8px;
  }

  .grid-row {
    gap: 8px;
  }
}

.grid-item-wide {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  border-radius: 0.5rem;
  height: 100%;
}

.image-info {
  position: absolute;
  bottom: 20px;
  left: 20px;
  color: white;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s ease;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  z-index: 2;
}

.grid-item-wide:hover .image-info {
  opacity: 1;
  transform: translateY(0);
}

.location-type {
  font-size: 14px;
  margin-bottom: 4px;
}

.location-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.location-address {
  font-size: 14px;
}

/* 이미지에 그라데이션 오버레이 추가 */
.grid-item-wide::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 50%;
  background: linear-gradient(to top, rgba(0,0,0,0.5) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 0.5rem;
}

.grid-item-wide:hover::after {
  opacity: 1;
}

/* 기존 hover 효과와 함께 작동하도록 수정 */
.grid-item-wide:hover img {
  animation: float 6s ease-in-out infinite;
  filter: brightness(1.1);
}
</style>
