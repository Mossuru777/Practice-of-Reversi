package net.mossan.java.reversi.model;

import net.mossan.java.reversi.model.eventlistener.GameEventListener;
import net.mossan.java.reversi.model.player.Player;

import java.util.ArrayList;

public class Game {
    private final Player[] players;
    private final Disc[][] board;
    private Disc currentTurn;
    private boolean isGameOver;
    private ArrayList<GameEventListener> eventListeners;

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
        this.players = new Player[2];
        this.currentTurn = Disc.BLACK;
        this.isGameOver = false;
        this.eventListeners = new ArrayList<>();
    }

    public boolean placeDisc(Player player, int horizontal, int vertical) {
        for (int i = 0; i < 2; i++) {
            if (players[i].equals(player)) {
                Disc disc = i == 0 ? Disc.BLACK : Disc.WHITE;
                if (currentTurn == disc &&
                        0 <= horizontal && horizontal < board.length && 0 <= vertical && vertical < board.length) {
                    board[horizontal][vertical] = currentTurn;
                    currentTurn = currentTurn == Disc.BLACK ? Disc.WHITE : Disc.BLACK;
                    for (GameEventListener listener : eventListeners) {
                        listener.boardUpdated(this);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean addPlayer(Player player) {
        if (isGameOver == false) {
            for (int i = 0; i < 2; i++) {
                if (players[i] == null) {
                    players[i] = player;
                    players[i].setGame(this);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removePlayer(Player player) {
        for (int i = 0; i < 2; i++) {
            if (players[i] != null && players[i].equals(player)) {
                players[i] = null;
                return true;
            }
        }
        return false;
    }

    public boolean addEventListener(GameEventListener eventListener) {
        if (eventListeners.contains(eventListener) == false) {
            eventListeners.add(eventListener);
            return true;
        }
        return false;
    }

    public boolean removeEventListener(GameEventListener eventListener) {
        return eventListeners.remove(eventListener);
    }

    public Disc[][] getBoard() {
        return board.clone();
    }

    public Player getCurrentTurnPlayer() {
        if (currentTurn == Disc.BLACK) {
            return players[0];
        } else {
            return players[1];
        }
    }
}
