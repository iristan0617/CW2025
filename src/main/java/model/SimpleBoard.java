package model;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;
import view.ClearRow;
import view.NextShapeInfo;

import java.awt.*;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;
    private final PowerUpManager powerUpManager;
    private Brick heldBrick;
    private boolean canHold = true; // Can only hold once per piece placement
    private boolean isBombPiece = false; // Flag for bomb piece power-up

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[width][height];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
        powerUpManager = new PowerUpManager();
    }

    @Override
    public boolean moveBrickDown() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(0, 1);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }


    @Override
    public boolean moveBrickLeft() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(-1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean moveBrickRight() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point p = new Point(currentOffset);
        p.translate(1, 0);
        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(), (int) p.getX(), (int) p.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = p;
            return true;
        }
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        int[][] rotatedShape = nextShape.getShape();
        int currentX = (int) currentOffset.getX();
        int currentY = (int) currentOffset.getY();
        
        // Try rotation at current position first
        if (!MatrixOperations.intersect(currentMatrix, rotatedShape, currentX, currentY)) {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
        
        // Wall kick: try shifting left and right to find a valid rotation position
        // Standard wall kick offsets: try 1 left, 1 right, 2 left, 2 right
        int[] kickOffsets = {-1, 1, -2, 2};
        
        for (int offset : kickOffsets) {
            int testX = currentX + offset;
            if (!MatrixOperations.intersect(currentMatrix, rotatedShape, testX, currentY)) {
                // Found a valid position, update offset and rotate
                currentOffset = new Point(testX, currentY);
                brickRotator.setCurrentShape(nextShape.getPosition());
                return true;
            }
        }
        
        // All attempts failed
        return false;
    }

    @Override
    public boolean hardDropBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        int[][] brickShape = brickRotator.getCurrentShape();
        int currentX = (int) currentOffset.getX();
        int currentY = (int) currentOffset.getY();
        
        // Calculate drop position by simulating drop
        int dropY = currentY;
        while (true) {
            int testY = dropY + 1;
            if (MatrixOperations.intersect(currentMatrix, brickShape, currentX, testY)) {
                break; // Found collision, stop here
            }
            dropY = testY;
            // Safety check to prevent infinite loop
            if (dropY >= currentGameMatrix.length) {
                break;
            }
        }
        
        // Move brick to drop position
        currentOffset = new Point(currentX, dropY);
        return true;
    }

    public int getHardDropDistance() {
        // Calculate how many rows the brick will drop
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        int[][] brickShape = brickRotator.getCurrentShape();
        int currentX = (int) currentOffset.getX();
        int currentY = (int) currentOffset.getY();
        
        int dropY = currentY;
        while (true) {
            int testY = dropY + 1;
            if (MatrixOperations.intersect(currentMatrix, brickShape, currentX, testY)) {
                break;
            }
            dropY = testY;
            if (dropY >= currentGameMatrix.length) {
                break;
            }
        }
        
        return Math.max(0, dropY - currentY);
    }

    @Override
    public boolean holdBrick() {
        // Can only hold once per piece placement
        if (!canHold) {
            return false;
        }
        
        // Get the current brick from the rotator
        Brick currentBrick = brickRotator.getBrick();
        
        if (heldBrick == null) {
            // No held brick, so hold the current one and get a new brick
            heldBrick = currentBrick;
            Brick newBrick = brickGenerator.getBrick();
            brickRotator.setBrick(newBrick);
            currentOffset = new Point(4, 0);
            canHold = false; // Can't hold again until this piece is placed
            return true;
        } else {
            // Swap current brick with held brick
            Brick temp = heldBrick;
            heldBrick = currentBrick;
            brickRotator.setBrick(temp);
            currentOffset = new Point(4, 0);
            canHold = false; // Can't hold again until this piece is placed
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(4, 0);
        canHold = true; // Reset hold ability when new piece is created
        return MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    @Override
    public ViewData getViewData() {
        int[][] heldBrickData = null;
        if (heldBrick != null) {
            heldBrickData = heldBrick.getShapeMatrix().get(0);
        }
        return new ViewData(brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY(), brickGenerator.getNextBrick().getShapeMatrix().get(0), heldBrickData);
    }

    @Override
    public void mergeBrickToBackground() {
        // If this is a bomb piece, clear 4x4 area and don't place the piece
        if (isBombPiece) {
            Point bombCenter = getBombCenterPosition();
            if (bombCenter != null) {
                clearBombArea((int)bombCenter.getX(), (int)bombCenter.getY());
                // Store bomb position for visual effect (before resetting flag)
                bombEffectX = (int)bombCenter.getX();
                bombEffectY = (int)bombCenter.getY();
                shouldShowBombEffect = true;
            }
            // Reset bomb flag after use
            isBombPiece = false;
            // Don't merge the piece - the bomb explodes and disappears
            return;
        }
        
        // Normal piece - merge it to the background
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }
    
    // Temporary storage for bomb effect position
    private int bombEffectX, bombEffectY;
    private boolean shouldShowBombEffect = false;
    
    /**
     * Check if bomb effect should be shown and get position
     */
    public boolean shouldShowBombEffect() {
        return shouldShowBombEffect;
    }
    
    public int getBombEffectX() {
        return bombEffectX;
    }
    
    public int getBombEffectY() {
        return bombEffectY;
    }
    
    public void clearBombEffectFlag() {
        shouldShowBombEffect = false;
    }

    /**
     * Get the center position of the current brick for bomb explosion
     */
    private Point getBombCenterPosition() {
        int[][] brickShape = brickRotator.getCurrentShape();
        int centerX = (int) currentOffset.getX();
        int centerY = (int) currentOffset.getY();
        
        // Find the actual center block (first non-zero cell)
        for (int i = 0; i < brickShape.length; i++) {
            for (int j = 0; j < brickShape[i].length; j++) {
                if (brickShape[i][j] != 0) {
                    // Use the first non-zero cell as center, or find average
                    int worldX = centerX + j;
                    int worldY = centerY + i;
                    return new Point(worldX, worldY);
                }
            }
        }
        
        // Fallback: use offset position
        return new Point(centerX, centerY);
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;

    }

    @Override
    public Score getScore() {
        return score;
    }


    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        score.reset();
        powerUpManager.reset();
        heldBrick = null;
        canHold = true;
        isBombPiece = false;
        createNewBrick();
    }

    public PowerUpManager getPowerUpManager() {
        return powerUpManager;
    }

    /**
     * Set bomb piece flag (for bomb piece power-up)
     */
    public void setBombPiece(boolean isBomb) {
        this.isBombPiece = isBomb;
    }

    /**
     * Clear bottom numRows rows and shift everything above downward
     */
    public boolean clearRowsPowerUp(int numRows) {
        // Matrix structure: currentGameMatrix[width][height] where:
        // - width = 25 (number of rows)
        // - height = 10 (number of columns)
        // - Matrix is accessed as matrix[row][col] = matrix[y][x]
        // - Row 0 is at TOP (spawn area, hidden)
        // - Row 24 is at BOTTOM (where blocks land, visible)
        // We want to CLEAR the bottom numRows rows and shift everything above downward
        
        if (numRows <= 0 || numRows > width) {
            return false;
        }
        
        int bottomRow = width - 1;
        int destinationRow = bottomRow;
        
        // Walk upward through the matrix, copying rows that should remain
        for (int row = bottomRow; row >= 0; row--) {
            boolean rowCleared = (row >= width - numRows);
            
            if (!rowCleared) {
                // Copy this row down to the destination
                for (int col = 0; col < height; col++) {
                    currentGameMatrix[destinationRow][col] = currentGameMatrix[row][col];
                }
                destinationRow--;
            }
        }
        
        // Fill any remaining rows (above destinationRow) with zeros
        for (int row = destinationRow; row >= 0; row--) {
            for (int col = 0; col < height; col++) {
                currentGameMatrix[row][col] = 0;
            }
        }
        
        return true;
    }

    /**
     * Clear a 4x4 area centered at (centerX, centerY)
     */
    public boolean clearBombArea(int centerX, int centerY) {
        // Clear exactly a 4x4 area (4 rows x 4 columns) centered at (centerX, centerY)
        // Matrix structure: currentGameMatrix[width][height] where width=rows, height=columns
        // centerX is column, centerY is row
        
        // For a 4x4 area centered at (centerX, centerY):
        // Start 2 cells before center, end 1 cell after center = 4 cells total
        // This gives: center-2, center-1, center, center+1 = 4 cells
        int startRow = centerY - 1;  // Start 1 row above center
        int endRow = centerY + 2;    // End 2 rows below center (total 4 rows)
        int startCol = centerX - 1;  // Start 1 col left of center
        int endCol = centerX + 2;    // End 2 cols right of center (total 4 cols)
        
        // Clear exactly 4x4 area, handling boundaries
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                // Only clear if within bounds
                if (row >= 0 && row < width && col >= 0 && col < height) {
                    currentGameMatrix[row][col] = 0;
                }
            }
        }
        
        return true;
    }

    /**
     * Get bomb center position for visual effect
     */
    public Point getBombCenterForEffect() {
        return getBombCenterPosition();
    }

    /**
     * Check if bomb piece is active (before it explodes)
     */
    public boolean isBombPieceActive() {
        return isBombPiece;
    }

}
