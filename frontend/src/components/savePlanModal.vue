<template>
    <div class="modal-backdrop" @click="$emit('close')" v-if="modelValue">
        <div class="modal-content" @click.stop>
            <h2>여행 계획 저장하기</h2>
            <div class="form-group">
                <label for="planTitle">여행 제목</label>
                <input type="text" id="planTitle" v-model="title" :placeholder="defaultTitle"
                    class="plan-title-input" />
            </div>

            <div class="form-group">
                <label for="planProfileImg">나만의 여행지 사진</label>
                <div class="image-upload-container">
                    <div class="image-preview" :class="{ 'has-image': previewUrl }" @click="triggerFileInput">
                        <img v-if="previewUrl" :src="previewUrl" alt="여행 대표 이미지" class="preview-image" />
                        <div v-else class="upload-placeholder">
                            <i class="fa-solid fa-camera"></i>
                            <p>클릭하여 이미지 업로드</p>
                        </div>
                    </div>
                    <input type="file" ref="fileInput" @change="handleImageUpload" accept="image/*"
                        class="hidden-input" />
                </div>
            </div>

            <div class="modal-actions">
                <button class="cancel-button" @click="$emit('close')">취소</button>
                <button class="save-button" @click="handleSave">저장</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue';

const props = defineProps({
    modelValue: Boolean,
    defaultTitle: String,
});

const emit = defineEmits(['update:modelValue', 'close', 'save']);

const title = ref('');
const fileInput = ref(null);
const selectedFile = ref(null);
const previewUrl = ref(null);

const triggerFileInput = () => {
    fileInput.value.click();
};

const handleImageUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
        if (file.size > 5 * 1024 * 1024) {
            throw new Error("이미지 크기는 5MB를 초과할 수 없습니다.");
        }

        if (!file.type.startsWith("image/")) {
            throw new Error("이미지 파일만 업로드할 수 있습니다.");
        }

        selectedFile.value = file;

        if (previewUrl.value) {
            URL.revokeObjectURL(previewUrl.value);
        }
        previewUrl.value = URL.createObjectURL(file);
    } catch (error) {
        console.error("Image selection failed:", error);
        alert(error.message || "이미지 선택에 실패했습니다.");
    }
};

const handleSave = () => {
    if (!title.value && !props.defaultTitle) {
        alert('여행 제목을 입력해주세요.');
        return;
    }

    emit('save', {
        title: title.value || props.defaultTitle,
        image: selectedFile.value
    });
};

onBeforeUnmount(() => {
    if (previewUrl.value) {
        URL.revokeObjectURL(previewUrl.value);
    }
});
</script>

<style scoped>
.modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    width: 90%;
    max-width: 500px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-content h2 {
    font-family: "EliceDigitalBaeum_Bold";
    font-size: 24px;
    margin-bottom: 1.5rem;
    text-align: center;
    color: #333;
}

.form-group {
    margin-bottom: 1.5rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
    color: #333;
}

.plan-title-input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 16px;
    outline: none;
    transition: border-color 0.2s;
}

.plan-title-input:focus {
    border-color: #ecb27b;
}

.image-upload-container {
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
}

.image-preview {
    width: 100%;
    height: 200px;
    border: 2px dashed #ddd;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
    transition: border-color 0.2s;
}

.image-preview:hover {
    border-color: #ecb27b;
}

.image-preview.has-image {
    border: none;
}

.preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.upload-placeholder {
    text-align: center;
    color: #666;
}

.upload-placeholder i {
    font-size: 48px;
    margin-bottom: 8px;
    color: #ddd;
}

.hidden-input {
    display: none;
}

.modal-actions {
    display: flex;
    justify-content: center;
    gap: 1rem;
    margin-top: 2rem;
}

.save-button,
.cancel-button {
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    font-size: 16px;
    cursor: pointer;
    border: none;
    transition: background-color 0.2s;
    min-width: 100px;
}

.save-button {
    background: #ecb27b;
    color: white;
}

.cancel-button {
    background: #f3f4f6;
    color: #4b5563;
}

.save-button:hover {
    background: #6e6156;
}

.cancel-button:hover {
    background: #e5e7eb;
}

.save-button:disabled {
    background: #cccccc;
    cursor: not-allowed;
}
</style>
