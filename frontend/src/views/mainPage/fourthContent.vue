<template>
  <section class="fourth-section">
    <h2 class="section-title">
      우리가 찾은 국내 여행 방곳 모음.zip<br />지금 바로 확인하기 ✨
    </h2>
    <div class="post-grid">
      <div v-for="(post, index) in 8" :key="index" class="post-card">
        <img
          :src="`@/assets/post${index + 1}.jpg`"
          alt="게시글 썸네일"
          class="post-image"
        />
        <div class="post-info">
          <h3 class="post-title">게시글 제목</h3>
          <p class="post-excerpt">게시글 내용 미리보기...</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {};
</script>

<style>
.fourth-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  width: 100%;
  height: 100vh;
  padding: 0; /* 추가 패딩 제거 */
  margin: 0;
  box-sizing: border-box;
}

/* 게시글 그리드 */
.post-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.post-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.post-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.post-info {
  padding: 15px;
}

.post-title {
  font-size: 1.1em;
  margin-bottom: 10px;
}

.post-excerpt {
  font-size: 0.9em;
  color: #666;
}
</style>
