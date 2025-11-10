package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScoreTest {

    @Test
    public void testInitialScoreIsZero() {
        Score score = new Score();
        assertEquals(0, score.scoreProperty().get());
    }

    @Test
    public void testAddScore() {
        Score score = new Score();
        score.add(10);
        assertEquals(10, score.scoreProperty().get());
    }

    @Test
    public void testResetScore() {
        Score score = new Score();
        score.add(50);
        score.reset();
        assertEquals(0, score.scoreProperty().get());
    }
}
