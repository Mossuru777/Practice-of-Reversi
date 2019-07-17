package net.mossan.java.reversi.server.model.seatplayer;

import net.mossan.java.reversi.common.message.request.CellSelect;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class NetworkPlayer extends SeatPlayer {
    private Function<CellSelect, Boolean> cellSelectFunction;

    public NetworkPlayer(UUID uuid) {
        // TODO "uuid.toString()" replace with name set by player
        super(uuid, uuid.toString());
    }

    public boolean selectCell(CellSelect cellSelect) {
        assert this.cellSelectFunction != null;
        return this.cellSelectFunction.apply(cellSelect);
    }

    // PlayerEventListener (inherit from SeatPlayer)
    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        this.cellSelectFunction = cellSelect -> {
            Optional<PlaceableCell> placeableCellOptional =
                    game.getPlaceableCellsList(game.getCurrentTurn())
                            .stream()
                            .filter(placeableCell ->
                                    placeableCell.placePoint[0] == cellSelect.horizontal
                                            && placeableCell.placePoint[1] == cellSelect.vertical)
                            .findFirst();
            placeableCellOptional.ifPresent(placeCell);
            return placeableCellOptional.isPresent();
        };
    }
}
