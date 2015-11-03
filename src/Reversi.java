import net.mossan.java.reversi.component.BoardDrawer;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.player.GUIConsolePlayer;

import javax.swing.*;

public class Reversi extends JPanel {
    public static void main(String[] args) {
        // 8x8, 50px x 50px per cell, "Reversi" window title
        Game game = new Game(8);
        BoardDrawer board = new BoardDrawer(game, 50, "Reversi");
        game.addEventListener(board);

        // Two gui console player
        GUIConsolePlayer player1 = new GUIConsolePlayer();
        GUIConsolePlayer player2 = new GUIConsolePlayer();
        game.addPlayer(player1);
        game.addPlayer(player2);
    }
}
