---
name: package-structure
description: DangCharge 서버의 패키지·폴더 구조 컨벤션(api / domain / infra / security / common 레이어, 기능별 하위 패키지, 클래스 이름 규칙, 의존 방향). 새 클래스·패키지·기능을 만들거나, 파일을 옮기거나, 클래스를 어디에 둘지 정할 때 반드시 먼저 읽는다.
---

# 패키지 구조 컨벤션

SAFORI-Server(`com.safori`)의 구조를 기준으로 한다. 이 프로젝트의 베이스 패키지는 `com.picka_chew.dangcharge`이고, 아래 경로는 모두 `src/main/java/com/picka_chew/dangcharge/` 기준이다.

## 최상위 레이어

| 패키지 | 역할 |
| --- | --- |
| `api` | 외부 요청의 진입점과 애플리케이션 흐름. 컨트롤러, 요청·응답 DTO, UseCase |
| `domain` | 비즈니스 규칙과 영속성. 엔티티, 리포지토리, 도메인 서비스 |
| `infra` | 외부 시스템 연동 구현체. 메시지 브로커, 외부 API, 푸시 등 |
| `security` | 인증·인가. 필터, 시큐리티 설정, 토큰 |
| `common` | 어느 기능에도 속하지 않는 공용 코드 |

베이스 패키지 바로 아래에는 `DangchargeApplication` 하나만 둔다.

## 의존 방향

```
api ──▶ domain ──▶ common
 ▲         ▲
 │         │
infra ─────┘        security ──▶ domain, common
```

- `domain`은 `api`, `infra`, `security`를 import하지 않는다. `common`만 쓴다.
- `api`는 `domain`과 `common`을 쓴다. 컨트롤러는 리포지토리를 직접 쓰지 않고 UseCase만 호출한다.
- `infra`는 `domain`이나 `api`에 정의된 인터페이스(포트)를 구현하는 방향으로만 의존한다. `api`와 `domain`이 `infra`의 구체 클래스에 의존하게 만들지 않는다.
- `common`은 특정 기능 패키지에 의존하지 않는다.

## 기능별 하위 패키지

`api`와 `domain` 아래는 기능(도메인) 이름으로 한 번 더 나눈다. 패키지 이름은 소문자 단수형이다. 아래 하위 패키지 중 필요한 것만 만들고, 빈 패키지를 미리 만들어두지 않는다.

```
api/
├── common/
│   ├── advice/          ExceptionAdvice
│   └── dto/             ApiResponseDto, PagedResponse
└── {feature}/
    ├── controller/      {Feature}ApiController
    ├── dto/             {Name}Request, {Name}Response
    ├── service/         {Verb}{Target}UseCase
    ├── event/           {Name}Listener          (필요 시)
    ├── scheduler/       {Name}Scheduler         (필요 시)
    └── port/            외부 연동 인터페이스     (필요 시)

domain/
├── common/
│   └── entity/          BaseTimeEntity
└── {feature}/
    ├── entity/          엔티티, 엔티티가 쓰는 enum
    ├── repository/      {Entity}Repository
    ├── adaptor/         {Entity}Adaptor, {Entity}AdaptorImpl
    ├── service/         {Feature}DomainService, {Feature}DomainServiceImpl
    ├── exception/       {Feature}Handler
    ├── model/           엔티티가 아닌 도메인 값 객체   (필요 시)
    ├── policy/          {Name}Policy, {Name}Resolver   (필요 시)
    ├── validator/       {Feature}Validator, {Feature}ValidatorImpl (필요 시)
    ├── event/           (필요 시)
    └── scheduler/       (필요 시)

infra/
└── {system}/            연동 대상 이름. 예: kafka, fcm, openai
    ├── config/          {System}Config, {System}Properties
    ├── dto/             외부 시스템과 주고받는 메시지
    └── {System}{Role}.java   Client, Sender, Mapper 등 구현체

security/
├── config/  ├── filter/  ├── service/  ├── dto/
├── entity/  ├── repository/  └── exception/

common/
├── annotation/          @UseCase, @Adaptor, @DomainService, @Validator
├── config/              {Name}Config
├── consts/              {Name}StaticValues
├── event/               기능 사이를 오가는 이벤트 {Name}Event
├── exception/           BaseErrorCode, ErrorStatus, SuccessStatus, GeneralException
├── interfaces/
├── service/
└── util/
```

`api`의 기능 이름과 `domain`의 기능 이름이 1:1일 필요는 없다. `api`는 화면·클라이언트 관점으로, `domain`은 데이터·규칙 관점으로 나눈다.

## 클래스별 규칙

| 종류 | 위치 | 이름 | 규칙 |
| --- | --- | --- | --- |
| 컨트롤러 | `api/{feature}/controller` | `{Feature}ApiController` | UseCase만 주입받는다. 반환은 `ApiResponseDto.onSuccess(...)`로 감싼다 |
| UseCase | `api/{feature}/service` | `{Verb}{Target}UseCase` | `@UseCase`. 한 클래스가 유스케이스 하나를 맡고 공개 메서드는 `execute` 하나다 |
| 요청·응답 DTO | `api/{feature}/dto` | `{Name}Request`, `{Name}Response` | 엔티티를 컨트롤러 밖으로 그대로 내보내지 않는다 |
| 엔티티 | `domain/{feature}/entity` | 명사 | `BaseTimeEntity` 상속. 상태 변경은 setter 대신 의도가 드러나는 메서드로 한다 |
| 리포지토리 | `domain/{feature}/repository` | `{Entity}Repository` | `JpaRepository` 상속 인터페이스 |
| Adaptor | `domain/{feature}/adaptor` | `{Entity}Adaptor` + `Impl` | `@Adaptor`. 조회 전용이고 메서드는 `query...`로 시작한다. 없으면 `{Feature}Handler`의 예외를 던진다 |
| DomainService | `domain/{feature}/service` | `{Feature}DomainService` + `Impl` | `@DomainService`, `@Transactional`. 생성·변경 같은 쓰기 로직을 맡는다 |
| Validator | `domain/{feature}/validator` | `{Feature}Validator` + `Impl` | `@Validator`. 입력·비즈니스 규칙 검증 |
| 예외 | `domain/{feature}/exception` | `{Feature}Handler` | `GeneralException` 상속. 예외 인스턴스를 `public static final` 상수로 정의한다 |
| 에러 코드 | `common/exception` | `ErrorStatus` | enum 하나에 모으고, 기능별로 코드 번호대를 나눠 주석으로 구분한다 |

- 인터페이스와 구현체는 같은 패키지에 두고, 구현체 이름은 인터페이스 이름에 `Impl`을 붙인다.
- `api`가 외부 시스템을 호출해야 하면 `api/{feature}/port`에 인터페이스를 두고 구현체는 `infra/{system}`에 둔다.
- `@Service` 대신 `common/annotation`의 `@UseCase`, `@Adaptor`, `@DomainService`, `@Validator`를 쓴다. 이 어노테이션들이 아직 없으면 처음 필요한 시점에 `common/annotation`에 만든다.

## 요청 한 건의 흐름

```
Controller ─▶ UseCase ─┬─▶ Validator        검증
                       ├─▶ Adaptor          조회
                       └─▶ DomainService    생성·변경 ─▶ Repository
```

UseCase는 위 셋을 조합해 흐름만 만들고, 규칙 자체는 `domain`에 둔다.

## 테스트

`src/test/java` 아래에 대상 클래스와 같은 패키지 경로로 두고, 이름은 `{대상클래스}Test`로 한다. 여러 클래스를 가로지르는 시나리오 테스트는 `api/{feature}` 바로 아래에 `{Scenario}Test`로 둔다.

## 리소스

`src/main/resources`에는 `application.yaml`과 프로필별 `application-{profile}.yaml`을 둔다.
