package net.mossan.java.reversi.model.user;

import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.eventlistener.PlayerEventListener;

public abstract class User implements PlayerEventListener {
    Game game;

    public void setGame(Game game) {
        if (this.game != null) {
            this.game.removeUser(this);
        }
        this.game = game;
    }
}
