package controller.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.PowerUp;

/**
 * Manages power-up UI for GuiController
 * Extracted from GuiController to reduce file size
 */
class GuiControllerPowerUpManager {
    
    private final GuiController guiController;
    
    GuiControllerPowerUpManager(GuiController guiController) {
        this.guiController = guiController;
    }
    
    void initializePowerUpUI() {
        if (guiController.gameController == null) {
            return;
        }
        
        // Clear both containers
        if (guiController.powerUpsContainer != null) {
            guiController.powerUpsContainer.getChildren().clear();
        }
        if (guiController.powerUpsContainerCentered != null) {
            guiController.powerUpsContainerCentered.getChildren().clear();
        }
        
        PowerUp[] powerUps = PowerUp.values();
        for (int i = 0; i < powerUps.length; i++) {
            PowerUp powerUp = powerUps[i];
            int keyNumber = i + 1; // 1, 2, 3 for the three power-ups
            
            // Create card container
            VBox card = new VBox(10);
            card.setAlignment(javafx.geometry.Pos.CENTER);
            card.setPrefWidth(140);
            card.setMinWidth(140);
            card.setMaxWidth(140);
            card.setStyle(
                "-fx-background-color: rgba(30, 30, 40, 0.9); " +
                "-fx-background-radius: 15; " +
                "-fx-border-radius: 15; " +
                "-fx-border-color: rgba(255, 255, 255, 0.3); " +
                "-fx-border-width: 2; " +
                "-fx-padding: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 8, 0.0, 0, 2);"
            );
            
            // Icon/Visual representation (using emoji/symbols)
            Label iconLabel = new Label(getPowerUpIcon(powerUp));
            iconLabel.setStyle(
                "-fx-font-size: 48px; " +
                "-fx-alignment: center; " +
                "-fx-padding: 10 0 5 0;"
            );
            iconLabel.setId("powerup_" + powerUp.name() + "_icon");
            
            // Power-up name
            Label nameLabel = new Label(powerUp.getName());
            nameLabel.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #ffff00; " +
                "-fx-alignment: center;"
            );
            nameLabel.setWrapText(true);
            nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            // Large quantity display
            Label quantityLabel = new Label("x0");
            quantityLabel.setId("powerup_" + powerUp.name() + "_qty");
            quantityLabel.setStyle(
                "-fx-font-size: 32px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #00ff00; " +
                "-fx-alignment: center; " +
                "-fx-padding: 5 0;"
            );
            
            // Action button
            Button actionButton = new Button();
            actionButton.setId("powerup_" + powerUp.name() + "_btn");
            actionButton.setPrefWidth(120);
            actionButton.setPrefHeight(35);
            actionButton.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-border-radius: 8;"
            );
            actionButton.setOnAction(e -> {
                int quantity = guiController.gameController.getPowerUpManager().getPowerUpQuantity(powerUp);
                if (quantity > 0) {
                    // Use power-up
                    guiController.gameController.activatePowerUp(powerUp);
                    updatePowerUpUI();
                    if (powerUp == PowerUp.ROW_CLEARER) {
                        guiController.refreshGameBackground(guiController.gameController.getBoard().getBoardMatrix());
                    }
                } else {
                    // Buy power-up
                    if (guiController.gameController.purchasePowerUp(powerUp)) {
                        updatePowerUpUI();
                    }
                }
            });
            
            // Key binding hint (small text)
            Label keyHint = new Label("[" + keyNumber + "] to use");
            keyHint.setStyle(
                "-fx-font-size: 8px; " +
                "-fx-text-fill: #aaaaaa; " +
                "-fx-alignment: center;"
            );
            keyHint.setWrapText(true);
            keyHint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            card.getChildren().addAll(iconLabel, nameLabel, quantityLabel, actionButton, keyHint);
            
            // Add to original container
            if (guiController.powerUpsContainer != null) {
                guiController.powerUpsContainer.getChildren().add(card);
            }
            
            // Create a copy for the centered overlay
            VBox cardCentered = new VBox(10);
            cardCentered.setAlignment(javafx.geometry.Pos.CENTER);
            cardCentered.setPrefWidth(140);
            cardCentered.setMinWidth(140);
            cardCentered.setMaxWidth(140);
            cardCentered.setStyle(card.getStyle());
            
            Label iconLabelCentered = new Label(getPowerUpIcon(powerUp));
            iconLabelCentered.setStyle(iconLabel.getStyle());
            iconLabelCentered.setId("powerup_centered_" + powerUp.name() + "_icon");
            
            Label nameLabelCentered = new Label(powerUp.getName());
            nameLabelCentered.setStyle(nameLabel.getStyle());
            nameLabelCentered.setWrapText(true);
            nameLabelCentered.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            Label quantityLabelCentered = new Label("x0");
            quantityLabelCentered.setId("powerup_centered_" + powerUp.name() + "_qty");
            quantityLabelCentered.setStyle(quantityLabel.getStyle());
            
            Button actionButtonCentered = new Button();
            actionButtonCentered.setId("powerup_centered_" + powerUp.name() + "_btn");
            actionButtonCentered.setPrefWidth(120);
            actionButtonCentered.setPrefHeight(35);
            actionButtonCentered.setStyle(actionButton.getStyle());
            actionButtonCentered.setOnAction(actionButton.getOnAction());
            
            Label keyHintCentered = new Label("[" + keyNumber + "] to use");
            keyHintCentered.setStyle(keyHint.getStyle());
            keyHintCentered.setWrapText(true);
            keyHintCentered.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            
            cardCentered.getChildren().addAll(iconLabelCentered, nameLabelCentered, quantityLabelCentered, actionButtonCentered, keyHintCentered);
            
            // Add to centered container
            if (guiController.powerUpsContainerCentered != null) {
                guiController.powerUpsContainerCentered.getChildren().add(cardCentered);
            }
        }
        
        // Initialize inventory display
        initializePowerUpInventoryDisplay();
        
        // Update UI periodically
        Timeline uiUpdater = new Timeline(
            new KeyFrame(Duration.millis(500), ae -> {
                updatePowerUpUI();
                updatePowerUpInventoryDisplay();
            })
        );
        uiUpdater.setCycleCount(Timeline.INDEFINITE);
        uiUpdater.play();
    }
    
    void initializePowerUpInventoryDisplay() {
        if (guiController.powerUpsInventoryContainer == null || guiController.gameController == null) {
            return;
        }
        
        guiController.powerUpsInventoryContainer.getChildren().clear();
        
        PowerUp[] powerUps = PowerUp.values();
        for (PowerUp powerUp : powerUps) {
            // Create compact inventory item
            VBox inventoryItem = new VBox(3);
            inventoryItem.setAlignment(javafx.geometry.Pos.CENTER);
            inventoryItem.setPrefWidth(50);
            inventoryItem.setMinWidth(50);
            
            // Icon
            Label iconLabel = new Label(getPowerUpIcon(powerUp));
            iconLabel.setId("inventory_" + powerUp.name() + "_icon");
            iconLabel.setStyle(
                "-fx-font-size: 24px; " +
                "-fx-alignment: center; " +
                "-fx-text-fill: #ffffff; " +
                "-fx-opacity: 0.9;"
            );
            
            // Quantity badge
            Label qtyLabel = new Label("x0");
            qtyLabel.setId("inventory_" + powerUp.name() + "_qty");
            qtyLabel.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #00ff00; " +
                "-fx-alignment: center; " +
                "-fx-background-color: rgba(0,0,0,0.5); " +
                "-fx-background-radius: 4; " +
                "-fx-padding: 2 4;"
            );
            
            inventoryItem.getChildren().addAll(iconLabel, qtyLabel);
            guiController.powerUpsInventoryContainer.getChildren().add(inventoryItem);
        }
    }
    
    void updatePowerUpInventoryDisplay() {
        if (guiController.gameController == null || guiController.powerUpsInventoryContainer == null) {
            return;
        }
        
        for (PowerUp powerUp : PowerUp.values()) {
            int quantity = guiController.gameController.getPowerUpManager().getPowerUpQuantity(powerUp);
            
            // Update quantity label
            javafx.scene.Node qtyNode = guiController.powerUpsInventoryContainer.lookup("#inventory_" + powerUp.name() + "_qty");
            if (qtyNode instanceof Label) {
                Label qtyLabel = (Label) qtyNode;
                qtyLabel.setText("x" + quantity);
                
                // Change color and visibility based on quantity
                if (quantity > 0) {
                    qtyLabel.setStyle(
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #00ff00; " +
                        "-fx-alignment: center; " +
                        "-fx-background-color: rgba(0,255,0,0.2); " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 2 4;"
                    );
                    qtyLabel.setVisible(true);
                } else {
                    qtyLabel.setStyle(
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #666666; " +
                        "-fx-alignment: center; " +
                        "-fx-background-color: rgba(0,0,0,0.3); " +
                        "-fx-background-radius: 4; " +
                        "-fx-padding: 2 4;"
                    );
                    qtyLabel.setVisible(true);
                }
            }
            
            // Update icon opacity based on availability
            javafx.scene.Node iconNode = guiController.powerUpsInventoryContainer.lookup("#inventory_" + powerUp.name() + "_icon");
            if (iconNode instanceof Label) {
                Label iconLabel = (Label) iconNode;
                if (quantity > 0) {
                    iconLabel.setStyle(
                        "-fx-font-size: 24px; " +
                        "-fx-alignment: center; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-opacity: 1.0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,255,0,0.6), 8, 0.0, 0, 0), " +
                        "dropshadow(gaussian, rgba(255,255,255,0.3), 5, 0.0, 0, 0);"
                    );
                } else {
                    iconLabel.setStyle(
                        "-fx-font-size: 24px; " +
                        "-fx-alignment: center; " +
                        "-fx-text-fill: #cccccc; " +
                        "-fx-opacity: 0.7;"
                    );
                }
            }
        }
    }
    
    private String getPowerUpIcon(PowerUp powerUp) {
        switch (powerUp) {
            case ROW_CLEARER:
                return "🧹"; // Broom icon
            case SLOW_MOTION:
                return "⏱️"; // Hourglass icon
            case BOMB_PIECE:
                return "💣"; // Bomb icon
            default:
                return "⚡"; // Default icon
        }
    }

    void updatePowerUpUI() {
        if (guiController.gameController == null) {
            return;
        }
        
        for (PowerUp powerUp : PowerUp.values()) {
            int quantity = guiController.gameController.getPowerUpManager().getPowerUpQuantity(powerUp);
            int skillPoints = guiController.gameController.getPowerUpManager().getSkillPoints();
            int cost = powerUp.getCost();
            
            // Update both containers (original and centered)
            updatePowerUpCard(powerUp, quantity, skillPoints, cost, "");
            updatePowerUpCard(powerUp, quantity, skillPoints, cost, "_centered");
        }
    }
    
    private void updatePowerUpCard(PowerUp powerUp, int quantity, int skillPoints, int cost, String suffix) {
        HBox container = suffix.isEmpty() ? guiController.powerUpsContainer : guiController.powerUpsContainerCentered;
        if (container == null) return;
        
        String prefix = "powerup" + suffix;
        
        // Update quantity label
        javafx.scene.Node qtyNode = container.lookup("#" + prefix + "_" + powerUp.name() + "_qty");
        if (qtyNode instanceof Label) {
            Label qtyLabel = (Label) qtyNode;
            qtyLabel.setText("x" + quantity);
            
            // Change color based on quantity
            if (quantity > 0) {
                qtyLabel.setStyle(
                    "-fx-font-size: 32px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #00ff00; " +
                    "-fx-alignment: center; " +
                    "-fx-padding: 5 0;"
                );
            } else {
                qtyLabel.setStyle(
                    "-fx-font-size: 32px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #ff6666; " +
                    "-fx-alignment: center; " +
                    "-fx-padding: 5 0;"
                );
            }
        }
        
        // Update action button
        javafx.scene.Node btnNode = container.lookup("#" + prefix + "_" + powerUp.name() + "_btn");
        if (btnNode instanceof Button) {
            Button actionBtn = (Button) btnNode;
            
            if (quantity > 0) {
                // Show "Use" button
                actionBtn.setText("USE [1]");
                actionBtn.setStyle(
                    "-fx-font-size: 12px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-color: linear-gradient(to bottom, #2ecc71, #27ae60); " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-radius: 8; " +
                    "-fx-cursor: hand;"
                );
                actionBtn.setDisable(false);
            } else {
                // Show "Buy" button
                actionBtn.setText("BUY " + cost + "pts");
                if (skillPoints >= cost) {
                    actionBtn.setStyle(
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-cursor: hand;"
                    );
                    actionBtn.setDisable(false);
                } else {
                    actionBtn.setStyle(
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: linear-gradient(to bottom, #7f8c8d, #5d6d7e); " +
                        "-fx-text-fill: #cccccc; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-cursor: default;"
                    );
                    actionBtn.setDisable(true);
                }
            }
        }
        
        // Update icon glow effect based on availability
        javafx.scene.Node iconNode = container.lookup("#" + prefix + "_" + powerUp.name() + "_icon");
        if (iconNode instanceof Label) {
            Label iconLabel = (Label) iconNode;
            if (quantity > 0) {
                iconLabel.setStyle(
                    "-fx-font-size: 48px; " +
                    "-fx-alignment: center; " +
                    "-fx-padding: 10 0 5 0; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,255,0,0.6), 10, 0.0, 0, 0);"
                );
            } else {
                iconLabel.setStyle(
                    "-fx-font-size: 48px; " +
                    "-fx-alignment: center; " +
                    "-fx-padding: 10 0 5 0;"
                );
            }
        }
    }
    
    void togglePowerUpsOverlay() {
        if (guiController.powerUpsOverlay == null) return;
        
        boolean isVisible = guiController.powerUpsOverlay.isVisible();
        guiController.powerUpsOverlay.setVisible(!isVisible);
        
        if (!isVisible) {
            // Show centered overlay
            guiController.powerUpsOverlay.toFront();
            // Make sure overlay doesn't block keyboard input
            guiController.powerUpsOverlay.setMouseTransparent(false);
            // Set up key handler for overlay
            guiController.powerUpsOverlay.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.B) {
                    togglePowerUpsOverlay();
                    e.consume();
                }
            });
            // Request focus so it can receive key events
            guiController.powerUpsOverlay.requestFocus();
            // Hide the original power-up panel
            if (guiController.powerUpsPanel != null) {
                guiController.powerUpsPanel.setVisible(false);
            }
            // Pause the game (only if not already paused)
            if (!guiController.isPause.getValue() && guiController.timeLine != null && guiController.timeLine.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                guiController.timeLine.pause();
            }
        } else {
            // Hide overlay, keep original panel hidden (as per requirement)
            if (guiController.powerUpsPanel != null) {
                guiController.powerUpsPanel.setVisible(false);
            }
            // Return focus to game panel
            if (guiController.gamePanel != null) {
                guiController.gamePanel.requestFocus();
            }
            // Resume the game (only if not paused by pause menu)
            if (!guiController.isPause.getValue() && guiController.timeLine != null && guiController.timeLine.getStatus() == javafx.animation.Animation.Status.PAUSED) {
                guiController.timeLine.play();
            }
        }
    }
}

