package net.mossan.java.reversi.model.eventlistener;

import net.mossan.java.reversi.model.DiscType;

import java.util.EventListener;
import java.util.List;

public interface PlayerEventListener extends EventListener{
    void notifyTurn(DiscType[][] board, List<List<int[]>> placeableCellList, boolean isOtherPlayerSkipped);
}
