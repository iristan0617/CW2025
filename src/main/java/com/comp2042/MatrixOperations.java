package com.comp2042;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixOperations {

    private static final int BASE_SCORE_PER_ROW = 50;

    // We don't want to instantiate this utility class
    private MatrixOperations() {
    }

    public static boolean intersect(final int[][] matrix, final int[][] brick, int x, int y) {
        for (int i = 0; i < brick.length; i++) {
            for (int j = 0; j < brick[i].length; j++) {
                int targetX = x + i;
                int targetY = y + j;
                if (brick[j][i] != 0 && (isOutOfBounds(matrix, targetX, targetY) || matrix[targetY][targetX] != 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOutOfBounds(int[][] matrix, int x, int y) {
        return x < 0 || y < 0 || y >= matrix.length || x >= matrix[y].length;
    }

    public static int[][] copy(int[][] original) {
        int[][] myInt = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            int[] aMatrix = original[i];
            int aLength = aMatrix.length;
            myInt[i] = new int[aLength];
            System.arraycopy(aMatrix, 0, myInt[i], 0, aLength);
        }
        return myInt;
    }

    public static int[][] merge(int[][] filledFields, int[][] brick, int x, int y) {
        int[][] copy = copy(filledFields);
        for (int i = 0; i < brick.length; i++) {
            for (int j = 0; j < brick[i].length; j++) {
                int targetX = x + i;
                int targetY = y + j;
                if (brick[j][i] != 0) {
                    copy[targetY][targetX] = brick[j][i];
                }
            }
        }
        return copy;
    }

    public static ClearRow checkRemoving(final int[][] matrix) {
        Deque<int[]> newRows = new ArrayDeque<>();
        List<Integer> clearedRows = identifyCompletedRows(matrix, newRows);
        int[][] newMatrix = buildNewMatrix(matrix, newRows);
        int scoreBonus = calculateScoreBonus(clearedRows.size());

        return new ClearRow(clearedRows.size(), newMatrix, scoreBonus);
    }

    private static List<Integer> identifyCompletedRows(int[][] matrix, Deque<int[]> newRows) {
        List<Integer> clearedRows = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            int[] currentRow = matrix[i];
            if (isRowComplete(currentRow)) {
                clearedRows.add(i);
            } else {
                newRows.add(currentRow);
            }
        }
        return clearedRows;
    }

    private static boolean isRowComplete(int[] row) {
        for (int cell : row) {
            if (cell == 0) {
                return false;
            }
        }
        return true;
    }

    private static int[][] buildNewMatrix(int[][] matrix, Deque<int[]> newRows) {
        int[][] newMatrix = new int[matrix.length][matrix[0].length];
        for (int i = matrix.length - 1; i >= 0; i--) {
            int[] row = newRows.pollLast();
            if (row != null) {
                newMatrix[i] = row;
            } else {
                break;
            }
        }
        return newMatrix;
    }

    private static int calculateScoreBonus(int clearedRowsCount) {
        return BASE_SCORE_PER_ROW * clearedRowsCount * clearedRowsCount;
    }

    public static List<int[][]> deepCopyList(List<int[][]> list) {
        return list.stream().map(MatrixOperations::copy).collect(Collectors.toList());
    }
}