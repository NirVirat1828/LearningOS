# LearningOS Backend

Spring Boot 3 / Java 21 API for LearningOS. Architecture only — no feature logic yet, just the
layered skeleton and a sample health endpoint.

## Run

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Try the sample endpoint:

```bash
curl http://localhost:8080/api/health
```

H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:learningos`, user `sa`, empty password).

## Note on JDK version

Prefer `./mvnw` (checked in above) over a globally installed `mvn` — it resolves the right Maven
version and picks up the `java` already on your `PATH` (JDK 21 here), which is what you want.

If you instead use a Homebrew-installed `mvn`, note that Homebrew's `mvn` launcher script defaults
to its *own* bundled JDK unless `JAVA_HOME` is set explicitly — and Lombok's annotation processor
(used for `@Getter`, `@Builder`, etc. throughout `dto/`) does not yet support newer JDKs, which
breaks the build with "cannot find symbol: method builder()" errors. If you hit that, point Maven
at JDK 21 first:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

The project also targets `java.version=21` in `pom.xml`, so building with anything older will fail too.

## Package layout

- `controller` — REST endpoints (HTTP in/out only, no business logic)
- `service` — business logic, called by controllers
- `repository` — Spring Data JPA repositories (empty until the first entity exists)
- `entity` — JPA-mapped domain classes (empty until the first entity exists)
- `dto` — request/response payloads, decoupled from entities
- `config` — Spring `@Configuration` classes (CORS, etc.)
- `exception` — custom exceptions + `@RestControllerAdvice` global handler
- `util` — stateless helpers, added as real needs come up
