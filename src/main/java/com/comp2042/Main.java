package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Main entry point for the Tetris JavaFX application.
 * Initializes the JavaFX application, loads the FXML layout, and displays
 * the main game window. The game controller is initialized via FXML.
 * 
 * @author COMP2042 Coursework
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application.
     * Loads the game layout from FXML and displays the main window.
     * 
     * @param primaryStage the primary stage for the application
     * @throws Exception if FXML loading fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, resources);
        Parent root = fxmlLoader.load();
        // Controller is initialized via FXML, game starts when user clicks Start button

        primaryStage.setTitle("TetrisJFX");
        Scene scene = new Scene(root, 300, 510);
        primaryStage.setScene(scene);
        primaryStage.show();
        // Game will be initialized when user clicks Start
    }


    /**
     * Main method to launch the JavaFX application.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
