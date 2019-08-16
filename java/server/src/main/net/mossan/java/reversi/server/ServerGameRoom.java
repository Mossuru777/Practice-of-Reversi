package net.mossan.java.reversi.server;

import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import net.mossan.java.reversi.common.exception.NoEmptySeatException;
import net.mossan.java.reversi.common.message.EventType;
import net.mossan.java.reversi.common.message.request.CellSelect;
import net.mossan.java.reversi.common.message.request.SeatRequest;
import net.mossan.java.reversi.common.message.request.UnSeatRequest;
import net.mossan.java.reversi.common.message.response.GameState;
import net.mossan.java.reversi.common.message.response.RequestReply;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.PlayerType;
import net.mossan.java.reversi.server.model.Referee;
import net.mossan.java.reversi.server.model.seatplayer.LongestPlaceCPU;
import net.mossan.java.reversi.server.model.seatplayer.NetworkPlayer;
import net.mossan.java.reversi.server.model.seatplayer.RandomPlaceCPU;
import net.mossan.java.reversi.server.model.seatplayer.SeatPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;

public class ServerGameRoom {
    final UUID uuid;
    private final SocketIONamespace nameSpace;
    private final Referee referee;
    private final Map<UUID, NetworkPlayer> networkPlayersMap = new HashMap<>();
    private final SeatPlayer[] seatPlayers = new SeatPlayer[2];

    ServerGameRoom(int board_rows, SocketIOServer socket) {
        this.referee = new Referee(board_rows, this);

        this.uuid = UUID.randomUUID();
        this.nameSpace = socket.addNamespace(String.format("/%s", this.uuid.toString()));

        this.nameSpace.addConnectListener(client -> {
            System.out.println(String.format("*** Enter (%s): %s ***", this.nameSpace.getName(), client.getSessionId()));
            NetworkPlayer networkPlayer = new NetworkPlayer(client.getSessionId());
            this.networkPlayersMap.put(client.getSessionId(), networkPlayer);
            this.sendGameState(client);
        });

        this.nameSpace.addDisconnectListener(client -> {
            System.out.println(String.format("*** Leave (%s): %s ***", this.nameSpace.getName(), client.getSessionId()));
            NetworkPlayer disconnectPlayer = this.networkPlayersMap.get(client.getSessionId());
            this.networkPlayersMap.remove(client.getSessionId());
            this.leaveSeat(disconnectPlayer);
        });

        this.nameSpace.addEventListener(EventType.seating.toString(), JSONObject.class, (client, data, ackSender) -> {
            if (!this.getInGame()) {
                ackSender.sendAckData(RequestReply.Failed("Game is over.").toJSONObject().toString());
                return;
            }

            SeatRequest request = new SeatRequest(data);

            SeatPlayer player;
            if (request.playerType == PlayerType.NetworkPlayer) {
                player = this.networkPlayersMap.get(client.getSessionId());
            } else if (request.playerType == PlayerType.LongestPlaceCPU) {
                player = new LongestPlaceCPU();
            } else {
                player = new RandomPlaceCPU();
            }

            try {
                ackSender.sendAckData(RequestReply.Success(null).toJSONObject().toString());
                this.getSeat(player, request.discType);
            } catch (NoEmptySeatException ignore) {
                ackSender.sendAckData(RequestReply.Failed("Sorry, another player was sitting on earlier than you.").toJSONObject().toString());
            }
        });

        this.nameSpace.addEventListener(EventType.unSeating.toString(), JSONObject.class, (client, data, ackSender) -> {
            UnSeatRequest request = new UnSeatRequest(data);

            SeatPlayer seatedPlayer = this.seatPlayers[request.discType.getInt()];
            if (seatedPlayer == null) {
                ackSender.sendAckData(RequestReply.Success(null).toJSONObject().toString());
            } else if (seatedPlayer instanceof NetworkPlayer) {
                if (seatedPlayer.uuid.equals(client.getSessionId())) {
                    this.leaveSeat(seatedPlayer);
                    ackSender.sendAckData(RequestReply.Success(null).toJSONObject().toString());
                } else {
                    ackSender.sendAckData(RequestReply.Failed("other player is seated.").toJSONObject().toString());
                }
            } else {
                this.leaveSeat(seatedPlayer);
                ackSender.sendAckData(RequestReply.Success(null).toJSONObject().toString());
            }
        });

        this.nameSpace.addEventListener(EventType.selectCell.toString(), JSONObject.class, (client, data, ackSender) -> {
            for (int i = 0; i < 2; ++i) {
                if (this.seatPlayers[i] == null || !this.seatPlayers[i].uuid.equals(client.getSessionId())) continue;
                DiscType discType = DiscType.fromInt(i);
                if (discType != this.referee.getCurrentTurn()) {
                    ackSender.sendAckData(RequestReply.Failed("It is not your turn now.").toJSONObject().toString());
                }

                CellSelect cellSelect = new CellSelect(data);
                NetworkPlayer seatNetworkPlayer = (NetworkPlayer) this.seatPlayers[i];
                if (seatNetworkPlayer.selectCell(cellSelect)) {
                    ackSender.sendAckData(RequestReply.Success(null).toJSONObject().toString());
                } else {
                    ackSender.sendAckData(RequestReply.Failed("It can not be placed in the specified place.").toJSONObject().toString());
                }
                return;
            }
            ackSender.sendAckData(RequestReply.Failed("You are not seated.").toJSONObject().toString());
        });

        this.nameSpace.addEventListener(EventType.requestGameState.toString(), Object.class, (client, data, ackSender) -> this.sendGameState(client));

        this.onGameUpdate();
    }

    private PlayerType[][] acquireSeatAvailabilities() {
        Function<@Nullable SeatPlayer, PlayerType[]> acquire = seatedPlayer -> {
            if (this.getInGame() && seatedPlayer == null) {
                return new PlayerType[]{PlayerType.NetworkPlayer, PlayerType.LongestPlaceCPU, PlayerType.RandomPlaceCPU};
            } else {
                return new PlayerType[0];
            }
        };

        return new PlayerType[][]{
                acquire.apply(this.seatPlayers[0]),
                acquire.apply(this.seatPlayers[1])
        };
    }

    public void onGameUpdate() {
        if (!this.getInGame()) {
            Arrays.stream(this.seatPlayers).filter(Objects::nonNull).forEach(this::leaveSeat);
        }
        this.sendGameState(this.nameSpace.getBroadcastOperations());
        if (this.getInGame()) {
            assert this.referee.getCurrentTurn() != null;
            SeatPlayer currentTurnPlayer = this.seatPlayers[this.referee.getCurrentTurn().getInt()];
            if (currentTurnPlayer != null) {
                currentTurnPlayer.notifyTurn(this.referee, this.referee::placeCell);
            }
        }
    }

    int getSeatedPlayerCount() {
        return (int) Arrays.stream(this.seatPlayers).filter(Objects::nonNull).count();
    }

    boolean getInGame() {
        return this.referee.getWinner() == null;
    }

    private void getSeat(SeatPlayer player, DiscType discType) throws NoEmptySeatException {
        if (this.seatPlayers[discType.getInt()] instanceof NetworkPlayer) {
            throw new NoEmptySeatException();
        }

        this.seatPlayers[discType.getInt()] = player;
        this.onGameUpdate();
    }

    private void leaveSeat(SeatPlayer player) {
        for (int i = 0; i < 2; ++i) {
            if (this.seatPlayers[i] != null && this.seatPlayers[i].uuid.equals(player.uuid)) {
                this.seatPlayers[i] = null;
                this.onGameUpdate();
                return;
            }
        }
    }

    private void sendGameState(ClientOperations clientOperations) {
        GameState state = new GameState(
                this.referee,
                this.getSeatedPlayerUUIDs(),
                this.getSeatedPlayerNames(),
                this.acquireSeatAvailabilities()
        );
        try {
            clientOperations.sendEvent(EventType.GameState.toString(), state.toJSONObject().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private UUID[] getSeatedPlayerUUIDs() {
        final UUID[] uuids = new UUID[2];
        for (int i = 0; i < 2; ++i) {
            if (this.seatPlayers[i] == null) {
                uuids[i] = null;
            } else {
                uuids[i] = this.seatPlayers[i].uuid;
            }
        }
        return uuids;
    }

    private String[] getSeatedPlayerNames() {
        final String[] names = new String[2];
        for (int i = 0; i < 2; ++i) {
            if (this.seatPlayers[i] == null) {
                names[i] = null;
            } else {
                names[i] = this.seatPlayers[i].name;
            }
        }
        return names;
    }
}
