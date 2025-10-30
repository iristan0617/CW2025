package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GridPane nextBrickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    @FXML
    private Pane pauseOverlay;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label linesLabel;

    private Rectangle[][] displayMatrix;
    private Rectangle[][] rectangles;
    private Rectangle[][] nextBrickRectangles;

    private InputEventListener eventListener;

    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();
    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
        } catch (Exception e) {
            System.err.println("Could not load font: " + e.getMessage());
        }

        // Focus handling
        gamePanel.setFocusTraversable(true);
        gamePanel.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                System.out.println("Game panel focused");
            }
        });

        gamePanel.setOnMouseClicked(e -> gamePanel.requestFocus());
        gamePanel.requestFocus();

        gamePanel.setOnKeyPressed(this::handleKeyPressed);

        gameOverPanel.setVisible(false);
        pauseOverlay.setVisible(false);

        Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);

        // Request focus after a short delay to ensure scene is ready
        Platform.runLater(() -> {
            gamePanel.requestFocus();
            System.out.println("Requested focus for game panel");
        });
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
        System.out.println("Key pressed: " + keyEvent.getCode());

        if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
            if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                System.out.println("Left movement");
                if (eventListener != null) {
                    refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                }
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                System.out.println("Right movement");
                if (eventListener != null) {
                    refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                }
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                System.out.println("Rotate");
                if (eventListener != null) {
                    refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                }
                keyEvent.consume();
            }
            if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                System.out.println("Down movement");
                moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                keyEvent.consume();
            }
        }
        if (keyEvent.getCode() == KeyCode.N) {
            newGame(null);
        }
        if (keyEvent.getCode() == KeyCode.P && isGameOver.getValue() == Boolean.FALSE) {
            System.out.println("Pause toggled");
            togglePause();
            keyEvent.consume();
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        // Initialize display matrix
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        // Initialize brick panel
        if (brick != null && brick.getBrickData() != null) {
            rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                    rectangles[i][j] = rectangle;
                    brickPanel.add(rectangle, j, i);
                }
            }

            // Set initial brick position
            refreshBrick(brick);
        }

        // Initialize next brick preview
        if (brick != null && brick.getNextBrickData() != null) {
            initNextBrickPreview(brick.getNextBrickData());
        }

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    private void initNextBrickPreview(int[][] nextBrickData) {
        if (nextBrickData == null) return;

        nextBrickRectangles = new Rectangle[nextBrickData.length][nextBrickData[0].length];
        nextBrickPanel.getChildren().clear();

        for (int i = 0; i < nextBrickData.length; i++) {
            for (int j = 0; j < nextBrickData[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(nextBrickData[i][j]));
                rectangle.setArcHeight(9);
                rectangle.setArcWidth(9);
                nextBrickRectangles[i][j] = rectangle;
                nextBrickPanel.add(rectangle, j, i);
            }
        }
    }

    private void updateNextBrickPreview(int[][] nextBrickData) {
        if (nextBrickRectangles != null && nextBrickData != null) {
            for (int i = 0; i < nextBrickData.length; i++) {
                for (int j = 0; j < nextBrickData[i].length; j++) {
                    if (i < nextBrickRectangles.length && j < nextBrickRectangles[i].length) {
                        setRectangleData(nextBrickData[i][j], nextBrickRectangles[i][j]);
                    }
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

    private void refreshBrick(ViewData brick) {
        if (brick == null) return;

        if (isPause.getValue() == Boolean.FALSE) {
            // FIXED: Proper positioning
            double baseX = 0; // Start from game panel origin
            double baseY = 0;

            brickPanel.setLayoutX(baseX + (brick.getxPosition() * BRICK_SIZE));
            brickPanel.setLayoutY(baseY + (brick.getyPosition() * BRICK_SIZE));

            // Update brick appearance
            if (brick.getBrickData() != null && rectangles != null) {
                for (int i = 0; i < brick.getBrickData().length; i++) {
                    for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                        if (i < rectangles.length && j < rectangles[i].length) {
                            setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                        }
                    }
                }
            }

            // Update next brick preview
            if (brick.getNextBrickData() != null) {
                updateNextBrickPreview(brick.getNextBrickData());
            }
        }
    }

    public void refreshGameBackground(int[][] board) {
        if (board == null || displayMatrix == null) return;

        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i < displayMatrix.length && j < displayMatrix[i].length) {
                    setRectangleData(board[i][j], displayMatrix[i][j]);
                }
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        if (rectangle != null) {
            rectangle.setFill(getFillColor(color));
            rectangle.setArcHeight(9);
            rectangle.setArcWidth(9);
        }
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE && eventListener != null) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData != null) {
                if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                    NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                    groupNotification.getChildren().add(notificationPanel);
                    notificationPanel.showScore(groupNotification.getChildren());
                }
                refreshBrick(downData.getViewData());
            }
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty integerProperty) {
        if (scoreLabel != null && integerProperty != null) {
            scoreLabel.textProperty().bind(integerProperty.asString());
        }
    }

    public void bindLines(IntegerProperty integerProperty) {
        if (linesLabel != null && integerProperty != null) {
            linesLabel.textProperty().bind(integerProperty.asString());
        }
    }

    public void gameOver() {
        if (timeLine != null) {
            timeLine.stop();
        }
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
    }

    public void newGame(ActionEvent actionEvent) {
        if (timeLine != null) {
            timeLine.stop();
        }
        gameOverPanel.setVisible(false);
        pauseOverlay.setVisible(false);
        if (eventListener != null) {
            eventListener.createNewGame();
        }
        gamePanel.requestFocus();
        if (timeLine != null) {
            timeLine.play();
        }
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }

    public void togglePause() {
        if (isGameOver.getValue() == Boolean.FALSE) {
            boolean newPauseState = !isPause.getValue();
            isPause.setValue(newPauseState);

            if (newPauseState) {
                if (timeLine != null) {
                    timeLine.pause();
                }
                pauseOverlay.setVisible(true);
                System.out.println("Game paused");
            } else {
                if (timeLine != null) {
                    timeLine.play();
                }
                pauseOverlay.setVisible(false);
                System.out.println("Game resumed");
            }
            // FIXED: Always request focus after pause state change
            Platform.runLater(() -> gamePanel.requestFocus());
        }
    }

    public void resumeGame(ActionEvent actionEvent) {
        if (isPause.getValue() == Boolean.TRUE) {
            togglePause();
        }
    }

    public void quitGame(ActionEvent actionEvent) {
        System.exit(0);
    }
}