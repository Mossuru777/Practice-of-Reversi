import net.mossan.java.reversi.component.BoardDrawer;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.user.GUIConsoleUser;

import javax.swing.*;

public class Reversi extends JPanel {
    // Two gui console user
    private static final GUIConsoleUser USER1 = new GUIConsoleUser();
    private static final GUIConsoleUser USER2 = new GUIConsoleUser();

    public static void main(String[] args) {
        // 8x8, 50px x 50px per cell, "Reversi" window title
        Game game = new Game(8);
        BoardDrawer board = new BoardDrawer(game, 50, "Reversi");
        game.addEventListener(board);
        game.addUser(USER1);
        game.addUser(USER2);
    }
}
