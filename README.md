# COMP2042 COURSEWORK

**Student ID:** 20611068

**GitHub Repository:** https://github.com/iristan0617/CW2025.git

## Compilation Instructions

This is a Maven-based JavaFX project. To compile and run:

1. **Prerequisites:** Java JDK 17+ and Maven
2. **Import** as Maven project in IDE (IntelliJ)
3. **Run using:** `mvn javafx:run`
4. **Alternative:** Use the Maven tab in IntelliJ → Plugins → javafx → javafx:run

## Current Status

### Implemented and Working Properly
- Basic Tetris gameplay (movement, rotation, line clearing)
- Game over detection
- JavaFX graphical interface
- Next piece preview
- Score system
- Pause menu



### Implemented but Not Working Properly
- Unable to control blocks

### Features Not Implemented
- Hold piece functionality
- Level progression system
- High score persistence
- Sound effects

### New Java Classes

### Modified Java Classes
- `GameController.java` - Replaced magic numbers with constants
- `SimpleBoard.java` - Extracted movement logic, replaced magic numbers
- `MatrixOperations` - Decomposed complex `checkRemoving()` into 4 focused methods
- `MatrixOperations` - Renamed `checkOutOfBound()` to `isOutOfBounds()`, added `BASE_SCORE_PER_ROW` constant
- `gameLayout.fxml` - Added next brick preview panel and score display
- `GuiController.java` - Added next brick initialization and update methods
- `gameLayout.fxml`, `GuiController.java` - Professional pause menu activated by P key serving Resume, New Game, and Quit functionality
- `gameLayout.fxml`, `GuiController.java`, `Score.java`, `GameController.java` - Enhanced with score and lines updates for display and property binding
- `gameLayout.fxml` - Added next brick preview panel and score display
### Unexpected Problems

## Initial Setup Commit
- Identified project structure as Maven-based JavaFX application
- Configured IDE for proper execution
- Documented compilation process

### Completed Refactorings
1. **GameController:** Replaced magic numbers with constants
2. **SimpleBoard:**  Extracted duplicated movement logic into `attemptMove()` method
3. **SimpleBoard:**  Replaced magic numbers for starting position
4. **MatrixOperations**: Split `checkRemoving()` into 4 focused methods
5. **UI Addition:** Added new `GridPane` in FXML for next piece display 
6. **Controller Updates:** Extended `GuiController` with next brick rendering logic
7. **Architecture:** Leverages existing `ViewData` class which already had `nextBrickData` support
8. **Automatic Updates:** Preview updates in real-time as pieces move
9. **Package organisation:**  Clear separation of Model, View, Controller for better code navigation, simplified testing and maintenance
