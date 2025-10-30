# Fatal Roulette (Java Console)

A Russian-roulette-inspired console game with simple strategy.

## Prerequisites

- Java JDK 8+ installed and `javac`/`java` available in your PATH.

## Rules & Controls

- Revolver has 6 chambers, 1 bullet hidden randomly.
- Your position is a chamber index. On each turn choose an action:
  - `s` or `spin`: Randomize your current chamber.
  - `a` or `advance`: Move to the next chamber (wraps around).
  - `f` or `fire`: Pull the trigger.
- If you fire on the bullet chamber: you die and the round ends.
- If you fire safely: you gain 1 point and the cylinder auto-advances by 1.
- `q` at any prompt quits the round/game.

Scoring: survive as many safe fires as possible. High score is tracked per session.

## Build (Windows)

```bat
build.bat
```

## Run (Windows)

```bat
run.bat
```

Manual:

```bat
javac -d out src\Main.java
java -cp out Main
```

## ASCII Assets

- Place ASCII art in `assets/` as UTF-8 `.txt` files.
- Files used: `assets/gun.txt`, `assets/person.txt`.
- Replace these with your own art; they will render at game start.

If assets are missing or unreadable, the game still runs without them.


"# FatalRoulette" 
