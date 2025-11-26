package controller.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import controller.game.EventSource;
import controller.game.EventType;
import controller.game.MoveEvent;

/**
 * Manages slow motion power-up effect for GuiController
 * Extracted from GuiController to reduce file size
 */
class GuiControllerSlowMotionManager {
    
    private static final long SLOW_MOTION_SPEED_MS = 800; // 2x slower than normal (400ms)
    private static final long NORMAL_SPEED_MS = 400;
    
    private final GuiController guiController;
    
    GuiControllerSlowMotionManager(GuiController guiController) {
        this.guiController = guiController;
    }
    
    void applySlowMotion() {
        if (guiController.timeLine == null || guiController.isPause.getValue() || guiController.isGameOver.getValue()) {
            return;
        }

        // Stop any existing timers
        if (guiController.slowMotionRestoreTimer != null) {
            guiController.slowMotionRestoreTimer.stop();
        }
        if (guiController.slowMotionCountdownTimer != null) {
            guiController.slowMotionCountdownTimer.stop();
        }

        // Reset countdown
        guiController.slowMotionRemainingSeconds = 10;

        // Show countdown timer (if label exists)
        if (guiController.slowMotionCountdownLabel != null) {
            guiController.slowMotionCountdownLabel.setVisible(true);
        }
        updateSlowMotionCountdown();

        // Stop current timeline
        guiController.timeLine.stop();

        // Create new timeline with slow speed
        guiController.timeLine = new Timeline(new KeyFrame(Duration.millis(SLOW_MOTION_SPEED_MS),
                ae -> guiController.moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        guiController.timeLine.setCycleCount(Timeline.INDEFINITE);
        guiController.timeLine.play();

        // Create countdown timer that updates every second
        guiController.slowMotionCountdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
            guiController.slowMotionRemainingSeconds--;
            updateSlowMotionCountdown();
            if (guiController.slowMotionRemainingSeconds <= 0) {
                guiController.slowMotionCountdownTimer.stop();
            }
        }));
        guiController.slowMotionCountdownTimer.setCycleCount(10); // 10 seconds
        guiController.slowMotionCountdownTimer.play();

        // Create timer to restore normal speed after 10 seconds
        guiController.slowMotionRestoreTimer = new Timeline(new KeyFrame(Duration.seconds(10), ae -> restoreNormalSpeed()));
        guiController.slowMotionRestoreTimer.setCycleCount(1);
        guiController.slowMotionRestoreTimer.play();
    }

    private void restoreNormalSpeed() {
        if (guiController.timeLine == null || guiController.isPause.getValue() || guiController.isGameOver.getValue()) {
            return;
        }

        guiController.timeLine.stop();
        guiController.timeLine = new Timeline(new KeyFrame(Duration.millis(NORMAL_SPEED_MS),
                ae -> guiController.moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        guiController.timeLine.setCycleCount(Timeline.INDEFINITE);
        guiController.timeLine.play();

        if (guiController.slowMotionCountdownLabel != null) {
            guiController.slowMotionCountdownLabel.setVisible(false);
        }
    }

    private void updateSlowMotionCountdown() {
        if (guiController.slowMotionCountdownLabel != null) {
            guiController.slowMotionCountdownLabel.setText(String.valueOf(guiController.slowMotionRemainingSeconds));
        }
    }
}

