package net.mossan.java.reversi.common.jsonExchange;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONSerializable {
    JSONObject toJSONObject() throws JSONException;
}
