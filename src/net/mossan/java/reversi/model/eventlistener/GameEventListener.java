package net.mossan.java.reversi.model.eventlistener;

import net.mossan.java.reversi.model.Disc;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.player.Player;

import java.util.EventListener;

public interface GameEventListener extends EventListener {
    void boardUpdated(Game game);

    void notifyGameResult(Disc disc, Player player);
}
