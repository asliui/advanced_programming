# ServerApplication (Lab 10 + Lab 11)

Independent Maven project for the **TCP quiz server** (assignment part 1) extended with **JPA / Spring Data JPA** persistence (Lab 11).

## Run

From this directory:

| Mode | Command |
|------|---------|
| Compulsory (echo + `stop`) | `mvn -q clean compile exec:java@compulsory` |
| Homework (full quiz + thread pool + **JPA**) | `mvn -q clean compile exec:java@homework` |
| Advanced (full quiz + virtual threads per connection, Java 21+) | `mvn -q clean compile exec:java@advanced` |
| **Lab 11** compulsory (same as Lab 10 compulsory) | `mvn -q clean compile exec:java@lab11-compulsory` |
| **Lab 11** homework (explicit Lab 11 mains) | `mvn -q clean compile exec:java@lab11-homework` |
| **Lab 11** advanced | `mvn -q clean compile exec:java@lab11-advanced` |
| Load demo (optional; needs server running first) | `mvn -q compile exec:java@load-demo -Dload.port=5555 -Dload.clients=400` |

Properties:

- `quiz.port`, `quiz.questions`, `quiz.kb` (see root `README-LAB10.md`)
- `-Dquiz.cacheBench=true` — after each finished homework match, logs a simple cold vs warm read timing for cached questions (console + `logs/jpa-queries.log`)

## Lab 11 — What was implemented

### Dependencies and configuration

- Spring Boot **parent** + `spring-boot-starter-data-jpa`, `spring-boot-starter-aop`, **H2** (file DB under `target/lab11-quiz-db`), **Hibernate JCache** + **Caffeine JCache** for second-level / query cache.
- `src/main/resources/application.yml` — datasource, Hibernate DDL `update`, SQL logging, cache settings.
- `src/main/resources/logback-spring.xml` — routes logger `lab11.jpql` to **console** and **`logs/jpa-queries.log`** (rolling).

### Entity model (`ro.uaic.asli.lab10.persistence.entity`)

| Entity | Purpose |
|--------|---------|
| `AuditableEntity` | Base `@MappedSuperclass` with `createdAt`, `updatedAt`, `createdBy`, `updatedBy` (Spring Data JPA auditing). |
| `PlayerEntity` | Player name (unique), one-to-many `results`. |
| `QuestionEntity` | Question text, `optionA`…`optionD`, `correctOption`, stable `sourceQuestionId` (Lab 10 file id), many-to-many `games`. |
| `GameEntity` | `gameCode`, `status` (`GameStatus` enum), `startedAt`, `endedAt`, many-to-many `questions`, one-to-many `results`. |
| `ResultEntity` | `score`, `correctAnswers`, `wrongAnswers`, `finishedAt`, many-to-one `player`, many-to-one `game`. |

Relationships:

- **Player 1 — * Result** (`ResultEntity.player`)
- **Game 1 — * Result** (`ResultEntity.game`)
- **Game * — * Question** via join table `game_questions` (owned by `GameEntity.questions`)

`toString` / `equals` / `hashCode` avoid graph recursion (ids / scalars only).

### Repositories (`ro.uaic.asli.lab10.persistence.repository`)

- `PlayerRepository`, `QuestionRepository`, `GameRepository`, `ResultRepository` (Spring Data JPA).
- **JPQL read** example: `ResultRepository.findByPlayerNamePrefixAndMinScore(String prefix, int minScore)`.
- **Transactional modifying** example: `GameRepository.updateStatusAndEndedAtById(...)`.
- **Query cache** hint example: `ResultRepository.findByIdCached(Long id)` (`@QueryHints`).

### Services (`ro.uaic.asli.lab10.persistence.service`)

Facade for gameplay: `QuizGamePersistenceService` (register participant, begin match, complete match).  
Split services: `PlayerPersistenceService`, `QuestionPersistenceService`, `GamePersistenceService`, `ResultPersistenceService`, `ResultSearchService`, `CachePerformanceProbe`.

### Integration with Lab 10 flow

- `Lab10Launcher` starts a **non-web** Spring context (`Lab10PersistenceSpringBoot`) for **HOMEWORK** and **ADVANCED** modes (not compulsory).
- `HomeworkGameSession` accepts optional `QuizGamePersistenceService`:
  - `join` / `joinbot` → `registerParticipant`
  - match start → `beginMatch(questionsSnapshot)` creates `GameEntity` + links `QuestionEntity` rows
  - match end → `completeMatch` writes `ResultEntity` rows and updates game status via modifying JPQL

### JPQL / query logging

- `QueryLoggingAspect` (`@Around` Spring Data repositories under `persistence.repository`) logs method, args, duration, and stack traces on failure to logger **`lab11.jpql`** (console + file per Logback).
- `PlayerPersistenceService` / `ResultSearchService` include small **service-level** timing logs for demonstration.

### Auditing

- `@EnableJpaAuditing` on `Lab10PersistenceSpringBoot`.
- `Lab11AuditContextHolder` + `Lab11AuditorConfiguration` (`AuditorAware<String>`) — uses active player name when set on the worker thread, otherwise **`system`**.

### Dynamic search (Advanced)

- `ResultSearchCriteria` — optional filters: `playerNameStartsWith`, `minScore`, `maxScore`, `gameStatus`, `gameCode`, `startedAfter`, `startedBefore`.
- `ResultSpecification` — JPA Criteria `Specification` combinator.
- `ResultSearchService.searchResults(ResultSearchCriteria)` — `ResultRepository.findAll(Specification)`.

### Caching (Advanced)

- Hibernate second-level cache + query cache via **JCache** (`hibernate-jcache`) and **Caffeine JCache** (`com.github.ben-manes.caffeine:jcache`) so no external `ehcache.xml` URI is required (more reliable on Windows paths that contain spaces).
- `CachePerformanceProbe` logs cold vs warm read times (nanos) when `-Dquiz.cacheBench=true`.

### Benchmarking (Advanced)

- JMH class: `ro.uaic.asli.lab10.persistence.benchmark.Lab11PersistenceBenchmark`.
- **Maven exec** runs JMH with **`-f 0`** (no forked JVM) so `exec:java` classpath includes `jmh-core` (forked subprocess would miss `ForkedMain` unless you build a shaded runner).

```bash
mvn -q -DskipTests compile exec:java@jmh-benchmarks
```

Optional extra tuning:

```bash
mvn -q -DskipTests compile exec:java@jmh-benchmarks -Dexec.args="-f 0 ro.uaic.asli.lab10.persistence.benchmark.Lab11PersistenceBenchmark -wi 2 -i 5"
```

Profile **`benchmark`** (`application-benchmark.yml`) is activated inside the benchmark bootstrap for an isolated in-memory H2.

## Tests (Lab 11)

```bash
mvn clean test
```

`Lab11PersistenceIT` covers persistence, JPQL read, modifying query, relationships, dynamic search, and end-to-end `QuizGamePersistenceService` match save. Uses `@ActiveProfiles("test")` and in-memory H2 (`application-test.yml`, caches disabled).

## Contents

`GameServer`, `ClientThread`, `HomeworkGameSession`, bots, `questions.txt`, `knowledge-base.txt`, tier apps under `app/` (Lab 10: `Lab10*App`), **`lab11/app/`** (`Lab11CompulsoryApp`, `Lab11HomeworkApp`, `Lab11AdvancedApp`, `Lab11Launcher`), plus **`persistence/**`** for Lab 11.

Full Lab 10 gameplay documentation: `../README-LAB10.md`
