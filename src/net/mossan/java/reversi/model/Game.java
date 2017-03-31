package net.mossan.java.reversi.model;

import net.mossan.java.reversi.model.eventlistener.GameEventListener;
import net.mossan.java.reversi.model.user.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private static final int[][] PLACEABLE_CELL_SEARCH_MOVE_PATTERNS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
    private final Player[] players;
    private final DiscType[][] board;
    private Player currentTurnPlayer;
    private boolean isGameOver;
    private ArrayList<GameEventListener> eventListeners;

    public Game(final int board_rows) {
        // Arguments Check
        assert board_rows > 0 && board_rows % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";

        // Initialize
        this.board = new DiscType[board_rows][board_rows];
        for (int i = 0; i < board_rows; i++) {
            for (int j = 0; j < board_rows; j++) {
                board[i][j] = null;
            }
        }
        for (int i = board_rows / 2 - 1; i <= board_rows / 2; i++) {
            int k = i % 2 == 1 ? 0 : 1;
            for (int j = board_rows / 2 - 1; j <= board_rows / 2; j++) {
                if (k % 2 == 0) {
                    board[i][j] = DiscType.WHITE;
                } else {
                    board[i][j] = DiscType.BLACK;
                }
                k++;
            }
        }
        this.players = new Player[]{new Player(DiscType.BLACK), new Player(DiscType.WHITE)};
        this.currentTurnPlayer = this.players[0];
        this.isGameOver = false;
        this.eventListeners = new ArrayList<>();
    }

    public List<List<int[]>> getCurrentPlayerPlaceableCellsList() {
        return getPlaceableCellsList(currentTurnPlayer);
    }

    private List<List<int[]>> getPlaceableCellsList(Player player) {
        if (isGameOver) {
            return new ArrayList<>(0);
        }

        Player anotherPlayer = player.equals(players[0]) ? players[1] : players[0];

        List<List<int[]>> placeableDiscs = new ArrayList<>(board.length * board.length);
        for (int base_h = 0; base_h < board.length; base_h++) {
            for (int base_v = 0; base_v < board.length; base_v++) {
                if (board[base_h][base_v] != null) {
                    continue;
                }
                List<int[]> basePlaceableDiscs = new ArrayList<>(board.length * board.length);
                basePlaceableDiscs.add(new int[]{base_h, base_v});
                for (int[] pattern : PLACEABLE_CELL_SEARCH_MOVE_PATTERNS) {
                    int h = base_h + pattern[0], v = base_v + pattern[1];
                    if (h < 0 || h >= board.length || v < 0 || v >= board.length || board[h][v] != anotherPlayer.type) {
                        continue;
                    }
                    List<int[]> tmpPlaceableDiscs = new ArrayList<>(board.length * board.length);
                    for (; h >= 0 && h < board.length && v >= 0 && v < board.length; h += pattern[0], v += pattern[1]) {
                        if (board[h][v] == anotherPlayer.type) {
                            tmpPlaceableDiscs.add(new int[]{h, v});
                        } else {
                            if (board[h][v] == null) {
                                tmpPlaceableDiscs.clear();
                            }
                            break;
                        }
                    }
                    if (h < 0 || h >= board.length || v < 0 || v >= board.length) {
                        tmpPlaceableDiscs.clear();
                    }
                    if (tmpPlaceableDiscs.size() > 0) {
                        basePlaceableDiscs.addAll(tmpPlaceableDiscs);
                    }
                }
                if (basePlaceableDiscs.size() > 1) {
                    placeableDiscs.add(basePlaceableDiscs);
                }
            }
        }
        return placeableDiscs;
    }

    public boolean placeDisc(User user, int horizontal, int vertical) {
        if (!determinePlayerFromUser(user).equals(currentTurnPlayer)) {
            return false;
        }

        List<List<int[]>> placeableCellsList = getPlaceableCellsList(currentTurnPlayer);
        for (List<int[]> placeableCells : placeableCellsList) {
            if (horizontal == placeableCells.get(0)[0] && vertical == placeableCells.get(0)[1]) {
                // Save current board
                DiscType[][] beforeBoard = getBoard();

                // Place Discs
                for (int[] disc_position : placeableCells) {
                    board[disc_position[0]][disc_position[1]] = currentTurnPlayer.type;
                }

                // Determine turn change and game over
                boolean isSkippedPlayer = false;
                Player anotherPlayer = currentTurnPlayer.equals(players[0]) ? players[1] : players[0];
                if (getPlaceableCellsList(anotherPlayer).size() > 0) {
                    currentTurnPlayer = anotherPlayer;
                } else if (getPlaceableCellsList(currentTurnPlayer).size() == 0) {
                    isGameOver = true;

                    // notify winner to event listeners
                    int[] discCount = new int[2];
                    for (int h = 0; h < board.length; h++) {
                        for (int v = 0; v < board.length; v++) {
                            if (board[h][v] == DiscType.BLACK) {
                                discCount[0]++;
                            } else if (board[h][v] == DiscType.WHITE) {
                                discCount[1]++;
                            }
                        }
                    }
                    Player winner = players[discCount[0] > discCount[1] ? 0 : 1];
                    for (GameEventListener gameEventListener : eventListeners) {
                        gameEventListener.notifyGameResult(this, beforeBoard, getBoard(), winner);
                    }
                } else {
                    isSkippedPlayer = true;
                }

                // Board update notify to event listeners
                for (GameEventListener listener : eventListeners) {
                    listener.boardUpdated(this, beforeBoard, getBoard(), currentTurnPlayer, isSkippedPlayer);
                }
                return true;
            }
        }
        return false;
    }

    public boolean addUser(User user) {
        if (!isGameOver) {
            for (int i = 0; i < 2; i++) {
                if (players[i].user.get() == null) {
                    players[i].user = new WeakReference<>(user);
                    user.setGame(this);
                    return true;
                }
            }
        }
        return false;
    }

    public void removeUser(User user) {
        for (int i = 0; i < 2; i++) {
            if (players[i].user.get() != null && players[i].user.get().equals(user)) {
                players[i].user.clear();
                return;
            }
        }
    }

    public void addEventListener(GameEventListener eventListener) {
        if (eventListeners.contains(eventListener) == false) {
            eventListeners.add(eventListener);
        }
    }

    public void removeEventListener(GameEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public DiscType[][] getBoard() {
        DiscType[][] cloneBoard = new DiscType[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, cloneBoard[i], 0, board.length);
        }

        return cloneBoard;
    }

    public Player getCurrentTurnPlayer() {
        return currentTurnPlayer;
    }

    private Player determinePlayerFromUser(User user) {
        Player player;
        if (user.equals(players[0].user.get())) {
            player = players[0];
        } else if (user.equals(players[1].user.get())) {
            player = players[1];
        } else {
            throw new AssertionError("Only user who operates \"player\" of this instance can be set as an argument.");
        }

        return player;
    }
}
