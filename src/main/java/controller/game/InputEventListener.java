package controller.game;

import model.DownData;
import model.ViewData;

/**
 * Interface for handling player input events in the game.
 * Defines methods for processing all types of player actions including movement,
 * rotation, hard drop, hold, and game creation. Implementations process these
 * events and return updated game state data.
 * 
 * @author COMP2042 Coursework
 */
public interface InputEventListener {

    /**
     * Handles a downward movement event (brick falling).
     * 
     * @param event the move event containing event source information
     * @return DownData containing line clearing info and updated view data
     */
    DownData onDownEvent(MoveEvent event);

    /**
     * Handles a left movement event.
     * 
     * @param event the move event
     * @return ViewData containing updated brick position
     */
    ViewData onLeftEvent(MoveEvent event);

    /**
     * Handles a right movement event.
     * 
     * @param event the move event
     * @return ViewData containing updated brick position
     */
    ViewData onRightEvent(MoveEvent event);

    /**
     * Handles a rotation event.
     * 
     * @param event the move event
     * @return ViewData containing updated brick rotation
     */
    ViewData onRotateEvent(MoveEvent event);

    /**
     * Handles a hard drop event (instant drop to bottom).
     * 
     * @param event the move event
     * @return DownData containing line clearing info and updated view data
     */
    DownData onHardDropEvent(MoveEvent event);

    /**
     * Handles a hold event (store current brick).
     * 
     * @param event the move event
     * @return ViewData containing updated brick (held or new)
     */
    ViewData onHoldEvent(MoveEvent event);

    /**
     * Creates a new game by resetting the board.
     */
    void createNewGame();
}
