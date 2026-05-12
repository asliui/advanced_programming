# ServerApplication (Lab 10)

Independent Maven project for the **TCP quiz server** (assignment part 1).

## Run

From this directory:

| Mode | Command |
|------|---------|
| Compulsory (echo + `stop`) | `mvn -q clean compile exec:java@compulsory` |
| Homework (full quiz + thread pool) | `mvn -q clean compile exec:java@homework` |
| Advanced (full quiz + virtual threads per connection, Java 21+) | `mvn -q clean compile exec:java@advanced` |
| Load demo (optional; needs server running first) | `mvn -q compile exec:java@load-demo -Dload.port=5555 -Dload.clients=400` |

Properties: `quiz.port`, `quiz.questions`, `quiz.kb` (see root `README-LAB10.md`).

## Contents

`GameServer`, `ClientThread`, `HomeworkGameSession`, bots, `questions.txt`, `knowledge-base.txt`, tier apps under `app/`.

Full documentation: `../README-LAB10.md`
