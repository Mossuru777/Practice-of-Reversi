package net.mossan.java.reversi.component.eventlistener;

import java.util.EventListener;

public interface BoardDrawerEventListener extends EventListener {
    void cellSelected(int horizontal, int vertical);
}
