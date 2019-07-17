package net.mossan.java.reversi.common.message.response;

import net.mossan.java.reversi.common.message.JSONMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomDetail implements JSONMessage {
    public final int seatedPlayerCount;
    public final boolean inGame;

    public RoomDetail(int seatedPlayerCount, boolean inGame) {
        this.seatedPlayerCount = seatedPlayerCount;
        this.inGame = inGame;
    }

    RoomDetail(JSONObject o) throws JSONException {
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
