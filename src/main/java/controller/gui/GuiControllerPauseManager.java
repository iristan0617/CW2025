package controller.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Manages pause/resume and countdown logic for GuiController
 * Extracted from GuiController to reduce file size
 */
class GuiControllerPauseManager {
    
    private final GuiController guiController;
    
    GuiControllerPauseManager(GuiController guiController) {
        this.guiController = guiController;
    }
    
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
    
    void showResumeCountdown() {
        // Don't show countdown if already counting down or if game is over
        if (guiController.countdownOverlay != null && guiController.countdownOverlay.isVisible()) {
            return;
        }
        if (guiController.isGameOver.getValue()) {
            return;
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

