package net.mossan.java.reversi.client.boarddrawer.cui;

import net.mossan.java.reversi.client.boarddrawer.BoardDrawer;
import net.mossan.java.reversi.client.model.GameStateHolder;
import net.mossan.java.reversi.common.message.request.SeatRequest;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;
import net.mossan.java.reversi.common.model.PlayerType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsoleBoardDrawer extends BoardDrawer {
    private static Map<@Nullable DiscType, String> DiscMarkString = Collections.unmodifiableMap(new HashMap<@Nullable DiscType, String>() {
        {
            put(null, "　");
            put(DiscType.Black, "●");
            put(DiscType.White, "○");
        }
    });
    private final Supplier<Scanner> scannerSupplier;
    private @Nullable Thread interruptableScannerThread = null;

    public ConsoleBoardDrawer(Supplier<Scanner> scannerSupplier, Consumer<SeatRequest> seatRequestConsumer, Supplier<Void> onBoardDrawerClosedCallback) {
        super(seatRequestConsumer, onBoardDrawerClosedCallback);

        this.scannerSupplier = scannerSupplier;
    }

    private void draw() {
        if (this.interruptableScannerThread != null && !interruptableScannerThread.isInterrupted()) {
            this.interruptableScannerThread.interrupt();
            this.interruptableScannerThread = null;
            System.out.println();
        }

        final GameStateHolder gameStateHolder = gameStateHolderWeakReference.get();
        assert gameStateHolder != null;

        // output after changed board to console
        outputBoardToConsole(gameStateHolder.getBoard());

        // output turn change or game over message to console
        final DiscType currentTurn = gameStateHolder.getCurrentTurn();
        if (currentTurn != null) {
            String turnPlayerName = currentTurn == this.myDiscType ? "You" : gameStateHolder.seatedPlayerNames[currentTurn.getInt()];
            if (turnPlayerName != null) {
                System.out.println(String.format("Next Turn: %s (%s) - %s", currentTurn.name(), DiscMarkString.get(currentTurn), turnPlayerName));
            } else {
                System.out.println(String.format("Next Turn: %s (%s)", currentTurn.name(), DiscMarkString.get(currentTurn)));
            }

            if (currentTurn != this.myDiscType) {
                this.outputPlayerSeatSelectMenuToConsole(currentTurn, gameStateHolder);
            }
        } else {
            assert gameStateHolder.getWinner() != null : "Winner decided, but null detected!";
            final DiscType winner = gameStateHolder.getWinner();

            System.out.println("*** Game Over ***");

            String winnerPlayerName = gameStateHolder.seatedPlayerNames[winner.getInt()];
            if (winnerPlayerName != null) {
                System.out.println(String.format("Winner: %s (%s) - %s", winner.name(), DiscMarkString.get(winner), winnerPlayerName));
            } else {
                System.out.println(String.format("Winner: %s (%s)", winner.name(), DiscMarkString.get(winner)));
            }

            System.out.println();
            System.out.print("Press enter to back to menu... ");
            try {
                this.executeInInterruptableScannerThread(Scanner::nextLine).join();
                this.notifyLeavingRoom();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void outputPlayerSeatSelectMenuToConsole(DiscType currentTurn, GameStateHolder gameStateHolder) {
        PlayerType[][] seatAvailabilities = gameStateHolder.seatAvailabilities;
        if (seatAvailabilities == null) {
            return;
        }

        Stream<PlayerType> availablePlayerType = Arrays.stream(seatAvailabilities[currentTurn.getInt()]);
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
                        DiscMarkString.get(currentTurn)
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
                        seatRequestConsumer.accept(seatRequests.get(select - 1));
                        break;
                    }
                } catch (NoSuchElementException | IllegalStateException ignore) {
                    break;
                }
            }
        });
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
                System.out.print(DiscMarkString.get(board[h][v]));
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

    private Thread executeInInterruptableScannerThread(Consumer<Scanner> scannerConsumer) {
        if (this.interruptableScannerThread != null) {
            this.interruptableScannerThread.interrupt();
            this.interruptableScannerThread = null;
        }

        final Scanner scanner = scannerSupplier.get();
        this.interruptableScannerThread = new Thread(() -> scannerConsumer.accept(scanner));
        this.interruptableScannerThread.start();
        return this.interruptableScannerThread;
    }

    // Override BoardDrawer Methods
    @Override
    public void update(GameStateHolder gameStateHolder) {
        super.update(gameStateHolder);
        this.draw();
    }

    @Override
    public void onSuccessSeatRequest(SeatRequest seatRequest) {
        super.onSuccessSeatRequest(seatRequest);
        System.out.println();
        System.out.println(String.format(
                "*** %s (%s) Seated: %s ***",
                seatRequest.discType.toString(),
                DiscMarkString.get(seatRequest.discType),
                seatRequest.playerType == PlayerType.NetworkPlayer ? "You" : seatRequest.playerType.toString()
        ));
    }

    @Override
    public void notifyLeavingRoom() {
        if (this.interruptableScannerThread != null) {
            this.interruptableScannerThread.interrupt();
        }
        this.onBoardDrawerClosedCallback.get();
    }
}
