# Lab 10 — Networking (two Maven projects, as in the assignment)

The coursework text asks for **two parts** and to **create a project for each one**. This repository implements that **literally**:

| Project | Role |
|---------|------|
| **`ServerApplication/`** | TCP server: `GameServer`, `ClientThread`, quiz logic, resources (`questions.txt`, `knowledge-base.txt`). |
| **`ClientApplication/`** | TCP client: `GameClient` + `ClientInputThread` + `ClientListenerThread`. |

Both are **standalone Maven modules** (each has its own `pom.xml`) and compile **independently**.

The umbrella course repo (`advanced-programming-lab`) **does not** contain duplicate Lab 10 sources under `src/main/java/ro/uaic/asli/lab10/` anymore; Lab 10 lives only in the two folders above.

---

## Why two projects?

Matches the assignment wording: *“The application will contain two parts (create a project for each one).”*  
Separating server and client matches the **client–server** model, keeps dependencies minimal on the client, and lets you run **N clients** against one server on different machines or terminals.

---

## Prerequisites

- **JDK 21** (virtual threads + sealed `BotStrategy`).

---

## 1) Compulsory server

```bash
cd ServerApplication
mvn -q clean compile exec:java@compulsory
```

Behaviour: any line → `Server received the request: <line>`; `stop` → `Server stopped` + graceful shutdown.

---

## 2) Homework server (full quiz)

```bash
cd ServerApplication
mvn -q clean compile exec:java@homework
```

Uses a **bounded `ThreadPoolExecutor`** for client handlers. Loads `questions.txt` and `knowledge-base.txt` from `src/main/resources/` (or paths from `-Dquiz.questions` / `-Dquiz.kb`).

---

## 3) Advanced server (virtual threads per connection)

```bash
cd ServerApplication
mvn -q clean compile exec:java@advanced
```

Same quiz as homework, but each **`ClientThread`** is scheduled on **`Executors.newVirtualThreadPerTaskExecutor()`** (Java 21+). If Java &lt; 21, the launcher falls back to the homework-style pool.

---

## 4) Client(s)

In **one or more** other terminals:

```bash
cd ClientApplication
mvn -q clean compile exec:java@client
```

Optional:

```bash
mvn -q compile exec:java@client -Dclient.host=localhost -Dclient.port=5555
```

---

## 5) Optional load demo (many short connections)

1. Start server (compulsory or advanced recommended): `exec:java@advanced` or `exec:java@compulsory`.
2. From `ServerApplication/`:

```bash
mvn -q compile exec:java@load-demo -Dload.port=5555 -Dload.clients=400
```

Uses **virtual threads** to open many TCP connections, send one line, read one line, close. See `VirtualThreadLoadDemo` Javadoc.

---

## Demo commands (client, homework/advanced server)

```text
join Asli
joinbot random Bot1
start
answer 1 A
score
quit
```

Server shutdown (from client):

```text
stop
```

Client-only exit:

```text
exit
```

---

## Requirements checklist

### Compulsory — **completed**

- `ServerApplication`: `GameServer`, `ClientThread`, `ServerSocket`, `stop` / echo responses, graceful stop.
- `ClientApplication`: `GameClient`, keyboard → server, **`exit`** stops client.

### Homework — **completed**

- Question file format and `questions.txt` in server resources.
- OOP: `Question`, `Player`, `QuestionRepository`, `QuizGame`, `HomeworkGameSession`, `ScoreBoard`, `CommandParser`, …
- Commands: `join`, `joinbot random|kb|llm`, `start`, `answer`, `score`, `quit`, `stop`.
- `ThreadPoolExecutor` (homework mode), graceful shutdown, **two client threads**, **blitz** (`HomeworkGameSession.BLITZ_MS`), winner = score then **lowest total response time**.

### Advanced — **completed / partial**

- Humans + bots in one game: **completed** (`join`, `joinbot`).
- Random / knowledge-base bots: **completed** (`RandomBot`, `KnowledgeBaseBot` + `knowledge-base.txt`).
- LLM bot: **`LLMBotStub`** implements difficulty and delay; **no real external HTTP API** — **stub / extension point** (documented in source).
- Virtual threads per client connection: **completed** (`Lab10AdvancedApp` + `VirtualThreadGameServer`).
- Many concurrent clients: **partial** — `VirtualThreadLoadDemo` demonstrates concurrent lightweight connections; not a full production load harness.

---

## Compile check (from repo root)

```bash
cd ServerApplication && mvn clean compile -DskipTests && cd ..
cd ClientApplication && mvn clean compile -DskipTests && cd ..
```
