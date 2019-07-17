package net.mossan.java.reversi.common.message.request;

import net.mossan.java.reversi.common.message.JSONMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class CellSelect implements JSONMessage {
    public final int horizontal;
    public final int vertical;

    public CellSelect(int horizontal, int vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public CellSelect(JSONObject o) throws JSONException {
        this.horizontal = o.getInt("horizontal");
        this.vertical = o.getInt("vertical");
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() {
            {
                this.put("horizontal", CellSelect.this.horizontal);
                this.put("vertical", CellSelect.this.vertical);
            }
        };
    }
}
