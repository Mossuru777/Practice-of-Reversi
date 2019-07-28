package net.mossan.java.reversi.common.model;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class Game {
    private static final int[][] PLACEABLE_CELL_SEARCH_MOVE_PATTERNS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
    protected DiscType[][] board;
    protected @Nullable DiscType currentTurn;
    protected @Nullable DiscType winner;

    public int getBoardRows() {
        return this.board.length;
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

    public @Nullable DiscType getCurrentTurn() {
        return this.currentTurn;
    }

    public @Nullable DiscType getWinner() {
        return this.winner;
    }
}
