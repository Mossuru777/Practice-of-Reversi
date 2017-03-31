package net.mossan.java.reversi.model.user;

import net.mossan.java.reversi.component.eventlistener.BoardDrawerEventListener;
import net.mossan.java.reversi.model.Game;

public class GUIConsoleUser implements User, BoardDrawerEventListener {
    private Game game;

    @Override
    public void setGame(Game game) {
        if (this.game != null) {
            this.game.removeUser(this);
        }
        this.game = game;
    }

    @Override
    public void cellSelected(int horizontal, int vertical) {
        boolean placed = game.placeDisc(this, horizontal, vertical);
    }
}
