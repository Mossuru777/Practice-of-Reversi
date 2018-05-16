package net.mossan.java.reversi.server.model.player;

import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.eventlistener.PlaceableCell;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class LongestPlaceCPU extends ServerPlayer {
    public LongestPlaceCPU() {
        super(UUID.nameUUIDFromBytes(("LongestPlaceCPU".getBytes())));
    }

    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        final List<PlaceableCell> placeableCellList = game.getPlaceableCellsList(game.getCurrentTurn());

        PlaceableCell maxNumOfReverseCellsPoint = null;
        for (PlaceableCell p : placeableCellList) {
            if (maxNumOfReverseCellsPoint == null || p.reversiblePoints.size() > maxNumOfReverseCellsPoint.reversiblePoints.size()) {
                maxNumOfReverseCellsPoint = p;
            }
        }
        assert maxNumOfReverseCellsPoint != null;

        placeCell.accept(maxNumOfReverseCellsPoint);
    }
}
