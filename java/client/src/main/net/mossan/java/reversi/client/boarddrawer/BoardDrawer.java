package net.mossan.java.reversi.client.boarddrawer;

import net.mossan.java.reversi.common.model.eventlistener.ObserverEventListener;
import net.mossan.java.reversi.common.model.eventlistener.PlayerEventListener;
import net.mossan.java.reversi.client.boarddrawer.eventlistener.RoomEventListener;

public interface BoardDrawer extends ObserverEventListener, PlayerEventListener, RoomEventListener {
}
