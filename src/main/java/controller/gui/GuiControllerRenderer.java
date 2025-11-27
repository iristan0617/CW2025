package controller.gui;

import javafx.geometry.Point2D;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import model.MatrixOperations;
import model.ViewData;

/**
 * Handles rendering logic for GuiController
 * Extracted from GuiController to reduce file size
 */
class GuiControllerRenderer {
    
    private static final int BRICK_SIZE = 22;
    
    private final GuiController guiController;
    
    GuiControllerRenderer(GuiController guiController) {
        this.guiController = guiController;
    }
    
    void initGameView(int[][] boardMatrix, ViewData brick) {
        guiController.currentBoardMatrix = boardMatrix;
        guiController.displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                guiController.displayMatrix[i][j] = rectangle;
                guiController.gamePanel.add(rectangle, j, i - 2);
            }
        }

        int cols = boardMatrix[0].length;
        int rowsVisible = boardMatrix.length - 2;
        double w = cols * BRICK_SIZE + (cols - 1) * guiController.gamePanel.getHgap();
        double h = rowsVisible * BRICK_SIZE + (rowsVisible - 1) * guiController.gamePanel.getVgap();
        w += 2; h += 4;
        guiController.gamePanel.setPrefSize(w, h);
        guiController.gamePanel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        guiController.gamePanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        guiController.rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                setRectangleData(brick.getBrickData()[i][j], rectangle);
                guiController.rectangles[i][j] = rectangle;
                guiController.brickPanel.add(rectangle, j, i);
            }
        }

        // Create shadow panel for ghost preview
        guiController.shadowPanel = new GridPane();
        guiController.shadowPanel.setHgap(guiController.brickPanel.getHgap());
        guiController.shadowPanel.setVgap(guiController.brickPanel.getVgap());
        guiController.shadowPanel.setMouseTransparent(true); // Don't block mouse events
        int[][] brickData = brick.getBrickData();
        if (brickData != null && brickData.length > 0 && brickData[0].length > 0) {
            guiController.shadowRectangles = new Rectangle[brickData.length][brickData[0].length];
            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    Rectangle shadowRect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    shadowRect.setFill(Color.GRAY);
                    shadowRect.setOpacity(0.55);
                    shadowRect.setStroke(Color.DARKGRAY);
                    shadowRect.setStrokeWidth(1.0);
                    guiController.shadowRectangles[i][j] = shadowRect;
                    guiController.shadowPanel.add(shadowRect, j, i);
                }
            }
        }
        // Add shadow panel to the same parent as brickPanel
        Pane root = (Pane) guiController.gameBoard.getParent();
        if (root != null && !root.getChildren().contains(guiController.shadowPanel)) {
            root.getChildren().add(guiController.shadowPanel);
        }

        Point2D origin = gamePanelOriginInRoot();
        guiController.brickPanel.setLayoutX(origin.getX() + brick.getxPosition() * guiController.brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
        int displayRow = brick.getyPosition() - 2;
        guiController.brickPanel.setLayoutY(origin.getY() + displayRow * (guiController.brickPanel.getHgap() + BRICK_SIZE));

        renderNextPreview(brick.getNextBrickData());
        renderHoldPreview(brick.getHeldBrickData());
        updateShadow(brick);
    }
    
    void refreshBrick(ViewData brick) {
        if (guiController.isPause.getValue() == Boolean.FALSE) {
            Point2D origin = gamePanelOriginInRoot();
            guiController.brickPanel.setLayoutX(origin.getX() + brick.getxPosition() * guiController.brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
            int displayRow = brick.getyPosition() - 2;
            guiController.brickPanel.setLayoutY(origin.getY() + displayRow * (guiController.brickPanel.getHgap() + BRICK_SIZE));
            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], guiController.rectangles[i][j]);
                }
            }
            renderNextPreview(brick.getNextBrickData());
            renderHoldPreview(brick.getHeldBrickData());
            updateShadow(brick);
        }
    }

    void refreshGameBackground(int[][] board) {
        guiController.currentBoardMatrix = board;
        for (int i = 2; i < board.length && i < guiController.displayMatrix.length; i++) {
            for (int j = 0; j < board[i].length && j < guiController.displayMatrix[i].length; j++) {
                if (guiController.displayMatrix[i][j] != null) {
                    setRectangleData(board[i][j], guiController.displayMatrix[i][j]);
                }
            }
        }
    }

    private Paint getFillColor(int i) {
        switch (i) {
            case 0: return Color.TRANSPARENT;
            case 1: return Color.AQUA;
            case 2: return Color.BLUEVIOLET;
            case 3: return Color.DARKGREEN;
            case 4: return Color.YELLOW;
            case 5: return Color.RED;
            case 6: return Color.BEIGE;
            case 7: return Color.BURLYWOOD;
            default: return Color.WHITE;
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(0);
        rectangle.setArcWidth(0);

        // Add glow effect
        if (color != 0) { // Only glow non-transparent bricks
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow();
            glow.setLevel(0.6); // Adjust between 0.0 (no glow) and 1.0 (maximum glow)
            rectangle.setEffect(glow);
        } else {
            rectangle.setEffect(null); // No glow for transparent bricks
        }
    }

    private int calculateDropPosition(int[][] brickData, int x, int y) {
        if (guiController.currentBoardMatrix == null) return y;
        
        int dropY = y;
        // Simulate dropping the brick until it hits something
        while (true) {
            int testY = dropY + 1;
            // Check if the brick would collide at testY position
            if (MatrixOperations.intersect(guiController.currentBoardMatrix, brickData, x, testY)) {
                break; // Found collision, stop here
            }
            dropY = testY;
            // Safety check to prevent infinite loop
            if (dropY >= guiController.currentBoardMatrix.length) {
                break;
            }
        }
        return dropY;
    }

    private void updateShadow(ViewData brick) {
        if (guiController.shadowPanel == null || guiController.currentBoardMatrix == null) {
            return;
        }

        int[][] brickData = brick.getBrickData();
        int dropY = calculateDropPosition(brickData, brick.getxPosition(), brick.getyPosition());
        
        // Only show shadow if it's different from current position and below the current position
        if (dropY <= brick.getyPosition()) {
            guiController.shadowPanel.setVisible(false);
            return;
        }

        guiController.shadowPanel.setVisible(true);
        
        // Check if shadow rectangles array needs to be resized
        if (brickData == null || brickData.length == 0 || brickData[0].length == 0) {
            guiController.shadowPanel.setVisible(false);
            return;
        }
        if (guiController.shadowRectangles == null || guiController.shadowRectangles.length != brickData.length || 
            (brickData.length > 0 && (guiController.shadowRectangles[0] == null || guiController.shadowRectangles[0].length != brickData[0].length))) {
            // Recreate shadow rectangles if size changed
            guiController.shadowPanel.getChildren().clear();
            guiController.shadowRectangles = new Rectangle[brickData.length][brickData[0].length];
            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    Rectangle shadowRect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    shadowRect.setFill(Color.GRAY);
                    shadowRect.setOpacity(0.7);
                    shadowRect.setStroke(Color.DARKGRAY);
                    shadowRect.setStrokeWidth(1.5);
                    guiController.shadowRectangles[i][j] = shadowRect;
                    guiController.shadowPanel.add(shadowRect, j, i);
                }
            }
        }
        
        // Update shadow rectangles to match brick shape - all grey color (darker)
        Color shadowGrey = Color.DARKGRAY;
        Color shadowStroke = Color.BLACK;
        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                Rectangle shadowRect = guiController.shadowRectangles[i][j];
                if (shadowRect != null) {
                    if (brickData[i][j] != 0) {
                        // Show shadow for non-empty cells - use grey for all blocks
                        shadowRect.setVisible(true);
                        shadowRect.setFill(shadowGrey);
                        shadowRect.setStroke(shadowStroke);
                    } else {
                        shadowRect.setVisible(false);
                    }
                }
            }
        }

        // Position shadow panel at drop location
        Point2D origin = gamePanelOriginInRoot();
        guiController.shadowPanel.setLayoutX(origin.getX() + brick.getxPosition() * guiController.shadowPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
        int displayRow = dropY - 2;
        guiController.shadowPanel.setLayoutY(origin.getY() + displayRow * (guiController.shadowPanel.getHgap() + BRICK_SIZE));
        
        // Ensure shadow is behind the brick but visible
        guiController.shadowPanel.toBack();
    }

    private void renderNextPreview(int[][] next) {
        if (next == null || next.length == 0) return;
        guiController.nextPanel.getChildren().clear();
        guiController.nextPreview = new Rectangle[next.length][next[0].length];
        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next[i].length; j++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(getFillColor(next[i][j]));
                guiController.nextPreview[i][j] = r;
                guiController.nextPanel.add(r, j, i);
            }
        }
    }

    private void renderHoldPreview(int[][] hold) {
        if (guiController.holdPanel == null) return;
        guiController.holdPanel.getChildren().clear();
        if (hold == null || hold.length == 0) {
            // No held piece, show empty placeholder (4x4 grid of transparent/empty cells)
            guiController.holdPreview = new Rectangle[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    r.setFill(Color.TRANSPARENT);
                    r.setStroke(Color.rgb(255, 255, 255, 0.1));
                    r.setStrokeWidth(0.5);
                    guiController.holdPreview[i][j] = r;
                    guiController.holdPanel.add(r, j, i);
                }
            }
            return;
        }
        guiController.holdPreview = new Rectangle[hold.length][hold[0].length];
        for (int i = 0; i < hold.length; i++) {
            for (int j = 0; j < hold[i].length; j++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(getFillColor(hold[i][j]));
                guiController.holdPreview[i][j] = r;
                guiController.holdPanel.add(r, j, i);
            }
        }
    }
    
    private Point2D gamePanelOriginInRoot() {
        Pane root = (Pane) guiController.gameBoard.getParent();
        Point2D scenePt = guiController.gamePanel.localToScene(0, 0);
        return root.sceneToLocal(scenePt);
    }
}

