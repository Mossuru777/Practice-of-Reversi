package net.mossan.java.reversi.common.jsonExchange;

import org.json.JSONException;
import org.json.JSONObject;

public class SelectCell implements JSONSerializable {
    public final int horizontal;
    public final int vertical;

    public SelectCell(int horizontal, int vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public SelectCell(JSONObject o) throws JSONException {
        this.horizontal = o.getInt("horizontal");
        this.vertical = o.getInt("vertical");
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() {
            {
                this.put("horizontal", SelectCell.this.horizontal);
                this.put("vertical", SelectCell.this.vertical);
            }
        };
    }
}
