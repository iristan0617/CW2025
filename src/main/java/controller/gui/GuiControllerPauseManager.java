package controller.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Manages pause/resume and countdown logic for GuiController.
 * This class handles the pause state, displays pause overlays, and manages
 * the 3-second countdown before resuming gameplay. Extracted from GuiController
 * to apply Single Responsibility Principle and improve maintainability.
 * 
 * @author COMP2042 Coursework
 */
class GuiControllerPauseManager {
    
    private final GuiController guiController;
    
    /**
     * Constructs a new GuiControllerPauseManager.
     * 
     * @param guiController the GuiController instance to manage pause state for
     */
    GuiControllerPauseManager(GuiController guiController) {
        this.guiController = guiController;
    }
    
    /**
     * Sets the pause state of the game.
     * If pausing, immediately pauses the game and shows the pause overlay.
     * If resuming, shows a 3-second countdown before actually resuming.
     * 
     * @param paused true to pause the game immediately, false to show resume countdown
     */
    void setPaused(boolean paused) {
        if (paused) {
            // Pausing - immediate
            guiController.isPause.setValue(true);
            if (guiController.timeLine != null) {
                guiController.timeLine.pause();
            }
            if (guiController.pauseOverlay != null) {
                guiController.pauseOverlay.setVisible(true);
                guiController.pauseOverlay.toFront();
            }
        } else {
            // Unpausing - show countdown first
            showResumeCountdown();
        }
    }
    
    /**
     * Displays a 3-second countdown before resuming the game.
     * Ensures the game remains paused during the countdown to prevent blocks
     * from dropping. The countdown overlay shows numbers 3, 2, 1 before
     * actually resuming gameplay.
     */
    void showResumeCountdown() {
        // Don't show countdown if already counting down or if game is over
        if (guiController.countdownOverlay != null && guiController.countdownOverlay.isVisible()) {
            return;
        }
        if (guiController.isGameOver.getValue()) {
            return;
        }
        
        // Ensure game stays paused during countdown - blocks should not drop
        guiController.isPause.setValue(true);
        if (guiController.timeLine != null) {
            guiController.timeLine.pause();
        }
        
        // Stop any existing countdown timer
        if (guiController.countdownTimer != null) {
            guiController.countdownTimer.stop();
        }
        
        // Ensure countdown overlay is in root stack pane
        if (guiController.rootStackPane != null && !guiController.rootStackPane.getChildren().contains(guiController.countdownOverlay)) {
            guiController.rootStackPane.getChildren().add(guiController.countdownOverlay);
        }
        
        // Hide pause overlay during countdown
        if (guiController.pauseOverlay != null) {
            guiController.pauseOverlay.setVisible(false);
        }
        
        // Show countdown overlay
        guiController.countdownOverlay.setVisible(true);
        guiController.countdownOverlay.toFront();
        
        // Start countdown from 3
        int[] countdownValue = {3};
        guiController.countdownLabel.setText(String.valueOf(countdownValue[0]));
        
        // Create countdown timeline
        guiController.countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
            countdownValue[0]--;
            if (countdownValue[0] > 0) {
                guiController.countdownLabel.setText(String.valueOf(countdownValue[0]));
            } else {
                // Countdown finished - actually resume
                guiController.countdownOverlay.setVisible(false);
                guiController.countdownTimer.stop();
                
                // Actually unpause now
                guiController.isPause.setValue(false);
                if (guiController.timeLine != null) {
                    guiController.timeLine.play();
                }
                guiController.gamePanel.requestFocus();
            }
        }));
        guiController.countdownTimer.setCycleCount(3); // 3 seconds
        guiController.countdownTimer.play();
    }
    
    /**
     * Resumes the game immediately without showing a countdown.
     * Used when starting a new game to avoid unnecessary delay.
     * Cancels any existing countdown and immediately unpauses the game.
     */
    void resumeImmediately() {
        // Cancel any existing countdown
        if (guiController.countdownTimer != null) {
            guiController.countdownTimer.stop();
        }
        if (guiController.countdownOverlay != null) {
            guiController.countdownOverlay.setVisible(false);
        }
        
        // Resume immediately
        guiController.isPause.setValue(false);
        if (guiController.timeLine != null) {
            guiController.timeLine.play();
        }
        if (guiController.pauseOverlay != null) {
            guiController.pauseOverlay.setVisible(false);
        }
        guiController.gamePanel.requestFocus();
    }
    
    /**
     * Cancels the current countdown and returns to the paused state.
     * Hides the countdown overlay and shows the pause overlay again.
     * Used when the user presses ESC during the countdown.
     */
    void cancelCountdown() {
        if (guiController.countdownTimer != null) {
            guiController.countdownTimer.stop();
        }
        if (guiController.countdownOverlay != null) {
            guiController.countdownOverlay.setVisible(false);
        }
        // Show pause overlay again
        if (guiController.pauseOverlay != null) {
            guiController.pauseOverlay.setVisible(true);
            guiController.pauseOverlay.toFront();
        }
    }
}

