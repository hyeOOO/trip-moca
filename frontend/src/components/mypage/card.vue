<template>
  <div class="col">
    <div class="card shadow-sm">
      <span class="img" :style="{ backgroundImage: `url(${item.imgPath})` }" />
    </div>
  </div>
</template>

<script>
export default {
  name: "card",
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
