package net.mossan.java.reversi.server.model.player;

import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.eventlistener.PlaceableCell;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RandomPlaceCPU extends ServerPlayer {
    private SecureRandom random = new SecureRandom();

    public RandomPlaceCPU() {
        super(UUID.nameUUIDFromBytes(("RandomPlaceCPU".getBytes())));
    }

    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        final List<PlaceableCell> placeableCellList = game.getPlaceableCellsList(game.getCurrentTurn());
        int choice = random.nextInt(placeableCellList.size());
        placeCell.accept(placeableCellList.get(choice));
    }
}
