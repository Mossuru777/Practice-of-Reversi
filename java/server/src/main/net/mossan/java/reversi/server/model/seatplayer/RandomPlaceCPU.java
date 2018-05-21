package net.mossan.java.reversi.server.model.seatplayer;

import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RandomPlaceCPU extends SeatPlayer {
    private SecureRandom random = new SecureRandom();

    public RandomPlaceCPU() {
        super(UUID.randomUUID(), "RandomPlaceCPU");
    }

    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        final List<PlaceableCell> placeableCellList = game.getPlaceableCellsList(game.getCurrentTurn());
        int choice = random.nextInt(placeableCellList.size());
        placeCell.accept(placeableCellList.get(choice));
    }
}
