package controller.gui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Manages visual effects for GuiController.
 * Handles boom/explosion effects for bomb power-ups and board centering calculations.
 * Extracted from GuiController to apply Single Responsibility Principle.
 * 
 * @author COMP2042 Coursework
 */
class GuiControllerEffectManager {
    
    private static final int BRICK_SIZE = 22;
    
    private final GuiController guiController;
    
    /**
     * Constructs a new GuiControllerEffectManager.
     * 
     * @param guiController the GuiController instance to manage effects for
     */
    GuiControllerEffectManager(GuiController guiController) {
        this.guiController = guiController;
    }
    
    /**
     * Centers the game board within its parent container.
     * Calculates the optimal position to center the board both horizontally and vertically.
     */
    void centerGameBoard() {
        javafx.scene.layout.Pane root = (javafx.scene.layout.Pane) guiController.gameBoard.getParent();
        javafx.geometry.Bounds b = guiController.gameBoard.getBoundsInParent();
        double w = b.getWidth();
        double h = b.getHeight();

        double x = Math.max(0, (root.getWidth() - w) / 2);
        double y = Math.max(0, (root.getHeight() - h) / 2);
        guiController.gameBoard.setLayoutX(x);
        guiController.gameBoard.setLayoutY(y);
    }

    void showBoomEffect(int gridX, int gridY) {
        if (guiController.gameBoard == null || guiController.boardStack == null) return;

        // Convert grid coordinates to screen coordinates
        // gridX is column (0-9), gridY is row (0-24, visible rows start at 2)
        double cellW = BRICK_SIZE + guiController.gamePanel.getHgap();
        double cellH = BRICK_SIZE + guiController.gamePanel.getVgap();

        // Calculate position relative to boardStack
        // gridY needs to account for the 2 hidden rows at top
        double x = gridX * cellW + cellW / 2; // Center of the cell
        double y = (gridY - 2) * cellH + cellH / 2; // Center of the cell (accounting for 2 hidden rows)

        // Create BOOM! label
        Label boomLabel = new Label("BOOM!");
        boomLabel.setStyle(
            "-fx-font-size: 48px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #FF4500; " + // Orange-red color
            "-fx-effect: dropshadow(gaussian, rgba(255,69,0,0.9), 15, 0.8, 0, 4);"
        );

        // Position it at the explosion location (relative to boardStack)
        boomLabel.setLayoutX(x - 60); // Center the text (approx half width)
        boomLabel.setLayoutY(y - 24); // Center the text (approx half height)

        // Add to boardStack so it appears on top of everything
        guiController.boardStack.getChildren().add(boomLabel);
        boomLabel.toFront();

        // Animate: scale up, then fade out
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), boomLabel);
        scaleTransition.setFromX(0.5);
        scaleTransition.setFromY(0.5);
        scaleTransition.setToX(1.5);
        scaleTransition.setToY(1.5);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), boomLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.setOnFinished(e -> {
            guiController.boardStack.getChildren().remove(boomLabel);
        });
        parallelTransition.play();
    }
}

