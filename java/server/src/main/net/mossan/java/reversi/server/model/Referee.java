package net.mossan.java.reversi.server.model;

import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;
import net.mossan.java.reversi.server.ServerGameRoom;

import java.lang.ref.WeakReference;
import java.util.Objects;

public final class Referee extends Game {
    private WeakReference<ServerGameRoom> serverGameRoomWeakReference;

    public Referee(final int board_rows, ServerGameRoom serverGameRoom) {
        super();

        // Arguments Check
        assert board_rows > 0 && board_rows % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";

        // Initialize
        this.serverGameRoomWeakReference = new WeakReference<>(serverGameRoom);
        this.board = new DiscType[board_rows][board_rows];
        this.resetBoard();
    }

    private synchronized void resetBoard() {
        for (int i = 0; i < this.getBoardRows(); i++) {
            for (int j = 0; j < this.getBoardRows(); j++) {
                this.board[i][j] = null;
            }
        }
        for (int i = this.getBoardRows() / 2 - 1; i <= this.getBoardRows() / 2; i++) {
            DiscType placeDiscType = i % 2 == 1 ? DiscType.Black : DiscType.White;
            for (int j = this.getBoardRows() / 2 - 1; j <= this.getBoardRows() / 2; j++) {
                this.board[i][j] = placeDiscType;
                placeDiscType = placeDiscType.otherDiscType();
            }
        }
        this.currentTurn = DiscType.Black;
        this.winner = null;
    }

    public synchronized void placeCell(PlaceableCell placeCell) {
        assert this.currentTurn != null && this.winner == null : "The winner decided.";

        this.getPlaceableCellsList(this.currentTurn).stream().filter(placeCell::equals).findFirst().ifPresent(p -> {
            // Place Disc
            this.board[p.placePoint[0]][p.placePoint[1]] = this.currentTurn;
            for (int[] reversiblePoint : p.reversiblePoints) {
                this.board[reversiblePoint[0]][reversiblePoint[1]] = this.currentTurn;
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
                        if (this.board[h][v] == null) {
                            continue;
                        }
                        discCount[this.board[h][v].getInt()]++;
                    }
                }
                if (discCount[DiscType.Black.getInt()] > discCount[DiscType.White.getInt()]) {
                    this.winner = DiscType.Black;
                } else {
                    this.winner = DiscType.White;
                }
            }
            Objects.requireNonNull(this.serverGameRoomWeakReference.get()).onGameUpdate();
        });
        System.out.print("");
    }
}
