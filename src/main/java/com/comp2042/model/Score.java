package com.comp2042;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public final class Score {

    private final IntegerProperty score = new SimpleIntegerProperty(0);

    private final IntegerProperty lines = new SimpleIntegerProperty(0);

    public IntegerProperty scoreProperty() {

        return score;
    }

    public IntegerProperty linesProperty() {
        return lines;
    }

    public void add(int i){
        score.setValue(score.getValue() + i);
    }

    public void addLines(int linesCleared) {
        lines.set(lines.get() + linesCleared);
    }

    public int get() {
        return score.get();
    }

    public int getLines() {
        return lines.get();
    }

    public void reset() {
        score.setValue(0);
        lines.setValue(0);
    }
}
