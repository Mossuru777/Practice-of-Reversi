package net.mossan.java.reversi.client.boarddrawer.eventlistener;

import net.mossan.java.reversi.common.message.request.SeatRequest;
import net.mossan.java.reversi.common.model.PlayerType;

import java.util.EventListener;
import java.util.UUID;

public interface RoomEventListener extends EventListener {
    void onSuccessSeatRequest(SeatRequest seatRequest);

    void seatStatusUpdated(UUID[] seatedPlayerUUIDs, String[] seatedPlayerNames, PlayerType[][] seatAvailabilities);
}
