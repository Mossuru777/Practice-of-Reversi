package net.mossan.java.reversi.client;

import io.socket.client.Ack;
import io.socket.client.Socket;
import net.mossan.java.reversi.client.boarddrawer.BoardDrawer;
import net.mossan.java.reversi.client.boarddrawer.BoardDrawerType;
import net.mossan.java.reversi.client.boarddrawer.cui.ConsoleBoardDrawer;
import net.mossan.java.reversi.client.boarddrawer.gui.GUIBoardDrawer;
import net.mossan.java.reversi.common.message.EventType;
import net.mossan.java.reversi.common.message.request.SeatRequest;
import net.mossan.java.reversi.common.message.request.CellSelect;
import net.mossan.java.reversi.common.message.response.GameState;
import net.mossan.java.reversi.common.message.response.RequestReply;
import net.mossan.java.reversi.common.model.Game;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Scanner;
import java.util.UUID;
import java.util.function.Supplier;

class ClientGameRoom {
    private final Socket nameSpaceSocket;
    private final BoardDrawerType boardDrawerType;
    private final Supplier<Scanner> scannerSupplier;

    private UUID uuid;
    private Game game = null;
    private BoardDrawer boardDrawer = null;

    private ClientGameRoom(Socket nameSpaceSocket, BoardDrawerType boardDrawerType, Supplier<Scanner> scannerSupplier) {
        this.nameSpaceSocket = nameSpaceSocket;
        this.boardDrawerType = boardDrawerType;
        this.scannerSupplier = scannerSupplier;

        this.nameSpaceSocket
                .on(Socket.EVENT_CONNECT, args -> {
                    this.uuid = UUID.fromString(this.nameSpaceSocket.id().replaceFirst("^.*#", ""));
                    if (this.game == null) {
                        System.out.println("*** Room Entered. ***");
                    } else {
                        System.out.println("*** Room Re-Entered. ***");
                        this.requestGameState();
                    }
                })
                .on(EventType.GameState.toString(), args -> {
                    try {
                        JSONObject obj = new JSONObject((String) args[args.length - 1]);
                        GameState newState = new GameState(obj);
                        this.updateState(newState);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        this.exitRoom();
                    }
                })
                .on(Socket.EVENT_DISCONNECT, args -> System.out.println("*** Room Leaved. ***"))
                .on(Socket.EVENT_CONNECT_ERROR, args -> this.exitRoom())
                .on(Socket.EVENT_RECONNECT_ERROR, args -> this.exitRoom())
                .on(Socket.EVENT_RECONNECT_FAILED, args -> this.exitRoom());
        this.nameSpaceSocket.open();
    }

    static void executeAndWait(Socket nameSpaceSocket, BoardDrawerType boardDrawerType, Supplier<Scanner> scannerSupplier) {
        final ClientGameRoom instance = new ClientGameRoom(nameSpaceSocket, boardDrawerType, scannerSupplier);
        try {
            synchronized (instance) {
                instance.wait();
            }
            if (instance.boardDrawer != null) {
                synchronized (instance.boardDrawer) {
                    instance.boardDrawer.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        instance.nameSpaceSocket.close();
        while (instance.nameSpaceSocket.connected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
        }
    }

    private void updateState(GameState state) {
        if (this.game == null) {
            switch (boardDrawerType) {
                case GUI:
                    this.boardDrawer = new GUIBoardDrawer(64, this.nameSpaceSocket.toString(), this::requestSeat);
                case CUI:
                    this.boardDrawer = new ConsoleBoardDrawer(this.scannerSupplier, this::requestSeat);
            }
            this.game = new Game(state, this.boardDrawer);
        } else {
            this.game.updateFromState(state);
        }

        final boolean myTurn =
                this.game.getCurrentTurn() != null
                        && state.playerUUIDs[this.game.getCurrentTurn().getInt()] != null
                        && state.playerUUIDs[this.game.getCurrentTurn().getInt()].equals(this.uuid);
        synchronized (this.boardDrawer) {
            this.boardDrawer.seatStatusUpdated(state.playerUUIDs, state.playerNames, state.seatAvailabilities);
            this.boardDrawer.boardUpdated(this.game);
            if (myTurn) {
                this.boardDrawer.notifyTurn(this.game, (placeableCell) -> {
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

        synchronized (this) {
            this.notifyAll();
        }
    }

    private void requestSeat(SeatRequest seatRequest) {
        this.nameSpaceSocket.emit(
                EventType.seating.toString(),
                seatRequest.toJSONObject().toString(),
                (Ack) args -> {
                    JSONObject json = new JSONObject((String) args[0]);
                    RequestReply requestReply = new RequestReply(json);
                    if (requestReply.success) {
                        this.boardDrawer.onSuccessSeatRequest(seatRequest);
                    } else if (requestReply.detail != null) {
                        System.err.println(requestReply.detail);
                    }
                }
        );
    }

    private void requestGameState() {
        this.nameSpaceSocket.emit(EventType.requestGameState.toString());
    }

    private void exitRoom() {
        synchronized (this.boardDrawer) {
            this.boardDrawer.notifyAll();
        }
    }
}
