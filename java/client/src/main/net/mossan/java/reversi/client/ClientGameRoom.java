package net.mossan.java.reversi.client;

import io.socket.client.Ack;
import io.socket.client.Socket;
import net.mossan.java.reversi.client.boarddrawer.BoardDrawer;
import net.mossan.java.reversi.client.boarddrawer.BoardDrawerType;
import net.mossan.java.reversi.client.boarddrawer.cui.ConsoleBoardDrawer;
import net.mossan.java.reversi.client.boarddrawer.gui.GUIBoardDrawer;
import net.mossan.java.reversi.client.model.GameStateHolder;
import net.mossan.java.reversi.common.message.EventType;
import net.mossan.java.reversi.common.message.request.CellSelect;
import net.mossan.java.reversi.common.message.request.SeatRequest;
import net.mossan.java.reversi.common.message.response.GameState;
import net.mossan.java.reversi.common.message.response.RequestReply;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Scanner;
import java.util.UUID;
import java.util.function.Supplier;

public class ClientGameRoom implements Runnable {
    private final Socket nameSpaceSocket;
    private final BoardDrawerType boardDrawerType;
    private final Supplier<Scanner> scannerSupplier;
    private final Object threadLock;

    private UUID uuid;
    private GameStateHolder gameStateHolder = null;
    private BoardDrawer boardDrawer = null;

    private ClientGameRoom(Socket nameSpaceSocket, BoardDrawerType boardDrawerType, Supplier<Scanner> scannerSupplier) {
        this.nameSpaceSocket = nameSpaceSocket;
        this.boardDrawerType = boardDrawerType;
        this.scannerSupplier = scannerSupplier;
        threadLock = new Object();
    }

    static void roomIn(Socket nameSpaceSocket, BoardDrawerType boardDrawerType, Supplier<Scanner> scannerSupplier) {
        Thread roomThread = new Thread(new ClientGameRoom(nameSpaceSocket, boardDrawerType, scannerSupplier));
        roomThread.start();
        try {
            roomThread.join();
        } catch (InterruptedException ignored) {
        }
    }

    public void run() {
        synchronized (this.threadLock) {
            this.nameSpaceSocket
                    .on(Socket.EVENT_CONNECT, args -> {
                        this.uuid = UUID.fromString(this.nameSpaceSocket.id().replaceFirst("^.*#", ""));
                        if (this.gameStateHolder == null) {
                            System.out.println("--- room connect and entered. ---");
                        } else {
                            System.out.println("--- room reconnected. ---");
                            this.requestGameState();
                        }
                    })
                    .on(EventType.GameState.toString(), args -> {
                        try {
                            JSONObject obj = new JSONObject((String) args[args.length - 1]);
                            GameState newState = new GameState(obj);
                            this.updateGameState(newState);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            this.exitRoom(true);
                        }
                    })
                    .on(Socket.EVENT_DISCONNECT, args -> System.out.println("--- room unexpectedly disconnected. ---"))
                    .on(Socket.EVENT_CONNECT_ERROR, args -> {
                        System.out.println("--- room connect error occurred. close board drawer. ---");
                        this.exitRoom(true);
                    })
                    .on(Socket.EVENT_RECONNECT_ERROR, args -> {
                        System.out.println("--- room reconnect error occurred. close board drawer. ---");
                        this.exitRoom(true);
                    })
                    .on(Socket.EVENT_RECONNECT_FAILED, args -> {
                        System.out.println("--- room reconnect failed. close board drawer. ---");
                        this.exitRoom(true);
                    });
            this.nameSpaceSocket.open();
            try {
                this.threadLock.wait();
                System.out.println("--- room leave and disconnected. ---");
            } catch (InterruptedException ignored) {
            }
        }
    }

    private synchronized void updateGameState(GameState state) {
        if (this.gameStateHolder == null) {
            Supplier<Void> onBoardDrawerClosedCallback = () -> {
                this.exitRoom(false);
                return null;
            };
            switch (this.boardDrawerType) {
                case GUI:
                    this.boardDrawer = new GUIBoardDrawer(64, nameSpaceSocket.toString(), this::requestSeat, onBoardDrawerClosedCallback);
                    break;
                case CUI:
                    this.boardDrawer = new ConsoleBoardDrawer(scannerSupplier, this::requestSeat, onBoardDrawerClosedCallback);
                    break;
            }
            this.gameStateHolder = new GameStateHolder(state);
        } else {
            this.gameStateHolder.update(state);
        }

        final boolean notifyMyTurn = this.gameStateHolder.getCurrentTurn() != null
                && state.playerUUIDs[this.gameStateHolder.getCurrentTurn().getInt()] != null
                && state.playerUUIDs[this.gameStateHolder.getCurrentTurn().getInt()].equals(this.uuid);

        this.boardDrawer.update(gameStateHolder);

        if (notifyMyTurn) {
            this.boardDrawer.notifyTurn(this.gameStateHolder, (placeableCell) -> {
                CellSelect cellSelect = new CellSelect(placeableCell.placePoint[0], placeableCell.placePoint[1]);
                try {
                    this.nameSpaceSocket.emit(
                            EventType.selectCell.toString(),
                            cellSelect.toJSONObject().toString(),
                            (Ack) args -> {
                                JSONObject json = new JSONObject((String) args[0]);
                                RequestReply requestReply = new RequestReply(json);
                                if (!requestReply.success && requestReply.detail != null) {
                                    System.err.println(requestReply.detail);
                                    this.requestGameState();
                                }
                            }
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void requestSeat(SeatRequest seatRequest) {
        nameSpaceSocket.emit(
                EventType.seating.toString(),
                seatRequest.toJSONObject().toString(),
                (Ack) args -> {
                    JSONObject json = new JSONObject((String) args[0]);
                    RequestReply requestReply = new RequestReply(json);
                    if (requestReply.success) {
                        boardDrawer.onSuccessSeatRequest(seatRequest);
                    } else if (requestReply.detail != null) {
                        System.err.println(requestReply.detail);
                    }
                }
        );
    }

    private void requestGameState() {
        nameSpaceSocket.emit(EventType.requestGameState.toString());
    }

    private synchronized void exitRoom(boolean requireBoardDrawerClosing) {
        if (this.gameStateHolder != null) {
            this.gameStateHolder = null;
            if (this.boardDrawer != null && requireBoardDrawerClosing) {
                this.boardDrawer.notifyLeavingRoom();
            }
            this.boardDrawer = null;
            synchronized (this.threadLock) {
                this.threadLock.notify();
            }
        }
    }
}
