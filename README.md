# Tetris Maintenance & Extension – COMP2042 Coursework

---
- Student ID: 20611068

## 1. GitHub Repository
- Repo: `https://github.com/iristan0617/CW2025`

---

## 2. Compilation Instructions
- **Prerequisites**
  - JDK 17+ (Temurin 17.0.11 verified)
  - Maven 3.9.x (or `mvnw`)
  - JavaFX pulled automatically via `org.openjfx:javafx-maven-plugin` (21.0.6 modules)
- **Build / Run**
  - `git clone https://github.com/iristan0617/CW2025.git && cd CW2025`
  - `./mvnw clean package` to compile + run tests
  - `./mvnw javafx:run` to launch `com.comp2042.Main`
  - `./mvnw test` for unit tests only
  - IDE tips: import as Maven project, set SDK=JDK 17+, mark `src/main/java` and `src/main/resources`, run `javafx:run` goal
- **Common fixes**
  - FXML controller errors → ensure `gameLayout.fxml` references `controller.gui.GuiController`
  - Missing JavaFX modules → always use Maven run goal
  - Version mismatch → set both IDE and Maven to JDK 17+

---


## 4. Implemented & Working Properly

### Core Game Functionality
- Tetromino spawning, movement, rotation, gravity
- Collision handling with walls, settled pieces, and floor
- Line clearing with scoring updates
- Game over on board saturation

### UI & Rendering Features
- Dynamic game board centering
- Main menu
- Ghost piece projection
- Next-piece preview
- Hold-piece function
- Pause overlay
- Power-ups
- Background video playback

### System & Testing
- Project builds and runs successfully using Maven
- `ScoreTest` ensures scoring logic remains functional
- Controller package split into `controller.gui` (UI helpers) and `controller.game` (gameplay orchestration)
- `GuiController` now delegates to dedicated helpers: keyboard, renderer, effect, pause, slow-motion, video, and power-up managers
- Power-up HUD/overlay managed by `GuiControllerPowerUpManager`, wired directly to `controller.game.GameController`
- Rendering centralised in `GuiControllerRenderer` (board, next/hold preview, ghost projection, board centring)
- FXML updated to `fx:controller="controller.gui.GuiController"` to align with new package
- Core gameplay loop unchanged; manual tests + `ScoreTest` confirm scoring still works


---

## 5. Implemented but Not Working Properly
- None

---

## 6. Features Not Implemented
- Full audio/SFX pass (music layers, adjustable mixer, mute toggles)
- Online leaderboards or persistent high-score sync
- Alternate challenge modes (time attack, endless marathon variants)

---

## 7. Project Structure

- **Model:** Game logic and state
- **View:** JavaFX UI components and FXML
- **Controller:** Input handling and orchestration

---

## 8. New Java Classes
- `controller.gui.GuiControllerKeyboardHandler` – keyboard shortcuts (move, hold, power-ups, pause)
- `controller.gui.GuiControllerRenderer` – board/preview/ghost rendering + `Timeline` management
- `controller.gui.GuiControllerEffectManager` – boom effect + board centring utilities
- `controller.gui.GuiControllerPauseManager` – pause state + resume countdown overlay
- `controller.gui.GuiControllerSlowMotionManager` – slow-motion timelines and countdown label updates
- `controller.gui.GuiControllerVideoManager` – background video player lifecycle
- `controller.gui.GuiControllerPowerUpManager` – builds HUD/overlay, updates counts, binds buttons to `GameController`
- `controller.game` package (`GameController`, `EventSource`, `EventType`, `MoveEvent`, `InputEventListener`) – reorganised into gameplay-specific namespace
- `src/test/java/model/ScoreTest` – regression tests for `Score` initial state, add, and reset

---

## 9. Modified Java Classes
- `controller.gui.GuiController` – split from monolith into an orchestrator that wires keyboard, renderer, pause, slow-motion, power-up, effect, and video managers plus owns the `GameController` lifecycle.
- `controller.game.GameController` – reworked to implement `InputEventListener`, integrate skill-point accounting, trigger GUI callbacks, and coordinate `Board` updates.
- `model.SimpleBoard` – enhanced collision/bomb handling, integrates `PowerUpManager`, and produces richer `DownData`/`ViewData` payloads.
- `model.Score` – extended to track both score and skill points, exposing helpers for shop pricing logic.
- `model.PowerUpManager` – upgraded inventory tracking, purchase validation, and DTO hand-offs for GUI updates.

---

## 10. Modified Files & Resources
- main/resources/gameLayout.fxml – Updated with HUD widgets, skill point labels, and refined layout bindings
- src/main/resources – refreshed background assets (`galaxy_main.jpg`), main-menu background artwork, and ambient video (`galaxy.mp4`)
- Legacy helper classes under `controller/*` – removed in favour of the new `controller.gui` equivalents
- README.md – Expanded documentation covering architecture, gameplay, and testing

---

## 11. Tests & Regression Notes
- Unit tests confirm core scoring logic unchanged
- Manual playtesting validates gameplay flow, rendering, input, and UI elements

---

## 12. Unexpected Problems & Fixes
- JavaFX couldn’t find `controller.GuiController` after the move → fixed by updating FXML reference
- Duplicate helper classes (old vs new) → deleted obsolete `controller/*` helpers so only `controller.gui/*` remain
- Package ripple effects (imports, resources, docs) → resolved via `mvn clean package`, `mvn javafx:run`, and manual gameplay verification

---

## 13. Summary
This repository contains a maintainability-focused refactor of the COMP2042 Tetris coursework. The focus was improving structure, applying the Single Responsibility Principle (SRP), and increasing maintainability and extensibility without altering core gameplay behaviour.

Refactor highlights:
- Clear separation between UI helpers and gameplay orchestration.
- Helper classes each encapsulate a single responsibility (input, rendering, effects, slow motion, pause, power-ups, video).
- GameController and event handling moved into a gameplay-specific package.
- Regression tests added to confirm scoring logic remained unchanged.

Features added:
- Main Menu: Complete main menu system with start game and exit functionality
- Pause Menu: Full pause system with "PAUSED" overlay, Resume, New Game or Quit option
- Hold Piece Function: Working hold piece mechanic allowing players to store and swap tetrominoes
- Next Piece Preview: Display of upcoming tetromino for strategic planning
- Score & Lines Tracking: Real-time SCORE and LINES counters with proper scoring system
- Hard Drop Feature: Instant piece placement with space bar for quick positioning
- Power-Ups: Line clearer, Slow Motion and Bomb 
- Power-Up HUD: On-screen display showing available power-up quantities
- Power-Up Shop: Dedicated power-up shop interface accessible via B key with buy options
- Skill Points Economy: SKILL PTS system for purchasing power-ups
- Input Bindings: Number keys [1], [2], [3] for power-up activation and B key for shop navigation
 
