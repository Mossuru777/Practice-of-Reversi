package net.mossan.java.reversi.server.model.player;

import com.corundumstudio.socketio.SocketIOClient;
import net.mossan.java.reversi.common.jsonExchange.GameState;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.eventlistener.ObserverEventListener;
import net.mossan.java.reversi.common.model.eventlistener.PlaceableCell;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NetworkPlayer extends ServerPlayer implements ObserverEventListener {
    private final WeakReference<SocketIOClient> clientRef;
    private final Supplier<UUID[]> gamePlayerUUIDsSupplier;

    public NetworkPlayer(SocketIOClient client, Supplier<UUID[]> gamePlayerUUIDsSupplier) {
        super(client.getSessionId());
        this.clientRef = new WeakReference<>(client);
        this.gamePlayerUUIDsSupplier = gamePlayerUUIDsSupplier;
    }

    @Override
    public void boardUpdated(Game game) {
        this.sendUpdatedGameState(game, false);
    }

    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        this.sendUpdatedGameState(game, true);
    }

    private void sendUpdatedGameState(Game game, boolean myTurn) {
        SocketIOClient client = this.clientRef.get();
        assert client != null;

        UUID[] gamePlayerUUIDs = this.gamePlayerUUIDsSupplier.get();
        GameState state = new GameState(game, gamePlayerUUIDs, myTurn);
        try {
            client.sendEvent("GameState", state.toJSONObject().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
