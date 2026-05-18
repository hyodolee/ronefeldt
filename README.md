# Ronnefeldt Teehaus Portfolio

Ronnefeldt Teehaus 쇼핑몰 화면을 Spring Boot와 Thymeleaf 기반으로 재구성한 개인 포트폴리오 프로젝트입니다.
단순 화면 구현뿐 아니라 Sentry 에러 모니터링, Actuator 헬스 체크, Nginx Blue/Green 무중단 배포 구조까지 고려해 운영 관점의 백엔드 프로젝트로 확장하고 있습니다.

## 주요 목표

- 기존 HTML 프론트 화면을 Spring Boot MVC + Thymeleaf 구조로 전환
- 공통 레이아웃을 Thymeleaf fragment로 분리해 재사용성 확보
- Store 카테고리 페이지를 공통 템플릿 기반으로 구성
- Sentry를 이용한 서버 에러 모니터링 구성
- Actuator 기반 헬스 체크 추가
- Nginx Blue/Green 무중단 배포 설계 문서화

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Java 21 |
| Backend | Spring Boot 4.0.6 |
| View | Thymeleaf |
| Build | Gradle |
| Monitoring | Sentry, Spring Boot Actuator |
| Deployment Plan | Nginx Reverse Proxy, Blue/Green Deployment |

## 주요 기능

### 화면

- 메인 랜딩 페이지
- Store 카테고리 공통 페이지
- Tea Set 상품 리스트
- 상품 그리드/리스트 보기 전환
- 상품 이미지 hover 액션
- 검색 오버레이
- 햄버거 슬라이딩 메뉴
- 공통 푸터

### 운영 기능

- Sentry 예외 수집
- 로컬/dev 전용 Sentry 테스트 엔드포인트
- Actuator 헬스 체크
- Nginx Blue/Green 배포 예시 설정
- 배포 롤백 전략 문서화

## 프로젝트 구조

```text
src/main/java/com/ronnefeldt
├─ RonnefeldtApplication.java
└─ controller
   ├─ MainController.java
   ├─ StoreController.java
   └─ DevSentryController.java

src/main/resources
├─ application.properties
├─ application-local.properties
├─ templates
│  ├─ index.html
│  ├─ fragments/layout.html
│  └─ store/category.html
└─ static
   ├─ css
   │  ├─ style.css
   │  ├─ store.css
   │  ├─ product-actions.css
   │  ├─ search-overlay.css
   │  └─ side-menu.css
   └─ js
      ├─ main.js
      ├─ store.js
      ├─ product-actions.js
      ├─ search-overlay.js
      └─ side-menu.js

docs
└─ blue-green-deployment.md

deploy/nginx
└─ ronnefeldt-blue-green.conf.example
```

## 공통 컴포넌트

Thymeleaf fragment를 사용해 반복되는 UI를 공통화했습니다.

- Header
- Left navigation
- Search overlay
- Side menu
- Footer
- Product card
- Product hover actions

상품 카드는 `fragments/layout.html`의 `productCard(product, delay)` fragment로 관리합니다.
Store 카테고리 페이지는 상품 데이터만 넘기면 같은 카드 UI를 반복 렌더링합니다.

## 로컬 실행

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

브라우저에서 접속:

```text
http://localhost:8080
```

## Sentry 설정

Sentry DSN은 코드에 직접 저장하지 않고 환경변수로 주입합니다.

```properties
sentry.dsn=${SENTRY_DSN:}
sentry.environment=${SENTRY_ENVIRONMENT:local}
sentry.release=${SENTRY_RELEASE:${spring.application.name}:local}
sentry.traces-sample-rate=${SENTRY_TRACES_SAMPLE_RATE:0.1}
sentry.send-default-pii=false
```

로컬 실행 시 필요한 환경변수:

```text
SPRING_PROFILES_ACTIVE=local
SENTRY_DSN={Sentry에서 발급받은 DSN}
```

`SENTRY_DSN` 값은 GitHub에 커밋하지 않습니다.

## Sentry 로컬 테스트

`local` 또는 `dev` 프로필에서만 테스트 엔드포인트가 활성화됩니다.

상태 확인:

```text
http://localhost:8080/dev/sentry-status
```

수동 이벤트 전송:

```text
http://localhost:8080/dev/sentry-capture
```

예외 발생 테스트:

```text
http://localhost:8080/dev/sentry-test
```

`/dev/sentry-test`는 의도적으로 예외를 발생시키므로 Whitelabel Error Page가 표시되는 것이 정상입니다.
Sentry Issues 화면에 `Sentry local test error`가 수집되면 연동 성공입니다.

## Health Check

Actuator를 사용해 애플리케이션 상태를 확인합니다.

```text
http://localhost:8080/actuator/health
```

정상 응답:

```json
{"status":"UP"}
```

## Blue/Green 배포 전략

이 프로젝트는 Nginx Reverse Proxy 앞단에서 Spring Boot 앱을 2개 포트로 번갈아 띄우는 Blue/Green 배포를 기준으로 설계했습니다.

```text
User -> Nginx -> Spring Boot

blue  : 127.0.0.1:8081
green : 127.0.0.1:8082
```

배포 흐름:

1. 현재 버전은 8081에서 서비스
2. 새 버전을 8082에 실행
3. `/actuator/health`로 새 버전 상태 확인
4. 정상이면 Nginx가 8082를 바라보도록 변경
5. 문제 발생 시 Nginx를 다시 8081로 전환해 롤백

자세한 내용:

- [Blue/Green Deployment 문서](docs/blue-green-deployment.md)
- [Nginx 예시 설정](deploy/nginx/ronnefeldt-blue-green.conf.example)

## 테스트

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew.bat test
```

## Git 보안 메모

다음 파일은 로컬 환경 정보와 민감 값 보호를 위해 Git에 올리지 않습니다.

```gitignore
.vscode/
.env
.env.*
```

Sentry DSN, 로컬 실행 설정, 개인 환경변수는 저장소에 커밋하지 않습니다.

## 포트폴리오 포인트

- Spring Boot MVC 기반 쇼핑몰 화면 전환
- Thymeleaf fragment를 활용한 UI 재사용 구조
- Sentry 기반 서버 예외 모니터링
- Actuator 기반 운영 헬스 체크
- Nginx Blue/Green 무중단 배포 설계
- 환경변수 기반 운영 설정 관리
