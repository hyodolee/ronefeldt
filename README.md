# Ronnefeldt Teehaus Portfolio

독일 프리미엄 티 브랜드 **Ronnefeldt Teehaus** 쇼핑몰을 모티브로 개발한 **Spring Boot 기반 E-Commerce** 포트폴리오 프로젝트입니다.

정적 HTML 페이지를 **Spring MVC + Thymeleaf** 아키텍처로 재구성하고, **상품 조회**, **회원 관리**, **장바구니**, **주문·결제**, **문의 게시판**, **OAuth 기반 소셜 로그인**, **애플리케이션 모니터링**, **원격 DB 연동** 등 실제 서비스 수준의 핵심 기능을 구현하였습니다.

특히 백엔드 중심의 설계와 개발에 집중하여 사용자 요청 처리, 비즈니스 로직 구현, 데이터 영속성 관리, 운영 환경 구성 및 모니터링까지 전 과정을 경험하며 **실무형 쇼핑몰 시스템 구축** 역량을 강화하였습니다.

## 프로젝트 주제

프리미엄 티 쇼핑몰의 핵심 기능을 구현한 **E-Commerce**웹 애플리케이션입니다.

- 메인 랜딩 페이지
- Store 상품 카테고리와 상품 상세 페이지
- **회원가입 / 로그인 / 네이버 OAuth 로그인**
- **장바구니 / 주문서 / 결제 완료 흐름**
- **상품 문의 게시판**
- 마이쇼핑 페이지
- 운영 관점의 **Sentry 모니터링**, **Actuator 헬스체크**, **Aiven MySQL 연동**
- 향후 구현 예정: **Nginx Blue/Green 무중단 배포**

## 사용 기술

| 구분 | 기술 |
| --- | --- |
| Language | **Java 21** |
| Framework | **Spring Boot 4.0.6** |
| View | **Thymeleaf** |
| Persistence | **Spring Data JPA** |
| Database | **MySQL(Aiven)**, **H2(local/test)** |
| Build | **Gradle** |
| Auth | **Local Login**, **Naver OAuth** |
| Payment | **PortOne** |
| Monitoring | **Sentry**, **Spring Boot Actuator** |
| Future Plan | **Nginx Reverse Proxy**, **Blue/Green Deployment** |
| Frontend | **HTML**, **CSS**, **JavaScript**, **Thymeleaf Fragment** |

## 주요 기능

### 1. 메인 페이지

- Ronnefeldt 스타일의 랜딩 화면 구현
- 공통 헤더, 좌측 내비게이션, 검색 오버레이, 햄버거 메뉴, 푸터 구성
- 두 번째 섹션에 `tea.mp4` 배경 영상 적용
- 영상 영역 마우스 진입/이탈에 따른 컨트롤 노출 처리

### 2. Store 상품 페이지

- Store 하위 카테고리 구현
  - Tea Set
  - Loose Tea
  - Tea-Caddy
  - LeafCup
  - Teavelope
  - Tea Ware
  - Life Style
- 상품 카드 공통 컴포넌트화
- 그리드 / 리스트 보기 전환
- 정렬 UI
- 상품 hover 시 관심상품 / 새창 열기 액션 노출
- Tea Set은 원본 사이트처럼 배경 이미지가 있는 레이아웃 적용
- 나머지 카테고리는 일반 상품 목록 레이아웃과 페이징 적용

### 3. 상품 상세 페이지

- 상품 이미지, 가격, 배송 정보, 수량 선택, 총 금액 계산
- 장바구니 담기
- 찜 버튼 UI
- 스크롤 시 오른쪽 구매 패널 고정 처리
- DETAIL / GUIDE / REVIEW / Q&A / RELATED 섹션 구성
- 상품 문의 작성 버튼 연동

### 4. 회원 기능

- **일반 회원가입**
- **일반 로그인**
- **bcrypt 기반 비밀번호 해시 저장**
- **네이버 OAuth 로그인**
- 로그인 상태에 따라 햄버거 메뉴 내용 변경
- 마이쇼핑 페이지 접근 제어

### 5. 장바구니 / 주문 / 결제

- 로그인 사용자 기준 **장바구니 생성**
- 상품 상세 페이지에서 **장바구니 담기**
- 헤더의 Cart 수량 실시간 반영
- 국내배송 / 해외배송 탭
- 주문서 생성
- **PortOne** 결제창 호출
- **결제 성공** 후 주문 상태를 `PAID`로 변경
- 결제 완료된 **장바구니 아이템 삭제**
- **결제 내역 저장**

### 6. 커뮤니티 문의 게시판

- **문의 목록**
- **문의 작성**
- **문의 상세 보기**
- 작성자 본인 수정
- 상품 상세 페이지에서 Q&A 작성 페이지로 이동

### 7. 운영 기능

- **Sentry 예외 모니터링** 연동
- 로컬 테스트용 **Sentry 엔드포인트**
- **Actuator 헬스체크**
- **Aiven MySQL** 연결 상태 확인용 엔드포인트
- **HikariCP 커넥션 풀** 설정
- 향후 배포를 위한 **Nginx Blue/Green** 초안 문서 정리

## 주요 URL

| 화면 | URL |
| --- | --- |
| 메인 | `/` |
| Tea Set | `/category/tea-set/24/` |
| Loose Tea | `/category/loose-tea/46/` |
| 상품 상세 | `/product/rich-aroma-leafcup/18/category/24/display/1/` |
| 로그인 | `/member/login.html` |
| 회원가입 | `/member/join.html` |
| 장바구니 | `/order/basket.html` |
| 주문서 | `/order/orderform.html` |
| 마이쇼핑 | `/myshop/index.html` |
| 상품 문의 | `/board/product/6/` |
| Actuator Health | `/actuator/health` |

## 프로젝트 구조

```text
src/main/java/com/ronnefeldt
├─ RonnefeldtApplication.java
├─ controller
│  ├─ MainController.java
│  ├─ StoreController.java
│  ├─ CartController.java
│  ├─ PaymentController.java
│  ├─ CommunityController.java
│  ├─ NaverOAuthController.java
│  ├─ DevSentryController.java
│  └─ AivenDbStatusController.java
├─ entity
│  ├─ MemberEntity.java
│  ├─ ProductEntity.java
│  ├─ CategoryEntity.java
│  ├─ CartEntity.java
│  ├─ CartItemEntity.java
│  ├─ OrderEntity.java
│  ├─ OrderItemEntity.java
│  ├─ PaymentEntity.java
│  └─ InquiryEntity.java
├─ repository/jpa
├─ service
└─ model

src/main/resources
├─ templates
│  ├─ fragments/layout.html
│  ├─ index.html
│  ├─ store
│  ├─ account
│  ├─ order
│  ├─ community
│  └─ myshop
├─ static
│  ├─ css
│  ├─ js
│  └─ video/tea.mp4
├─ application.properties
└─ application-aiven.properties

docs
├─ schema.sql
├─ teehaus-store-seed.sql
├─ ronnefeldt-erd-simple.drawio
├─ ronnefeldt-erd-simple.svg
└─ blue-green-deployment.md
```

## DB 설계 요약

핵심 테이블은 커머스 흐름을 기준으로 구성했습니다.

| 영역 | 테이블 |
| --- | --- |
| 회원 | `members` |
| 상품 | `categories`, `products` |
| 장바구니 | `carts`, `cart_items` |
| 주문 | `orders`, `order_items` |
| 결제 | `payments` |
| 커뮤니티 | `inquiries` |

ERD와 SQL은 `docs` 디렉터리에 정리되어 있습니다.

- `docs/schema.sql`
- `docs/teehaus-store-seed.sql`
- `docs/ronnefeldt-erd-simple.drawio`
- `docs/ronnefeldt-erd-simple.svg`

## 향후 개선 계획

현재는 로컬 개발과 기능 구현 중심으로 구성되어 있으며, 이후 운영 환경 배포를 위해 다음 내용을 추가로 구현할 예정입니다.

- **Nginx Reverse Proxy** 기반 **Blue/Green 무중단 배포**
- `/actuator/health`를 활용한 배포 전 상태 검증
- 문제 발생 시 이전 버전으로 되돌리는 롤백 흐름

관련 초안 문서:

- `docs/blue-green-deployment.md`

## 포트폴리오 포인트

- 정적 HTML 화면을 **Spring Boot MVC + Thymeleaf** 구조로 전환
- **Thymeleaf Fragment**를 이용한 공통 UI 재사용
- **JPA 기반 커머스 도메인 모델** 구성
- **장바구니 → 주문서 → 결제 → 장바구니 정리**까지 실제 쇼핑몰 흐름 구현
- **Naver OAuth**, **PortOne**, **Sentry**, **Aiven MySQL** 등 외부 서비스 연동 경험 반영
- **Actuator**, **HikariCP** 등 운영 관점 기능과 **Blue/Green 배포 계획** 정리
- 테스트 코드로 주요 사용자 흐름 검증
