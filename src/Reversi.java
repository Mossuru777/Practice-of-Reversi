import net.mossan.java.reversi.component.BoardDrawer;

import javax.swing.*;

public class Reversi extends JPanel {
    public static void main(String[] args) {
        // 8x8, 50px x 50px per cell, "Reversi" window title
        BoardDrawer board = new BoardDrawer(8, 50, "Reversi");
    }
}
