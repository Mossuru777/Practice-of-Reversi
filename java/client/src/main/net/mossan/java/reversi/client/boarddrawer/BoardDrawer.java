package net.mossan.java.reversi.client.boarddrawer;

import net.mossan.java.reversi.client.model.GameStateHolder;
import net.mossan.java.reversi.common.message.request.SeatRequest;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.PlayerType;
import net.mossan.java.reversi.common.model.eventlistener.PlayerEventListener;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BoardDrawer implements PlayerEventListener {
    protected final Consumer<SeatRequest> seatRequestConsumer;
    protected final Supplier<Void> onBoardDrawerClosedCallback;

    protected WeakReference<GameStateHolder> gameStateHolderWeakReference = null;
    protected @Nullable DiscType myDiscType = null;

    protected BoardDrawer(Consumer<SeatRequest> seatRequestConsumer, Supplier<Void> onBoardDrawerClosedCallback) {
        this.seatRequestConsumer = seatRequestConsumer;
        this.onBoardDrawerClosedCallback = onBoardDrawerClosedCallback;
    }

    public void update(GameStateHolder gameStateHolder) {
        if (this.gameStateHolderWeakReference == null) {
            this.gameStateHolderWeakReference = new WeakReference<>(gameStateHolder);
        }
        if (this.gameStateHolderWeakReference.get() == null) {
            System.out.println("*** Detected that game state object is null!!! ***");
            this.notifyLeavingRoom();
        }
    }

    public void onSuccessSeatRequest(SeatRequest seatRequest) {
        if (seatRequest.playerType == PlayerType.NetworkPlayer) {
            this.myDiscType = seatRequest.discType;
        }
    }

    public abstract void notifyLeavingRoom();
}
