# COMP2042 Coursework - Tetris Maintenance

## GitHub Repository
https://github.com/iristan0617/CW2025.git

## Compilation Instructions
- Configured a new Maven run configuration named “run tetris”
- Set the goal to `javafx:run` to properly launch the JavaFX application
- Verified that the game starts up and runs

## Implemented and Working Properly
- Basic Tetris Gameplay: Tetrominoes spawn, fall, and can be moved/rotated
- Line Clearing: Complete horizontal lines disappear and player scores points
- Collision Detection: Pieces cannot move through walls or settled blocks
- Game Over Detection: Game ends when blocks reach the top of the playing field
- Maven Build System: Project successfully compiles and runs via mvn javafx:run
- Dynamic Game Board Centering: Game board automatically centers in window
- Score and lines display
- Next piece preview
- Pause menu overlay
- Main Menu
- Hold piece function
- 3 differnet Power-Ups
  
## Implemented but Not Working Properly
- [Feature with issues]
- [Another feature with problems]

## Features Not Implemented
- 

## New Java Classes
- [Class Name]: [Purpose] - [Location]

## Modified Java Classes
- Reorganized all classes into MVC architecture
- Model: Game logic classes
- View: UI components
- Controller: Input/coordination
- Improved code structure
- GuiController: Added dynamic game board centering system
- GuiController: Added score, lines and next piece preview
- GuiController: Added pause menu overlay

## Unexpected Problems
- Game panel is twitching a bit
- Bricks in the game board and next piece preview looks different
