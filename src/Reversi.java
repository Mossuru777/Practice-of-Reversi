import net.mossan.java.reversi.component.boarddrawer.ConsoleBoardDrawer;
import net.mossan.java.reversi.component.boarddrawer.GUIBoardDrawer;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.user.GUIConsoleUser;
import net.mossan.java.reversi.model.user.LongestPlaceCPU;
import net.mossan.java.reversi.model.user.RandomPlaceCPU;

public class Reversi {
    private static final RandomPlaceCPU RANDOM_PLACE_CPU = new RandomPlaceCPU();
    private static final LongestPlaceCPU LONGEST_PLACE_CPU = new LongestPlaceCPU();
    private static final GUIConsoleUser GUI_CONSOLE_USER = new GUIConsoleUser();

    public static void main(String[] args) {
        // 8x8, 50px x 50px per cell, "Reversi" window title
        Game game = new Game(8);

        GUIBoardDrawer guiBoardDrawer = new GUIBoardDrawer(game, 50, "Reversi");
        game.addEventListener(guiBoardDrawer);

        ConsoleBoardDrawer consoleBoardDrawer = new ConsoleBoardDrawer(game);
        game.addEventListener(consoleBoardDrawer);

        game.addUser(GUI_CONSOLE_USER);
        game.addUser(LONGEST_PLACE_CPU);
    }
}
