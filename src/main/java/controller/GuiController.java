package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import model.DownData;
import model.ViewData;
import model.MatrixOperations;
import view.GameOverPanel;
import view.NotificationPanel;
import javafx.geometry.Point2D;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.effect.Glow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import model.PowerUp;


import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 22;

    @FXML private GridPane gamePanel;
    @FXML private StackPane groupNotification;
    @FXML private GridPane brickPanel;
    @FXML private GameOverPanel gameOverPanel;
    @FXML private BorderPane gameBoard;
    @FXML private StackPane boardStack;
    @FXML private Label scoreLabel;
    @FXML private Label linesLabel;
    @FXML private Label skillPointsLabel;
    @FXML private GridPane nextPanel;
    @FXML private GridPane holdPanel;
    @FXML private HBox powerUpsContainer;
    @FXML private HBox powerUpsContainerCentered;
    @FXML private VBox powerUpsPanel;
    @FXML private StackPane powerUpsOverlay;
    @FXML private VBox powerUpsInventoryDisplay;
    @FXML private HBox powerUpsInventoryContainer;
    @FXML private StackPane pauseOverlay;
    @FXML private StackPane mainMenuOverlay;
    @FXML private StackPane rootStackPane; // Root StackPane from FXML

    private int totalLinesCleared = 0;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private GameController gameController;

    private Rectangle[][] displayMatrix;
    private Rectangle[][] nextPreview;
    private Rectangle[][] holdPreview;
    private InputEventListener eventListener;
    private Rectangle[][] rectangles;
    private Rectangle[][] shadowRectangles;
    private GridPane shadowPanel;
    private Timeline timeLine;
    private int[][] currentBoardMatrix;
    
    // Slow motion power-up
    private static final long SLOW_MOTION_SPEED_MS = 800; // 2x slower than normal (400ms)
    private static final long NORMAL_SPEED_MS = 400;
    private Timeline slowMotionRestoreTimer;
    private Timeline slowMotionCountdownTimer;
    private int slowMotionRemainingSeconds = 0;
    private Label slowMotionCountdownLabel;

    private final BooleanProperty isPause = new SimpleBooleanProperty();
    private final BooleanProperty isGameOver = new SimpleBooleanProperty();
    
    // Countdown overlay for resume
    private StackPane countdownOverlay;
    private Label countdownLabel;
    private Timeline countdownTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // load font (guarded)
        try {
            if (getClass().getClassLoader().getResource("digital.ttf") != null) {
                Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
            }
        } catch (Exception ignored) {}

        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        hardDrop(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.C) {
                        refreshBrick(eventListener.onHoldEvent(new MoveEvent(EventType.HOLD, EventSource.USER)));
                        keyEvent.consume();
                    }
                    
                    // Power-up shortcuts: 1, 2, 3 to use power-ups
                    if (keyEvent.getCode() == KeyCode.DIGIT1 || keyEvent.getCode() == KeyCode.NUMPAD1) {
                        if (gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 0) {
                                gameController.activatePowerUp(powerUps[0]); // Row Clearer
                                updatePowerUpUI();
                                if (powerUps[0] == PowerUp.ROW_CLEARER) {
                                    refreshGameBackground(gameController.getBoard().getBoardMatrix());
                                }
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT2 || keyEvent.getCode() == KeyCode.NUMPAD2) {
                        if (gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 1) {
                                gameController.activatePowerUp(powerUps[1]); // Slow Motion
                                updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT3 || keyEvent.getCode() == KeyCode.NUMPAD3) {
                        if (gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 2) {
                                gameController.activatePowerUp(powerUps[2]); // Bomb Piece
                                updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                }
                
                // Power-up purchase shortcuts: Shift+1, Shift+2, Shift+3 to buy
                if (keyEvent.isShiftDown()) {
                    if (keyEvent.getCode() == KeyCode.DIGIT1 || keyEvent.getCode() == KeyCode.NUMPAD1) {
                        if (gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 0) {
                                gameController.purchasePowerUp(powerUps[0]);
                                updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT2 || keyEvent.getCode() == KeyCode.NUMPAD2) {
                        if (gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 1) {
                                gameController.purchasePowerUp(powerUps[1]);
                                updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DIGIT3 || keyEvent.getCode() == KeyCode.NUMPAD3) {
                        if (gameController != null) {
                            PowerUp[] powerUps = PowerUp.values();
                            if (powerUps.length > 2) {
                                gameController.purchasePowerUp(powerUps[2]);
                                updatePowerUpUI();
                            }
                        }
                        keyEvent.consume();
                    }
                }
                
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
                if (keyEvent.getCode() == KeyCode.P || keyEvent.getCode() == KeyCode.ESCAPE) {
                    // If countdown is showing, cancel it and stay paused
                    if (countdownOverlay != null && countdownOverlay.isVisible()) {
                        cancelCountdown();
                        keyEvent.consume();
                    } else {
                        setPaused(!isPause.get());
                        keyEvent.consume();
                    }
                }
                if (keyEvent.getCode() == KeyCode.B) {
                    togglePowerUpsOverlay();
                    keyEvent.consume();
                }
            }
        });

        gameOverPanel.setVisible(false);
        if (pauseOverlay != null) pauseOverlay.setVisible(false);
        // Main menu is visible by default
        if (mainMenuOverlay != null) mainMenuOverlay.setVisible(true);
        
        // Initialize countdown overlay
        initializeCountdownOverlay();

        if (scoreLabel != null) {
            if (scoreLabel.textProperty().isBound()) {
                scoreLabel.textProperty().unbind();
            }
            scoreLabel.setText("0");
        }
        if (linesLabel != null) {
            linesLabel.setText("0");
        }
        if (skillPointsLabel != null) {
            if (skillPointsLabel.textProperty().isBound()) {
                skillPointsLabel.textProperty().unbind();
            }
            skillPointsLabel.setText("0");
        }
        
        // Hide game content initially (will be shown when Start is clicked)
        // Game content will be shown when Start button is clicked
        Platform.runLater(() -> {
            if (gameBoard != null) gameBoard.setVisible(false);
            if (powerUpsInventoryDisplay != null) powerUpsInventoryDisplay.setVisible(false);
            // Hide score and next panel containers
            if (scoreLabel != null) {
                Node parent = scoreLabel.getParent();
                if (parent != null && parent.getParent() != null) {
                    parent.getParent().setVisible(false);
                }
            }
            if (nextPanel != null) {
                Node parent = nextPanel.getParent();
                if (parent != null) {
                    parent.setVisible(false);
                }
            }
            if (holdPanel != null) {
                Node parent = holdPanel.getParent();
                if (parent != null) {
                    parent.setVisible(false);
                }
            }
        });

        Platform.runLater(() -> {
            Pane root = (Pane) gameBoard.getParent();

            ChangeListener<Number> relayout = (obs, o, n) -> centerGameBoard();
            root.widthProperty().addListener(relayout);
            root.heightProperty().addListener(relayout);
            gameBoard.layoutBoundsProperty().addListener((obs, o, n) -> centerGameBoard());
            centerGameBoard();
        });

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);

        // Setup video background
        setupVideoBackground();
    }

    private void initializeCountdownOverlay() {
        // Create countdown overlay
        countdownOverlay = new StackPane();
        countdownOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        countdownOverlay.setVisible(false);
        countdownOverlay.setMouseTransparent(false); // Block input during countdown
        
        // Create countdown label
        countdownLabel = new Label();
        countdownLabel.setStyle(
            "-fx-font-size: 120px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #ffff00; " +
            "-fx-effect: dropshadow(gaussian, rgba(255,255,0,0.9), 20, 0.8, 0, 4);"
        );
        
        countdownOverlay.getChildren().add(countdownLabel);
        countdownOverlay.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Add to root stack pane (will be added when needed)
    }

    private void setupVideoBackground() {
        try {
            // Load the video file from resources
            URL videoUrl = getClass().getClassLoader().getResource("galaxy.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaView = new MediaView(mediaPlayer);
                
                // Configure video view
                mediaView.setPreserveRatio(true);
                mediaView.setSmooth(true);
                
                // Configure media player
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
                mediaPlayer.setAutoPlay(true);
                mediaPlayer.setMute(true); // Mute audio
                
                // Add video to root StackPane behind all content
                Platform.runLater(() -> {
                    if (rootStackPane != null) {
                        // Add video at index 0 to be behind everything
                        rootStackPane.getChildren().add(0, mediaView);
                        
                        // Bind video size to fill the window
                        mediaView.fitWidthProperty().bind(rootStackPane.widthProperty());
                        mediaView.fitHeightProperty().bind(rootStackPane.heightProperty());
                        mediaView.setPreserveRatio(false); // Stretch to fill
                    }
                });
            } else {
                System.err.println("Video file galaxy.mp4 not found in resources");
            }
        } catch (Exception e) {
            // If video fails to load, continue without it
            System.err.println("Failed to load background video: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        currentBoardMatrix = boardMatrix;
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        int cols = boardMatrix[0].length;
        int rowsVisible = boardMatrix.length - 2;
        double w = cols * BRICK_SIZE + (cols - 1) * gamePanel.getHgap();
        double h = rowsVisible * BRICK_SIZE + (rowsVisible - 1) * gamePanel.getVgap();
        w += 2; h += 4;
        gamePanel.setPrefSize(w, h);
        gamePanel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        gamePanel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                //rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                setRectangleData(brick.getBrickData()[i][j], rectangle);
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }

        // Create shadow panel for ghost preview
        shadowPanel = new GridPane();
        shadowPanel.setHgap(brickPanel.getHgap());
        shadowPanel.setVgap(brickPanel.getVgap());
        shadowPanel.setMouseTransparent(true); // Don't block mouse events
        int[][] brickData = brick.getBrickData();
        if (brickData != null && brickData.length > 0 && brickData[0].length > 0) {
            shadowRectangles = new Rectangle[brickData.length][brickData[0].length];
            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    Rectangle shadowRect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    shadowRect.setFill(Color.GRAY);
                    shadowRect.setOpacity(0.55);
                    shadowRect.setStroke(Color.DARKGRAY);
                    shadowRect.setStrokeWidth(1.0);
                    shadowRectangles[i][j] = shadowRect;
                    shadowPanel.add(shadowRect, j, i);
                }
            }
        }
        // Add shadow panel to the same parent as brickPanel
        Pane root = (Pane) gameBoard.getParent();
        if (root != null && !root.getChildren().contains(shadowPanel)) {
            root.getChildren().add(shadowPanel);
        }

        Point2D origin = gamePanelOriginInRoot();
        brickPanel.setLayoutX(origin.getX() + brick.getxPosition() * brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
        // Calculate Y position: brick yPosition is in matrix coordinates, display starts from matrix row 2
        // So we need to subtract 2 from yPosition to get the display row, then calculate pixel position
        // Negative displayRow means the brick is above the visible area (outside the border)
        int displayRow = brick.getyPosition() - 2;
        brickPanel.setLayoutY(origin.getY() + displayRow * (brickPanel.getHgap() + BRICK_SIZE));

        renderNextPreview(brick.getNextBrickData());
        renderHoldPreview(brick.getHeldBrickData());
        updateShadow(brick);

        timeLine = new Timeline(new KeyFrame(Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
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

    public void refreshBrick(ViewData brick) {
        if (isPause.getValue() == Boolean.FALSE) {
            Point2D origin = gamePanelOriginInRoot();
            brickPanel.setLayoutX(origin.getX() + brick.getxPosition() * brickPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
            // Calculate Y position: brick yPosition is in matrix coordinates, display starts from matrix row 2
            // So we need to subtract 2 from yPosition to get the display row, then calculate pixel position
            // Negative displayRow means the brick is above the visible area (outside the border)
            int displayRow = brick.getyPosition() - 2;
            brickPanel.setLayoutY(origin.getY() + displayRow * (brickPanel.getHgap() + BRICK_SIZE));
            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }
            renderNextPreview(brick.getNextBrickData());
            renderHoldPreview(brick.getHeldBrickData());
            updateShadow(brick);
        }
    }

    public void refreshGameBackground(int[][] board) {
        currentBoardMatrix = board;
        for (int i = 2; i < board.length && i < displayMatrix.length; i++) {
            for (int j = 0; j < board[i].length && j < displayMatrix[i].length; j++) {
                if (displayMatrix[i][j] != null) {
                    setRectangleData(board[i][j], displayMatrix[i][j]);
                }
            }
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
        if (currentBoardMatrix == null) return y;
        
        int dropY = y;
        // Simulate dropping the brick until it hits something
        while (true) {
            int testY = dropY + 1;
            // Check if the brick would collide at testY position
            if (MatrixOperations.intersect(currentBoardMatrix, brickData, x, testY)) {
                break; // Found collision, stop here
            }
            dropY = testY;
            // Safety check to prevent infinite loop
            if (dropY >= currentBoardMatrix.length) {
                break;
            }
        }
        return dropY;
    }

    private void updateShadow(ViewData brick) {
        if (shadowPanel == null || currentBoardMatrix == null) {
            return;
        }

        int[][] brickData = brick.getBrickData();
        int dropY = calculateDropPosition(brickData, brick.getxPosition(), brick.getyPosition());
        
        // Only show shadow if it's different from current position and below the current position
        if (dropY <= brick.getyPosition()) {
            shadowPanel.setVisible(false);
            return;
        }

        shadowPanel.setVisible(true);
        
        // Check if shadow rectangles array needs to be resized
        if (brickData == null || brickData.length == 0 || brickData[0].length == 0) {
            shadowPanel.setVisible(false);
            return;
        }
        if (shadowRectangles == null || shadowRectangles.length != brickData.length || 
            (brickData.length > 0 && (shadowRectangles[0] == null || shadowRectangles[0].length != brickData[0].length))) {
            // Recreate shadow rectangles if size changed
            shadowPanel.getChildren().clear();
            shadowRectangles = new Rectangle[brickData.length][brickData[0].length];
            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    Rectangle shadowRect = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    shadowRect.setFill(Color.GRAY);
                    shadowRect.setOpacity(0.7);
                    shadowRect.setStroke(Color.DARKGRAY);
                    shadowRect.setStrokeWidth(1.5);
                    shadowRectangles[i][j] = shadowRect;
                    shadowPanel.add(shadowRect, j, i);
                }
            }
        }
        
        // Update shadow rectangles to match brick shape - all grey color (darker)
        Color shadowGrey = Color.DARKGRAY;
        Color shadowStroke = Color.BLACK;
        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                Rectangle shadowRect = shadowRectangles[i][j];
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
        shadowPanel.setLayoutX(origin.getX() + brick.getxPosition() * shadowPanel.getVgap() + brick.getxPosition() * BRICK_SIZE);
        int displayRow = dropY - 2;
        shadowPanel.setLayoutY(origin.getY() + displayRow * (shadowPanel.getHgap() + BRICK_SIZE));
        
        // Ensure shadow is behind the brick but visible
        shadowPanel.toBack();
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                updateLinesCleared(downData.getClearRow().getLinesRemoved());
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    private void hardDrop(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onHardDropEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                updateLinesCleared(downData.getClearRow().getLinesRemoved());
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            // Hard drop already merged the brick, so we just refresh the background
            // The view data will be for the next brick
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty integerProperty) {
        if (integerProperty != null) {
            Platform.runLater(() -> {
                if (scoreLabel != null) {
                    scoreLabel.textProperty().bind(integerProperty.asString());
                }
            });
        }
    }

    public void bindSkillPoints(IntegerProperty integerProperty) {
        if (integerProperty != null) {
            Platform.runLater(() -> {
                if (skillPointsLabel != null) {
                    skillPointsLabel.textProperty().bind(integerProperty.asString());
                }
            });
        }
    }

    public void updateLinesCleared(int lines) {
        if (lines > 0) {
            totalLinesCleared += lines;
            if (linesLabel != null) {
                linesLabel.setText(String.valueOf(totalLinesCleared));
            }
        }
    }

    public void resetLinesCleared() {
        totalLinesCleared = 0;
        if (linesLabel != null) {
            linesLabel.setText("0");
        }
    }

    public void gameOver() {
        if (timeLine != null) timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
    }

    public void newGame(ActionEvent actionEvent) {
        if (timeLine != null) timeLine.stop();
        gameOverPanel.setVisible(false);
        resetLinesCleared();
        eventListener.createNewGame();
        gamePanel.requestFocus();
        if (timeLine != null) timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();
    }

    private void centerGameBoard() {
        Pane root = (Pane) gameBoard.getParent();
        Bounds b = gameBoard.getBoundsInParent();
        double w = b.getWidth();
        double h = b.getHeight();

        double x = Math.max(0, (root.getWidth() - w) / 2);
        double y = Math.max(0, (root.getHeight() - h) / 2);
        gameBoard.setLayoutX(x);
        gameBoard.setLayoutY(y);
    }

    private Point2D gamePanelOriginInRoot() {
        Pane root = (Pane) gameBoard.getParent();
        Point2D scenePt = gamePanel.localToScene(0, 0);
        return root.sceneToLocal(scenePt);
    }

    private void renderNextPreview(int[][] next) {
        if (next == null || next.length == 0) return;
        nextPanel.getChildren().clear();
        nextPreview = new Rectangle[next.length][next[0].length];
        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next[i].length; j++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(getFillColor(next[i][j]));
                nextPreview[i][j] = r;
                nextPanel.add(r, j, i);
            }
        }
    }

    private void renderHoldPreview(int[][] hold) {
        if (holdPanel == null) return;
        holdPanel.getChildren().clear();
        if (hold == null || hold.length == 0) {
            // No held piece, show empty placeholder (4x4 grid of transparent/empty cells)
            holdPreview = new Rectangle[4][4];
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                    r.setFill(Color.TRANSPARENT);
                    r.setStroke(Color.rgb(255, 255, 255, 0.1));
                    r.setStrokeWidth(0.5);
                    holdPreview[i][j] = r;
                    holdPanel.add(r, j, i);
                }
            }
            return;
        }
        holdPreview = new Rectangle[hold.length][hold[0].length];
        for (int i = 0; i < hold.length; i++) {
            for (int j = 0; j < hold[i].length; j++) {
                Rectangle r = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                r.setFill(getFillColor(hold[i][j]));
                holdPreview[i][j] = r;
                holdPanel.add(r, j, i);
            }
        }
    }

    private void setPaused(boolean paused) {
        if (paused) {
            // Pausing - immediate
            isPause.setValue(true);
            if (timeLine != null) {
                timeLine.pause();
            }
            if (pauseOverlay != null) {
                pauseOverlay.setVisible(true);
                pauseOverlay.toFront();
            }
        } else {
            // Unpausing - show countdown first
            showResumeCountdown();
        }
    }
    
    private void showResumeCountdown() {
        // Don't show countdown if already counting down or if game is over
        if (countdownOverlay != null && countdownOverlay.isVisible()) {
            return;
        }
        if (isGameOver.getValue()) {
            return;
        }
        
        // Stop any existing countdown timer
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        
        // Ensure countdown overlay is in root stack pane
        if (rootStackPane != null && !rootStackPane.getChildren().contains(countdownOverlay)) {
            rootStackPane.getChildren().add(countdownOverlay);
        }
        
        // Hide pause overlay during countdown
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }
        
        // Show countdown overlay
        countdownOverlay.setVisible(true);
        countdownOverlay.toFront();
        
        // Start countdown from 3
        int[] countdownValue = {3};
        countdownLabel.setText(String.valueOf(countdownValue[0]));
        
        // Create countdown timeline
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
            countdownValue[0]--;
            if (countdownValue[0] > 0) {
                countdownLabel.setText(String.valueOf(countdownValue[0]));
            } else {
                // Countdown finished - actually resume
                countdownOverlay.setVisible(false);
                countdownTimer.stop();
                
                // Actually unpause now
                isPause.setValue(false);
                if (timeLine != null) {
                    timeLine.play();
                }
                gamePanel.requestFocus();
            }
        }));
        countdownTimer.setCycleCount(3); // 3 seconds
        countdownTimer.play();
    }
    
    private void cancelCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        if (countdownOverlay != null) {
            countdownOverlay.setVisible(false);
        }
        // Show pause overlay again
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(true);
            pauseOverlay.toFront();
        }
    }

    @FXML
    private void onResume(ActionEvent e) {
        // setPaused(false) will trigger the countdown
        setPaused(false);
    }

    @FXML
    private void onRestart(ActionEvent e) {
        setPaused(false);
        newGame(null);
    }

    @FXML
    private void onStartGame(ActionEvent e) {
        // Hide main menu
        if (mainMenuOverlay != null) {
            mainMenuOverlay.setVisible(false);
        }
        
        // Show game content
        if (gameBoard != null) gameBoard.setVisible(true);
        if (powerUpsInventoryDisplay != null) powerUpsInventoryDisplay.setVisible(true);
        // Show score and next panel containers
        if (scoreLabel != null) {
            Node parent = scoreLabel.getParent();
            if (parent != null && parent.getParent() != null) {
                parent.getParent().setVisible(true);
            }
        }
        if (nextPanel != null) {
            Node parent = nextPanel.getParent();
            if (parent != null) {
                parent.setVisible(true);
            }
        }
        if (holdPanel != null) {
            Node parent = holdPanel.getParent();
            if (parent != null) {
                parent.setVisible(true);
            }
        }
        
        // Show brick panel and shadow panel (they will be positioned by the game)
        if (brickPanel != null) {
            brickPanel.setVisible(true);
        }
        if (shadowPanel != null) {
            shadowPanel.setVisible(true);
        }
        
        // Always create a fresh game when starting
        // Stop any existing timeline first
        if (timeLine != null) {
            timeLine.stop();
        }
        
        // Clear the game board display
        if (gamePanel != null) {
            gamePanel.getChildren().clear();
        }
        if (brickPanel != null) {
            brickPanel.getChildren().clear();
        }
        if (shadowPanel != null) {
            shadowPanel.getChildren().clear();
        }
        displayMatrix = null;
        rectangles = null;
        shadowRectangles = null;
        currentBoardMatrix = null;
        
        // Reset game state
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        gameOverPanel.setVisible(false);
        resetLinesCleared();
        
        // Create a new game controller (this will reset the board and start fresh)
        // The constructor will call initGameView which properly initializes the display
        gameController = new GameController(this);
        
        // Initialize power-up UI
        initializePowerUpUI();
        
        // Request focus for game controls
        if (gamePanel != null) {
            gamePanel.requestFocus();
        }
    }

    @FXML
    private void onExit(ActionEvent e) {
        // Clean up video player before exiting
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        Platform.exit();
    }

    @FXML
    private void onQuit(ActionEvent e) {
        // Return to main menu instead of exiting
        // Stop the game timeline
        if (timeLine != null) {
            timeLine.stop();
        }
        
        // Hide pause overlay
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }
        
        // Hide game content
        if (gameBoard != null) gameBoard.setVisible(false);
        if (powerUpsInventoryDisplay != null) powerUpsInventoryDisplay.setVisible(false);
        if (scoreLabel != null) {
            Node parent = scoreLabel.getParent();
            if (parent != null && parent.getParent() != null) {
                parent.getParent().setVisible(false);
            }
        }
        if (nextPanel != null) {
            Node parent = nextPanel.getParent();
            if (parent != null) {
                parent.setVisible(false);
            }
        }
        if (holdPanel != null) {
            Node parent = holdPanel.getParent();
            if (parent != null) {
                parent.setVisible(false);
            }
        }
        
        // Hide brick panel and shadow panel
        if (brickPanel != null) {
            brickPanel.setVisible(false);
        }
        if (shadowPanel != null) {
            shadowPanel.setVisible(false);
        }
        
        // Show main menu
        if (mainMenuOverlay != null) {
            mainMenuOverlay.setVisible(true);
        }
        
        // Reset game state
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);
        resetLinesCleared();
        
        // Reset score display (unbind first if bound)
        if (scoreLabel != null) {
            if (scoreLabel.textProperty().isBound()) {
                scoreLabel.textProperty().unbind();
            }
            scoreLabel.setText("0");
        }
        if (skillPointsLabel != null) {
            if (skillPointsLabel.textProperty().isBound()) {
                skillPointsLabel.textProperty().unbind();
            }
            skillPointsLabel.setText("0");
        }
        
        // Reset game controller (will be recreated when Start is clicked)
        gameController = null;
        eventListener = null;
    }
    
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Apply slow motion effect (slows falling speed for 10 seconds)
     */
    public void applySlowMotion() {
        if (timeLine == null || isPause.getValue() || isGameOver.getValue()) {
            return;
        }

        // Stop any existing timers
        if (slowMotionRestoreTimer != null) {
            slowMotionRestoreTimer.stop();
        }
        if (slowMotionCountdownTimer != null) {
            slowMotionCountdownTimer.stop();
        }

        // Reset countdown
        slowMotionRemainingSeconds = 10;

        // Show countdown timer (if label exists)
        if (slowMotionCountdownLabel != null) {
            slowMotionCountdownLabel.setVisible(true);
        }
        updateSlowMotionCountdown();

        // Stop current timeline
        timeLine.stop();

        // Create new timeline with slow speed
        timeLine = new Timeline(new KeyFrame(Duration.millis(SLOW_MOTION_SPEED_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

        // Create countdown timer that updates every second
        slowMotionCountdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
            slowMotionRemainingSeconds--;
            updateSlowMotionCountdown();
            if (slowMotionRemainingSeconds <= 0) {
                slowMotionCountdownTimer.stop();
            }
        }));
        slowMotionCountdownTimer.setCycleCount(10); // 10 seconds
        slowMotionCountdownTimer.play();

        // Create timer to restore normal speed after 10 seconds
        slowMotionRestoreTimer = new Timeline(new KeyFrame(Duration.seconds(10), ae -> restoreNormalSpeed()));
        slowMotionRestoreTimer.setCycleCount(1);
        slowMotionRestoreTimer.play();
    }

    /**
     * Restore normal falling speed
     */
    private void restoreNormalSpeed() {
        if (timeLine == null || isPause.getValue() || isGameOver.getValue()) {
            return;
        }

        timeLine.stop();
        timeLine = new Timeline(new KeyFrame(Duration.millis(NORMAL_SPEED_MS),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();

        if (slowMotionCountdownLabel != null) {
            slowMotionCountdownLabel.setVisible(false);
        }
    }

    /**
     * Update slow motion countdown display
     */
    private void updateSlowMotionCountdown() {
        if (slowMotionCountdownLabel != null) {
            slowMotionCountdownLabel.setText(String.valueOf(slowMotionRemainingSeconds));
        }
    }

    /**
     * Show bomb explosion effect at the specified grid position
     */
    public void showBoomEffect(int gridX, int gridY) {
        if (gameBoard == null || boardStack == null) return;

        // Convert grid coordinates to screen coordinates
        // gridX is column (0-9), gridY is row (0-24, visible rows start at 2)
        double cellW = BRICK_SIZE + gamePanel.getHgap();
        double cellH = BRICK_SIZE + gamePanel.getVgap();

        // Calculate position relative to boardStack
        // gridY needs to account for the 2 hidden rows at top
        double x = gridX * cellW + cellW / 2; // Center of the cell
        double y = (gridY - 2) * cellH + cellH / 2; // Center of the cell (accounting for 2 hidden rows)

        // Create BOOM! label
        javafx.scene.control.Label boomLabel = new javafx.scene.control.Label("BOOM!");
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
        boardStack.getChildren().add(boomLabel);
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
            boardStack.getChildren().remove(boomLabel);
        });
        parallelTransition.play();
    }

    /**
     * Initialize the power-up UI panel with card-based design
     */
    private void initializePowerUpUI() {
        if (gameController == null) {
            return;
        }
        
        // Clear both containers
        if (powerUpsContainer != null) {
            powerUpsContainer.getChildren().clear();
        }
        if (powerUpsContainerCentered != null) {
            powerUpsContainerCentered.getChildren().clear();
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
                int quantity = gameController.getPowerUpManager().getPowerUpQuantity(powerUp);
                if (quantity > 0) {
                    // Use power-up
                    gameController.activatePowerUp(powerUp);
                    updatePowerUpUI();
                    if (powerUp == PowerUp.ROW_CLEARER) {
                        refreshGameBackground(gameController.getBoard().getBoardMatrix());
                    }
                } else {
                    // Buy power-up
                    if (gameController.purchasePowerUp(powerUp)) {
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
            if (powerUpsContainer != null) {
                powerUpsContainer.getChildren().add(card);
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
            if (powerUpsContainerCentered != null) {
                powerUpsContainerCentered.getChildren().add(cardCentered);
            }
        }
        
        // Initialize inventory display
        initializePowerUpInventoryDisplay();
        
        // Update UI periodically
        javafx.animation.Timeline uiUpdater = new javafx.animation.Timeline(
            new KeyFrame(Duration.millis(500), ae -> {
                updatePowerUpUI();
                updatePowerUpInventoryDisplay();
            })
        );
        uiUpdater.setCycleCount(Timeline.INDEFINITE);
        uiUpdater.play();
    }
    
    /**
     * Initialize the small power-up inventory display
     */
    private void initializePowerUpInventoryDisplay() {
        if (powerUpsInventoryContainer == null || gameController == null) {
            return;
        }
        
        powerUpsInventoryContainer.getChildren().clear();
        
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
            powerUpsInventoryContainer.getChildren().add(inventoryItem);
        }
    }
    
    /**
     * Update the power-up inventory display
     */
    private void updatePowerUpInventoryDisplay() {
        if (gameController == null || powerUpsInventoryContainer == null) {
            return;
        }
        
        for (PowerUp powerUp : PowerUp.values()) {
            int quantity = gameController.getPowerUpManager().getPowerUpQuantity(powerUp);
            
            // Update quantity label
            javafx.scene.Node qtyNode = powerUpsInventoryContainer.lookup("#inventory_" + powerUp.name() + "_qty");
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
            javafx.scene.Node iconNode = powerUpsInventoryContainer.lookup("#inventory_" + powerUp.name() + "_icon");
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
    
    /**
     * Get icon/symbol for power-up
     */
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

    /**
     * Update power-up UI to reflect current quantities and button states
     */
    private void updatePowerUpUI() {
        if (gameController == null) {
            return;
        }
        
        for (PowerUp powerUp : PowerUp.values()) {
            int quantity = gameController.getPowerUpManager().getPowerUpQuantity(powerUp);
            int skillPoints = gameController.getPowerUpManager().getSkillPoints();
            int cost = powerUp.getCost();
            
            // Update both containers (original and centered)
            updatePowerUpCard(powerUp, quantity, skillPoints, cost, "");
            updatePowerUpCard(powerUp, quantity, skillPoints, cost, "_centered");
        }
    }
    
    /**
     * Helper method to update a single power-up card
     */
    private void updatePowerUpCard(PowerUp powerUp, int quantity, int skillPoints, int cost, String suffix) {
        HBox container = suffix.isEmpty() ? powerUpsContainer : powerUpsContainerCentered;
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
    
    /**
     * Toggle power-ups overlay visibility
     */
    private void togglePowerUpsOverlay() {
        if (powerUpsOverlay == null) return;
        
        boolean isVisible = powerUpsOverlay.isVisible();
        powerUpsOverlay.setVisible(!isVisible);
        
        if (!isVisible) {
            // Show centered overlay
            powerUpsOverlay.toFront();
            // Make sure overlay doesn't block keyboard input
            powerUpsOverlay.setMouseTransparent(false);
            // Set up key handler for overlay
            powerUpsOverlay.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.B) {
                    togglePowerUpsOverlay();
                    e.consume();
                }
            });
            // Request focus so it can receive key events
            powerUpsOverlay.requestFocus();
            // Hide the original power-up panel
            if (powerUpsPanel != null) {
                powerUpsPanel.setVisible(false);
            }
            // Pause the game (only if not already paused)
            if (!isPause.getValue() && timeLine != null && timeLine.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                timeLine.pause();
            }
        } else {
            // Hide overlay, keep original panel hidden (as per requirement)
            if (powerUpsPanel != null) {
                powerUpsPanel.setVisible(false);
            }
            // Return focus to game panel
            if (gamePanel != null) {
                gamePanel.requestFocus();
            }
            // Resume the game (only if not paused by pause menu)
            if (!isPause.getValue() && timeLine != null && timeLine.getStatus() == javafx.animation.Animation.Status.PAUSED) {
                timeLine.play();
            }
        }
    }
}