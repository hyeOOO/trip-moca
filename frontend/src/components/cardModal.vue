<template>
    <Teleport to="body">
        <div v-if="card" class="modal-overlay" @click.self="$emit('close')">
            <div class="modal-container">
                <div :class="['card-content', { 'flipped': flipped }]" @mousemove="handleMouseMove"
                    @mouseleave="handleMouseLeave" ref="cardContainer" @click="$emit('close')">
                    <div class="card-inner" :style="cardStyle">
                        <div class="card-front">
                            <img :src="card.image1" :alt="card.cardName" class="card-image" />
                            <div class="card__shine"></div>
                            <div class="card__glare"></div>
                        </div>
                        <div class="card-back">
                            <img src="https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/card_back.png"
                                alt="Card Back" class="card-image" />
                        </div>
                    </div>
                </div>
                <router-link to="/mypage/card" class="cardbook-button" @click="closeModalAndNavigate">
                    <span class="button-text">카드도감 보러가기</span>
                    <i class="fas fa-chevron-right"></i>
                </router-link>
            </div>
            <div class="confetti-container">
                <div v-for="n in 50" :key="n" class="confetti" :style="getConfettiStyle(n)"></div>
            </div>
        </div>
    </Teleport>
</template>

<script>
const clamp = (value, min = 0, max = 100) => Math.min(Math.max(value, min), max);
const round = (value, precision = 3) => parseFloat(value.toFixed(precision));
const adjust = (value, fromMin, fromMax, toMin, toMax) => {
    return round(toMin + (toMax - toMin) * (value - fromMin) / (fromMax - fromMin));
};

export default {
    name: 'CardModal',
    props: {
        card: {
            type: Object,
            default: null
        }
    },
    data() {
        return {
            flipped: true,
            springRotate: { x: 0, y: 0 },
            springGlare: { x: 50, y: 50, o: 0 },
            springBackground: { x: 50, y: 50 }
        }
    },
    computed: {
        cardStyle() {
            return {
                transform: `rotateY(${this.flipped ? 180 : 0}deg) 
                           rotateX(${this.springRotate.x}deg) 
                           rotateY(${this.springRotate.y}deg)`
            }
        }
    },
    mounted() {
        setTimeout(() => {
            this.flipped = false;
        }, 100);
    },
    methods: {
        closeModalAndNavigate() {
            this.$emit('close');
        },
        handleMouseMove(e) {
            const rect = this.$refs.cardContainer.getBoundingClientRect();
            const absolute = {
                x: e.clientX - rect.left,
                y: e.clientY - rect.top
            };
            const percent = {
                x: clamp(round((100 / rect.width) * absolute.x)),
                y: clamp(round((100 / rect.height) * absolute.y))
            };
            const center = {
                x: percent.x - 50,
                y: percent.y - 50
            };

            this.springRotate = {
                x: round(center.x / 10),
                y: round(-center.y / 10)
            };
            this.springGlare = {
                x: round(percent.x),
                y: round(percent.y),
                o: 1
            };
        },
        handleMouseLeave() {
            this.springRotate = { x: 0, y: 0 };
            this.springGlare = { x: 50, y: 50, o: 0 };
        },
        getConfettiStyle(n) {
            const randomX = Math.random() * 100;
            const randomDelay = Math.random() * 2;
            const randomSize = 5 + Math.random() * 10;
            const randomRotation = Math.random() * 360;
            const colors = ['#ECB27B', '#e5a165', '#f4d03f', '#58D68D', '#5DADE2'];
            const randomColor = colors[Math.floor(Math.random() * colors.length)];

            return {
                '--x': `${randomX}vw`,
                '--delay': `${randomDelay}s`,
                '--size': `${randomSize}px`,
                '--rotation': `${randomRotation}deg`,
                '--color': randomColor
            };
        }
    },
    emits: ['close']
}
</script>

<style scoped>
.modal-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.card-content {
    perspective: 1000px;
    width: 24rem;
    height: 33.6rem;
}

.card-inner {
    position: relative;
    width: 100%;
    height: 100%;
    transform-style: preserve-3d;
    transition: transform 0.3s ease;
}

.card-front,
.card-back {
    position: absolute;
    width: 100%;
    height: 100%;
    backface-visibility: hidden;
    transform-style: preserve-3d;
}

.card-back {
    transform: rotateY(180deg);
}

.card-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
    cursor: pointer;
}

.card__shine {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: linear-gradient(125deg,
            transparent 0%,
            rgba(255, 255, 255, 0.2) 30%,
            transparent 100%);
    mix-blend-mode: color-dodge;
    opacity: v-bind('springGlare.o');
    transform: translateZ(1px);
}

.card__glare {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(farthest-corner circle at v-bind('springGlare.x + "%"') v-bind('springGlare.y + "%"'),
            rgba(255, 255, 255, 0.8) 10%,
            rgba(255, 255, 255, 0.65) 20%,
            rgba(0, 0, 0, 0.5) 90%);
    opacity: v-bind('springGlare.o');
    mix-blend-mode: overlay;
    transform: translateZ(1px);
}

.confetti-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 999;
}

.confetti {
    position: absolute;
    top: -20px;
    left: var(--x);
    width: var(--size);
    height: var(--size);
    background-color: var(--color);
    animation: fall 3s linear forwards;
    animation-delay: var(--delay);
    transform: rotate(var(--rotation));
}

.cardbook-button {
    background-color: #ECB27B;
    color: white;
    padding: 0.8rem 2rem;
    border-radius: 2rem;
    text-decoration: none;
    font-family: "Pretendard-Medium";
    display: flex;
    align-items: center;
    gap: 0.5rem;
    transition: all 0.3s ease;
    border: 2px solid transparent;
}

.cardbook-button:hover {
    background-color: white;
    color: #ECB27B;
    border: 2px solid #ECB27B;
}

.button-text {
    font-size: 1rem;
}

.cardbook-button i {
    font-size: 0.8rem;
}

@keyframes fall {
    0% {
        transform: translateY(0) rotate(var(--rotation));
        opacity: 1;
    }

    80% {
        opacity: 1;
    }

    100% {
        transform: translateY(100vh) rotate(calc(var(--rotation) + 360deg));
        opacity: 0;
    }
}
</style>