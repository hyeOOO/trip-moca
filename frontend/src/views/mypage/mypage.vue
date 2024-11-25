<template>
  <div class="mypage">
    <navBar />
    <div class="profile-icon">
      <img src="@/assets/image/user-profile.png" alt="Profile" />
    </div>
    <div class="profile-container">
      <div class="mypage-contents">
        <div class="user-info">
          <div class="info-container">
            <h2>{{ memberId }}</h2>
            <button v-if="!isEditing" @click="startEditing">프로필 수정</button>
            <div v-else class="button-group">
              <button class="save-btn" @click="saveProfile" :disabled="!isPasswordValid">
                저장하기
              </button>
              <button class="cancel-btn" @click="cancelEdit">취소</button>
            </div>
          </div>

          <div v-if="isEditing" class="edit-form">
            <div class="form-group">
              <label>이름</label>
              <input
                type="text"
                v-model="editForm.memberName"
                placeholder="이름을 입력해주세요"
                @input="validateName"
                :class="{ error: validationErrors.name }"
              />
              <span v-if="validationErrors.name" class="error-message">
                {{ validationErrors.name }}
              </span>
            </div>
            <div class="form-group current-password">
              <div>
                <label>현재 비밀번호</label>
                <input
                  type="password"
                  v-model="editForm.currentPassword"
                  placeholder="현재 비밀번호"
                  :disabled="isPasswordValid"
                />
                <span v-if="validationErrors.currentPassword" class="error-message">
                  {{ validationErrors.currentPassword }}
                </span>
              </div>
              <button
                v-if="!isPasswordValid"
                class="confirm-btn"
                @click="checkCurrentPassword"
                :disabled="isPasswordChecking || !editForm.currentPassword"
              >
                확인
              </button>
              <font-awesome-icon
                v-else
                :icon="['fas', 'circle-check']"
                size="2xl"
                :style="{ color: '#69C364', marginTop: '40px', marginLeft: '35px' }"
              />
            </div>

            <template v-if="isPasswordValid">
              <div class="form-group">
                <label>새 비밀번호</label>
                <input
                  type="password"
                  v-model="editForm.newPassword"
                  placeholder="새 비밀번호"
                  @input="validatePassword"
                  :class="{ error: validationErrors.password }"
                />
                <span v-if="validationErrors.password" class="error-message">
                  {{ validationErrors.password }}
                </span>
              </div>
              <div class="form-group">
                <label>새 비밀번호 확인</label>
                <input
                  type="password"
                  v-model="editForm.passwordConfirm"
                  placeholder="새 비밀번호 확인"
                  @input="validatePasswordConfirm"
                  :class="{ error: validationErrors.passwordConfirm }"
                />
                <span v-if="validationErrors.passwordConfirm" class="error-message">
                  {{ validationErrors.passwordConfirm }}
                </span>
              </div>
              <div class="form-group">
                <label>이메일</label>
                <input
                  type="email"
                  v-model="editForm.email"
                  placeholder="이메일을 입력해주세요"
                  @input="validateEmail"
                  :class="{ error: validationErrors.email }"
                />
                <span v-if="validationErrors.email" class="error-message">
                  {{ validationErrors.email }}
                </span>
              </div>
              <div class="form-group">
                <label>휴대폰</label>
                <input
                  type="tel"
                  v-model="editForm.phone"
                  placeholder="번호를 입력해주세요"
                  @input="validatePhone"
                  :class="{ error: validationErrors.phone }"
                />
                <span v-if="validationErrors.phone" class="error-message">
                  {{ validationErrors.phone }}
                </span>
              </div>
              <div class="form-group">
                <button class="delete-btn" @click="confirmDelete" :disabled="!isPasswordValid">
                  회원 탈퇴
                </button>
              </div>
            </template>
          </div>

          <template v-if="!isEditing">
            <div class="meter orange nostripes">
              <span ref="progressBar" :style="mypageStore.progressStyle"></span>
            </div>
            <div class="user-info-card">
              <span>{{ mypageStore.userCards.length }}</span>
              <span
                >개 카드 수집({{ mypageStore.userCards.length }} /
                {{ mypageStore.totalCards.length }})</span
              >
            </div>
          </template>
        </div>

        <template v-if="!isEditing">
          <div class="mypage-header">
            <router-link to="/mypage/plan" custom v-slot="{ navigate }">
              <button @click="navigate">
                <span>나의 여행 계획 </span>
                <span>{{ mypageStore.userPlans.length }}</span>
              </button>
            </router-link>
            <router-link to="/mypage/card" custom v-slot="{ navigate }">
              <button @click="navigate">
                <span>보유 카드 </span>
                <span>{{ mypageStore.userCards.length }}</span>
              </button>
            </router-link>
            <hr :class="{ 'slide-card': $route.path === '/mypage/card' }" />
          </div>
          <div class="mypage-body">
            <router-view></router-view>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import navBar from "@/components/navBar.vue";
import { useAuthStore } from "@/store/auth";
import { useMypageStore } from "@/store/mypageStore";

const authStore = useAuthStore();
const mypageStore = useMypageStore();
const memberId = ref("");
const isEditing = ref(false);
const isPasswordChecking = ref(false);
const isPasswordValid = ref(false);

const editForm = ref({
  memberName: "",
  currentPassword: "",
  newPassword: "",
  passwordConfirm: "",
  email: "",
  phone: "",
});

const validationErrors = ref({
  name: "",
  currentPassword: "",
  password: "",
  passwordConfirm: "",
  email: "",
  phone: "",
});

const initUserData = () => {
  memberId.value = authStore.memberId;
};

const validateName = () => {
  if (!editForm.value.memberName.trim()) {
    validationErrors.value.name = "이름을 입력해주세요.";
  } else if (editForm.value.memberName.length < 2) {
    validationErrors.value.name = "이름은 2자 이상이어야 합니다.";
  } else {
    validationErrors.value.name = "";
  }
};

const validateEmail = () => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!editForm.value.email) {
    validationErrors.value.email = "이메일을 입력해주세요.";
  } else if (!emailRegex.test(editForm.value.email)) {
    validationErrors.value.email = "올바른 이메일 형식이 아닙니다.";
  } else {
    validationErrors.value.email = "";
  }
};

const validatePassword = () => {
  // 특수문자를 허용하도록 수정
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*(),.?":{}|<>]{8,}$/;

  // 현재 비밀번호와 새 비밀번호 비교
  if (editForm.value.newPassword === editForm.value.currentPassword) {
    validationErrors.value.password = "현재 비밀번호와 일치합니다.";
    return;
  }

  if (editForm.value.newPassword && !passwordRegex.test(editForm.value.newPassword)) {
    validationErrors.value.password = "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.";
  } else {
    validationErrors.value.password = "";
  }
  validatePasswordConfirm();
};

const validatePasswordConfirm = () => {
  if (editForm.value.newPassword !== editForm.value.passwordConfirm) {
    validationErrors.value.passwordConfirm = "비밀번호가 일치하지 않습니다.";
  } else {
    validationErrors.value.passwordConfirm = "";
  }
};

const validatePhone = () => {
  editForm.value.phone = formatPhoneNumber(editForm.value.phone);

  const phoneRegex = /^01[016789]-?\d{3,4}-?\d{4}$/;
  if (editForm.value.phone && !phoneRegex.test(editForm.value.phone)) {
    validationErrors.value.phone = "올바른 휴대폰 번호 형식이 아닙니다.";
  } else {
    validationErrors.value.phone = "";
  }
};

const checkCurrentPassword = async () => {
  if (!editForm.value.currentPassword) {
    alert("현재 비밀번호를 입력해주세요.");
    return;
  }

  isPasswordChecking.value = true;
  try {
    const result = await mypageStore.checkPassword(editForm.value.currentPassword);
    isPasswordValid.value = result;
    if (!result) {
      alert("비밀번호가 일치하지 않습니다.");
    }
  } catch (error) {
    console.error("비밀번호 확인 실패:", error);
  } finally {
    isPasswordChecking.value = false;
  }
};

const cancelEdit = () => {
  isEditing.value = false;
  isPasswordValid.value = false;
  editForm.value = {
    memberName: "",
    currentPassword: "",
    newPassword: "",
    passwordConfirm: "",
    email: "",
    phone: "",
  };
  validationErrors.value = {
    name: "",
    currentPassword: "",
    password: "",
    passwordConfirm: "",
    email: "",
    phone: "",
  };
};

const startEditing = async () => {
  try {
    await mypageStore.fetchUserData();
    isPasswordValid.value = false;

    editForm.value = {
      memberName: mypageStore.userInfo?.memberName || "",
      currentPassword: "",
      newPassword: "",
      passwordConfirm: "",
      email: mypageStore.userInfo?.email || "",
      phone: mypageStore.userInfo?.phone || "",
    };

    isEditing.value = true;
  } catch (error) {
    console.error("사용자 정보 조회 실패:", error);
    alert("사용자 정보를 불러오는데 실패했습니다.");
  }
};

const isFormValid = () => {
  return (
    isPasswordValid.value &&
    !validationErrors.value.name &&
    !validationErrors.value.email &&
    !validationErrors.value.password &&
    !validationErrors.value.passwordConfirm &&
    !validationErrors.value.phone
  );
};

const saveProfile = async () => {
  if (!isPasswordValid.value) {
    alert("현재 비밀번호 확인이 필요합니다.");
    return;
  }

  if (!isFormValid()) {
    alert("입력한 정보를 다시 확인해주세요.");
    return;
  }

  try {
    const updateData = {
      memberId: memberId.value,
      memberName: editForm.value.memberName,
      currentPassword: editForm.value.currentPassword,
      newPassword: editForm.value.newPassword || undefined,
      email: editForm.value.email,
      phone: editForm.value.phone,
    };

    await mypageStore.updateUserData(updateData);
    isEditing.value = false;
    isPasswordValid.value = false;
    alert("프로필이 성공적으로 수정되었습니다.");
  } catch (error) {
    console.error("프로필 수정 실패:", error);
    alert("프로필 수정에 실패했습니다.");
  }
};

const confirmDelete = async () => {
  if (!isPasswordValid.value) {
    alert("현재 비밀번호 확인이 필요합니다.");
    return;
  }

  if (confirm("정말로 탈퇴하시겠습니까?")) {
    try {
      await mypageStore.deleteUserData();
    } catch (error) {
      alert("회원 탈퇴에 실패했습니다.");
    }
  }
};

// 전화번호 자동 하이픈 추가 함수
const formatPhoneNumber = (phone) => {
  const value = phone.replace(/[^0-9]/g, ""); // 숫자만 추출
  let result = "";

  if (value.length > 3 && value.length <= 7) {
    result = value.slice(0, 3) + "-" + value.slice(3);
  } else if (value.length > 7) {
    result = value.slice(0, 3) + "-" + value.slice(3, 7) + "-" + value.slice(7, 11);
  } else {
    result = value;
  }

  return result;
};

onMounted(async () => {
  initUserData();
  await mypageStore.fetchCardData();
  await mypageStore.fetchPlanData();
});
</script>

<style scoped>
html {
  scroll-behavior: smooth;
}

.mypage {
  position: relative;
  min-height: calc(100vh - 120px);
  /* 푸터 높이만큼 빼기 */

  margin-bottom: 50px;
  padding-bottom: 0;
  /* 패딩 제거 */
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  background-attachment: fixed;
  -webkit-overflow-scrolling: touch;
  /* 이 속성을 추가하여 배경 이미지 고정 */
  z-index: 0;
  background-image: url("https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/enjoy-trip-main-photo.jpg");
  flex: 1;
  /* 이 부분 추가 */
  will-change: transform; /* 브라우저에게 변환이 일어날 것임을 알림 */
  -webkit-backface-visibility: hidden; /* Safari에서의 성능 개선 */
  -webkit-perspective: 1000; /* Safari에서의 성능 개선 */
}

.mypage::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: -1;
}

.profile-container {
  position: relative;
  background-color: white;
  padding: 6rem 2rem 2rem 2rem;
  top: 110px;
  padding-left: 200px;
  padding-right: 200px;
  min-height: calc(100vh - 110px); /* 동적 높이 계산 최소화 */
  height: auto;
  transform: translateZ(0); /* 하드웨어 가속 활성화 */
  -webkit-transform: translateZ(0);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.mypage-header {
  position: relative;
  /* 버튼들을 나란히 배치하기 위한 스타일 */
  display: flex;
  flex-wrap: wrap;
}

.mypage-header button {
  padding-left: 10px;
  text-align: left;
  width: 50%;
  font-family: "Pretendard-Medium";
  font-size: 32px;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 10px 0px;
}

.mypage-header button:hover {
  opacity: 0.8;
}

.mypage-header button span:nth-child(2) {
  font-family: "Pretendard-ExtraBold";
}

.mypage-header hr {
  border-width: 5px 0 0 0;
  border-color: #ecb27b;
  margin: 10px 0px;
  width: 50%;
  position: absolute;
  bottom: -10px;
  left: 0;
  transition: transform 0.3s ease-in-out;
}

/* hr 애니메이션을 위한 클래스 */
.mypage-header hr.slide-card {
  transform: translateX(100%);
}

/* 버튼 클릭 효과 */
.mypage-header button:active {
  transform: scale(0.98);
}

.white-content {
  margin-top: 3rem;
}

/* mypage-contents도 수정 */
.mypage-contents {
  height: auto;
  /* 내용물 크기에 맞춰서 자동으로 크기 조절 */
  min-height: 100%;
}

.mypage-body {
  min-height: 300px; /* 최소 높이 설정 */
  height: auto;
  position: relative;
  z-index: 1;
  margin-top: 20px;
}

.profile-icon {
  position: relative;
  top: 200px;
  z-index: 1;
  padding-left: 200px;
}

.user-info {
  margin: 15px 0px;
}

.user-info h2 {
  font-family: "Pretendard-Bold";
  font-size: 40px;
}

.user-info-card span:nth-child(1) {
  font-family: "Pretendard-Bold";
  font-size: 20px;
}

.user-info-card span:nth-child(2) {
  font-family: "Pretendard-SemiBold";
  font-size: 20px;
  color: #777777;
}

.info-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button-group > button {
  margin: 0px 5px;
}

.info-container > button {
  border-radius: 5px;
  background-color: #d9d9d9;
  padding: 15px 30px;
  font-family: "Pretendard-Medium";
  font-size: 16px;
  color: #000000;
  margin-right: 10px;
}

.info-container button:hover {
  opacity: 0.8;
}

.current-password {
  display: flex;
}

.current-password div {
  width: 85%;
}

.confirm-btn {
  width: 15%;
  margin: 32px 0px 10px 10px;
  padding: 12px 24px;
  border-radius: 5px;
  font-family: "Pretendard-Medium";
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.2s;
  background-color: #d9d9d9;
}

.meter {
  box-sizing: content-box;
  height: 20px;
  position: relative;
  margin: 20px 0 20px 0;
  background: #433629;
  border-radius: 25px;
  padding: 8px;
  box-shadow: inset 0 -1px 1px rgba(255, 255, 255, 0.3);
}

.meter > span {
  display: block;
  height: 100%;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
  border-top-left-radius: 20px;
  border-bottom-left-radius: 20px;
  background-color: #ecb27b;
  background-image: linear-gradient(center bottom, rgb(43, 194, 83) 37%, rgb(84, 240, 84) 69%);
  box-shadow: inset 0 2px 9px rgba(255, 255, 255, 0.3), inset 0 -2px 6px rgba(0, 0, 0, 0.4);
  position: relative;
  overflow: hidden;
}

.meter > span:after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background-image: linear-gradient(
    -45deg,
    rgba(255, 255, 255, 0.2) 25%,
    transparent 25%,
    transparent 50%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.2) 75%,
    transparent 75%,
    transparent
  );
  z-index: 1;
  background-size: 50px 50px;
  animation: move 2s linear infinite;
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
}

@keyframes move {
  0% {
    background-position: 0 0;
  }

  100% {
    background-position: 50px 50px;
  }
}

/* 스크롤바 스타일링 - mypage-body에 적용 */
.mypage-body::-webkit-scrollbar {
  width: 8px;
}

.mypage-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.mypage-body::-webkit-scrollbar-thumb {
  background: #ecb27b;
  border-radius: 4px;
}

.mypage-body::-webkit-scrollbar-thumb:hover {
  background: #dca06a;
}

.edit-form {
  width: 100%;
  max-width: 700px;
  margin: 100px auto;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-family: "Pretendard-Medium";
  font-size: 16px;
  color: #333;
}

.delete-btn {
  width: 100%;
  /* 추가 */
  border-radius: 5px;
  border-color: #ff3c3f;
  background-color: #fff;
  padding: 15px 30px;
  font-family: "Pretendard-Medium";
  font-size: 16px;
  color: #ff3c3f;
  margin-right: 10px;
}

.form-group input {
  width: 100%;
  max-width: -webkit-fill-available;
  /* 추가 */
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
  font-family: "Pretendard-Regular";
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.save-btn,
.cancel-btn {
  padding: 12px 24px;
  border-radius: 5px;
  font-family: "Pretendard-Medium";
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.save-btn {
  background-color: #ecb27b;
  color: white;
  border: none;
}

.cancel-btn {
  background-color: #d9d9d9;
  color: #333;
  border: none;
}

.save-btn:hover,
.cancel-btn:hover {
  opacity: 0.8;
}

.error-message {
  color: #ff3c3f;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.form-group input.error {
  border-color: #ff3c3f;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.current-password {
  display: flex;
  align-items: flex-start;
}
</style>
