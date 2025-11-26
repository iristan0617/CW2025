package controller.gui;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import controller.game.EventSource;
import controller.game.EventType;
import controller.game.MoveEvent;
import model.PowerUp;

/**
 * Handles keyboard input for GuiController
 * Extracted from GuiController to reduce file size
 */
class GuiControllerKeyboardHandler {
    
    private final GuiController guiController;
    
    GuiControllerKeyboardHandler(GuiController guiController) {
        this.guiController = guiController;
    }
    
    EventHandler<KeyEvent> createKeyHandler() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (guiController.isPause.getValue() == Boolean.FALSE && guiController.isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        guiController.refreshBrick(guiController.eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        guiController.refreshBrick(guiController.eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        guiController.refreshBrick(guiController.eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        guiController.moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        guiController.hardDrop(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.C) {
                        guiController.refreshBrick(guiController.eventListener.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER)));
                        keyEvent.consume();
                    }
                    
                    // Power-up shortcuts: 1, 2, 3 to use power-ups
                    if (keyEvent.getCode() == KeyCode.DIGIT1 || keyEvent.getCode() == KeyCode.NUMPAD1) {
                        if (guiController.gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 0) {
                                guiController.gameController.activatePowerUp(powerUps[0]); // Row Clearer
                                guiController.updatePowerUpUI();
                                if (powerUps[0] == PowerUp.ROW_CLEARER) {
                                    guiController.refreshGameBackground(guiController.gameController.getBoard().getBoardMatrix());
                                }
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT2 || keyEvent.getCode() == KeyCode.NUMPAD2) {
                        if (guiController.gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 1) {
                                guiController.gameController.activatePowerUp(powerUps[1]); // Slow Motion
                                guiController.updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT3 || keyEvent.getCode() == KeyCode.NUMPAD3) {
                        if (guiController.gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 2) {
                                guiController.gameController.activatePowerUp(powerUps[2]); // Bomb Piece
                                guiController.updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                }
                
                // Power-up purchase shortcuts: Shift+1, Shift+2, Shift+3 to buy
                if (keyEvent.isShiftDown()) {
                    if (keyEvent.getCode() == KeyCode.DIGIT1 || keyEvent.getCode() == KeyCode.NUMPAD1) {
                        if (guiController.gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 0) {
                                guiController.gameController.purchasePowerUp(powerUps[0]);
                                guiController.updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT2 || keyEvent.getCode() == KeyCode.NUMPAD2) {
                        if (guiController.gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 1) {
                                guiController.gameController.purchasePowerUp(powerUps[1]);
                                guiController.updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT3 || keyEvent.getCode() == KeyCode.NUMPAD3) {
                        if (guiController.gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 2) {
                                guiController.gameController.purchasePowerUp(powerUps[2]);
                                guiController.updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                }
                
                if (keyEvent.getCode() == KeyCode.N) {
                    guiController.newGame(null);
                }
                if (keyEvent.getCode() == KeyCode.P || keyEvent.getCode() == KeyCode.ESCAPE) {
                    // If countdown is showing, cancel it and stay paused
                    if (guiController.countdownOverlay != null && guiController.countdownOverlay.isVisible()) {
                        guiController.cancelCountdown();
                        keyEvent.consume();
                    } else {
                        guiController.setPaused(!guiController.isPause.get());
                        keyEvent.consume();
                    }
                }
                if (keyEvent.getCode() == KeyCode.B) {
                    guiController.togglePowerUpsOverlay();
                    keyEvent.consume();
                }
            }
        };
    }
}

