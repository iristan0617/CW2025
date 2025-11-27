package controller.gui;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.net.URL;

/**
 * Manages video background for GuiController
 * Extracted from GuiController to reduce file size
 */
class GuiControllerVideoManager {
    
    private final GuiController guiController;
    
    GuiControllerVideoManager(GuiController guiController) {
        this.guiController = guiController;
    }
    
    void setupVideoBackground() {
        try {
            // Load the video file from resources
            URL videoUrl = getClass().getClassLoader().getResource("galaxy.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                guiController.mediaPlayer = new MediaPlayer(media);
                guiController.mediaView = new MediaView(guiController.mediaPlayer);
                
                // Configure video view
                guiController.mediaView.setPreserveRatio(true);
                guiController.mediaView.setSmooth(true);
                
                // Configure media player
                guiController.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
                guiController.mediaPlayer.setAutoPlay(true);
                guiController.mediaPlayer.setMute(true); // Mute audio
                
                // Add video to root StackPane behind all content
                Platform.runLater(() -> {
                    if (guiController.rootStackPane != null) {
                        // Add video at index 0 to be behind everything
                        guiController.rootStackPane.getChildren().add(0, guiController.mediaView);
                        
                        // Bind video size to fill the window
                        guiController.mediaView.fitWidthProperty().bind(guiController.rootStackPane.widthProperty());
                        guiController.mediaView.fitHeightProperty().bind(guiController.rootStackPane.heightProperty());
                        guiController.mediaView.setPreserveRatio(false); // Stretch to fill
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
}

