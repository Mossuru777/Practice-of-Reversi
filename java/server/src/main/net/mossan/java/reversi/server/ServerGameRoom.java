package net.mossan.java.reversi.server;

import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import net.mossan.java.reversi.common.exception.NoEmptySeatException;
import net.mossan.java.reversi.common.jsonExchange.GameState;
import net.mossan.java.reversi.common.jsonExchange.SeatReservation;
import net.mossan.java.reversi.common.jsonExchange.SelectCell;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.eventlistener.ObserverEventListener;
import net.mossan.java.reversi.server.model.player.LongestPlaceCPU;
import net.mossan.java.reversi.server.model.player.NetworkPlayer;
import net.mossan.java.reversi.server.model.player.ServerPlayer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

class ServerGameRoom implements ObserverEventListener {
    final UUID uuid;
    private final SocketIONamespace nameSpace;
    private final Game game;
    private final Map<UUID, ServerPlayer> playersMap = new HashMap<>();
    private final ServerPlayer[] players = new ServerPlayer[2];

    ServerGameRoom(int board_rows, SocketIOServer socket) {
        this.game = new Game(board_rows, this::onChangeTurn);

        // DEBUG
        this.players[DiscType.WHITE.getInt()] = new LongestPlaceCPU();

        while (true) {
            UUID uuid = UUID.randomUUID();
            String nameSpaceStr = String.format("/%s", uuid.toString());
            if (socket.getNamespace(nameSpaceStr) != null) {
                continue;
            }
            this.uuid = uuid;
            this.nameSpace = socket.addNamespace(nameSpaceStr);
            break;
        }

        this.nameSpace.addConnectListener(client -> {
            System.out.println(String.format("*** Enter (%s): %s ***", this.nameSpace.getName(), client.getSessionId()));
            NetworkPlayer networkPlayer = new NetworkPlayer(client, this::getSeatedPlayerUUIDs);
            this.playersMap.put(client.getSessionId(), networkPlayer);
            this.sendGameState(client);
        });

        this.nameSpace.addDisconnectListener(client -> {
            System.out.println(String.format("*** Leave (%s): %s ***", this.nameSpace.getName(), client.getSessionId()));
            NetworkPlayer disconnectPlayer = (NetworkPlayer) this.playersMap.get(client.getSessionId());
            this.playersMap.remove(client.getSessionId());
            this.leaveSeat(disconnectPlayer);
        });

        this.nameSpace.addEventListener("getSeat", JSONObject.class, (client, data, ackSender) -> {
            SeatReservation reservation = new SeatReservation(data);
            NetworkPlayer player = (NetworkPlayer) this.playersMap.get(client.getSessionId());
            try {
                this.getSeat(player, reservation.discType);
            } catch (NoEmptySeatException ignore) {
            }
        });

        this.nameSpace.addEventListener("SelectCell", JSONObject.class, (client, data, ackSender) -> {
            for (int i = 0; i < 2; ++i) {
                if (this.players[i] == null || !this.players[i].uuid.equals(client.getSessionId())) continue;
                DiscType discType = DiscType.fromInt(i);
                if (discType == this.game.getCurrentTurn()) {
                    SelectCell selectCell = new SelectCell(data);
                    this.game.placeCell(selectCell);
                }
                break;
            }
        });

        this.onChangeTurn();
    }

    private void onChangeTurn() {
        this.sendGameState(this.nameSpace.getBroadcastOperations());
        if (this.getInGame()) {
            assert this.game.getCurrentTurn() != null;
            ServerPlayer currentTurnPlayer = this.players[this.game.getCurrentTurn().getInt()];
            if (currentTurnPlayer == null) return;

            currentTurnPlayer.notifyTurn(game, this.game::placeCell);
        }
    }

    int getSeatedPlayerCount() {
        return (int) Arrays.stream(this.players).filter(Objects::nonNull).count();
    }

    boolean getInGame() {
        return this.game.getWinner() == null;
    }

    private void getSeat(ServerPlayer player, DiscType discType) throws NoEmptySeatException {
        if (this.players[discType.getInt()] != null) {
            throw new NoEmptySeatException();
        }

        this.players[discType.getInt()] = player;

        this.sendGameState(this.nameSpace.getBroadcastOperations());
        this.onChangeTurn();
    }

    private void leaveSeat(ServerPlayer player) {
        for (int i = 0; i < 2; ++i) {
            if (this.players[i] != null && this.players[i].uuid.equals(player.uuid)) {
                this.players[i] = null;
                this.sendGameState(this.nameSpace.getBroadcastOperations());
                return;
            }
        }
    }

    private void sendGameState(ClientOperations clientOperations) {
        GameState state = new GameState(game, this.getSeatedPlayerUUIDs(), false);
        try {
            clientOperations.sendEvent("GameState", state.toJSONObject().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private UUID[] getSeatedPlayerUUIDs() {
        final UUID[] uuids = new UUID[2];
        for (int i = 0; i < 2; ++i) {
            if (this.players[i] == null) {
                uuids[i] = null;
            } else {
                uuids[i] = this.players[i].uuid;
            }
        }
        return uuids;
    }

    // ObserverEventListener
    @Override
    public void boardUpdated(Game game) {
        this.sendGameState(this.nameSpace.getBroadcastOperations());
    }
}
