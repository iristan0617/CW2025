package controller.game;

import controller.gui.GuiController;
import model.Board;
import model.DownData;
import model.PowerUp;
import model.SimpleBoard;
import model.ViewData;
import view.ClearRow;

/**
 * Main game controller that orchestrates gameplay logic.
 * Implements InputEventListener to handle player input events and coordinates
 * between the game board model and the GUI view. Manages power-ups, scoring,
 * skill points, and game state transitions.
 * 
 * @author COMP2042 Coursework
 */
public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    /**
     * Constructs a new GameController and initializes the game.
     * Creates a new game board, sets up the GUI, and binds score/skill points.
     * 
     * @param c the GuiController instance to coordinate with
     */
    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.bindSkillPoints(getPowerUpManager().skillPointsProperty());
    }

    /**
     * Handles the down movement event (block falling).
     * Moves the current brick down one row. If it can't move, merges it to the
     * background, clears completed rows, awards points, and creates a new brick.
     * 
     * @param event the move event containing event source information
     * @return DownData containing clear row information and updated view data
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        if (!canMove) {
            board.mergeBrickToBackground();
            clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                int bonus = clearRow.getScoreBonus();
                board.getScore().add(bonus);
                // Award skill points (1 point per 10 score)
                getPowerUpManager().awardSkillPoints(bonus);
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }

            viewGuiController.refreshGameBackground(board.getBoardMatrix());

        } else {
            if (event.getEventSource() == EventSource.USER) {
                board.getScore().add(1);
                // Award skill points
                getPowerUpManager().awardSkillPoints(1);
            }
        }
        
        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Handles the left movement event.
     * Moves the current brick one position to the left.
     * 
     * @param event the move event
     * @return ViewData containing the updated brick position
     */
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    /**
     * Handles the right movement event.
     * Moves the current brick one position to the right.
     * 
     * @param event the move event
     * @return ViewData containing the updated brick position
     */
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    /**
     * Handles the rotation event.
     * Rotates the current brick counter-clockwise.
     * 
     * @param event the move event
     * @return ViewData containing the updated brick rotation
     */
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

    /**
     * Handles the hard drop event (instant drop to bottom).
     * Instantly drops the brick to the lowest possible position, awards bonus
     * points based on drop distance, and processes any bomb effects.
     * 
     * @param event the move event
     * @return DownData containing clear row information and updated view data
     */
    @Override
    public DownData onHardDropEvent(MoveEvent event) {
        // Hard drop: instantly drop the brick to the bottom
        // Calculate drop distance before dropping for scoring
        int dropDistance = ((SimpleBoard) board).getHardDropDistance();
        boolean dropped = board.hardDropBrick();
        if (dropped) {
            // Give score bonus based on drop distance (2 points per row dropped)
            int bonus = dropDistance * 2;
            board.getScore().add(bonus);
            // Award skill points
            getPowerUpManager().awardSkillPoints(bonus);
        }
        
        // Now merge the brick and process
        board.mergeBrickToBackground();
        
        // Check if bomb effect should be shown (only on hard drop)
        SimpleBoard simpleBoard = (SimpleBoard) board;
        if (simpleBoard.shouldShowBombEffect()) {
            viewGuiController.showBoomEffect(simpleBoard.getBombEffectX(), simpleBoard.getBombEffectY());
            simpleBoard.clearBombEffectFlag();
        }
        
        ClearRow clearRow = board.clearRows();
        if (clearRow.getLinesRemoved() > 0) {
            int bonus = clearRow.getScoreBonus();
            board.getScore().add(bonus);
            // Award skill points
            getPowerUpManager().awardSkillPoints(bonus);
        }
        if (board.createNewBrick()) {
            viewGuiController.gameOver();
        }
        
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        return new DownData(clearRow, board.getViewData());
    }

    /**
     * Handles the hold event (store current brick).
     * Stores the current brick in the hold slot and retrieves the previously
     * held brick if available.
     * 
     * @param event the move event
     * @return ViewData containing the updated brick (held or new)
     */
    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        board.holdBrick();
        return board.getViewData();
    }

    /**
     * Creates a new game by resetting the board.
     * Clears the game board and initializes a fresh game state.
     */
    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }

    /**
     * Gets the PowerUpManager instance from the board.
     * 
     * @return the PowerUpManager managing power-up inventory and purchases
     */
    public model.PowerUpManager getPowerUpManager() {
        return ((SimpleBoard) board).getPowerUpManager();
    }

    /**
     * Gets the game board instance.
     * 
     * @return the Board instance managing game state
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Purchases a power-up using skill points.
     * 
     * @param powerUp the power-up to purchase
     * @return true if purchase was successful, false if insufficient skill points
     */
    public boolean purchasePowerUp(PowerUp powerUp) {
        return getPowerUpManager().purchasePowerUp(powerUp);
    }

    /**
     * Activates a power-up if available in inventory.
     * Handles different power-up types: row clearer, slow motion, and bomb piece.
     * 
     * @param powerUp the power-up to activate
     * @return true if activation was successful, false if power-up not available
     */
    public boolean activatePowerUp(PowerUp powerUp) {
        // Check if player has the power-up
        if (!getPowerUpManager().usePowerUp(powerUp)) {
            return false;
        }

        switch (powerUp) {
            case ROW_CLEARER:
                // Clear bottom 3 rows
                boolean cleared = ((SimpleBoard) board).clearRowsPowerUp(3);
                if (cleared) {
                    // Refresh the game view
                    viewGuiController.refreshGameBackground(board.getBoardMatrix());
                    viewGuiController.refreshBrick(board.getViewData());
                }
                return cleared;

            case SLOW_MOTION:
                // Slow motion is handled by GuiController.applySlowMotion()
                viewGuiController.applySlowMotion();
                return true;

            case BOMB_PIECE:
                // Mark the next piece as a bomb piece
                // The bomb will explode when the piece lands
                ((SimpleBoard) board).setBombPiece(true);
                return true;

            default:
                return false;
        }
    }
}
