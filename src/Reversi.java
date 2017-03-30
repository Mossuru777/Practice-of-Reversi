import net.mossan.java.reversi.component.BoardDrawer;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.user.GUIConsoleUser;

import javax.swing.*;

public class Reversi extends JPanel {
    public static void main(String[] args) {
        // 8x8, 50px x 50px per cell, "Reversi" window title
        Game game = new Game(8);
        BoardDrawer board = new BoardDrawer(game, 50, "Reversi");
        game.addEventListener(board);

        // Two gui console user
        GUIConsoleUser player1 = new GUIConsoleUser();
        GUIConsoleUser player2 = new GUIConsoleUser();
        game.addUser(player1);
        game.addUser(player2);
    }
}
