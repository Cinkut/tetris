package org.example.demo3;

import javafx.scene.paint.Color;

/**
 * Reprezentuje klocek w grze Tetris (tetromino)
 */
public class Tetromino {
    // Definicja kształtów Tetromino (I, O, T, S, Z, J, L)
    private static final int[][][] SHAPES = {
            // I
            {
                {1, 1, 1, 1}
            },
            // O
            {
                {1, 1},
                {1, 1}
            },
            // T
            {
                {0, 1, 0},
                {1, 1, 1}
            },
            // S
            {
                {0, 1, 1},
                {1, 1, 0}
            },
            // Z
            {
                {1, 1, 0},
                {0, 1, 1}
            },
            // J
            {
                {1, 0, 0},
                {1, 1, 1}
            },
            // L
            {
                {0, 0, 1},
                {1, 1, 1}
            }
    };

    // Kolory dla poszczególnych kształtów
    private static final Color[] COLORS = {
            Color.CYAN, Color.YELLOW, Color.PURPLE,
            Color.GREEN, Color.RED, Color.BLUE, Color.ORANGE
    };

    private int[][] shape;
    private Color color;
    private int x, y; // Pozycja na planszy

    public Tetromino() {
        // Losowy wybór kształtu
        int shapeNum = (int) (Math.random() * SHAPES.length);
        shape = SHAPES[shapeNum];
        color = COLORS[shapeNum];

        // Początkowa pozycja
        x = 4;
        y = 0;
    }

    public int[][] getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * Obraca klocek w prawo
     */
    public void rotate() {
        int[][] rotated = new int[shape[0].length][shape.length];
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[0].length; j++) {
                rotated[j][shape.length - 1 - i] = shape[i][j];
            }
        }
        shape = rotated;
    }

    /**
     * Tworzy kopię klocka
     */
    public Tetromino copy() {
        Tetromino copy = new Tetromino();
        copy.shape = new int[shape.length][];
        for (int i = 0; i < shape.length; i++) {
            copy.shape[i] = shape[i].clone();
        }
        copy.color = color;
        copy.x = x;
        copy.y = y;
        return copy;
    }
}
