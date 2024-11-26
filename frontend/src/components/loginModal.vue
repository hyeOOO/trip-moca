<template>
  <div class="modal-wrap" v-show="modelValue" @click.self="closeModal">
    <div class="modal-container" :class="{ 's--signup': isSignUp }" @click.stop="">
      <!-- 로그인 폼 -->
      <div class="form sign-in" v-show="!isForgotPassword">
        <h2>트립 모카와 함께 출발하기,</h2>
        <label>
          <span>ID</span>
          <input type="id" v-model="loginForm.id" @input="validateLoginId" />
          <p class="error-message" v-if="loginErrors.id">{{ loginErrors.id }}</p>
        </label>
        <label>
          <span>Password</span>
          <input type="password" v-model="loginForm.password" @input="validateLoginPassword" />
          <p class="error-message" v-if="loginErrors.password">{{ loginErrors.password }}</p>
        </label>
        <p class="forgot-pass" @click="showForgotPassword">비밀번호를 잊어버리셨나요?</p>
        <button type="button" class="modal-btn submit" @click="handleLogin" :disabled="!isLoginFormValid">
          로그인
        </button>
        <button type="button" class="modal-btn close" @click="closeModal">닫기</button>
      </div>

      <!-- 비밀번호 찾기 폼 -->
      <div class="form forgot-password" v-show="isForgotPassword">
        <h2>비밀번호 찾기</h2>
        <p class="subtitle">가입 시 등록한 정보를 입력해주세요.</p>
        <label>
          <span>ID</span>
          <input type="text" v-model="resetForm.id" @input="validateResetId" />
          <p class="error-message" v-if="resetErrors.id">{{ resetErrors.id }}</p>
        </label>
        <label>
          <span>Email</span>
          <input type="email" v-model="resetForm.email" @input="validateResetEmail" />
          <p class="error-message" v-if="resetErrors.email">{{ resetErrors.email }}</p>
        </label>
        <button type="button" class="modal-btn submit" @click="handlePasswordReset" :disabled="!isResetFormValid">
          임시 비밀번호 발급
        </button>
        <button type="button" class="modal-btn close" @click="backToLogin">로그인으로 돌아가기</button>
      </div>

      <!-- 회원가입 섹션 -->
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
            <input type="text" v-model="signupForm.id" @input="validateSignupId" />
            <p class="error-message" v-if="signupErrors.id">{{ signupErrors.id }}</p>
          </label>
          <label>
            <span>Password</span>
            <input type="password" v-model="signupForm.password" @input="validateSignupPassword" />
            <p class="error-message" v-if="signupErrors.password">{{ signupErrors.password }}</p>
          </label>
          <label>
            <span>Name</span>
            <input type="text" v-model="signupForm.name" @input="validateName" />
            <p class="error-message" v-if="signupErrors.name">{{ signupErrors.name }}</p>
          </label>
          <label>
            <span>Email</span>
            <input type="email" v-model="signupForm.email" @input="validateEmail" />
            <p class="error-message" v-if="signupErrors.email">{{ signupErrors.email }}</p>
          </label>
          <button type="button" class="modal-btn submit" @click="handleSignUp" :disabled="!isSignupFormValid">
            회원가입
          </button>
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
import { showLoginModalFlag } from "@/eventBus"; // eventBus import 추가

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
      isForgotPassword: false, // 비밀번호 찾기 모드
      resetForm: {
        id: "",
        email: "",
      },
      resetErrors: {
        id: "",
        email: "",
      },
      isSignUp: false,
      loginForm: {
        id: "",
        password: "",
      },
      signupForm: {
        id: "",
        password: "",
        email: "",
        name: "",
      },
      loginErrors: {
        id: "",
        password: "",
      },
      signupErrors: {
        id: "",
        password: "",
        email: "",
        name: "",
      },
    };
  },
  computed: {
    isResetFormValid() {
      return (
        !this.resetErrors.id &&
        !this.resetErrors.email &&
        this.resetForm.id &&
        this.resetForm.email
      );
    },
    isLoginFormValid() {
      return (
        !this.loginErrors.id &&
        !this.loginErrors.password &&
        this.loginForm.id &&
        this.loginForm.password
      );
    },
    isSignupFormValid() {
      return (
        !this.signupErrors.id &&
        !this.signupErrors.password &&
        !this.signupErrors.email &&
        !this.signupErrors.name &&
        this.signupForm.id &&
        this.signupForm.password &&
        this.signupForm.email &&
        this.signupForm.name
      );
    },
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
    showForgotPassword() {
      this.isForgotPassword = true;
      this.isSignUp = false;
    },

    backToLogin() {
      this.isForgotPassword = false;
      this.resetForm = {
        id: "",
        email: "",
      };
      this.resetErrors = {
        id: "",
        email: "",
      };
    },

    validateResetId() {
      const idRegex = /^[a-zA-Z0-9]{4,20}$/;
      if (!this.resetForm.id) {
        this.resetErrors.id = "아이디를 입력해주세요.";
      } else if (!idRegex.test(this.resetForm.id)) {
        this.resetErrors.id = "아이디는 영문과 숫자로 4~20자 사이로 입력해주세요.";
      } else {
        this.resetErrors.id = "";
      }
    },

    validateResetEmail() {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!this.resetForm.email) {
        this.resetErrors.email = "이메일을 입력해주세요.";
      } else if (!emailRegex.test(this.resetForm.email)) {
        this.resetErrors.email = "올바른 이메일 형식이 아닙니다.";
      } else {
        this.resetErrors.email = "";
      }
    },

    async handlePasswordReset() {
      if (!this.isResetFormValid) return;

      try {
        const response = await api.post("/domain/member/reset-password", {
          memberId: this.resetForm.id,
          email: this.resetForm.email,
        });

        alert("임시 비밀번호가 이메일로 발송되었습니다.");
        this.backToLogin();
      } catch (error) {
        if (error.response?.status === 404) {
          alert("존재하지 않는 사용자입니다.");
        } else {
          alert("비밀번호 재설정 중 오류가 발생했습니다.");
        }
        console.error("Password reset error:", error);
      }
    },
    toggleForm() {
      this.isSignUp = !this.isSignUp;
      // 비밀번호 찾기 모드 해제
      this.isForgotPassword = false;
      // 비밀번호 찾기 폼 초기화
      this.resetForm = {
        id: "",
        email: "",
      };
      this.resetErrors = {
        id: "",
        email: "",
      };
    },
    validateLoginId() {
      const idRegex = /^[a-zA-Z0-9]{4,20}$/;
      if (!this.loginForm.id) {
        this.loginErrors.id = "아이디를 입력해주세요.";
      } else if (!idRegex.test(this.loginForm.id)) {
        this.loginErrors.id = "아이디는 영문과 숫자로 4~20자 사이로 입력해주세요.";
      } else {
        this.loginErrors.id = "";
      }
    },
    validateLoginPassword() {
      if (!this.loginForm.password) {
        this.loginErrors.password = "비밀번호를 입력해주세요.";
      } else if (this.loginForm.password.length < 8) {
        this.loginErrors.password = "비밀번호는 8자 이상이어야 합니다.";
      } else {
        this.loginErrors.password = "";
      }
    },
    validateSignupId() {
      const idRegex = /^[a-zA-Z0-9]{4,20}$/;
      if (!this.signupForm.id) {
        this.signupErrors.id = "아이디를 입력해주세요.";
      } else if (!idRegex.test(this.signupForm.id)) {
        this.signupErrors.id = "아이디는 영문과 숫자로 4~20자 사이로 입력해주세요.";
      } else {
        this.signupErrors.id = "";
      }
    },
    validateSignupPassword() {
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
      if (!this.signupForm.password) {
        this.signupErrors.password = "비밀번호를 입력해주세요.";
      } else if (!passwordRegex.test(this.signupForm.password)) {
        this.signupErrors.password =
          "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.";
      } else {
        this.signupErrors.password = "";
      }
    },
    validateName() {
      const nameRegex = /^[가-힣]{2,10}$/;
      if (!this.signupForm.name) {
        this.signupErrors.name = "이름을 입력해주세요.";
      } else if (!nameRegex.test(this.signupForm.name)) {
        this.signupErrors.name = "이름은 2~10자의 한글만 입력 가능합니다.";
      } else {
        this.signupErrors.name = "";
      }
    },
    validateEmail() {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!this.signupForm.email) {
        this.signupErrors.email = "이메일을 입력해주세요.";
      } else if (!emailRegex.test(this.signupForm.email)) {
        this.signupErrors.email = "올바른 이메일 형식이 아닙니다.";
      } else {
        this.signupErrors.email = "";
      }
    },
    closeModal() {
      this.$emit("update:modelValue", false);
      showLoginModalFlag.value = false;
    },
    resetForms() {
      this.isSignUp = false;
      this.isForgotPassword = false;
      // 기존 폼 초기화 코드 유지
      this.resetForm = {
        id: "",
        email: "",
      };
      this.resetErrors = {
        id: "",
        email: "",
      };
    },
    async handleLogin() {
      if (!this.isLoginFormValid) {
        return;
      }
      try {
        const response = await this.authStore.login({
          memberId: this.loginForm.id,
          password: this.loginForm.password,
        });

        if (response || this.authStore.accessToken) {
          this.closeModal(); // 모달 닫기
          // 로그인 성공 시 showLoginModalFlag 값을 false로 설정
          showLoginModalFlag.value = false;

          // 위치 정보 요청
          try {
            const position = await new Promise((resolve, reject) => {
              navigator.geolocation.getCurrentPosition(resolve, reject, {
                enableHighAccuracy: true,
                timeout: 5000,
                maximumAge: 0
              });
            });

            // 위치 기반 카드 체크
            const cardResponse = await api.post('/domain/card/get-card', {
              latitude: position.coords.latitude,
              longitude: position.coords.longitude
            });

            if (cardResponse.data) {
              // Vue 인스턴스에 카드 모달 상태 추가
              this.$emit('card-acquired', cardResponse.data);
            }
          } catch (geoError) {
            console.error('위치 정보 오류:', geoError);
            if (geoError.code === 1) {
              alert('위치 정보 접근을 허용해주세요.');
            }
          }

          // 메인 페이지로의 리다이렉션 제거
          // 대신 저장된 pendingAction이 있다면 실행됨 (AuthStore에서 처리)

        } else {
          alert("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
        }
      } catch (error) {
        console.error("Login error:", error);
        alert("로그인 중 오류가 발생했습니다.");
      }
    },
    async handleSignUp() {
      if (!this.isSignupFormValid) {
        return;
      }

      try {
        const response = await api.post("/api/auth/signup", {
          memberId: this.signupForm.id,
          password: this.signupForm.password,
          email: this.signupForm.email,
          name: this.signupForm.name,
        });

        if (response.status === 200) {
          alert("회원가입이 완료되었습니다. 로그인해주세요.");
          this.isSignUp = false;
          this.resetForms();
        }
      } catch (error) {
        console.group('회원가입 에러');
        console.error('Error object:', error);
        console.error('Error response:', error.response?.data);
        console.groupEnd();

        // axios 에러인지 확인
        if (error.isAxiosError && error.response?.data) {
          const { code, message } = error.response.data;

          switch (code) {
            case 2001:
              alert(message || "이미 사용중인 아이디입니다.");
              this.signupForm.id = "";
              this.$nextTick(() => {
                const idInput = document.querySelector('.sign-up input[type="text"]');
                if (idInput) idInput.focus();
              });
              break;

            case 2004:
              alert(message || "이미 사용중인 이메일입니다.");
              this.signupForm.email = "";
              this.$nextTick(() => {
                const emailInput = document.querySelector('.sign-up input[type="email"]');
                if (emailInput) emailInput.focus();
              });
              break;

            default:
              alert(message || "회원가입 중 오류가 발생했습니다.");
          }
        } else {
          alert("서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
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
  background: rgba(0, 0, 0, 0.6);
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
  padding-right: 17px;
  /* 스크롤바 너비만큼 padding 추가 */
  margin: 0;
  touch-action: none;
  /* 모바일에서 스크롤 방지 */
}

/* 추가 */
.modal-open .app-container {
  filter: blur(10px);
  /* 선택사항: 모달 뒷배경 블러 효과 */
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
  transform: translate(-50%, -50%);
  /* 위치 조정 */
  box-sizing: border-box;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.07), 0 2px 4px rgba(0, 0, 0, 0.07),
    0 4px 8px rgba(0, 0, 0, 0.07), 0 8px 16px rgba(0, 0, 0, 0.07), 0 16px 32px rgba(0, 0, 0, 0.07),
    0 32px 64px rgba(0, 0, 0, 0.07);
  border-radius: 10px;
}

.form {
  position: relative;
  width: 640px;
  height: 100%;
  transition: transform 1.2s ease-in-out;
  padding: 50px 30px 0;
}

.form>h2 {
  color: black;
  font-family: "Pretendard-Bold";
  font-size: 26px;
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
  font-family: "Pretendard-SemiBold";
  font-size: 15px;
  cursor: pointer;
}

.submit {
  margin-top: 30px;
  margin-bottom: 10px;
  background-color: #988d82;
  color: #fff;
}

.close {
  background: #fff;
  border: 1px solid #000;
  color: #000;
}

/* subtitle 스타일 추가 */
.subtitle {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin: 10px 0 30px;
  /* 여백 조정 */
  font-family: "Pretendard-Regular";
}

.forgot-password {
  position: relative;
  /* absolute에서 relative로 변경 */
  width: 640px;
  /* 로그인 폼과 동일한 너비 */
  height: 100%;
  transition: transform 1.2s ease-in-out;
  padding: 50px 30px 0;
  background: white;
}

.form.forgot-password h2 {
  margin-bottom: 10px;
  /* 제목 아래 여백 추가 */
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
  font-family: "Pretendard-SemiBold";
  margin-bottom: 10px;
  font-weight: normal;
}

.img__text p {
  font-family: "Pretendard-Regular";
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
  font-family: "Pretendard-Regular";
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
  margin: 18px auto 0;
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
  margin-top: 120px;
  text-align: center;
  font-size: 12px;
  color: #cfcfcf;
}

.sign-in {
  transition-timing-function: ease-out;
}

.forgot-pass {
  margin-top: 15px;
  text-align: center;
  font-size: 12px;
  color: #cfcfcf;
  cursor: pointer;
}

.forgot-pass:hover {
  text-decoration: underline;
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

.error-message {
  color: #ff4444;
  font-size: 12px;
  margin-top: 5px;
  text-align: center;
  min-height: 15px;
  font-family: "Pretendard-Regular";
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>
