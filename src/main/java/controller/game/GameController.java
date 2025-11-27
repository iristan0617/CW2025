package controller.game;

import controller.gui.GuiController;
import model.Board;
import model.DownData;
import model.PowerUp;
import model.SimpleBoard;
import model.ViewData;
import view.ClearRow;

public class GameController implements InputEventListener {

    private Board board = new SimpleBoard(25, 10);

    private final GuiController viewGuiController;

    public GameController(GuiController c) {
        viewGuiController = c;
        board.createNewBrick();
        viewGuiController.setEventListener(this);
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.bindSkillPoints(getPowerUpManager().skillPointsProperty());
    }

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

    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        return board.getViewData();
    }

    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        return board.getViewData();
    }

    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        return board.getViewData();
    }

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

    @Override
    public ViewData onHoldEvent(MoveEvent event) {
        board.holdBrick();
        return board.getViewData();
    }

    @Override
    public void createNewGame() {
        board.newGame();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
    }

    /**
     * Get PowerUpManager
     */
    public model.PowerUpManager getPowerUpManager() {
        return ((SimpleBoard) board).getPowerUpManager();
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Purchase a power-up
     */
    public boolean purchasePowerUp(PowerUp powerUp) {
        return getPowerUpManager().purchasePowerUp(powerUp);
    }

    /**
     * Activate a power-up
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
