package org.example.demo3;

import javafx.scene.paint.Color;

/**
 * Klasa reprezentująca planszę do gry w Tetrisa
 */
public class TetrisBoard {
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;

    // Reprezentacja planszy - null oznacza puste pole
    private Color[][] board;

    // Aktualny klocek
    private Tetromino currentPiece;

    // Następny klocek
    private Tetromino nextPiece;

    // Licznik punktów
    private int score;

    // Flaga informująca o końcu gry
    private boolean isGameOver = false;

    public TetrisBoard() {
        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        nextPiece = new Tetromino(); // Najpierw tworzymy następny klocek
        newPiece();                  // Potem tworzymy aktualny klocek
        score = 0;
        isGameOver = false;
    }

    /**
     * Tworzy nowy klocek
     */
    public void newPiece() {
        currentPiece = nextPiece;        // Aktualny klocek staje się tym, który był "następny"
        nextPiece = new Tetromino();     // Tworzymy nowy "następny" klocek

        // Ustawiamy pozycję startową dla aktualnego klocka
        currentPiece.setX(BOARD_WIDTH / 2 - currentPiece.getShape()[0].length / 2);
        currentPiece.setY(0);
    }

    /**
     * Zwraca następny klocek
     */
    public Tetromino getNextPiece() {
        return nextPiece;
    }

    /**
     * Sprawdza kolizję klocka z planszą lub granicami planszy
     */
    public boolean hasCollision(Tetromino piece) {
        int[][] shape = piece.getShape();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 0) continue;

                int x = piece.getX() + j;
                int y = piece.getY() + i;

                if (y < 0) continue; // Powyżej górnej granicy jest ok

                // Sprawdź kolizję z granicami lub innymi klockami
                if (y >= BOARD_HEIGHT || x < 0 || x >= BOARD_WIDTH ||
                    (y >= 0 && board[y][x] != null)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Próba przesunięcia klocka w dół
     * @return false jeśli nie można przesunąć (klocek został "przytwierdzony")
     */
    public boolean moveDown() {
        Tetromino test = currentPiece.copy();
        test.setY(test.getY() + 1);

        if (hasCollision(test)) {
            placePiece();
            return false;
        } else {
            currentPiece.setY(currentPiece.getY() + 1);
            return true;
        }
    }

    /**
     * Próba przesunięcia klocka w bok
     */
    public boolean moveSideways(int deltaX) {
        Tetromino test = currentPiece.copy();
        test.setX(test.getX() + deltaX);

        if (!hasCollision(test)) {
            currentPiece.setX(currentPiece.getX() + deltaX);
            return true;
        }

        return false;
    }

    /**
     * Próba obrócenia klocka
     */
    public boolean rotate() {
        Tetromino test = currentPiece.copy();
        test.rotate();

        if (!hasCollision(test)) {
            currentPiece.rotate();
            return true;
        }

        return false;
    }

    /**
     * Zrzuca klocek natychmiast na sam dół
     */
    public void dropDown() {
        Tetromino test = currentPiece.copy();
        int dropDistance = 0;

        // Sprawdź, jak daleko może spaść klocek
        while (!hasCollision(test)) {
            dropDistance++;
            test.setY(test.getY() + 1);
        }

        // Ustaw klocek na odpowiedniej pozycji (o jeden wyżej niż kolizja)
        currentPiece.setY(currentPiece.getY() + dropDistance - 1);
        placePiece(); // Natychmiast umieść klocek na planszy
    }

    /**
     * Umieszcza klocek na planszy
     */
    private void placePiece() {
        int[][] shape = currentPiece.getShape();
        Color color = currentPiece.getColor();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = currentPiece.getX() + j;
                    int y = currentPiece.getY() + i;

                    if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
                        board[y][x] = color;
                    }
                }
            }
        }

        // Sprawdź pełne wiersze
        checkRows();

        // Stwórz nowy klocek
        newPiece();

        // Sprawdź czy gra się skończyła
        if (hasCollision(currentPiece)) {
            // Koniec gry
            isGameOver = true;
        }
    }

    /**
     * Usuwa pełne wiersze i aktualizuje punktację
     */
    private void checkRows() {
        int rowsCleared = 0;

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            boolean fullRow = true;

            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == null) {
                    fullRow = false;
                    break;
                }
            }

            if (fullRow) {
                rowsCleared++;
                // Przesuń wiersze w dół
                for (int k = i; k > 0; k--) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[k][j] = board[k-1][j];
                    }
                }
                // Wyczyść najwyższy wiersz
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[0][j] = null;
                }
            }
        }

        // Przyznaj punkty
        if (rowsCleared > 0) {
            // Stosujemy tradycyjne punktowanie z Tetrisa
            int[] points = {0, 40, 100, 300, 1200};
            score += points[rowsCleared];
        }
    }

    /**
     * Zwraca aktualny stan planszy
     */
    public Color[][] getBoard() {
        return board;
    }

    /**
     * Zwraca aktualny klocek
     */
    public Tetromino getCurrentPiece() {
        return currentPiece;
    }

    /**
     * Zwraca aktualny wynik
     */
    public int getScore() {
        return score;
    }

    /**
     * Sprawdza czy nastąpił koniec gry
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Resetuje planszę do początkowego stanu
     */
    public void reset() {
        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        nextPiece = new Tetromino(); // Najpierw tworzymy następny klocek
        newPiece();                  // Potem tworzymy aktualny klocek
        score = 0;
        isGameOver = false;
    }

    /**
     * Zwraca kopię aktualnego klocka w pozycji, gdzie wylądowałby po zrzuceniu
     */
    public Tetromino getGhostPiecePosition() {
        Tetromino ghost = currentPiece.copy();

        // Przesuń klocek jak najdalej w dół bez kolizji
        while (!hasCollision(ghost)) {
            ghost.setY(ghost.getY() + 1);
        }

        // Cofnij o jedną pozycję w górę (aby nie było kolizji)
        ghost.setY(ghost.getY() - 1);

        return ghost;
    }
}
