package model;

import com.comp2042.logic.bricks.Brick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.NextShapeInfo;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BrickRotatorTest {

    private BrickRotator rotator;

    @BeforeEach
    void setUp() {
        rotator = new BrickRotator();
        rotator.setBrick(new FakeBrick(
                new int[][]{{1}},
                new int[][]{{2}},
                new int[][]{{3}}
        ));
    }

    @Test
    void getNextShapeCyclesAndUpdatesCurrentShape() {
        NextShapeInfo next = rotator.getNextShape();
        assertArrayEquals(new int[][]{{2}}, next.getShape());
        rotator.setCurrentShape(next.getPosition());

        next = rotator.getNextShape();
        assertArrayEquals(new int[][]{{3}}, next.getShape());

        // Cycle should wrap back to first shape
        rotator.setCurrentShape(next.getPosition());
        next = rotator.getNextShape();
        assertArrayEquals(new int[][]{{1}}, next.getShape());
    }

    @Test
    void setBrickResetsCurrentShapeToZero() {
        rotator.setCurrentShape(2);
        rotator.setBrick(new FakeBrick(new int[][]{{9}}));

        assertArrayEquals(new int[][]{{9}}, rotator.getCurrentShape());
        assertArrayEquals(new int[][]{{9}}, rotator.getNextShape().getShape());
    }

    private static class FakeBrick implements Brick {
        private final List<int[][]> shapes;

        FakeBrick(int[][]... shapes) {
            this.shapes = Arrays.asList(shapes);
        }

        @Override
        public List<int[][]> getShapeMatrix() {
            return shapes;
        }
    }
}

