package net.mossan.java.reversi.client.boarddrawer;

import net.mossan.java.reversi.common.jsonExchange.SeatRequest;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlayerType;
import net.mossan.java.reversi.common.model.eventlistener.PlaceableCell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsoleBoardDrawer implements BoardDrawer {
    private final Supplier<Scanner> scannerSupplier;
    private final Consumer<SeatRequest> seatRequestConsumer;

    private DiscType[][] currentBoard = null;
    private String[] seatedPlayerNames = null;
    private PlayerType[][] seatAvailabilities = null;
    private @Nullable Thread interruptableScannerThread = null;

    private @Nullable DiscType myDiscType = null;

    public ConsoleBoardDrawer(Supplier<Scanner> scannerSupplier, Consumer<SeatRequest> seatRequestConsumer) {
        this.scannerSupplier = scannerSupplier;
        this.seatRequestConsumer = seatRequestConsumer;
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

    private void outputPlayerSeatSelectMenuToConsole(@NotNull DiscType currentTurn) {
        while (this.seatAvailabilities == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
        }

        Stream<PlayerType> availablePlayerType = Arrays.stream(this.seatAvailabilities[currentTurn.getInt()]);
        if (this.myDiscType != null) {
            availablePlayerType = availablePlayerType.filter(playerType -> playerType != PlayerType.NetworkPlayer);
        }

        List<SeatRequest> seatRequests =
                availablePlayerType
                        .map(type -> new SeatRequest(currentTurn, type))
                        .collect(Collectors.toList());
        if (seatRequests.size() == 0) return;

        this.executeInInterruptableScannerThread(scanner -> {
            while (true) {
                System.out.println(String.format(
                        "### %s (%s) - Available Seat List ###",
                        currentTurn.toString(),
                        getDiscMarkString(currentTurn)
                ));

                int itemCount = 1;
                for (SeatRequest request : seatRequests) {
                    if (request.playerType == PlayerType.NetworkPlayer) {
                        System.out.println(String.format("   %d: You", itemCount));
                    } else {
                        System.out.println(String.format("   %d: %s", itemCount, request.playerType.toString()));
                    }
                    ++itemCount;
                }

                System.out.print("select seat player > ");
                try {
                    int select = scanner.nextInt();
                    scanner.nextLine();
                    if (select >= 1 && select <= seatRequests.size()) {
                        this.seatRequestConsumer.accept(seatRequests.get(select - 1));
                        break;
                    }
                } catch (NoSuchElementException | IllegalStateException ignore) {
                    break;
                }
            }
        });
    }

    // ObserverEventListener (inherit from BoardDrawer)
    @Override
    public void boardUpdated(Game game) {
        if (this.interruptableScannerThread != null && !this.interruptableScannerThread.isInterrupted()) {
            this.interruptableScannerThread.interrupt();
            this.interruptableScannerThread = null;
            System.out.println();
        }

        final DiscType[][] board = game.getBoard();
        if (this.currentBoard != null && Arrays.deepEquals(board, this.currentBoard)) return;

        // output after changed board to console
        this.outputBoardToConsole(board);

        // output turn change or game over message to console
        final DiscType currentTurn = game.getCurrentTurn();
        if (currentTurn != null) {
            String turnPlayerName = currentTurn == this.myDiscType ? "You" : this.seatedPlayerNames[currentTurn.getInt()];
            if (turnPlayerName != null) {
                System.out.println(String.format("Next Turn: %s (%s) - %s", currentTurn.name(), getDiscMarkString(currentTurn), turnPlayerName));
            } else {
                System.out.println(String.format("Next Turn: %s (%s)", currentTurn.name(), getDiscMarkString(currentTurn)));
            }

            if (this.myDiscType != currentTurn) {
                this.outputPlayerSeatSelectMenuToConsole(currentTurn);
            }
        } else {
            assert game.getWinner() != null : "Winner decided, but null detected!";
            final DiscType winner = game.getWinner();

            System.out.println("*** Game Over ***");

            String winnerPlayerName = this.seatedPlayerNames[winner.getInt()];
            if (winnerPlayerName != null) {
                System.out.println(String.format("Winner: %s (%s) - %s", winner.name(), getDiscMarkString(winner), winnerPlayerName));
            } else {
                System.out.println(String.format("Winner: %s (%s)", winner.name(), getDiscMarkString(winner)));
            }

            System.out.println();
            System.out.print("Press enter to back to menu... ");
            this.executeInInterruptableScannerThread(scanner -> {
                scanner.nextLine();
                synchronized (this) {
                    this.notifyAll();
                }
            });
        }

        this.currentBoard = board;
    }

    // PlayerEventListener (inherit from BoardDrawer)
    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        if (this.interruptableScannerThread != null) {
            this.interruptableScannerThread.interrupt();
            this.interruptableScannerThread = null;
            System.out.println();
        }

        final List<PlaceableCell> placeableCellList = game.getPlaceableCellsList(game.getCurrentTurn());
        this.executeInInterruptableScannerThread(scanner -> {
            while (true) {
                System.out.println("*** YOUR TURN ***");
                System.out.println("[Candidate ([Num] Horizontal - Vertical -> Reverse cell num)]");
                for (int i = 0; i < placeableCellList.size(); ++i) {
                    PlaceableCell candidate = placeableCellList.get(i);
                    System.out.printf("[%d] %d - %d -> %d\n",
                            i + 1, candidate.placePoint[0], candidate.placePoint[1], candidate.reversiblePoints.size());
                }

                System.out.print("Place point number? > ");
                try {
                    int select = scanner.nextInt();
//                    scanner.nextLine();
                    if (select >= 1 && select <= placeableCellList.size()) {
                        placeCell.accept(placeableCellList.get(select - 1));
                        break;
                    }
                } catch (NoSuchElementException | IllegalStateException | NumberFormatException ignore) {
                    break;
                }
            }
        });
    }

    private void executeInInterruptableScannerThread(Consumer<Scanner> scannerConsumer) {
        if (this.interruptableScannerThread != null) {
            this.interruptableScannerThread.interrupt();
            this.interruptableScannerThread = null;
        }

        final Scanner scanner = this.scannerSupplier.get();
        this.interruptableScannerThread = new Thread(() -> scannerConsumer.accept(scanner));
        this.interruptableScannerThread.start();
    }

    // RoomEventListener (inherit from BoardDrawer)
    @Override
    public void onSuccessSeatRequest(SeatRequest seatRequest) {
        if (seatRequest.playerType == PlayerType.NetworkPlayer) {
            this.myDiscType = seatRequest.discType;
        }
        System.out.println();
        System.out.println(String.format(
                "*** %s (%s) Seated: %s ***",
                seatRequest.discType.toString(),
                getDiscMarkString(seatRequest.discType),
                seatRequest.playerType == PlayerType.NetworkPlayer ? "You" : seatRequest.playerType.toString()
        ));
    }

    @Override
    public void seatStatusUpdated(UUID[] seatedPlayerUUIDs, String[] seatedPlayerNames, PlayerType[][] seatAvailabilities) {
        this.seatedPlayerNames = seatedPlayerNames;
        this.seatAvailabilities = seatAvailabilities;
    }
}
