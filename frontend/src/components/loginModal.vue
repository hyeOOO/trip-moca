<!-- src/components/LoginModal.vue -->
<template>
  <div class="modal-wrap" v-show="modelValue" @click.self="closeModal">
    <div class="modal-container" :class="{ 's--signup': isSignUp }" @click.stop="">
      <div class="form sign-in">
        <h2>트립 모카와 함께 출발하기,</h2>
        <label>
          <span>ID</span>
          <input type="id" v-model="loginForm.id" />
        </label>
        <label>
          <span>Password</span>
          <input type="password" v-model="loginForm.password" />
        </label>
        <p class="forgot-pass">비밀번호를 잊어버리셨나요?</p>
        <button type="button" class="modal-btn submit" @click="handleLogin">로그인</button>
        <button type="button" class="modal-btn close" @click="closeModal">닫기</button>
      </div>
      <div class="sub-cont">
        <div class="img">
          <div class="img__text m--up">
            <h2>처음이신가요?</h2>
            <p>
              여행이 쉬워지는 마법 ✨ <br />
              트립모카에서 특별한 여행을 시작해보세요!
            </p>
          </div>
          <div class="img__text m--in">
            <h2>오신 적 있으신가요?</h2>
            <p>
              만나서 반가워요 😎 <br />
              트립모카와 함께 여행을 떠나볼까요?
            </p>
          </div>
          <div class="img__btn" @click="toggleForm">
            <span class="m--up">회원가입</span>
            <span class="m--in">로그인</span>
          </div>
        </div>
        <div class="form sign-up">
          <h2>트립 모카와 함께 출발하기,</h2>
          <label>
            <span>ID</span>
            <input type="id" v-model="signupForm.id" />
          </label>
          <label>
            <span>Password</span>
            <input type="password" v-model="signupForm.password" />
          </label>
          <label>
            <span>Email</span>
            <input type="email" v-model="signupForm.email" />
          </label>
          <button type="button" class="modal-btn submit" @click="handleSignUp">회원가입</button>
          <button type="button" class="modal-btn close" @click="closeModal">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from "@/store/auth";
import { useRouter } from "vue-router"; // router 추가
import api from "@/plugins/axios"; // api import 추가

export default {
  name: "LoginModal",
  props: {
    modelValue: {
      type: Boolean,
      required: true,
    },
  },
  setup() {
    const authStore = useAuthStore();
    const router = useRouter();
    return { authStore, router };
  },
  data() {
    return {
      isSignUp: false,
      loginForm: {
        id: "",
        password: "",
      },
      signupForm: {
        id: "",
        password: "",
        email: "",
      },
    };
  },
  watch: {
    modelValue(newValue) {
      if (newValue) {
        document.body.classList.add("modal-open");
      } else {
        document.body.classList.remove("modal-open");
        this.resetForms();
      }
    },
  },
  methods: {
    closeModal() {
      this.$emit("update:modelValue", false);
    },
    toggleForm() {
      this.isSignUp = !this.isSignUp;
    },
    resetForms() {
      this.isSignUp = false;
      this.loginForm = {
        id: "",
        password: "",
      };
      this.signupForm = {
        id: "",
        password: "",
        email: "",
      };
    },
    async handleLogin() {
      try {
        const success = await this.authStore.login({
          memberId: this.loginForm.id,
          password: this.loginForm.password,
        });

        if (success) {
          this.closeModal(); // 모달 닫기
          this.$router.push("/"); // 또는 다른 페이지로 리다이렉트
        } else {
          // 로그인 실패 처리
          alert("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
        }
      } catch (error) {
        console.error("Login error:", error);
        alert("로그인 중 오류가 발생했습니다.");
      }
    },
    async handleSignUp() {
      try {
        // api를 import 해서 사용
        const response = await api.post("/api/auth/signup", {
          memberId: this.signupForm.id,
          password: this.signupForm.password,
          email: this.signupForm.email,
        });

        if (response.status === 200) {
          alert("회원가입이 완료되었습니다. 로그인해주세요.");
          this.isSignUp = false; // 로그인 폼으로 전환
          this.resetForms();
        }
      } catch (error) {
        console.error("Signup error:", error);
        alert("회원가입 중 오류가 발생했습니다.");
      }
    },
  },
};
</script>

<style scoped>
.modal-wrap {
  position: fixed;
  left: 0;
  top: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

body.modal-open {
  overflow: hidden !important;
  height: 100vh !important;
  width: 100vw !important;
  padding-right: 17px; /* 스크롤바 너비만큼 padding 추가 */
  margin: 0;
  touch-action: none; /* 모바일에서 스크롤 방지 */
}

/* 추가 */
.modal-open .app-container {
  filter: blur(10px); /* 선택사항: 모달 뒷배경 블러 효과 */
  pointer-events: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-container {
  overflow: hidden;
  position: fixed;
  width: 900px;
  height: 550px;
  background: #fff;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* 위치 조정 */
  box-sizing: border-box;
}

.form {
  position: relative;
  width: 640px;
  height: 100%;
  transition: transform 1.2s ease-in-out;
  padding: 50px 30px 0;
}

.form > h2 {
  color: black;
  font-family: "Pretendard-Bold";
  font-size: 32px;
}

.sub-cont {
  overflow: hidden;
  position: absolute;
  left: 640px;
  top: 0;
  width: 900px;
  height: 100%;
  padding-left: 260px;
  background: #fff;
  transition: transform 1.2s ease-in-out;
}

.modal-container.s--signup .sub-cont {
  transform: translate3d(-640px, 0, 0);
}

.modal-btn {
  display: block;
  margin: 0 auto;
  width: 260px;
  height: 36px;
  border-radius: 30px;
  font-size: 15px;
  cursor: pointer;
}

.submit {
  margin-top: 40px;
  margin-bottom: 20px;
  background-color: #988d82;
  color: #fff;
}

.close {
  background: #fff;
  border: 1px solid #000;
  color: #000;
}

.img {
  overflow: hidden;
  z-index: 10;
  position: absolute;
  left: 0;
  top: 0;
  width: 260px;
  height: 100%;
  padding-top: 360px;
}
.img:before {
  content: "";
  position: absolute;
  right: 0;
  top: 0;
  width: 900px;
  height: 100%;
  background-image: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/jeju-island-sunset.jpg");
  background-size: cover;
  transition: transform 1.2s ease-in-out;
}
.img:after {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
}
.modal-container.s--signup .img:before {
  transform: translate3d(640px, 0, 0);
}
.img__text {
  z-index: 2;
  position: absolute;
  left: 0;
  top: 50px;
  width: 100%;
  padding: 0 20px;
  text-align: center;
  color: #fff;
  transition: transform 1.2s ease-in-out;
}
.img__text h2 {
  margin-bottom: 10px;
  font-weight: normal;
}
.img__text p {
  font-size: 14px;
  line-height: 1.5;
}
.modal-container.s--signup .img__text.m--up {
  transform: translateX(520px);
}
.img__text.m--in {
  transform: translateX(-520px);
}
.modal-container.s--signup .img__text.m--in {
  transform: translateX(0);
}
.img__btn {
  overflow: hidden;
  z-index: 2;
  position: relative;
  width: 100px;
  height: 36px;
  margin: 0 auto;
  background: transparent;
  color: #fff;
  text-transform: uppercase;
  font-size: 15px;
  cursor: pointer;
}
.img__btn:after {
  content: "";
  z-index: 2;
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  border: 2px solid #fff;
  border-radius: 30px;
}
.img__btn span {
  position: absolute;
  left: 0;
  top: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  transition: transform 1.2s;
}
.img__btn span.m--in {
  transform: translateY(-72px);
}
.modal-container.s--signup .img__btn span.m--in {
  transform: translateY(0);
}
.modal-container.s--signup .img__btn span.m--up {
  transform: translateY(72px);
}
h2 {
  width: 100%;
  font-size: 24px;
  text-align: center;
}
label {
  display: block;
  width: 260px;
  margin: 25px auto 0;
  text-align: center;
}
label span {
  font-size: 12px;
  color: #cfcfcf;
  text-transform: uppercase;
}
input {
  display: block;
  width: 100%;
  margin-top: 5px;
  padding-bottom: 5px;
  font-size: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.4);
  text-align: center;
  color: #000;
}
.forgot-pass {
  margin-top: 15px;
  text-align: center;
  font-size: 12px;
  color: #cfcfcf;
}
.sign-in {
  transition-timing-function: ease-out;
}
.modal-container.s--signup .sign-in {
  transition-timing-function: ease-in-out;
  transition-duration: 1.2s;
  transform: translate3d(640px, 0, 0);
}

.sign-up {
  transform: translate3d(-900px, 0, 0);
}
.modal-container.s--signup .sign-up {
  transform: translate3d(0, 0, 0);
}
</style>
