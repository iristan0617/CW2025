package model;

public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int[][] nextBrickData;
    private final int[][] heldBrickData;

    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData, int[][] heldBrickData) {
        this.brickData = brickData;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = nextBrickData;
        this.heldBrickData = heldBrickData;
    }

    // Constructor for backward compatibility
    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData) {
        this(brickData, xPosition, yPosition, nextBrickData, null);
    }

    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }

    public int[][] getHeldBrickData() {
        return heldBrickData != null ? MatrixOperations.copy(heldBrickData) : null;
    }
}
