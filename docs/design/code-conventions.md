# Code conventions

Every service, now and in later phases, follows these rules (user, 2026-11-10). A new service copies the layout of accounts-service. Reviewing a change includes checking it against this page.

## Package layout

Each service has one root package, `com.bankassistant.<service>`, holding only the `@SpringBootApplication` class. Everything else goes in a labelled sub-package:

| Package | Holds | Example in accounts-service |
| --- | --- | --- |
| `controller` | REST controllers: request mapping, reading headers and parameters, delegating to a service. No business logic, no repository calls | `AccountController` |
| `service` | Business logic and transactions; maps entities to DTOs | `AccountService` |
| `repository` | Spring Data `JpaRepository` interfaces, and `Specification` factories for optional filters | `AccountRepository`, `AccountTransactionSpecifications` |
| `model/entity` | JPA entities and the enums they use | `Account`, `AccountTransaction`, `AccountType` |
| `model/dto` | Records sent or received over HTTP (requests, responses) | `AccountResponse`, `BalanceResponse` |
| `events` | Event records, publishers and listeners (from phase 6, Kafka) | none yet |
| `exception` | Exceptions the service throws, mapped to HTTP status codes | `AccountNotFoundException` |
| `config` | `@Configuration` classes | `SecurityConfig` (api-gateway) |
| `filter` | Gateway or servlet filters | `UserIdHeaderFilter` (api-gateway) |

Create a package only when the first class for it arrives; no empty packages. A service without a database (such as api-gateway) has no `repository` or `model/entity`.

## One type per file

- No nested or inner classes, records, enums or interfaces. Each type gets its own file in the package above. This includes tests: test configuration lives in its own class (for example `TestcontainersConfiguration`), imported with `@Import`.
- Lambdas and anonymous `Specification`s returned from a static factory method are fine; they are not named types.

## Lombok

- `@Slf4j` for the logger; never declare a logger by hand.
- `@RequiredArgsConstructor` with `private final` fields for dependency injection; no `@Autowired` on fields in main code.
- Entities: `@Getter` and `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, plus `@Setter` only on fields that the code really changes. Never `@Data`, `@EqualsAndHashCode` or `@ToString` on entities (they break lazy loading and identity).
- DTOs are Java records, so they need no Lombok.
- Lombok is added once, in the root `build.gradle.kts`, for every module; its version comes from the Spring Boot BOM.

## Persistence: JPA, Hibernate, Flyway

- Data access goes through Spring Data `JpaRepository` interfaces with derived query methods (`findByIdAndCustomerId`). Optional or combined filters use `JpaSpecificationExecutor` with `Specification` factories in the `repository` package.
- No SQL or JPQL strings in Java code: no `JdbcClient`, `JdbcTemplate` or `@Query`. If a case really needs one (a large aggregate, a lock), ask first and record why in the decision log.
- Flyway owns the schema: every change is a new `V<n>__<description>.sql` migration, and an applied migration is never edited. Hibernate runs with `spring.jpa.hibernate.ddl-auto=validate`, so a startup fails when an entity and the schema disagree.
- `spring.jpa.open-in-view=false` and `spring.jpa.properties.hibernate.default_schema=<service schema>` in every service with a database.
- Column names follow the default snake_case mapping (`customerId` -> `customer_id`); add `@Column` only when the default is wrong. A `CHAR(n)` column needs `@JdbcTypeCode(SqlTypes.CHAR)`.
- Read-only service classes or methods are `@Transactional(readOnly = true)`.

## Logging

- Every public method of a controller, service, filter and event listener logs one line when it is entered, with its key arguments.
- Controllers log at INFO: `GET /accounts/{}/balance customerId={}`. Services, filters and listeners log at DEBUG: `getBalance customerId={} accountId={}`. Configuration beans log what they set up at INFO.
- Each service sets `logging.level.com.bankassistant: debug` in `application.yaml`, so both levels show in `docker compose logs`.
- Use `{}` placeholders, never string concatenation.
- Never log tokens, passwords, API keys or whole request bodies. Customer ids and account ids are fine: all data is fake.

## Tests

- Integration tests run against a real PostgreSQL through Testcontainers, with the same image as Compose.
- Security rules at a trust boundary (the gateway, a money-moving tool) get a test that tries to break them.
