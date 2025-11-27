package model;

import org.junit.jupiter.api.Test;
import view.ClearRow;

import static org.junit.jupiter.api.Assertions.*;

class MatrixOperationsTest {

    @Test
    void intersectDetectsOverlapAndBounds() {
        int[][] board = new int[][]{
                {0, 0, 0},
                {0, 1, 0},
                {0, 0, 0}
        };
        int[][] brick = new int[][]{
                {1}
        };

        assertTrue(MatrixOperations.intersect(board, brick, 1, 1), "Intersect should be true when cells overlap");
        assertTrue(MatrixOperations.intersect(board, brick, -1, 0), "Intersect should be true when brick would be out of bounds");
        assertFalse(MatrixOperations.intersect(board, brick, 0, 0), "Valid placement should not report an intersect");
    }

    @Test
    void mergePlacesBrickWithoutExceedingBounds() {
        int[][] board = new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        int[][] brick = new int[][]{
                {2}
        };

        int[][] merged = MatrixOperations.merge(board, brick, 1, 1);
        assertEquals(2, merged[1][1]);

        // Attempting to merge partially outside the board should simply skip cells without throwing
        int[][] untouchedMerge = MatrixOperations.merge(board, brick, 3, 3);
        assertArrayEquals(board, untouchedMerge);
    }

    @Test
    void checkRemovingReturnsClearedRowsAndScore() {
        int[][] matrix = new int[][]{
                {1, 1},
                {0, 1},
                {1, 1}
        };

        ClearRow result = MatrixOperations.checkRemoving(matrix);
        assertEquals(2, result.getLinesRemoved());
        assertEquals(50 * 2 * 2, result.getScoreBonus());

        int[][] newMatrix = result.getNewMatrix();
        assertArrayEquals(new int[]{0, 1}, newMatrix[2], "Remaining row should shift to the bottom");
        assertArrayEquals(new int[]{0, 0}, newMatrix[0], "Top rows should be cleared");
    }
}

