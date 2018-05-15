package net.mossan.java.reversi.common.jsonExchange;

import net.mossan.java.reversi.common.model.DiscType;
import org.json.JSONException;
import org.json.JSONObject;

public class SeatReservation implements JSONSerializable {
    public final DiscType discType;

    public SeatReservation(DiscType discType) {
        this.discType = discType;
    }

    public SeatReservation(JSONObject o) throws JSONException {
        this.discType = DiscType.fromInt(o.getInt("discType"));
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() {
            {
                this.put("discType", SeatReservation.this.discType.getInt());
            }
        };
    }
}
