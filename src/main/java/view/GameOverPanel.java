package view;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;


public class GameOverPanel extends BorderPane {

    public GameOverPanel() {
        // Set red background on the BorderPane itself
        setStyle("-fx-background-color: red;");

        // Make the panel bigger by adding padding
        setStyle("-fx-background-color: red; -fx-padding: 10px 12px;"); // 30px top/bottom, 100px left/right

        final Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.getStyleClass().add("gameOverStyle");
        setCenter(gameOverLabel);

        // Make BorderPane fill its parent
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
    }

}
