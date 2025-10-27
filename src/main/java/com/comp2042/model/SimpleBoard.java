package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import com.comp2042.logic.bricks.RandomBrickGenerator;

import java.awt.*;

public class SimpleBoard implements Board {

    private final int width;
    private final int height;
    private final BrickGenerator brickGenerator;
    private final BrickRotator brickRotator;
    private int[][] currentGameMatrix;
    private Point currentOffset;
    private final Score score;

    // Added constants
    private static final int START_POSITION_X = 4;
    private static final int START_POSITION_Y = 10;

    public SimpleBoard(int width, int height) {
        this.width = width;
        this.height = height;
        currentGameMatrix = new int[width][height];
        brickGenerator = new RandomBrickGenerator();
        brickRotator = new BrickRotator();
        score = new Score();
    }

    private boolean attemptMove(int deltaX, int deltaY) {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        Point newPosition = new Point(currentOffset);
        newPosition.translate(deltaX, deltaY);

        boolean conflict = MatrixOperations.intersect(currentMatrix, brickRotator.getCurrentShape(),
                (int) newPosition.getX(), (int) newPosition.getY());
        if (conflict) {
            return false;
        } else {
            currentOffset = newPosition;
            return true;
        }
    }

    @Override
    public boolean moveBrickDown() {
        return attemptMove(0, 1);
    }


    @Override
    public boolean moveBrickLeft() {
        return attemptMove(-1, 0);
    }

    @Override
    public boolean moveBrickRight() {
        return attemptMove(1, 0);
    }

    @Override
    public boolean rotateLeftBrick() {
        int[][] currentMatrix = MatrixOperations.copy(currentGameMatrix);
        NextShapeInfo nextShape = brickRotator.getNextShape();
        boolean conflict = MatrixOperations.intersect(currentMatrix, nextShape.getShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
        if (conflict) {
            return false;
        } else {
            brickRotator.setCurrentShape(nextShape.getPosition());
            return true;
        }
    }

    @Override
    public boolean createNewBrick() {
        Brick currentBrick = brickGenerator.getBrick();
        brickRotator.setBrick(currentBrick);
        currentOffset = new Point(START_POSITION_X, START_POSITION_Y);
        return MatrixOperations.intersect(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public int[][] getBoardMatrix() {
        return currentGameMatrix;
    }

    @Override
    public ViewData getViewData() {
        return new ViewData(brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY(), brickGenerator.getNextBrick().getShapeMatrix().get(0));
    }

    @Override
    public void mergeBrickToBackground() {
        currentGameMatrix = MatrixOperations.merge(currentGameMatrix, brickRotator.getCurrentShape(), (int) currentOffset.getX(), (int) currentOffset.getY());
    }

    @Override
    public ClearRow clearRows() {
        ClearRow clearRow = MatrixOperations.checkRemoving(currentGameMatrix);
        currentGameMatrix = clearRow.getNewMatrix();
        return clearRow;

    }

    @Override
    public Score getScore() {
        return score;
    }


    @Override
    public void newGame() {
        currentGameMatrix = new int[width][height];
        score.reset();
        createNewBrick();
    }
}