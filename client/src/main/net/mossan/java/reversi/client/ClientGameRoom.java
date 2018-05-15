package net.mossan.java.reversi.client;

import io.socket.client.Socket;
import net.mossan.java.reversi.client.boarddrawer.BoardDrawerBase;
import net.mossan.java.reversi.client.boarddrawer.BoardDrawerType;
import net.mossan.java.reversi.client.boarddrawer.ConsoleBoardDrawer;
import net.mossan.java.reversi.client.boarddrawer.GUIBoardDrawer;
import net.mossan.java.reversi.common.jsonExchange.GameState;
import net.mossan.java.reversi.common.jsonExchange.SeatReservation;
import net.mossan.java.reversi.common.jsonExchange.SelectCell;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Scanner;

public class ClientGameRoom {
    private final Socket nameSpaceSocket;
    private BoardDrawerBase boardDrawer;
    private Game game = null;

    private ClientGameRoom(Socket nameSpaceSocket, BoardDrawerType boardDrawerType, Scanner scanner) {
        this.nameSpaceSocket = nameSpaceSocket;
        switch (boardDrawerType) {
            case GUI:
                this.boardDrawer = new GUIBoardDrawer(64, this.nameSpaceSocket.toString());
                break;
            case CUI:
                this.boardDrawer = new ConsoleBoardDrawer(scanner);
                break;
        }

        this.nameSpaceSocket
                .on(Socket.EVENT_CONNECT, args -> System.out.println("*** Room Entered. ***"))
                .on("GameState", args -> {
                    try {
                        JSONObject obj = new JSONObject((String) args[args.length - 1]);
                        GameState newState = new GameState(obj);
                        this.updateState(newState);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        this.notifyAll();
                    }
                });
        this.nameSpaceSocket.open();
    }

    public static void executeAndWait(Socket nameSpaceSocket, BoardDrawerType boardDrawerType, Scanner scanner) {
        final ClientGameRoom instance = new ClientGameRoom(nameSpaceSocket, boardDrawerType, scanner);
        synchronized (instance.boardDrawer) {
            try {
                instance.boardDrawer.wait();
            } catch (InterruptedException ignore) {
            }
        }
    }

    private void updateState(GameState state) {
        if (this.game == null) {
            this.game = new Game(state);
            this.boardDrawer.boardUpdated(this.game);
            this.nameSpaceSocket.emit("getSeat", new SeatReservation(DiscType.BLACK).toJSONObject().toString());
        } else {
            this.game.updateFromState(state);
            this.boardDrawer.boardUpdated(this.game);
            if (state.yourTurn != null && state.yourTurn) {
                this.boardDrawer.notifyTurn(this.game, (placeableCell) -> {
                    SelectCell selectCell = new SelectCell(placeableCell.placePoint[0], placeableCell.placePoint[1]);
                    try {
                        this.nameSpaceSocket.emit("SelectCell", selectCell.toJSONObject().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
