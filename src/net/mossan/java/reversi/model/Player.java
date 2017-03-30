package net.mossan.java.reversi.model;

import net.mossan.java.reversi.model.user.User;

import java.lang.ref.WeakReference;

public final class Player {
    final DiscType type;
    public WeakReference<User> user = new WeakReference<>(null);

    Player(DiscType type) {
        assert type == DiscType.BLACK || type == DiscType.WHITE : "\"type\" can set only BLACK or WHITE.";
        this.type = type;
    }
}
