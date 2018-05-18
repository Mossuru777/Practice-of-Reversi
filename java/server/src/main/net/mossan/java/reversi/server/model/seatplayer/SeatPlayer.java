package net.mossan.java.reversi.server.model.seatplayer;

import net.mossan.java.reversi.common.model.eventlistener.PlayerEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class SeatPlayer implements PlayerEventListener {
    public final UUID uuid;
    public final String name;

    SeatPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof SeatPlayer)) {
            return false;
        }
        return this.uuid.equals(((SeatPlayer) obj).uuid);
    }
}
