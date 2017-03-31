package net.mossan.java.reversi.model.eventlistener;

import net.mossan.java.reversi.model.DiscType;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.Player;

import java.util.EventListener;

public interface GameEventListener extends EventListener {
    void boardUpdated(Game game, DiscType[][] beforeBoard, DiscType[][] afterBoard, Player currentTurnPlayer, boolean isOtherPlayerSkipped);

    void notifyGameResult(Game game, DiscType[][] beforeBoard, DiscType[][] afterBoard, Player winner);
}
