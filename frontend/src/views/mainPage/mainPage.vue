<template>
  <!-- 메인 콘텐츠 영역 -->
  <div class="background">
    <navBar />
    <div
      class="main-section"
      style="display: grid; grid-template-columns: 1fr 1fr"
    >
      <div class="box1">
        <h1 class="h1-text">
          평범한 여행은 가라,<br />우리만의 특별한 여행 스토리
        </h1>
      </div>
      <div class="box2">
        <div class="search-form">
          <div class="search-group">
            <div>
              <p class="text1">당신만의 힙한 여행,<br />여기서 시작해요✨</p>
            </div>
            <br />
            <div>
              <p class="text2">출발일자</p>
              <input
                type="date"
                v-model="startDate"
                @focus="showPlaceholder = false"
                @blur="showPlaceholder = !startDate"
                placeholder=" "
              />
              <span v-if="showPlaceholder" class="custom-placeholder"
                >선택하세요</span
              >
            </div>
            <div>
              <p class="text2">도착일자</p>
              <input
                type="date"
                v-model="startDate"
                @focus="showPlaceholder = false"
                @blur="showPlaceholder = !startDate"
                placeholder=" "
              />
              <span v-if="showPlaceholder" class="custom-placeholder"
                >선택하세요</span
              >
            </div>
            <div>
              <p class="text2">인원 수</p>
              <input type="number" class="form-input" />
            </div>
            <div>
              <p class="text2">지역(선택)</p>
              <select
                v-model="result"
                style="text-align: center; width: 395px; height: 42px"
              >
                <option
                  v-for="(region, index) in 지역"
                  :key="index"
                  :value="region.value"
                >
                  {{ region.name }}
                </option>
              </select>
            </div>
          </div>
          <button class="search-submit">출 발 하 기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import region from "@/assets/data/region.js";
import navBar from "@/components/navBar.vue";

export default {
  name: "FirstSearch",
  data() {
    return {
      지역: region,
      searchQuery: "", //검색어를 저장할 데이터 추가
      searchResults: [], //검색 결과를 저장할 배열
    };
  },
  components: {
    navBar: navBar,
  },
  methods: {
    handleSearch() {
      if (!this.searchQuery.trim()) {
        alert("검색어를 입력해주세요!!");
        return;
      }
      // 검색 로직 구현
      // 예시: groupList에서 검색
      this.searchResults = this.groupList.filter(
        (item) =>
          item.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          item.description
            .toLowerCase()
            .includes(this.searchQuery.toLowerCase())
      );
      console.log("검색어:", this.searchQuery);
      console.log("검색 결과:", this.searchResults);

      // 검색 결과 페이지로 이동하거나 결과를 표시하는 로직 추가
      // 예시: 라우터를 사용한 페이지 이동
      this.$router.push({
        path: "/search",
        query: { q: this.searchQuery },
      });
    },
  },
};
</script>

<style>
/* 배경 이미지 */
.background {
  position: relative;
  height: 100vh;
  overflow: hidden;
  margin: 0;
  background-image: url("@/assets/image/main.jpg");
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 0;
}
.background::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.2) 0%,
    rgba(0, 0, 0, 0.2) 50%,
    rgba(0, 0, 0, 0.2) 100%
  );
  z-index: 1;
}

.main-section {
  position: relative;
  height: 85%;
  padding: 100px;
  text-align: center;
  color: white;
  z-index: 1;
}

.box1 {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 30px;
  float: left;
  text-align: left;
  justify-self: center;
  margin-top: 20vh;
}

.box2 {
  /* justify-self: center; */
  margin-left: 140px;
  align-self: center;
  background: rgba(0, 0, 0, 0.5);
  height: 600px;
  width: 450px;
  padding: 30px;
  border-radius: 20px;
  display: inline-block;
}

.search-group {
  align-content: center;
  /* display: flex; */
  gap: 10px;
  margin-bottom: 10px;
}
.h1-text {
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 56px;
}
.text1 {
  font-family: "EliceDigitalBaeum_Regular";
  text-align: left;
  font-size: 24px;
}

.text2 {
  font-family: "EliceDigitalBaeum_Bold";
  text-align: left;
}
</style>
