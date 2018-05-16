package net.mossan.java.reversi.common.jsonExchange;

import org.json.JSONException;
import org.json.JSONObject;

public class RoomDetail implements JSONSerializable {
    public final int seatedPlayerCount;
    public final boolean inGame;

    public RoomDetail(int seatedPlayerCount, boolean inGame) {
        this.seatedPlayerCount = seatedPlayerCount;
        this.inGame = inGame;
    }

    public RoomDetail(JSONObject o) throws JSONException {
        this.seatedPlayerCount = o.getInt("seatedPlayerCount");
        this.inGame = o.getBoolean("inGame");
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("seatedPlayerCount", this.seatedPlayerCount);
        obj.put("inGame", this.inGame);
        return obj;
    }
}
