package net.mossan.java.reversi.common.model;

import net.mossan.java.reversi.common.jsonExchange.GameState;
import net.mossan.java.reversi.common.jsonExchange.SelectCell;
import net.mossan.java.reversi.common.model.eventlistener.PlaceableCell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private static final int[][] PLACEABLE_CELL_SEARCH_MOVE_PATTERNS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
    private final Runnable serverTurnEventLoopFunc;
    private DiscType[][] board;
    private @Nullable DiscType currentTurn;
    private @Nullable DiscType winner;

    // For server
    public Game(final int board_rows, Runnable serverTurnEventLoopFunc) {
        // Arguments Check
        assert board_rows > 0 && board_rows % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";

        // Initialize
        this.serverTurnEventLoopFunc = serverTurnEventLoopFunc;
        this.board = new DiscType[board_rows][board_rows];
        this.resetBoard();
    }

    // For client
    public Game(@NotNull GameState state) {
        this.serverTurnEventLoopFunc = null;
        this.updateFromState(state);
    }

    public int getBoardRows() {
        return this.board.length;
    }

    public void updateFromState(@NotNull GameState state) {
        assert this.serverTurnEventLoopFunc == null : "Server can't call this.";

        // Parameters Check
        assert state.board.length > 0 && state.board.length % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";
        assert state.board.length == state.board[0].length : "board is not square.";
        if (state.turn != null) {
            assert state.winner == null : "There must be no winners in games that are someone's turn.";
        } else {
            assert state.winner != null : "Nobody turn the game must have a winner.";
        }

        // Update state
        this.board = state.board;
        this.currentTurn = state.turn;
        this.winner = state.winner;
    }

    private void resetBoard() {
        assert this.serverTurnEventLoopFunc != null : "Client can't reset board.";

        for (int i = 0; i < this.getBoardRows(); i++) {
            for (int j = 0; j < this.getBoardRows(); j++) {
                board[i][j] = null;
            }
        }
        for (int i = this.getBoardRows() / 2 - 1; i <= this.getBoardRows() / 2; i++) {
            DiscType placeDiscType = i % 2 == 1 ? DiscType.BLACK : DiscType.WHITE;
            for (int j = this.getBoardRows() / 2 - 1; j <= this.getBoardRows() / 2; j++) {
                board[i][j] = placeDiscType;
                placeDiscType = placeDiscType.otherDiscType();
            }
        }
        currentTurn = DiscType.BLACK;
        winner = null;
    }

    public DiscType[][] getBoard() {
        DiscType[][] returnBoard = new DiscType[this.getBoardRows()][this.getBoardRows()];
        for (int i = 0; i < this.getBoardRows(); i++) {
            System.arraycopy(board[i], 0, returnBoard[i], 0, this.getBoardRows());
        }
        return returnBoard;
    }

    public List<PlaceableCell> getPlaceableCellsList(DiscType discType) {
        if (this.winner != null) {
            return new ArrayList<>();
        }

        DiscType anotherDiscType = discType.otherDiscType();

        // 全ての空マスに対して石を置けるかチェック
        List<PlaceableCell> placeableCells = new ArrayList<>(this.getBoardRows() * this.getBoardRows());
        for (int base_h = 0; base_h < this.getBoardRows(); base_h++) {
            for (int base_v = 0; base_v < this.getBoardRows(); base_v++) {
                // すでに石が置いてある箇所はスキップ
                if (board[base_h][base_v] != null) {
                    continue;
                }

                // PLACEABLE_CELL_SEARCH_MOVE_PATTERNSの全方向に対して反転石をチェック
                int[] placePoint = new int[]{base_h, base_v}; // 石置位置を追加
                List<int[]> reversiblePoints = new ArrayList<>(this.getBoardRows() * this.getBoardRows());
                for (int[] pattern : PLACEABLE_CELL_SEARCH_MOVE_PATTERNS) {
                    // 石置位置からパターンに従い位置をオフセット
                    int h = base_h + pattern[0], v = base_v + pattern[1];
                    if (h < 0 || h >= this.getBoardRows() || v < 0 || v >= this.getBoardRows() || board[h][v] != anotherDiscType) {
                        continue;
                    }

                    // オフセット位置からその方向に対して1マスずつ進めて反転できる石を探索
                    List<int[]> directionReversiblePoints = new ArrayList<>(this.getBoardRows() * this.getBoardRows());
                    for (; h >= 0 && h < this.getBoardRows() && v >= 0 && v < this.getBoardRows(); h += pattern[0], v += pattern[1]) {
                        if (board[h][v] == anotherDiscType) {
                            // 別色なので反転できる (位置追加)
                            directionReversiblePoints.add(new int[]{h, v});
                        } else {
                            // 同色or空にたどり着いた
                            // (同色の場合は反転できるがそれ以上は探索しない)
                            if (board[h][v] == null) {
                                // 空にたどり着いたので反転できない
                                directionReversiblePoints.clear();
                            }
                            break;
                        }
                    }
                    if (h < 0 || h >= this.getBoardRows() || v < 0 || v >= this.getBoardRows()) {
                        // 盤面端にたどり着いた場合も反転できない
                        directionReversiblePoints.clear();
                    }

                    // 反転できる場合は対象として追加
                    if (directionReversiblePoints.size() > 0) {
                        reversiblePoints.addAll(directionReversiblePoints);
                    }
                }

                // 反転できる場合 (石置位置以外に反転石がある場合)、候補リストに追加
                if (reversiblePoints.size() > 0) {
                    placeableCells.add(new PlaceableCell(placePoint, reversiblePoints));
                }
            }
        }
        return placeableCells;
    }

    public void placeCell(PlaceableCell placeCell) {
        assert this.serverTurnEventLoopFunc != null : "Client can't place disc.";
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
                if (discCount[DiscType.BLACK.getInt()] > discCount[DiscType.WHITE.getInt()]) {
                    winner = DiscType.BLACK;
                } else {
                    winner = DiscType.WHITE;
                }
            }
        });

        new Thread(this.serverTurnEventLoopFunc).start();
    }

    public void placeCell(SelectCell selectCell) {
        this.getPlaceableCellsList(this.currentTurn)
                .stream()
                .filter(placeableCell ->
                        placeableCell.placePoint[0] == selectCell.horizontal
                                && placeableCell.placePoint[1] == selectCell.vertical)
                .findFirst().ifPresent(this::placeCell);
    }

    public @Nullable DiscType getCurrentTurn() {
        return this.currentTurn;
    }

    public @Nullable DiscType getWinner() {
        return this.winner;
    }
}
