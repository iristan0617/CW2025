package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Manages the game score using JavaFX properties for reactive UI binding.
 * Provides methods to add points and reset the score. The score is observable
 * through the scoreProperty() method for automatic UI updates.
 * 
 * @author COMP2042 Coursework
 */
public final class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Gets the score property for reactive UI binding.
     * 
     * @return the IntegerProperty representing the current score
     */
    public IntegerProperty scoreProperty() {
        return score;
    }

    /**
     * Adds points to the current score.
     * 
     * @param i the number of points to add
     */
    public void add(int i){
        score.setValue(score.getValue() + i);
    }

    /**
     * Resets the score to zero.
     */
    public void reset() {
        score.setValue(0);
    }
}
