package model;

import view.ClearRow;

/**
 * Interface defining the contract for a Tetris game board.
 * Provides methods for brick movement, rotation, line clearing, and game state management.
 * Implementations manage the game matrix, current brick position, and scoring.
 * 
 * @author COMP2042 Coursework
 */
public interface Board {

    boolean moveBrickDown();

    boolean moveBrickLeft();

    boolean moveBrickRight();

    boolean rotateLeftBrick();

    boolean hardDropBrick();

    boolean holdBrick();

    boolean createNewBrick();

    int[][] getBoardMatrix();

    ViewData getViewData();

    void mergeBrickToBackground();

    ClearRow clearRows();

    Score getScore();

    void newGame();
}
