package net.mossan.java.reversi.common.model.eventlistener;

import net.mossan.java.reversi.common.model.Game;

import java.util.EventListener;

public interface ObserverEventListener extends EventListener {
    void boardUpdated(Game game);
}
