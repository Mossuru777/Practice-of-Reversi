package net.mossan.java.reversi.common.jsonExchange;

import net.mossan.java.reversi.common.model.DiscType;
import org.json.JSONException;
import org.json.JSONObject;

public class UnSeatRequest implements JSONSerializable {
    public final DiscType discType;

    public UnSeatRequest(DiscType discType) {
        this.discType = discType;
    }

    public UnSeatRequest(JSONObject o) {
        this.discType = DiscType.fromInt(o.getInt("discType"));
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() {
            {
                this.put("discType", UnSeatRequest.this.discType.getInt());
            }
        };
    }
}
