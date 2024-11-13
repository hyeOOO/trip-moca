<!-- src/components/mypagePlan.vue -->
<template>
  <div class="mypage-plan">
    <div class="col" v-for="(item, index) in items" :key="index">
      <div class="card shadow-sm">
        <span class="img" :style="{ backgroundImage: `url(${item.imgPath})` }"></span>
        <div class="card-body">
          <p class="card-text">
            <span>{{ item.location }}</span>
          </p>
          <div class="d-flex justify-content-between align-items-center">
            <p>{{ item.title }}</p>
            <p>{{ item.dates }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "mypagePlan",
  data() {
    return {
      items: [
        {
          imgPath: "path_to_image_1.jpg",
          location: "제주",
          title: "귤 조 아",
          dates: "2024.12.05~2024.12.08",
        },
        {
          imgPath: "path_to_image_2.jpg",
          location: "서울",
          title: "서울 여행",
          dates: "2024.12.10~2024.12.12",
        },
        {
          imgPath: "path_to_image_3.jpg",
          location: "부산",
          title: "부산 여행",
          dates: "2024.12.15~2024.12.17",
        },
      ],
    };
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
