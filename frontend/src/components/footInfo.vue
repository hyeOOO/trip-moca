<template>
  <footer class="footer">
    <div class="footer-content">
      <!-- 왼쪽 섹션 -->
      <div class="footer-left">
        <h3>H.I.W.S.W</h3>
        <p>
          나는 무엇인지 그리워 이 많은 별빛이 내린 언덕 위에 내 이름자를 써보고 흙으로 덮어 버리었읍니다. 딴은 밤을 세워 우는 벌레는 부끄러운 이름을 슬퍼하는 까닭입니다. 어머님, 그리고 당신은 멀리 북간도에 계십니다.
        </p>
      </div>
      <!-- 중앙 섹션 -->
      <div class="footer-center">
        <h3>개발자 정보</h3>
        <p>
          <span>SSAFY 12기, 부울경 03반</span><br />
          <span>박혜원 / hyewon5342@nate.com</span><br />
          <span>서성우 / fatmunk7504@gmail.com</span>
        </p>
      </div>
      <!-- 오른쪽 섹션 -->
      <div class="footer-right">
        <p>&copy; Copyright 2024 Hotel Mellow.</p>
        <p>Free Website Template: <a href="#">TemplatesJungle</a></p>
        <p>Distributed By: <a href="#">ThemeWagon</a></p>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: "Footer",
};
</script>

<style scoped>

.footer {
  background: white;
  padding: 40px 20px;
  margin-top: 80px;
  border-top: 1px solid #D9D9D9;
}
.footer-content {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-left, .footer-center, .footer-right {
  flex: 1;
}
.footer h3 {
  margin-bottom: 10px;
  font-size: 1.2rem;
}
.footer p {
  font-size: 0.9rem;
  color: #333;
}
.footer-right a {
  color: #333;
  text-decoration: none;
}
.footer-right a:hover {
  text-decoration: underline;
}
</style>
