## Lab 7 – Spring Boot REST (Lab 6 continuation)

This Lab 7 project **continues Lab 6** by keeping the same domain (movies, actors, genres) and running against the **same PostgreSQL schema** managed by **Flyway**.

### Database (PostgreSQL + Flyway)

Lab 7 uses the **Lab 6 Flyway migrations** from `src/main/resources/db/migration/`.

Default connection values (override with environment variables):
- `LAB6_DB_URL` (default: `jdbc:postgresql://localhost:5432/lab6_movies`)
- `LAB6_DB_USER` (default: `postgres`)
- `LAB6_DB_PASSWORD` (default: `1234`)

On startup, Flyway runs automatically (Spring Boot Flyway integration):
- `spring.flyway.enabled=true`
- `spring.flyway.baseline-on-migrate=true`
- `spring.flyway.locations=classpath:db/migration`

Hibernate is configured to **validate** the schema (no auto-create):
- `spring.jpa.hibernate.ddl-auto=validate`

### How to run

From repo root:

```bash
mvn -DskipTests spring-boot:run
```

Or run from IDE:
- `ro.uaic.asli.lab7.Lab7CompulsoryApp` (profile `lab7-compulsory`, port **8081**)
- `ro.uaic.asli.lab7.Lab7HomeworkApp` (profile `lab7-homework`, port **8082**)
- `ro.uaic.asli.lab7.Lab7Application` / `Lab7AdvancedApp` (profile `lab7-advanced`, port **8080**)

### Swagger
- Homework/Advanced: `http://localhost:<port>/swagger-ui.html`

### Endpoints

Movies:
- `GET /api/movies`
- `GET /api/movies/{id}` (homework+)
- `POST /api/movies` (homework+)
- `PUT /api/movies/{id}` (homework+)
- `PATCH /api/movies/{id}/score` (homework+)
- `DELETE /api/movies/{id}` (homework+)

Actors (homework+):
- `GET /api/actors`
- `GET /api/actors/{id}`
- `POST /api/actors`
- `PUT /api/actors/{id}`
- `DELETE /api/actors/{id}`

Advanced:
- `GET /api/movies/unrelated?threshold=N`
- `POST /api/auth/login` → returns JWT `{ "token": "..." }`

### Example request bodies

Create movie (`POST /api/movies`):

```json
{
  "title": "My Movie",
  "releaseDate": "2024-01-01",
  "duration": 120,
  "score": 8.5,
  "genreId": 1,
  "actorIds": [1, 2]
}
```

Patch score (`PATCH /api/movies/1/score`):

```json
{ "score": 9.2 }
```

### JWT usage (Advanced)
1. Login:
   - `POST /api/auth/login` with `{ "username": "labuser", "password": "labpass" }`
2. Call write endpoints with header:
   - `Authorization: Bearer <token>`

### Notes
- The old H2 demo seeder (`Lab7DataInitializer`) is now disabled by default and runs only with profile `lab7-seed`.

