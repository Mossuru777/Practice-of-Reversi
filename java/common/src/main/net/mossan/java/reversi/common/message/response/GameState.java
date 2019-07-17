package net.mossan.java.reversi.common.message.response;

import net.mossan.java.reversi.common.message.JSONMessage;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlayerType;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class GameState implements JSONMessage {
    public final DiscType[][] board;
    public final UUID[] playerUUIDs;
    public final String[] playerNames;
    public final PlayerType[][] seatAvailabilities;
    public final @Nullable DiscType turn;
    public final @Nullable DiscType winner;

    public GameState(Game game, UUID[] playerUUIDs, String[] playerNames, PlayerType[][] seatAvailabilities) {
        this.board = game.getBoard();
        this.playerUUIDs = playerUUIDs;
        this.playerNames = playerNames;
        this.seatAvailabilities = seatAvailabilities;
        this.turn = game.getCurrentTurn();
        this.winner = game.getWinner();
    }

    public GameState(JSONObject o) throws JSONException {
        JSONArray jsonRow = o.getJSONArray("board");
        this.board = new DiscType[jsonRow.length()][];
        for (int i = 0; i < jsonRow.length(); ++i) {
            JSONArray jsonColumn = jsonRow.getJSONArray(i);
            DiscType[] column = new DiscType[jsonColumn.length()];
            for (int j = 0; j < jsonColumn.length(); ++j) {
                column[j] = jsonColumn.isNull(j) ? null : DiscType.fromInt(jsonColumn.getInt(j));
            }
            board[i] = column;
        }

        this.playerUUIDs = new UUID[2];
        JSONArray jsonPlayerUUIDs = o.getJSONArray("playerUUIDs");
        for (int i = 0; i < 2; ++i) {
            if (jsonPlayerUUIDs.isNull(i)) {
                this.playerUUIDs[i] = null;
            } else {
                this.playerUUIDs[i] = UUID.fromString(jsonPlayerUUIDs.getString(i));
            }
        }

        this.playerNames = new String[2];
        JSONArray jsonPlayerNames = o.getJSONArray("playerNames");
        for (int i = 0; i < 2; ++i) {
            if (jsonPlayerNames.isNull(i)) {
                this.playerNames[i] = null;
            } else {
                this.playerNames[i] = jsonPlayerNames.getString(i);
            }
        }

        JSONArray jsonSittableSeat = o.getJSONArray("PlayerSeatAvailabilities");
        this.seatAvailabilities = new PlayerType[][]{
                PlayerType.fromBits(jsonSittableSeat.getInt(0)),
                PlayerType.fromBits(jsonSittableSeat.getInt(1)),
        };

        this.turn = o.isNull("turn") ? null : DiscType.fromInt(o.getInt("turn"));
        this.winner = o.isNull("winner") ? null : DiscType.fromInt(o.getInt("winner"));
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() {
            {
                this.put("board", new JSONArray() {
                    {
                        for (DiscType[] column : GameState.this.board) {
                            JSONArray jsonColumn = new JSONArray();
                            for (DiscType cell : column) {
                                jsonColumn.put(cell == null ? JSONObject.NULL : cell.getInt());
                            }
                            this.put(jsonColumn);
                        }
                    }
                });
                this.put("playerUUIDs", new JSONArray() {
                    {
                        for (UUID playerUUID : GameState.this.playerUUIDs) {
                            this.put(playerUUID == null ? JSONObject.NULL : playerUUID.toString());
                        }
                    }
                });
                this.put("playerNames", new JSONArray() {
                    {
                        for (String playerName : GameState.this.playerNames) {
                            this.put(playerName == null ? JSONObject.NULL : playerName);
                        }
                    }
                });
                this.put("PlayerSeatAvailabilities", new JSONArray() {
                    {
                        this.put(PlayerType.getBits(GameState.this.seatAvailabilities[0]));
                        this.put(PlayerType.getBits(GameState.this.seatAvailabilities[1]));
                    }
                });
                this.put("turn", GameState.this.turn == null ? JSONObject.NULL : GameState.this.turn.getInt());
                this.put("winner", GameState.this.winner == null ? JSONObject.NULL : GameState.this.winner.getInt());
            }
        };
    }
}
