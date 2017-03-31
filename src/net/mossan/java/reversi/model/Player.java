package net.mossan.java.reversi.model;

import net.mossan.java.reversi.model.user.User;

import java.lang.ref.WeakReference;

public final class Player {
    public final DiscType type;
    public WeakReference<User> user = new WeakReference<>(null);

    Player(DiscType type) {
        this.type = type;
    }
}
