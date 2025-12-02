package controller.gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import controller.game.EventSource;
import controller.game.EventType;
import controller.game.GameController;
import controller.game.InputEventListener;
import controller.game.MoveEvent;
import model.DownData;
import model.PowerUp;
import model.ViewData;
import view.GameOverPanel;
import view.NotificationPanel;


import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Main GUI controller for the Tetris game.
 * Orchestrates all UI components, manages game state, and coordinates between
 * the game logic (GameController) and the JavaFX UI. Uses an orchestrator pattern
 * delegating specific responsibilities to specialized manager classes:
 * - KeyboardHandler: Input processing
 * - Renderer: Visual rendering
 * - PauseManager: Pause/resume functionality
 * - PowerUpManager: Power-up UI and shop
 * - EffectManager: Visual effects
 * - SlowMotionManager: Slow-motion power-up
 * - VideoManager: Background video
 * 
 * @author COMP2042 Coursework
 */
public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 22;

    @FXML GridPane gamePanel;
    @FXML private StackPane groupNotification;
    @FXML GridPane brickPanel;
    @FXML private GameOverPanel gameOverPanel;
    @FXML BorderPane gameBoard;
    @FXML StackPane boardStack;
    @FXML private Label scoreLabel;
    @FXML private Label linesLabel;
    @FXML private Label skillPointsLabel;
    @FXML GridPane nextPanel;
    @FXML GridPane holdPanel;
    @FXML HBox powerUpsContainer;
    @FXML HBox powerUpsContainerCentered;
    @FXML VBox powerUpsPanel;
    @FXML StackPane powerUpsOverlay;
    @FXML private VBox powerUpsInventoryDisplay;
    @FXML HBox powerUpsInventoryContainer;
    @FXML StackPane pauseOverlay;
    @FXML private StackPane mainMenuOverlay;
    @FXML StackPane rootStackPane; // Root StackPane from FXML

    private int totalLinesCleared = 0;
    MediaPlayer mediaPlayer;
    MediaView mediaView;
    GameController gameController;

    Rectangle[][] displayMatrix;
    Rectangle[][] nextPreview;
    Rectangle[][] holdPreview;
    InputEventListener eventListener;
    Rectangle[][] rectangles;
    Rectangle[][] shadowRectangles;
    GridPane shadowPanel;
    Timeline timeLine;
    int[][] currentBoardMatrix;
    
    // Slow motion power-up
    Timeline slowMotionRestoreTimer;
    Timeline slowMotionCountdownTimer;
    int slowMotionRemainingSeconds = 0;
    Label slowMotionCountdownLabel;

    final BooleanProperty isPause = new SimpleBooleanProperty();
    final BooleanProperty isGameOver = new SimpleBooleanProperty();
    
    // Countdown overlay for resume
    StackPane countdownOverlay;
    Label countdownLabel;
    Timeline countdownTimer;
    
    // Helper classes
    private GuiControllerKeyboardHandler keyboardHandler;
    private GuiControllerVideoManager videoManager;
    private GuiControllerRenderer renderer;
    private GuiControllerPauseManager pauseManager;
    private GuiControllerSlowMotionManager slowMotionManager;
    private GuiControllerEffectManager effectManager;
    private GuiControllerPowerUpManager powerUpManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize helper classes
        keyboardHandler = new GuiControllerKeyboardHandler(this);
        videoManager = new GuiControllerVideoManager(this);
        renderer = new GuiControllerRenderer(this);
        pauseManager = new GuiControllerPauseManager(this);
        slowMotionManager = new GuiControllerSlowMotionManager(this);
        effectManager = new GuiControllerEffectManager(this);
        powerUpManager = new GuiControllerPowerUpManager(this);
        
        // load font (guarded)
        try {
            if (getClass().getClassLoader().getResource("digital.ttf") != null) {
                Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
            }
        } catch (Exception ignored) {}

        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(keyboardHandler.createKeyHandler());

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
        videoManager.setupVideoBackground();
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

    public void initGameView(int[][] boardMatrix, ViewData brick) {
        renderer.initGameView(boardMatrix, brick);
        timeLine = new Timeline(new KeyFrame(Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    public void refreshBrick(ViewData brick) {
        renderer.refreshBrick(brick);
    }

    public void refreshGameBackground(int[][] board) {
        renderer.refreshGameBackground(board);
    }

    void moveDown(MoveEvent event) {
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

    void hardDrop(MoveEvent event) {
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
        effectManager.centerGameBoard();
    }
    
    void setPaused(boolean paused) {
        pauseManager.setPaused(paused);
    }
    
    void cancelCountdown() {
        pauseManager.cancelCountdown();
    }
    
    void resumeImmediately() {
        pauseManager.resumeImmediately();
    }

    @FXML
    private void onResume(ActionEvent e) {
        pauseManager.setPaused(false);
    }

    @FXML
    private void onRestart(ActionEvent e) {
        resumeImmediately();
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
        powerUpManager.initializePowerUpUI();
        
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
        slowMotionManager.applySlowMotion();
    }

    /**
     * Show bomb explosion effect at the specified grid position
     */
    public void showBoomEffect(int gridX, int gridY) {
        effectManager.showBoomEffect(gridX, gridY);
    }

    void updatePowerUpUI() {
        powerUpManager.updatePowerUpUI();
    }
    
    void togglePowerUpsOverlay() {
        powerUpManager.togglePowerUpsOverlay();
    }
}