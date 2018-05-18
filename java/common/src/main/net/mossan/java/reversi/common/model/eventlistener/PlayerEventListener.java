package net.mossan.java.reversi.common.model.eventlistener;

import net.mossan.java.reversi.common.model.Game;

import java.util.EventListener;
import java.util.function.Consumer;

public interface PlayerEventListener extends EventListener {
    void notifyTurn(Game game, Consumer<PlaceableCell> placeCell);
}
