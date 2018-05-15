package net.mossan.java.reversi.common.jsonExchange;

import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class GameState implements JSONSerializable {
    public final DiscType[][] board;
    public final @Nullable DiscType turn;
    public final @Nullable DiscType winner;
    public final UUID[] playerUUIDs;
    public final @Nullable Boolean yourTurn;

    public GameState(Game game, UUID[] playerUUIDs, @Nullable Boolean yourTurn) {
        this.board = game.getBoard();
        this.turn = game.getCurrentTurn();
        this.winner = game.getWinner();
        this.playerUUIDs = playerUUIDs;
        this.yourTurn = yourTurn;
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

        this.turn = o.isNull("turn") ? null : DiscType.fromInt(o.getInt("turn"));
        this.winner = o.isNull("winner") ? null : DiscType.fromInt(o.getInt("winner"));

        this.playerUUIDs = new UUID[2];
        JSONArray jsonPlayerUUIDs = o.getJSONArray("playerUUIDs");
        for (int i = 0; i < 2; ++i) {
            if (jsonPlayerUUIDs.isNull(i)) {
                this.playerUUIDs[i] = null;
            } else {
                this.playerUUIDs[i] = UUID.fromString(jsonPlayerUUIDs.getString(i));
            }
        }

        this.yourTurn = o.isNull("yourTurn") ? null : o.getBoolean("yourTurn");
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
                this.put("turn", GameState.this.turn == null ? JSONObject.NULL : GameState.this.turn.getInt());
                this.put("winner", GameState.this.winner == null ? JSONObject.NULL : GameState.this.winner.getInt());
                this.put("playerUUIDs", new JSONArray() {
                    {
                        for (UUID playerUUID : GameState.this.playerUUIDs) {
                            this.put(playerUUID == null ? JSONObject.NULL : playerUUID.toString());
                        }
                    }
                });
                this.put("yourTurn", GameState.this.yourTurn == null ? JSONObject.NULL : GameState.this.yourTurn);
            }
        };
    }
}
