# ClientApplication (Lab 10)

Independent Maven project for the **TCP quiz client** (assignment part 2).

## Run

Start **ServerApplication** first (homework or advanced for the real game).

From this directory:

```bash
mvn -q clean compile exec:java@client
```

Optional:

```bash
mvn -q compile exec:java@client -Dclient.host=localhost -Dclient.port=5555
```

Two threads: `ClientInputThread` (keyboard → socket), `ClientListenerThread` (socket → console). Type `exit` to quit.

Full documentation: `../README-LAB10.md`
