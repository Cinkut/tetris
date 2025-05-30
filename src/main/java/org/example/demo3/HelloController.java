package org.example.demo3;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;

public class HelloController {
    @FXML
    private Canvas gameCanvas;

    @FXML
    private Canvas nextPieceCanvas;

    @FXML
    private Label scoreLabel;

    @FXML
    private Button startButton;

    @FXML
    private Button pauseButton;

    private TetrisBoard board;
    private AnimationTimer gameTimer;
    private boolean isRunning = false;
    private boolean isPaused = false;

    // Rozmiary planszy do rysowania
    private final int BLOCK_SIZE = 25;
    private final int NEXT_BLOCK_SIZE = 20;  // Mniejszy rozmiar dla podglądu
    private final int BORDER_WIDTH = 2;

    // Czas aktualizacji gry (ms)
    private long lastUpdate = 0;
    private long updateInterval = 500_000_000; // 0.5 sekundy w nanosekundach

    public void initialize() {
        board = new TetrisBoard();

        // Ustawienie obsługi klawiatury dla Canvas
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPress);

        drawInitialBoard();

        // Inicjalizacja timera gry
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isPaused) return;

                if (now - lastUpdate >= updateInterval) {
                    updateGame();
                    lastUpdate = now;
                }
                drawGame();
            }
        };
    }

    @FXML
    protected void onStartButtonClick() {
        if (!isRunning) {
            // Rozpocznij nową grę
            board.reset();
            isRunning = true;
            isPaused = false;
            startButton.setText("Restart");
            pauseButton.setDisable(false);

            // Uzyskaj fokus dla obsługi klawiatury
            gameCanvas.requestFocus();

            // Uruchom timer gry
            lastUpdate = System.nanoTime();
            gameTimer.start();
        } else {
            // Zresetuj grę
            board.reset();
            isPaused = false;
            pauseButton.setText("Pauza");

            // Uzyskaj fokus dla obsługi klawiatury
            gameCanvas.requestFocus();
        }

        updateScoreLabel();
    }

    @FXML
    protected void onPauseButtonClick() {
        if (isRunning) {
            isPaused = !isPaused;
            pauseButton.setText(isPaused ? "Wznów" : "Pauza");

            if (!isPaused) {
                gameCanvas.requestFocus();
            }
        }
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        if (isRunning) {
            // Obsługa pauzy - działa nawet gdy gra jest zapauzowana
            if (code == KeyCode.P) {
                isPaused = !isPaused;
                pauseButton.setText(isPaused ? "Wznów" : "Pauza");

                if (!isPaused) {
                    gameCanvas.requestFocus();
                }
                event.consume();
                return;
            }

            // Obsługa resetu gry - działa również w trakcie pauzy
            if (code == KeyCode.R) {
                board.reset();
                isPaused = false;
                pauseButton.setText("Pauza");

                gameCanvas.requestFocus();
                updateScoreLabel();
                event.consume();
                return;
            }

            // Jeśli gra jest zapauzowana, nie obsługuj pozostałych klawiszy
            if (isPaused) return;

            // Kontrolowanie klocka (tylko gdy gra jest aktywna i nie zapauzowana)
            switch (code) {
                case LEFT:
                    board.moveSideways(-1);
                    break;
                case RIGHT:
                    board.moveSideways(1);
                    break;
                case DOWN:
                    board.moveDown();
                    break;
                case UP:
                    board.rotate();
                    break;
                case SPACE:
                    board.dropDown(); // Natychmiastowe zrzucenie klocka na dół
                    break;
                default:
                    break;
            }
        }

        drawGame();
        event.consume();
    }

    private void updateGame() {
        // Przesuń klocek w dół
        boolean moved = board.moveDown();

        // Sprawdź czy nastąpił koniec gry
        if (board.isGameOver()) {
            gameOver();
        }

        updateScoreLabel();
    }

    private void updateScoreLabel() {
        scoreLabel.setText(Integer.toString(board.getScore()));
    }

    private void gameOver() {
        isRunning = false;
        gameTimer.stop();
        pauseButton.setDisable(true);
        startButton.setText("Start");

        // Nie wyświetlaj żadnego komunikatu, po prostu zatrzymaj grę
    }

    private void drawInitialBoard() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Tło
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Siatka
        gc.setStroke(Color.DARKGREY);
        gc.setLineWidth(1);

        for (int i = 0; i <= TetrisBoard.BOARD_HEIGHT; i++) {
            gc.strokeLine(0, i * BLOCK_SIZE, BLOCK_SIZE * TetrisBoard.BOARD_WIDTH, i * BLOCK_SIZE);
        }

        for (int i = 0; i <= TetrisBoard.BOARD_WIDTH; i++) {
            gc.strokeLine(i * BLOCK_SIZE, 0, i * BLOCK_SIZE, BLOCK_SIZE * TetrisBoard.BOARD_HEIGHT);
        }
    }

    private void drawGame() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Tło
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Rysuj planszę
        Color[][] boardState = board.getBoard();
        for (int i = 0; i < TetrisBoard.BOARD_HEIGHT; i++) {
            for (int j = 0; j < TetrisBoard.BOARD_WIDTH; j++) {
                if (boardState[i][j] != null) {
                    drawBlock(gc, j, i, boardState[i][j]);
                }
            }
        }

        if (isRunning && !isPaused) {
            // Rysuj "cień" klocka (pozycja gdzie wyląduje po zrzuceniu)
            Tetromino ghost = board.getGhostPiecePosition();
            int[][] ghostShape = ghost.getShape();

            for (int i = 0; i < ghostShape.length; i++) {
                for (int j = 0; j < ghostShape[i].length; j++) {
                    if (ghostShape[i][j] != 0) {
                        int x = ghost.getX() + j;
                        int y = ghost.getY() + i;
                        if (y >= 0) { // Nie rysuj ponad górną granicą planszy
                            drawGhostBlock(gc, x, y);
                        }
                    }
                }
            }
        }

        // Rysuj aktualny klocek
        Tetromino current = board.getCurrentPiece();
        int[][] shape = current.getShape();
        Color color = current.getColor();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = current.getX() + j;
                    int y = current.getY() + i;
                    if (y >= 0) { // Nie rysuj ponad górną granicą planszy
                        drawBlock(gc, x, y, color);
                    }
                }
            }
        }

        // Rysuj siatkę
        gc.setStroke(Color.DARKGREY);
        gc.setLineWidth(0.5);

        for (int i = 0; i <= TetrisBoard.BOARD_HEIGHT; i++) {
            gc.strokeLine(0, i * BLOCK_SIZE, BLOCK_SIZE * TetrisBoard.BOARD_WIDTH, i * BLOCK_SIZE);
        }

        for (int i = 0; i <= TetrisBoard.BOARD_WIDTH; i++) {
            gc.strokeLine(i * BLOCK_SIZE, 0, i * BLOCK_SIZE, BLOCK_SIZE * TetrisBoard.BOARD_HEIGHT);
        }

        // Rysuj następny klocek
        drawNextPiece();
    }

    /**
     * Rysuje kontur bloku (cień) w miejscu, gdzie wyląduje klocek
     */
    private void drawGhostBlock(GraphicsContext gc, int x, int y) {
        // Rysuj tylko obramowanie klocka
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRect(x * BLOCK_SIZE + 2, y * BLOCK_SIZE + 2,
                     BLOCK_SIZE - 4, BLOCK_SIZE - 4);
    }

    private void drawBlock(GraphicsContext gc, int x, int y, Color color) {
        gc.setFill(color);
        gc.fillRect(x * BLOCK_SIZE + BORDER_WIDTH, y * BLOCK_SIZE + BORDER_WIDTH,
                   BLOCK_SIZE - 2 * BORDER_WIDTH, BLOCK_SIZE - 2 * BORDER_WIDTH);

        // Efekt 3D
        gc.setFill(color.brighter());
        gc.fillRect(x * BLOCK_SIZE + BORDER_WIDTH, y * BLOCK_SIZE + BORDER_WIDTH,
                   BLOCK_SIZE - 2 * BORDER_WIDTH, BORDER_WIDTH);
        gc.fillRect(x * BLOCK_SIZE + BORDER_WIDTH, y * BLOCK_SIZE + BORDER_WIDTH,
                   BORDER_WIDTH, BLOCK_SIZE - 2 * BORDER_WIDTH);

        gc.setFill(color.darker());
        gc.fillRect(x * BLOCK_SIZE + BLOCK_SIZE - 2 * BORDER_WIDTH, y * BLOCK_SIZE + BORDER_WIDTH,
                   BORDER_WIDTH, BLOCK_SIZE - 2 * BORDER_WIDTH);
        gc.fillRect(x * BLOCK_SIZE + BORDER_WIDTH, y * BLOCK_SIZE + BLOCK_SIZE - 2 * BORDER_WIDTH,
                   BLOCK_SIZE - 2 * BORDER_WIDTH, BORDER_WIDTH);
    }

    private void drawNextPiece() {
        GraphicsContext gc = nextPieceCanvas.getGraphicsContext2D();

        // Tło
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, nextPieceCanvas.getWidth(), nextPieceCanvas.getHeight());

        // Rysuj następny klocek
        Tetromino next = board.getNextPiece();
        int[][] shape = next.getShape();
        Color color = next.getColor();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = j;
                    int y = i;
                    drawNextBlock(gc, x, y, color);
                }
            }
        }
    }

    private void drawNextBlock(GraphicsContext gc, int x, int y, Color color) {
        gc.setFill(color);
        gc.fillRect(x * NEXT_BLOCK_SIZE + BORDER_WIDTH, y * NEXT_BLOCK_SIZE + BORDER_WIDTH,
                   NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH, NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH);

        // Efekt 3D
        gc.setFill(color.brighter());
        gc.fillRect(x * NEXT_BLOCK_SIZE + BORDER_WIDTH, y * NEXT_BLOCK_SIZE + BORDER_WIDTH,
                   NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH, BORDER_WIDTH);
        gc.fillRect(x * NEXT_BLOCK_SIZE + BORDER_WIDTH, y * NEXT_BLOCK_SIZE + BORDER_WIDTH,
                   BORDER_WIDTH, NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH);

        gc.setFill(color.darker());
        gc.fillRect(x * NEXT_BLOCK_SIZE + NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH, y * NEXT_BLOCK_SIZE + BORDER_WIDTH,
                   BORDER_WIDTH, NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH);
        gc.fillRect(x * NEXT_BLOCK_SIZE + BORDER_WIDTH, y * NEXT_BLOCK_SIZE + NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH,
                   NEXT_BLOCK_SIZE - 2 * BORDER_WIDTH, BORDER_WIDTH);
    }
}
