package net.mossan.java.reversi.common.message.request;

import net.mossan.java.reversi.common.message.JSONMessage;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.PlayerType;
import org.json.JSONException;
import org.json.JSONObject;

public class SeatRequest implements JSONMessage {
    public final DiscType discType;
    public final PlayerType playerType;

    public SeatRequest(DiscType discType, PlayerType playerType) {
        this.discType = discType;
        this.playerType = playerType;
    }

    public SeatRequest(JSONObject o) throws JSONException {
        this.discType = DiscType.fromInt(o.getInt("discType"));
        this.playerType = PlayerType.fromBit(o.getInt("playerType"));
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() {
            {
                this.put("discType", SeatRequest.this.discType.getInt());
                this.put("playerType", SeatRequest.this.playerType.getBit());
            }
        };
    }
}
