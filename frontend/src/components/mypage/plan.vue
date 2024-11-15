<template>
  <div class="col">
    <div class="card shadow-sm">
      <span class="img" :style="{ backgroundImage: `url(${item.imgPath})` }" />
      <div class="card-body">
        <p class="card-text">
          <span>제주</span>
        </p>
        <div class="d-flex justify-content-between align-items-center">
          <p>귤 조 아</p>
          <p>2024.12.05~2024.12.08</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "plan",
  props: {
    item: Object,
  },
};
</script>

<style scoped>
.card .img {
  display: inline-block;
  width: 100%;
  height: 250px;
  background-size: cover;
  background-position: center;
}

.card .card-body .price {
  text-decoration: line-through;
}
</style>
