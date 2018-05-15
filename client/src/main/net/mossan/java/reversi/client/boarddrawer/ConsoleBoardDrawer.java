package net.mossan.java.reversi.client.boarddrawer;

import net.mossan.java.reversi.client.ClientGameRoom;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.eventlistener.PlaceableCell;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

public class ConsoleBoardDrawer implements BoardDrawerBase {
    private final Scanner scanner;
    private final ClientGameRoom clientGameRoom;

    private DiscType[][] currentBoard = null;

    public ConsoleBoardDrawer(Scanner scanner, ClientGameRoom clientGameRoom) {
        this.scanner = scanner;
        this.clientGameRoom = clientGameRoom;
    }

    private static String getDiscMarkString(@Nullable DiscType discType) {
        return discType == null ? "　" : discType == DiscType.BLACK ? "●" : "○";
    }

    private void outputBoardToConsole(DiscType[][] board) {
        // horizontal num
        System.out.print("  |");
        for (int i = 0; i < board.length; i++) {
            System.out.printf("%2d|", i);
        }
        System.out.print("\n");

        // top horizontal bars
        System.out.print("  -");
        for (int i = 0; i < board.length; i++) {
            System.out.print("－-");
        }
        System.out.print("\n");

        // vertical
        for (int v = 0; v < board.length; v++) {
            System.out.printf("%2d|", v);

            // horizontal (cell)
            for (int h = 0; h < board.length; h++) {
                System.out.print(getDiscMarkString(board[h][v]));
                System.out.print("|");
            }
            System.out.print("\n");

            // vertical(row) split bars
            System.out.print("  -");
            for (int i = 0; i < board.length; i++) {
                System.out.print("－-");
            }
            System.out.print("\n");
        }
    }

    // BoardDrawerBase
    @Override
    public void boardUpdated(Game game) {
        final DiscType[][] board = game.getBoard();
        if (this.currentBoard != null && Arrays.deepEquals(board, this.currentBoard)) return;

        // output after changed board to console
        outputBoardToConsole(board);

        // output turn change or game over message to console
        if (game.getCurrentTurn() != null) {
            final DiscType currentTurn = game.getCurrentTurn();
            System.out.println(String.format("Next Turn: %s (%s)", currentTurn.name(), getDiscMarkString(currentTurn)));
        } else {
            assert game.getWinner() != null : "Winner decided, but null detected!";
            final DiscType winner = game.getWinner();

            System.out.println("*** Game Over ***");
            System.out.println(String.format("Winner: %s (%s)", winner.name(), getDiscMarkString(winner)));
            System.out.println();
            System.out.print("Press enter to back to menu... ");
            this.scanner.nextLine();

            synchronized (this.clientGameRoom) {
                this.clientGameRoom.notifyAll();
            }
        }

        this.currentBoard = board;
    }

    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        System.out.println("*** YOUR TURN ***");

        final List<PlaceableCell> placeableCellList = game.getPlaceableCellsList(game.getCurrentTurn());
        while (true) {
            System.out.println("[Candidate ([Num] Horizontal - Vertical -> Reverse cell num)]");
            for (int i = 0; i < placeableCellList.size(); ++i) {
                PlaceableCell candidate = placeableCellList.get(i);
                System.out.printf("[%d] %d - %d -> %d\n",
                        i + 1, candidate.placePoint[0], candidate.placePoint[1], candidate.reversiblePoints.size());
            }

            System.out.print("Place point number? > ");
            try {
                int select = this.scanner.nextInt();
                this.scanner.nextLine();
                if (select >= 1 && select <= placeableCellList.size()) {
                    placeCell.accept(placeableCellList.get(select - 1));
                    break;
                }
            } catch (NoSuchElementException | IllegalStateException ignore) {
                break;
            }
        }
    }
}