package net.mossan.java.reversi.client.boarddrawer;

import net.mossan.java.reversi.client.boarddrawer.eventlistener.RoomEventListener;
import net.mossan.java.reversi.common.model.eventlistener.ObserverEventListener;
import net.mossan.java.reversi.common.model.eventlistener.PlayerEventListener;

public interface BoardDrawer extends ObserverEventListener, PlayerEventListener, RoomEventListener {
}
