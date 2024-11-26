# 🍫 TRIP MOCA (여행 계획 서비스)
![thumnail](https://enjoy-trip-static-files.s3.ap-northeast-2.amazonaws.com/tripmoca_thumnail.png)
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

## 📌 커밋 컨벤션

💡 커밋은 논리적으로 구분이 되고, 일관성이 유지되는 단위로 최대한 작게 쪼개서 작성

 **1. 메시지 타입**
```
- INIT: 프로젝트 생성
- FEAT: 새로운 기능 추가
- FIX: 버그 수정
- DOCS: 문서 수정
- STYLE: 코드 formatting, 세미콜론(;) 누락, 코드 변경이 없는 경우
- DESIGN: 디자인 적용 및 디자인 관련 코드 수정
- REFACTOR: 코드 리팩토링
- TEST: 테스트 코드, 리팽토링 테스트 코드 추가
- CHORE: 빌드 업무 수정, 패키지 매니저 수정
- MINOR: 사소한 변화
```
 **2. 제목은 50글자 이내**

 **3. 제목 + 본문(선택)으로 구성 / 제목에서 설명하지 못하는 경우 본문에 자세히 작성**

 **4. 커밋 메세지는 무엇을 했는지 파악할 수 있게 자세히 작성**

 **5. 어떻게 보다는 무엇과 왜를 설명하기.**

## ✔️ 예시
```
[타입] 제목
# 엔터 꼭 넣어주세요.
body(본문, 생략 가능)
Resolves : #issue, ...(해결한 이슈, 생략 가능)
See also : #issue, ...(참고 이슈, 생략 가능)
```
```
[FEAT] 사용자 프로필 수정 기능 개발 

- 이렇게 이렇게 개발해서 이러이러한 결과물이 나옴
- 어떠한 문제가 발생하여 무엇을 해결
- 왜 이러한 방법을 사용하여 개발
```

## 📌 브랜치 컨벤션

💡 해당 프로젝트의 브랜치 전략은 Git Flow 전략을 채택했습니다.

 **- 브랜치 종류**
```
- main: 프로젝트 메인 브랜치(배포 가능한 상태만 관리)
- develop: 개발을 위한 개발 브랜치(기능개발을 위한 브랜치로 안정적일 때 main으로 병합)
- feature: 기능 개발 브랜치(develop 브랜치로부터 분기하여 기능 개발 및 버그 수정하고 develop로 병합)
- release: 이번 출시 버전을 준비하는 브랜치
- hotfix: 출시 버전에서 발생한 버그를 수정 하는 브랜치
```
### 1. main branch

제품으로 출시될 수 있는 브랜치
배포(Release) 이력을 관리하기 위해 사용. 즉, 배포 가능한 상태만을 관리한다.

### 2. develop branch
기능 개발을 위한 브랜치들을 병합하기 위해 사용. 즉, 모든 기능이 추가되고 버그가 수정되어 배포 가능한 안정적인 상태라면 develop 브랜치를 ‘master’ 브랜치에 병합(merge)한다.
평소에는 이 브랜치를 기반으로 개발을 진행한다.

### 3. feature branch
feature 브랜치는 새로운 기능 개발 및 버그 수정이 필요할 때마다 ‘develop’ 브랜치로부터 분기한다. feature 브랜치에서의 작업은 기본적으로 공유할 필요가 없기 때문에, 자신의 로컬 저장소에서 관리한다.
개발이 완료되면 ‘develop’ 브랜치로 병합(merge)하여 다른 사람들과 공유한다.

1. ‘develop’ 브랜치에서 새로운 기능에 대한 feature 브랜치를 분기한다.
2. 새로운 기능에 대한 작업 수행한다.
3. 작업이 끝나면 ‘develop’ 브랜치로 병합(merge)한다.
4. 더 이상 필요하지 않은 feature 브랜치는 삭제한다.
5. 새로운 기능에 대한 ‘feature’ 브랜치를 중앙 원격 저장소에 올린다.(push)


### 4. release branch
배포를 위한 전용 브랜치를 사용함으로써 한 팀이 해당 배포를 준비하는 동안 다른 팀은 다음 배포를 위한 기능 개발을 계속할 수 있다. 즉, 딱딱 끊어지는 개발 단계를 정의하기에 아주 좋다.
예를 들어, ‘이번 주에 버전 1.3 배포를 목표로 한다!’라고 팀 구성원들과 쉽게 소통하고 합의할 수 있다는 말이다.

1. ‘develop’ 브랜치에서 배포할 수 있는 수준의 기능이 모이면 또는 정해진 배포 일정이 되면, release 브랜치를 분기한다.
- release 브랜치를 만드는 순간부터 배포 사이클이 시작된다.
- release 브랜치에서는 배포를 위한 최종적인 버그 수정, 문서 추가 등 릴리스와 직접적으로 관련된 작업을 수행한다.
- 직접적으로 관련된 작업들을 제외하고는 release 브랜치에 새로운 기능을 추가로 병합(merge)하지 않는다.
2. ‘release’ 브랜치에서 배포 가능한 상태가 되면(배포 준비가 완료되면),
- 배포 가능한 상태: 새로운 기능을 포함한 상태로 모든 기능이 정상적으로 동작 하는 상태
- ‘master’ 브랜치에 병합한다. (이때, 병합한 커밋에 Release 버전 태그를 부여!)
- 배포를 준비하는 동안 release 브랜치가 변경되었을 수 있으므로 배포 완료 후 ‘develop’ 브랜치에도 병합한다.
이때, 다음 번 배포(Release)를 위한 개발 작업은 ‘develop’ 브랜치에서 계속 진행해 나간다.

### 5. hotfix branch
배포한 버전에 긴급하게 수정을 해야 할 필요가 있을 경우, ‘master’ 브랜치에서 분기하는 브랜치이다. ‘develop’ 브랜치에서 문제가 되는 부분을 수정하여 배포 가능한 버전을 만들기에는 시간도 많이 소요되고 안정성을 보장하기도 어려우므로 바로 배포가 가능한 ‘master’ 브랜치에서 직접 브랜치를 만들어 필요한 부분만을 수정한 후 다시 ‘master’브랜치에 병합하여 이를 배포해야 하는 것이다.

1. 배포한 버전에 긴급하게 수정을 해야 할 필요가 있을 경우,
- ‘master’ 브랜치에서 hotfix 브랜치를 분기한다. (‘hotfix’ 브랜치만 master에서 바로 딸 수 있다.)
2. 문제가 되는 부분만을 빠르게 수정한다.
- 다시 ‘master’ 브랜치에 병합(merge)하여 이를 안정적으로 다시 배포한다.
- 새로운 버전 이름으로 태그를 매긴다.
3. hotfix 브랜치에서의 변경 사항은 ‘develop’ 브랜치에도 병합(merge)한다.


