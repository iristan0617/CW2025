package model;

/**
 * Data transfer object containing all view-related data for rendering.
 * Encapsulates the current brick data, position, next brick preview, and held brick data.
 * Used to pass rendering information from the game model to the GUI.
 * 
 * @author COMP2042 Coursework
 */
public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int[][] nextBrickData;
    private final int[][] heldBrickData;

    /**
     * Constructs a ViewData object with all brick information.
     * 
     * @param brickData the 2D array representing the current brick shape
     * @param xPosition the x-coordinate of the current brick
     * @param yPosition the y-coordinate of the current brick
     * @param nextBrickData the 2D array representing the next brick shape
     * @param heldBrickData the 2D array representing the held brick shape, or null if none
     */
    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData, int[][] heldBrickData) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = nextBrickData;
        this.heldBrickData = heldBrickData;
    }

    /**
     * Constructs a ViewData object without held brick (for backward compatibility).
     * 
     * @param brickData the 2D array representing the current brick shape
     * @param xPosition the x-coordinate of the current brick
     * @param yPosition the y-coordinate of the current brick
     * @param nextBrickData the 2D array representing the next brick shape
     */
    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData) {
        this(brickData, xPosition, yPosition, nextBrickData, null);
    }

    /**
     * Gets a copy of the current brick data.
     * 
     * @return a copy of the 2D array representing the current brick shape
     */
    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    /**
     * Gets the x-coordinate of the current brick.
     * 
     * @return the x position
     */
    public int getxPosition() {
        return xPosition;
    }

    /**
     * Gets the y-coordinate of the current brick.
     * 
     * @return the y position
     */
    public int getyPosition() {
        return yPosition;
    }

    /**
     * Gets a copy of the next brick data.
     * 
     * @return a copy of the 2D array representing the next brick shape
     */
    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }

    /**
     * Gets a copy of the held brick data, or null if no brick is held.
     * 
     * @return a copy of the 2D array representing the held brick shape, or null
     */
    public int[][] getHeldBrickData() {
        return heldBrickData != null ? MatrixOperations.copy(heldBrickData) : null;
    }
}
