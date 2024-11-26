<template>
  <footer class="footer">
    <div class="footer-content">
      <!-- 왼쪽 섹션 -->
      <div class="footer-left">
        <h3>HW&SW</h3>
        <p>
          나는 무엇인지 그리워 이 많은 별빛이 내린 언덕 위에 내 이름자를 써보고 흙으로 덮어
          버리었읍니다. 딴은 밤을 세워 우는 벌레는 부끄러운 이름을 슬퍼하는 까닭입니다. 어머님,
          그리고 당신은 멀리 북간도에 계십니다.
        </p>
      </div>
      <!-- 중앙 섹션 -->
      <div class="footer-center">
        <h3>개발자 정보</h3>
        <div class="developer-info">
          <div class="info-item">
            <img src="@/assets/image/location_icon.png" alt="위치" class="icon" />
            <span>SSAFY 12기, 부울경 03반</span>
          </div>
          <div class="info-item">
            <img src="@/assets/image/mail_icon.png" alt="개발자" class="icon" />
            <span>
              박혜원 / <a href="mailto:hyewon5342@nate.com">hyewon5342@nate.com</a><br />
              서성우 / <a href="mailto:test@test.com">test@test.com</a>
            </span>
          </div>
        </div>
      </div>
      <!-- 오른쪽 섹션 -->
      <div class="footer-right">
        <p>&copy; Copyright 2024 Hotel Mellow</p>
        <p>Free Website Template: <a href="#">TemplatesJungle</a></p>
        <p>Distributed By: <a href="#">ThemeWagon</a></p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  font-family: "EliceDigitalBaeum_Regular";
  background: white;
  padding: 40px 20px;
  border-top: 1px solid #d9d9d9;
  margin-top: 50px; /* 이 부분 추가 */
  width: 100%;
  position: relative; /* 이 부분 추가 */
  z-index: 100; /* z-index 증가 */
}

.footer-content {
  max-width: 1600px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 30px;
}

.footer-left,
.footer-center,
.footer-right {
  margin-left: 20px;
  display: flex;
  flex-direction: column;
}

.footer h3 {
  margin-bottom: 20px;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.footer p {
  font-size: 0.95rem;
  color: #666;
  line-height: 1.6;
  margin: 0;
  word-break: keep-all;
}

.developer-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

a {
  color: #666;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

.footer-right p {
  margin-bottom: 5px;
  font-size: 0.85rem;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .footer {
    padding: 30px 15px 80px 15px;
    margin-top: 100px;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .footer-left,
  .footer-center,
  .footer-right {
    text-align: center;
  }

  .info-item {
    justify-content: center;
  }
}
</style>
