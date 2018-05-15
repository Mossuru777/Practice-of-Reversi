package net.mossan.java.reversi.server.model.player;

import net.mossan.java.reversi.common.model.eventlistener.PlayerEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class ServerPlayer implements PlayerEventListener {
    public final UUID uuid;

    ServerPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ServerPlayer)) {
            return false;
        }
        return this.uuid.equals(((ServerPlayer) obj).uuid);
    }
}
