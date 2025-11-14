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
import javafx.scene.effect.Glow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;


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
    @FXML private GridPane nextPanel;
    @FXML private GridPane holdPanel;
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

    private final BooleanProperty isPause = new SimpleBooleanProperty();
    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

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
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
                if (keyEvent.getCode() == KeyCode.P || keyEvent.getCode() == KeyCode.ESCAPE) {
                    setPaused(!isPause.get());
                    keyEvent.consume();
                }
            }
        });

        gameOverPanel.setVisible(false);
        if (pauseOverlay != null) pauseOverlay.setVisible(false);
        // Main menu is visible by default
        if (mainMenuOverlay != null) mainMenuOverlay.setVisible(true);

        if (scoreLabel != null) scoreLabel.setText("0");
        if (linesLabel != null) linesLabel.setText("0");
        
        // Hide game content initially (will be shown when Start is clicked)
        // Game content will be shown when Start button is clicked
        Platform.runLater(() -> {
            if (gameBoard != null) gameBoard.setVisible(false);
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
                    shadowRect.setOpacity(0.4);
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

    private void refreshBrick(ViewData brick) {
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
                    shadowRect.setOpacity(0.4);
                    shadowRect.setStroke(Color.DARKGRAY);
                    shadowRect.setStrokeWidth(1.0);
                    shadowRectangles[i][j] = shadowRect;
                    shadowPanel.add(shadowRect, j, i);
                }
            }
        }
        
        // Update shadow rectangles to match brick shape - all grey color
        Color shadowGrey = Color.GRAY;
        Color shadowStroke = Color.DARKGRAY;
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
        isPause.setValue(paused);
        if (timeLine != null) {
            if (paused) timeLine.pause(); else timeLine.play();
        }
        if (pauseOverlay != null) {
            if (paused) {
                pauseOverlay.setVisible(true);
                pauseOverlay.toFront(); // Ensure it's on top
            } else {
                pauseOverlay.setVisible(false);
            }
        }
    }

    @FXML
    private void onResume(ActionEvent e) {
        setPaused(false);
        gamePanel.requestFocus();
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
        
        // Reset score display
        if (scoreLabel != null) {
            scoreLabel.setText("0");
        }
        
        // Reset game controller (will be recreated when Start is clicked)
        gameController = null;
        eventListener = null;
    }
    
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
}