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
  - Version mismatch → set both IDE and Maven to JDK 17+

---

## 3. Key Modifications Overview

This coursework involved a comprehensive refactoring of the Tetris codebase to improve maintainability and extensibility. The main focus was applying the Single Responsibility Principle (SRP) by splitting the monolithic `GuiController` into specialized helper classes. Key modifications include:

- **Architecture Refactoring**: Split `GuiController` into 7 specialized manager classes handling keyboard input, rendering, effects, pause/resume, slow-motion, video, and power-ups
- **Package Reorganization**: Moved game logic classes into `controller.game` package for better separation of concerns
- **Feature Additions**: Implemented pause system with countdown, power-up shop, skill points economy, and enhanced UI features
- **Bug Fixes**: Resolved pause countdown issue where blocks continued dropping during resume countdown

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

### GUI Helper Classes (Location: `src/main/java/controller/gui/`)
- **`GuiControllerKeyboardHandler.java`** – Handles all keyboard input events (arrow keys for movement, space for hard drop, hold key, power-up activation keys [1-3], pause/resume with ESC/P, shop toggle with B). Extracted from `GuiController` to separate input handling concerns.

- **`GuiControllerRenderer.java`** – Centralizes all rendering logic including game board display, next piece preview, hold piece preview, ghost piece projection, and board centering calculations. Manages the main game `Timeline` for block dropping.

- **`GuiControllerEffectManager.java`** – Manages visual effects including the boom/explosion effect for bomb power-ups and board centering utilities. Provides animation effects for enhanced gameplay feedback.

- **`GuiControllerPauseManager.java`** – Handles pause/resume functionality including pause overlay display, 3-second countdown before resuming, and immediate pause on ESC key press. Ensures game state remains paused during countdown to prevent blocks from dropping.

- **`GuiControllerSlowMotionManager.java`** – Manages slow-motion power-up functionality including timeline speed adjustments and countdown label updates showing remaining slow-motion duration.

- **`GuiControllerVideoManager.java`** – Handles background video playback lifecycle including video loading, playback control, and resource management for the ambient background video.

- **`GuiControllerPowerUpManager.java`** – Manages power-up HUD/overlay display, updates power-up inventory counts, and binds shop buttons to `GameController` for purchasing power-ups. Handles skill points display and shop UI interactions.

### Game Controller Package (Location: `src/main/java/controller/game/`)
- **`GameController.java`** – Main game orchestration class implementing `InputEventListener`. Coordinates board updates, power-up management, and GUI callbacks. Moved from root `controller` package to `controller.game` for better organization.

- **`EventSource.java`**, **`EventType.java`**, **`MoveEvent.java`**, **`InputEventListener.java`** – Event handling classes reorganized into `controller.game` package to group gameplay-specific event logic together.

### Test Classes (Location: `src/test/java/model/`)
- **`ScoreTest.java`** – Unit tests for `Score` class ensuring scoring logic remains functional after refactoring. Tests initial state, score addition, and reset functionality.

---

## 9. Modified Java Classes

### `controller.gui.GuiController`
- **Location**: `src/main/java/controller/gui/GuiController.java`
- **Changes Made**:
  - Refactored from monolithic class (~500+ lines) to orchestrator pattern (~200 lines)
  - Extracted keyboard handling to `GuiControllerKeyboardHandler` (delegates via `keyboardHandler.createKeyHandler()`)
  - Extracted rendering logic to `GuiControllerRenderer` (delegates board, preview, ghost rendering)
  - Extracted pause logic to `GuiControllerPauseManager` (delegates pause/resume operations)
  - Extracted slow-motion logic to `GuiControllerSlowMotionManager`
  - Extracted power-up UI management to `GuiControllerPowerUpManager`
  - Extracted effect management to `GuiControllerEffectManager`
  - Extracted video management to `GuiControllerVideoManager`
  - Added `resumeImmediately()` method to support new games without countdown
- **Rationale**: Applied Single Responsibility Principle to improve maintainability. The original class violated SRP by handling UI, input, rendering, effects, and game state all in one place, making it difficult to test and modify individual components.
- **Impact**: Each component can now be tested and modified independently. The orchestrator pattern makes the codebase more maintainable and extensible.

### `controller.gui.GuiControllerPauseManager`
- **Location**: `src/main/java/controller/gui/GuiControllerPauseManager.java`
- **Changes Made**:
  - Added explicit pause enforcement at start of `showResumeCountdown()` method (lines 45-49) to ensure `isPause` flag and timeline remain paused during countdown
  - Added `resumeImmediately()` method (lines 99-117) for new games to start without countdown
  - Modified `setPaused()` to handle immediate pause on ESC key press
- **Rationale**: Fixed critical bug where blocks continued dropping during the 3-second resume countdown. The countdown overlay was showing but the game state wasn't properly paused, causing blocks to fall during the countdown period. Also, new games were unnecessarily showing a countdown when they should start immediately.
- **Impact**: Blocks now correctly pause during resume countdown, and new games start immediately without unnecessary countdown delay.

### `controller.game.GameController`
- **Location**: `src/main/java/controller/game/GameController.java`
- **Changes Made**:
  - Reworked to implement `InputEventListener` interface
  - Integrated skill-point accounting system that awards skill points based on score bonuses
  - Added power-up management integration via `PowerUpManager`
  - Enhanced `onHardDropEvent()` to calculate drop distance and award skill points
  - Added GUI callback triggers for game state changes
- **Rationale**: Moved from root `controller` package to `controller.game` for better package organization. Enhanced to support new power-up and skill point systems while maintaining existing gameplay functionality.
- **Impact**: Better separation of concerns between game logic and UI. Skill points system enables power-up economy.

### `model.SimpleBoard`
- **Location**: `src/main/java/model/SimpleBoard.java`
- **Changes Made**:
  - Enhanced collision detection and bomb handling
  - Integrated `PowerUpManager` for power-up effects
  - Enhanced `DownData` and `ViewData` payloads to include additional game state information
  - Added bomb piece flagging and explosion handling
- **Rationale**: Extended to support new power-up features (bomb pieces, row clearing) while maintaining core Tetris gameplay mechanics.
- **Impact**: Enables power-up functionality without breaking existing game logic.

### `model.Score`
- **Location**: `src/main/java/model/Score.java`
- **Changes Made**:
  - Extended to track both score and skill points as separate values
  - Added helper methods for shop pricing logic
  - Maintained backward compatibility with existing score tracking
- **Rationale**: Skill points system needed for power-up economy. Separating score and skill points allows different reward mechanisms.
- **Impact**: Enables power-up shop functionality where players can purchase power-ups using skill points earned during gameplay.

### `model.PowerUpManager`
- **Location**: `src/main/java/model/PowerUpManager.java`
- **Changes Made**:
  - Upgraded inventory tracking system
  - Enhanced purchase validation logic
  - Added DTO (Data Transfer Object) methods for GUI updates
  - Integrated skill points awarding system
- **Rationale**: Power-up system required robust inventory management and integration with skill points economy. DTO methods enable clean separation between model and view layers.
- **Impact**: Provides foundation for power-up shop and inventory management features.

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

### Problem 1: FXML Controller Reference Error
- **Issue**: After moving `GuiController` from `controller` package to `controller.gui` package, JavaFX couldn't find the controller class, causing application startup failures.
- **Root Cause**: FXML file (`gameLayout.fxml`) still referenced the old package path `controller.GuiController`.
- **Solution**: Updated FXML file to reference `controller.gui.GuiController` using `fx:controller="controller.gui.GuiController"`.
- **Verification**: Tested by running `mvn javafx:run` and confirming application starts successfully.

### Problem 2: Duplicate Helper Classes
- **Issue**: During refactoring, both old helper classes in `controller/*` and new helper classes in `controller.gui/*` existed simultaneously, causing confusion and potential conflicts.
- **Root Cause**: Incomplete migration during refactoring process.
- **Solution**: Deleted obsolete `controller/*` helper classes, ensuring only `controller.gui/*` manager classes remain. Verified no imports reference old classes.
- **Verification**: Searched codebase for references to old classes and confirmed all imports updated.

### Problem 3: Package Ripple Effects
- **Issue**: Package reorganization caused import errors across multiple files, breaking compilation.
- **Root Cause**: Moving classes to new packages required updating all import statements throughout the codebase.
- **Solution**: Systematically updated all import statements, ran `mvn clean package` to verify compilation, and tested with `mvn javafx:run` to ensure runtime functionality.
- **Verification**: Full compilation success and manual gameplay testing confirmed all functionality intact.

### Problem 4: Blocks Dropping During Resume Countdown
- **Issue**: When resuming from pause, a 3-second countdown was displayed but blocks continued to drop during the countdown period, defeating the purpose of the pause system.
- **Root Cause**: The `showResumeCountdown()` method in `GuiControllerPauseManager` was showing the countdown overlay but not explicitly ensuring the game state remained paused. The `isPause` flag and timeline pause state weren't being enforced during countdown.
- **Solution**: Added explicit pause enforcement at the start of `showResumeCountdown()` method (lines 45-49) to set `isPause = true` and pause the timeline before starting countdown. Also added `resumeImmediately()` method for new games to avoid unnecessary countdown.
- **Verification**: Tested pause/resume functionality - confirmed blocks stop dropping immediately on ESC press and remain stopped during countdown. Verified new games start immediately without countdown.

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
 
