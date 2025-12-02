package model;

import view.ClearRow;

/**
 * Data transfer object containing information about a downward movement event.
 * Encapsulates line clearing results and updated view data after a brick moves down
 * or is placed. Used to communicate game state changes from the board to the controller.
 * 
 * @author COMP2042 Coursework
 */
public final class DownData {
    private final ClearRow clearRow;
    private final ViewData viewData;

    /**
     * Constructs a DownData object.
     * 
     * @param clearRow information about cleared rows (lines removed, score bonus)
     * @param viewData updated view data after the movement
     */
    public DownData(ClearRow clearRow, ViewData viewData) {
        this.clearRow = clearRow;
        this.viewData = viewData;
    }

    /**
     * Gets the line clearing information.
     * 
     * @return ClearRow containing lines removed and score bonus information
     */
    public ClearRow getClearRow() {
        return clearRow;
    }

    /**
     * Gets the updated view data after the movement.
     * 
     * @return ViewData containing updated brick positions and shapes
     */
    public ViewData getViewData() {
        return viewData;
    }
}
