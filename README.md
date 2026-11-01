# Smart Learning - SME33

Small Java learning app for the Software Maintenance course.

## Run the program

Requires Java 17 and Maven. Open a terminal in the project folder and run:

```bash
mvn compile
java -cp target/classes Main
```

Or, you can use on Linux
```bash
./run.sh
```

Or, double click on run.bat on Windows

## Run the tests

```bash
mvn test
```

## Terminal commands

| Command | Description |
| --- | --- |
| `help` | Show the commands |
| `switch english` / `switch physics` / `switch general` | Choose a subject |
| `start easy` / `start medium` / `start hard` | Start a lesson |
| `answer 1` to `answer 4` | Answer a question |
| `home`, `shop`, `profile`, `stats`, `flashcards`, `settings` | Open a page |
| `buy stats` / `buy premium` / `buy refill` | Buy an item with coins |
| `activate PREMIUM` | Activate a feature |
| `deactivate PREMIUM` | Deactivate a feature |
| `change +PREMIUM,-STREAKS` | Change multiple features at once |
| `status` | Show the current state |
| `logs` | Show the current UI logs |
| `ui on` / `ui off` | Open or close the basic window |
| `exit` / `quit` | Close the program |

Feature names can contain spaces, for example `activate DAILY GOAL`.
