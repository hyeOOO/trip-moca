# 🍫 TRIP MOCA (여행 계획 서비스)
![thumnail](https://github.com/user-attachments/assets/68c88985-262f-4f1d-9618-454b95b19768)
## 📝 프로젝트 소개
> *"AI랑 여행 계획 뚝딱! 여행하고 카드 받고 TRIP MOCA(모으다 카드를!)✨"*

인생 여행은 가고 싶은데... 계획 세우기는 막막하시다구요? 🤔  
이제 트립모카와 함께라면 걱정 끝! 

### ✨ 이런 분들을 위해 준비했어요!
- 여행 계획 세우다가 멘붕 오신 분들
- 효율적인 동선으로 여행하고 싶으신 분들
- 여행하면서 특별한 추억도 모으고 싶으신 분들

### 🎁 트립모카에서는
- AI가 척척 짜주는 맞춤 일정
- 여행지마다 GET! 할 수 있는 귀여운 지역 캐릭터 카드
- 헤매지 않게 알려주는 동선까지!

### 💫 이제 여행 계획, 어렵지 않아요
1️⃣ 여행지랑 기간만 알려주세요  
2️⃣ AI가 일정을 뚝딱 만들어드려요  
3️⃣ 마음에 안 들면 수정도 가능!  
4️⃣ 여행지 도착하면 귀여운 캐릭터 카드 GET!  

## 🎯 핵심 기능

### 1. 간편한 AI 여행 계획 생성
- **간단한 입력으로 일정 생성**
  - 여행 지역 선택
  - 여행 기간 설정
  - AI 기반 일자별 추천 장소 생성

- **일정 커스터마이징**
  - 생성된 일정 수정 가능
  - 방문할 장소 추가/삭제
  - 날짜별 일정 조정

### 2. 지역 캐릭터 카드 수집
- **방문 인증 카드 시스템**
  - 지역별 고유 캐릭터 카드 제공
  - 방문 시 해당 지역 카드 획득
  - 나만의 카드 컬렉션 구축

- **카드 도감**
  - 수집한 카드 목록 확인
  - 미수집 카드 확인
  - 카드 수집 현황 표시

### 3. 이동 경로 안내
- **기본 경로 정보 제공**
  - 출발지-도착지 간 소요 시간 계산
  - 이동 거리 정보 제공
  - 경로 지도 표시
 

## 👥 팀 소개
## Team
|<img src="https://avatars.githubusercontent.com/u/165285610?v=4" width="150" height="150"/>|<img src="https://avatars.githubusercontent.com/u/127578678?v=4" width="150" height="150"/>|
|:-:|:-:|
|hyewon<br/>[@hyeOOO](https://github.com/hyeOOO)|서성우<br/>[@bamtol2](https://github.com/bamtol2)|


## ⚙️ 기술 스택
[![stackticon](https://firebasestorage.googleapis.com/v0/b/stackticon-81399.appspot.com/o/images%2F1731252800943?alt=media&token=2d8aa63e-74a0-491e-84a9-2fa314debd65)](https://github.com/msdio/stackticon)
### Backend
- **Spring Boot**
  - Spring Security를 활용한 인증/인가 구현
  - RESTful API 설계 및 구현
- **MySQL**
  - 사용자 정보, 여행 계획, 장소 정보 등 영구 데이터 저장
- **Redis**
  - refresh Token 관리

### Frontend
- **Vue.js**
  - 반응형 사용자 인터페이스 구현
  - Vuex를 통한 상태 관리
  - Vue Router를 이용한 SPA 구현


## 📦 프로젝트 구조
```
📦src
 ┣ 📂main
 ┃ ┣ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂ssafy
 ┃ ┃ ┃ ┃ ┗ 📂enjoyTrip
 ┃ ┃ ┃ ┃ ┃ ┣ 📂api
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂attraction
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂util
 ┃ ┃ ┃ ┃ ┃ ┣ 📂domain
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂attraction
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂card
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂mapper
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂member
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂plan
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂detail
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂plan
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┃ ┃ ┃ ┣ 📂global
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂annotation
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂jwt
 ┃ ┃ ┃ ┃ ┃ ┗ 📜EnjoyTripApplication.java
 ┃ ┗ 📂resources
 ┃ ┃ ┣ 📂prompts
 ┃ ┃ ┗ 📜application.yml
 ┗ 📂test
 ┃ ┗ 📂java
 ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┗ 📂ssafy
 ┃ ┃ ┃ ┃ ┗ 📂enjoyTrip
 ┃ ┃ ┃ ┃ ┃ ┗ 📜EnjoyTripApplicationTests.java
```

## 💻 설치 및 실행 방법
1. 요구사항
   - JDK 11 이상
   - MySQL 8.0
   - Redis 6.0
   - Node.js 14.0 이상

2. 백엔드 실행
```bash
# 프로젝트 클론
git clone [repository URL]

# 데이터베이스 설정
mysql -u root -p < database.sql

# Spring Boot 실행
./gradlew bootRun
```

3. 프론트엔드 실행
```bash
# 의존성 설치
cd frontend
npm install

# 개발 서버 실행
npm run serve
```

## 🔒 환경 설정
`application.properties` 파일에 다음 설정이 필요합니다:
```properties
server:
  port: {port}
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: {username}
    password: {password}
    url: {url}
    hikari:
      maximum-pool-size: 20
  redis:
    host: {host}
    port: {port}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  ai:
    openai:
      api-key: {apiKey}
      chat:
        options:
          model: {model}
          temperature: {temperature}
    template:
      path: {path}
      cache: true
jwt:
  secret: {jwtKey}
  access-token-validity: 3600000  # 1시간
  refresh-token-validity: 604800000  # 7일
springdoc:
  swagger-ui:
    path: /swagger/enjoy-trip.html
  api-docs:
    path: /v3/api-docs
```

## 📱 API 문서
API 문서는 Swagger UI를 통해 확인할 수 있습니다: `http://localhost:8081/swagger/enjoy-trip.html`
