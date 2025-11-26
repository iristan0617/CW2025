package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewDataTest {

    @Test
    void getterReturnsDefensiveCopies() {
        int[][] brick = {{1, 0}, {0, 1}};
        int[][] next = {{2}};
        int[][] held = {{3}};

        ViewData viewData = new ViewData(brick, 5, 10, next, held);

        int[][] brickCopy = viewData.getBrickData();
        int[][] nextCopy = viewData.getNextBrickData();
        int[][] heldCopy = viewData.getHeldBrickData();

        // Mutate copies and ensure originals inside ViewData remain unchanged
        brickCopy[0][0] = 99;
        nextCopy[0][0] = 99;
        heldCopy[0][0] = 99;

        assertEquals(1, viewData.getBrickData()[0][0]);
        assertEquals(2, viewData.getNextBrickData()[0][0]);
        assertEquals(3, viewData.getHeldBrickData()[0][0]);
    }

    @Test
    void accessorsExposeCoordinatesAndOptionalHeldData() {
        ViewData viewData = new ViewData(new int[][]{{1}}, 3, 4, new int[][]{{2}}, null);

        assertEquals(3, viewData.getxPosition());
        assertEquals(4, viewData.getyPosition());
        assertNull(viewData.getHeldBrickData(), "Held data should be null when not provided");
    }
}

