<template>
  <div class="section">
    <input
      class="modal-btn"
      type="checkbox"
      id="modal-btn"
      name="modal-btn"
      v-model="localIsOpen"
    />
    <label for="modal-btn" v-if="false">Open Modal</label>
    <div class="modal" @click.self="closeModal">   
      <div class="modal-wrap">  
        <img :src="destinationImage" :alt="destinationName" />
        <div class="modal-content">
          <h2 class="destination-title">{{ destinationName }}</h2>
          <p class="destination-info">{{ destinationInfo }}</p>
          <div class="people-selector">
            <h3>며칠동안 떠나시나요?!</h3>
            <div class="number-input">
              <i
                class="fa-solid fa-minus minus-icon"
                @click="decreaseDays"
                :class="{ disabled: numberOfDays <= 1 }"
              >
              </i>
              <input
                type="number"
                v-model="numberOfDays"
                min="1"
                max="10"
                @input="validateInput"
                class="number-input-field"
              />
              <i
                class="fa-solid fa-plus plus-icon"
                @click="increaseDays"
                :class="{ disabled: numberOfDays >= 10 }"
              >
              </i>
            </div>
          </div>
          <div class="modal-buttons">
            <button class="action-btn cancel" @click="closeModal">
              <i class="fa-solid fa-xmark"></i>
            </button>
            <button class="action-btn confirm" @click="confirmSelection">
              <i class="fa-solid fa-plane"></i> 출발하기
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useAiRecommendPlanStore } from "@/store/aiRecommendPlanStore";

export default {
  name: "RecommendModal",
  props: {
    isOpen: {
      type: Boolean,
      required: true,
    },
    destinationImage: {
      type: String,
      required: true,
    },
    destinationName: {
      type: String,
      required: true,
    },
    destinationInfo: {
      type: String,
      required: true,
    },
  },
  setup() {
    const aiRecommendStore = useAiRecommendPlanStore();
    return { aiRecommendStore };
  },
  data() {
    return {
      numberOfDays: 1,
    };
  },
  computed: {
    localIsOpen: {
      get() {
        return this.isOpen;
      },
      set(value) {
        this.$emit("update:isOpen", value);
      },
    },
  },
  methods: {
    closeModal() {
      this.$emit("update:isOpen", false);
    },
    increaseDays() {
      if (this.numberOfDays < 10) {
        this.numberOfDays++;
      }
    },
    decreaseDays() {
      if (this.numberOfDays > 1) {
        this.numberOfDays--;
      }
    },
    validateInput() {
      let value = parseInt(this.numberOfPeople);
      if (isNaN(value) || value < 1) {
        this.numberOfPeople = 1;
      } else if (value > 10) {
        this.numberOfPeople = 10;
      }
    },
    confirmSelection() {
      this.$emit("confirm", {
        days: this.numberOfDays,
      });
      this.closeModal();
    },
  },
};
</script>

<style scoped>
.section {
  position: fixed;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1000;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

[type="checkbox"]:checked,
[type="checkbox"]:not(:checked) {
  position: absolute;
  left: -9999px;
}

.modal {
  position: fixed;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  margin: 0 auto;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 100;
  overflow-x: hidden;
  background-color: rgba(31, 32, 41, 0.75);
  pointer-events: auto;
  opacity: 1;
  transition: all 300ms ease-in-out;
}

.modal-wrap {
  position: relative;
  display: block;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  margin-top: 20px;
  margin-bottom: 20px;
  border-radius: 4px;
  overflow: hidden;
  padding-bottom: 20px;
  background-color: #fff;
  align-self: center;
  box-shadow: 0 12px 25px 0 rgba(199, 175, 189, 0.25);
}

.modal-wrap img {
  display: block;
  width: 100%;
  height: 250px;
  object-fit: cover;
}

.modal-content {
  padding: 20px 30px 0 30px;
}

.destination-title {
  font-family: "EliceDigitalBaeum_Regular";
  font-weight: 600;
  color: #1f2029;
  margin-bottom: 20px;
  text-align: center;
  font-size: 20px;
}
.destination-info {
  font-family: "EliceDigitalBaeum_Regular";
  color: #6e6156;
  text-align: center;
  margin: 10px 0 20px;
  line-height: 1.5;
  font-size: 16px;
}

.people-selector {
  margin: 20px 0;
  text-align: center;
}

.people-selector h3 {
  font-family: "EliceDigitalBaeum_Regular";
  font-weight: 500;
  color: #1f2029;
}

.number-input {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin: 15px 0;
}

.number-input input {
  text-align: center;
  font-family: "Poppins", sans-serif;
}

.ctrl-btn {
  width: 40px;
  height: 40px;
  border: none;
  background-color: #102770;
  color: #ffeba7;
  border-radius: 4px;
  cursor: pointer;
  font-size: 20px;
  transition: all 200ms linear;
}

.ctrl-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ctrl-btn:not(:disabled):hover {
  background-color: #ffeba7;
  color: #102770;
}

.modal-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 20px;
}

.action-btn {
  font-family: "Poppins", sans-serif;
  font-weight: 500;
  font-size: 15px;
  padding: 0 20px;
  height: 40px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 200ms linear;
  box-shadow: 0 12px 35px 0 rgba(16, 39, 112, 0.25);
}

.action-btn.confirm {
  background-color: #ecb27b;
  color: #6e6156;
}

.action-btn.cancel {
  background-color: #ecb27b;
  color: #6e6156;
}

.action-btn:hover {
  transform: scale(0.95);
}

.minus-icon,
.plus-icon {
  font-size: 24px;
  color: #6e6156;
  cursor: pointer;
  transition: all 0.2s ease;
}

.minus-icon:hover,
.plus-icon:hover {
  color: #ecb27b;
  transform: scale(1.1);
}

.minus-icon.disabled,
.plus-icon.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.number-input-field {
  width: 60px;
  height: 40px;
  border: 2px solid #ecb27b;
  border-radius: 8px;
  font-family: "EliceDigitalBaeum_Regular";
  font-size: 18px;
  color: #6e6156;
  background-color: #fff;
}

/* 숫자 입력 필드의 화살표 버튼 숨기기 */
.number-input-field::-webkit-outer-spin-button,
.number-input-field::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.number-input-field:focus {
  outline: none;
  border-color: #6e6156;
  box-shadow: 0 0 5px rgba(110, 97, 86, 0.3);
}

@media screen and (max-width: 500px) {
  .modal-wrap {
    width: calc(100% - 40px);
    padding-bottom: 15px;
  }

  .modal-content {
    padding: 15px 20px 0 20px;
  }
}
</style>
