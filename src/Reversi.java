import net.mossan.java.reversi.component.boarddrawer.ConsoleBoardDrawer;
import net.mossan.java.reversi.component.boarddrawer.GUIBoardDrawer;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.user.GUIConsoleUser;
import net.mossan.java.reversi.model.user.RandomPlaceUser;

public class Reversi {
    private static final GUIConsoleUser CONSOLE_USER = new GUIConsoleUser();
    private static final RandomPlaceUser RANDOM_PLACE_USER = new RandomPlaceUser();

    public static void main(String[] args) {
        // 8x8, 50px x 50px per cell, "Reversi" window title
        Game game = new Game(8);

        GUIBoardDrawer guiBoardDrawer = new GUIBoardDrawer(game, 50, "Reversi");
        game.addEventListener(guiBoardDrawer);

        ConsoleBoardDrawer consoleBoardDrawer = new ConsoleBoardDrawer(game);
        game.addEventListener(consoleBoardDrawer);

        game.addUser(CONSOLE_USER);
        game.addUser(RANDOM_PLACE_USER);
    }
}
