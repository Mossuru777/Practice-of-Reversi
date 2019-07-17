package net.mossan.java.reversi.common.message.response;

import net.mossan.java.reversi.common.message.JSONMessage;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestReply implements JSONMessage {
    public final boolean success;
    public final @Nullable String detail;

    private RequestReply(boolean success, @Nullable String detail) {
        this.success = success;
        this.detail = detail;
    }

    public RequestReply(JSONObject o) {
        this.success = o.getBoolean("success");
        this.detail = o.isNull("detail") ? null : o.getString("detail");
    }

    public static RequestReply Success(@Nullable String detail) {
        return new RequestReply(true, detail);
    }

    public static RequestReply Failed(@Nullable String detail) {
        return new RequestReply(false, detail);
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        return new JSONObject() {
            {
                this.put("success", RequestReply.this.success);
                this.put("detail", RequestReply.this.detail == null ? JSONObject.NULL : RequestReply.this.detail);
            }
        };
    }
}
