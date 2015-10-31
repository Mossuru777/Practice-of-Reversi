package net.mossan.java.reversi.model;

public class Game {
    private final Disc[][] board;

    public Game(final int board_rows) {
        // Arguments Check
        assert board_rows > 0 && board_rows % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";

        // Initialize
        this.board = new Disc[board_rows][board_rows];
        for (int i = 0; i < board_rows; i++) {
            for (int j = 0; j < board_rows; j++) {
                board[i][j] = Disc.NONE;
            }
        }
        for (int i = board_rows / 2 - 1; i <= board_rows / 2; i++) {
            int k = i % 2 == 1 ? 0 : 1;
            for (int j = board_rows / 2 - 1; j <= board_rows / 2; j++) {
                if (k % 2 == 0) {
                    board[i][j] = Disc.WHITE;
                } else {
                    board[i][j] = Disc.BLACK;
                }
                k++;
            }
        }
    }

    public Disc[][] getBoard() {
        return this.board;
    }
}
