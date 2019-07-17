package net.mossan.java.reversi.common.message;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONMessage {
    JSONObject toJSONObject() throws JSONException;
}
