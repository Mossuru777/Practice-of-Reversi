package net.mossan.java.reversi.model.player;

import net.mossan.java.reversi.component.eventlistener.BoardDrawerEventListener;
import net.mossan.java.reversi.model.Game;

public class GUIConsolePlayer implements Player, BoardDrawerEventListener {
    private Game game;

    @Override
    public void setGame(Game game) {
        if (game != null) {
            game.removePlayer(this);
        }
        this.game = game;
    }

    @Override
    public void cellClicked(int horizontal, int vertical) {
        game.placeDisc(this, horizontal, vertical);
    }
}
