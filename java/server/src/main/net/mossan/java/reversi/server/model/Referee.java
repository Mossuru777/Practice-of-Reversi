package net.mossan.java.reversi.server.model;

import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;
import net.mossan.java.reversi.common.model.eventlistener.ObserverEventListener;

public final class Referee extends Game {
    private final ObserverEventListener observer;

    public Referee(final int board_rows, ObserverEventListener observer) {
        super();

        // Arguments Check
        assert board_rows > 0 && board_rows % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";

        // Initialize
        this.observer = observer;
        this.board = new DiscType[board_rows][board_rows];
        this.resetBoard();
    }

    private void resetBoard() {
        for (int i = 0; i < this.getBoardRows(); i++) {
            for (int j = 0; j < this.getBoardRows(); j++) {
                board[i][j] = null;
            }
        }
        for (int i = this.getBoardRows() / 2 - 1; i <= this.getBoardRows() / 2; i++) {
            DiscType placeDiscType = i % 2 == 1 ? DiscType.Black : DiscType.White;
            for (int j = this.getBoardRows() / 2 - 1; j <= this.getBoardRows() / 2; j++) {
                board[i][j] = placeDiscType;
                placeDiscType = placeDiscType.otherDiscType();
            }
        }
        currentTurn = DiscType.Black;
        winner = null;
    }

    public void placeCell(PlaceableCell placeCell) {
        assert this.currentTurn != null && this.winner == null : "The winner decided.";

        this.getPlaceableCellsList(this.currentTurn).stream().filter(placeCell::equals).findFirst().ifPresent(p -> {
            // Place Disc
            board[p.placePoint[0]][p.placePoint[1]] = this.currentTurn;
            for (int[] reversiblePoint : p.reversiblePoints) {
                board[reversiblePoint[0]][reversiblePoint[1]] = this.currentTurn;
            }

            // Determine turn change and game over
            if (getPlaceableCellsList(this.currentTurn.otherDiscType()).size() > 0) {
                this.currentTurn = this.currentTurn.otherDiscType();
            } else if (getPlaceableCellsList(this.currentTurn).size() == 0) {
                this.currentTurn = null;

                // Determine winner
                int[] discCount = new int[2];
                for (int h = 0; h < this.getBoardRows(); h++) {
                    for (int v = 0; v < this.getBoardRows(); v++) {
                        if (board[h][v] == null) {
                            continue;
                        }
                        discCount[board[h][v].getInt()]++;
                    }
                }
                if (discCount[DiscType.Black.getInt()] > discCount[DiscType.White.getInt()]) {
                    winner = DiscType.Black;
                } else {
                    winner = DiscType.White;
                }
            }
            this.observer.boardUpdated(this);
        });
        System.out.print("");
    }
}
