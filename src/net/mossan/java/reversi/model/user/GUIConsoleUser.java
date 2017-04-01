package net.mossan.java.reversi.model.user;

import net.mossan.java.reversi.component.eventlistener.BoardDrawerEventListener;
import net.mossan.java.reversi.model.DiscType;

import java.util.List;

public class GUIConsoleUser extends User implements BoardDrawerEventListener {
    @Override
    public void notifyTurn(DiscType[][] board, List<List<int[]>> placeableCellList, boolean isOtherPlayerSkipped) {
        // ignore
    }

    @Override
    public void cellSelected(int horizontal, int vertical) {
        boolean placed = game.placeDisc(this, horizontal, vertical);
    }
}
