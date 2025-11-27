package model;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.ClearRow;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleBoardTest {

    private SimpleBoard board;

    @BeforeEach
    void setUp() throws Exception {
        board = new SimpleBoard(25, 10);
        injectBrickGenerator(board, new FixedBrickGenerator(new SingleCellBrick()));
        board.createNewBrick();
    }

    @Test
    void moveBrickDownUntilCollisionThenMerge() {
        boolean moved;
        int moves = 0;
        do {
            moved = board.moveBrickDown();
            if (moved) {
                moves++;
            }
        } while (moved);

        assertTrue(moves > 0, "Brick should move at least once on an empty board");

        board.mergeBrickToBackground();
        int[][] matrix = board.getBoardMatrix();

        // Brick should have settled on the last row at its column (offset column starts at 4)
        assertEquals(1, matrix[24][4]);
    }

    @Test
    void hardDropMovesBrickToBottomAndReportsDistance() {
        int distance = board.getHardDropDistance();
        assertTrue(distance > 0, "Distance should be positive on an empty board");

        board.hardDropBrick();
        board.mergeBrickToBackground();

        int[][] matrix = board.getBoardMatrix();
        assertEquals(1, matrix[24][4], "Hard drop should move piece to last row");
    }

    @Test
    void holdBrickResetsAfterNewPiece() {
        assertTrue(board.holdBrick(), "Should be able to hold once per piece");
        assertFalse(board.holdBrick(), "Cannot hold twice before placing piece");

        board.mergeBrickToBackground();
        board.createNewBrick(); // resets hold ability

        assertTrue(board.holdBrick(), "Hold should be available after new piece is created");
    }

    @Test
    void clearRowsPowerUpRemovesBottomRows() {
        int[][] matrix = board.getBoardMatrix();
        // Fill bottom three rows
        for (int row = 22; row < 25; row++) {
            for (int col = 0; col < 10; col++) {
                matrix[row][col] = 1;
            }
        }

        assertTrue(board.clearRowsPowerUp(3), "Power-up should clear when valid row count provided");

        for (int row = 22; row < 25; row++) {
            for (int col = 0; col < 10; col++) {
                assertEquals(0, matrix[row][col], "Bottom rows should be empty after clear");
            }
        }
    }

    @Test
    void clearRowsUpdatesScoreAndMatrix() {
        int[][] matrix = board.getBoardMatrix();
        // Fill entire bottom row to trigger clear via regular gameplay path
        for (int col = 0; col < 10; col++) {
            matrix[24][col] = 1;
        }

        ClearRow result = board.clearRows();
        assertEquals(1, result.getLinesRemoved());
        assertEquals(50, result.getScoreBonus());
    }

    private static void injectBrickGenerator(SimpleBoard board, BrickGenerator generator) throws Exception {
        Field field = SimpleBoard.class.getDeclaredField("brickGenerator");
        field.setAccessible(true);
        field.set(board, generator);
    }

    private static class FixedBrickGenerator implements BrickGenerator {
        private final Brick brick;

        FixedBrickGenerator(Brick brick) {
            this.brick = brick;
        }

        @Override
        public Brick getBrick() {
            return brick;
        }

        @Override
        public Brick getNextBrick() {
            return brick;
        }
    }

    private static class SingleCellBrick implements Brick {
        private final List<int[][]> shapes = Collections.singletonList(new int[][]{{1}});

        @Override
        public List<int[][]> getShapeMatrix() {
            return shapes;
        }
    }
}

