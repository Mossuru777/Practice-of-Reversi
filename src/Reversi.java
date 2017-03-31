import net.mossan.java.reversi.component.boarddrawer.ConsoleBoardDrawer;
import net.mossan.java.reversi.component.boarddrawer.GUIBoardDrawer;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.user.GUIConsoleUser;

public class Reversi {
    // Two gui console user
    private static final GUIConsoleUser USER1 = new GUIConsoleUser();
    private static final GUIConsoleUser USER2 = new GUIConsoleUser();

    public static void main(String[] args) {
        // 8x8, 50px x 50px per cell, "Reversi" window title
        Game game = new Game(8);

        GUIBoardDrawer guiBoardDrawer = new GUIBoardDrawer(game, 50, "Reversi");
        game.addEventListener(guiBoardDrawer);

        ConsoleBoardDrawer consoleBoardDrawer = new ConsoleBoardDrawer(game);
        game.addEventListener(consoleBoardDrawer);
        
        game.addUser(USER1);
        game.addUser(USER2);
    }
}
