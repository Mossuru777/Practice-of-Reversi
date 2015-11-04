package net.mossan.java.reversi.model;

import net.mossan.java.reversi.model.eventlistener.GameEventListener;
import net.mossan.java.reversi.model.player.Player;

import java.util.ArrayList;

public class Game {
    private static final int[][] PLACEABLE_CELL_SEARCH_MOVE_PATTERNS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
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

    public int[][] getPlaceableDiscs(Player player, int horizontal, int vertical) {
        if (isGameOver || board[horizontal][vertical] != Disc.NONE) {
            return new int[0][0];
        }

        Disc anotherDisc = null;
        for (int i = 0; i < 2; i++) {
            if (players[i].equals(player)) {
                Disc disc = i == 0 ? Disc.BLACK : Disc.WHITE;
                if (disc == currentTurn) {
                    anotherDisc = currentTurn == Disc.BLACK ? Disc.WHITE : Disc.BLACK;
                }
                break;
            }
        }
        if (anotherDisc == null) {
            return new int[0][0];
        }

        ArrayList<int[]> placeableDiscs = new ArrayList<>(board.length * board.length);
        ArrayList<int[]> tmpPlaceableDiscs = new ArrayList<>(board.length * board.length);
        for (int[] pattern : PLACEABLE_CELL_SEARCH_MOVE_PATTERNS) {
            int h = horizontal + pattern[0], v = vertical + pattern[1];
            if (h < 0 || h >= board.length || v < 0 || v >= board.length || board[h][v] != anotherDisc) {
                continue;
            }
            tmpPlaceableDiscs.clear();
            for (; h >= 0 && h < board.length && v >= 0 && v < board.length; h += pattern[0], v += pattern[1]) {
                if (board[h][v] == anotherDisc) {
                    tmpPlaceableDiscs.add(new int[]{h, v});
                } else {
                    if (board[h][v] != currentTurn) {
                        tmpPlaceableDiscs.clear();
                    }
                    break;
                }
            }
            if (h < 0 || h >= board.length || v < 0 || v >= board.length) {
                tmpPlaceableDiscs.clear();
            }
            placeableDiscs.addAll(tmpPlaceableDiscs);
        }
        return placeableDiscs.toArray(new int[0][0]);
    }

    public boolean placeDisc(Player player, int horizontal, int vertical) {
        int[][] placeableDiscs = getPlaceableDiscs(player, horizontal, vertical);
        if (placeableDiscs.length > 0) {
            //DEBUG output before board to console
            System.out.println("Before:");
            outputBoardToConsole();

            // Place Discs
            board[horizontal][vertical] = currentTurn;
            for (int[] disc_position : placeableDiscs) {
                board[disc_position[0]][disc_position[1]] = currentTurn;
            }

            //DEBUG output after changed board to console
            System.out.println("After:");
            outputBoardToConsole();

            // Change Turn
            boolean turnChanged = false;
            for (int h = 0; h < board.length; h++) {
                for (int v = 0; v < board.length; v++) {
                    if (board[h][v] != Disc.NONE) {
                        continue;
                    } else if (getPlaceableDiscs(player, h, v).length > 0) {
                        turnChanged = true;
                        break;
                    }
                }
            }
            if (turnChanged) {
                currentTurn = currentTurn == Disc.BLACK ? Disc.WHITE : Disc.BLACK;
            }

            // Board update notify to eventlisteners
            for (GameEventListener listener : eventListeners) {
                listener.boardUpdated(this);
            }
            return true;
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

    //DEBUG
    private void outputBoardToConsole() {
        System.out.print("-");
        for (int i = 0; i < board.length; i++) {
            System.out.print("－-");
        }
        System.out.print("\n");
        for (int v = 0; v < board.length; v++) {
            System.out.print("|");
            for (int h = 0; h < board.length; h++) {
                switch (board[h][v]) {
                    case NONE:
                        System.out.print("　");
                        break;
                    case BLACK:
                        System.out.print("○");
                        break;
                    case WHITE:
                        System.out.print("●");
                        break;
                }
                System.out.print("|");
            }
            System.out.print("\n");
            System.out.print("-");
            for (int i = 0; i < board.length; i++) {
                System.out.print("－-");
            }
            System.out.print("\n");
        }
    }
}
