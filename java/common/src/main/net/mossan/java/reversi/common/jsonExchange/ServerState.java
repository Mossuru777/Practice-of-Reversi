package net.mossan.java.reversi.common.jsonExchange;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ServerState implements JSONSerializable {
    public final Map<UUID, RoomDetail> roomDetailMap;

    public ServerState(Map<UUID, RoomDetail> roomDetailMap) {
        this.roomDetailMap = roomDetailMap;
    }

    public ServerState(JSONObject o) throws JSONException {
        Map<UUID, RoomDetail> roomDetailMap = new HashMap<>();
        JSONObject jsonRoomStateMap = o.getJSONObject("roomDetailMap");
        Iterator jsonRoomStateMapKeyit = jsonRoomStateMap.keys();
        while (jsonRoomStateMapKeyit.hasNext()) {
            String uuid = (String) jsonRoomStateMapKeyit.next();
            JSONObject jsonRoomState = jsonRoomStateMap.getJSONObject(uuid);
            roomDetailMap.put(UUID.fromString(uuid), new RoomDetail(jsonRoomState));
        }
        this.roomDetailMap = roomDetailMap;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("roomDetailMap", new HashMap<String, JSONObject>() {
            {
                for (Entry<UUID, RoomDetail> entry : ServerState.this.roomDetailMap.entrySet()) {
                    put(entry.getKey().toString(), entry.getValue().toJSONObject());
                }
            }
        });
        return json;
    }
}
