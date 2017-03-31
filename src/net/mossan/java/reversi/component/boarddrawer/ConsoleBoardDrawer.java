package net.mossan.java.reversi.component.boarddrawer;

import net.mossan.java.reversi.model.DiscType;
import net.mossan.java.reversi.model.Game;
import net.mossan.java.reversi.model.Player;
import net.mossan.java.reversi.model.eventlistener.GameEventListener;

public class ConsoleBoardDrawer implements GameEventListener {
    private Game game;

    public ConsoleBoardDrawer(Game game) {
        this.game = game;
        this.game.addEventListener(this);
    }

    @Override
    public void boardUpdated(Game game, DiscType[][] beforeBoard, DiscType[][] afterBoard, Player currentTurnPlayer, boolean isOtherPlayerSkipped) {
        if (game != this.game) {
            game.removeEventListener(this);
        }

        // output before board to console
        System.out.println("Before:");
        outputBoardToConsole(beforeBoard);

        // output after changed board to console
        System.out.println("After:");
        outputBoardToConsole(afterBoard);

        // output turn change message to console
        if (!isOtherPlayerSkipped) {
            System.out.println("Next Turn: " + currentTurnPlayer.type.name());
        } else {
            System.out.println("Next Turn: " + currentTurnPlayer.type.name() + "  (Skipped: " + currentTurnPlayer.type.otherDiscType().name() + ")");
        }
    }

    @Override
    public void notifyGameResult(Game game, DiscType[][] beforeBoard, DiscType[][] afterBoard, Player winner) {
        if (game != this.game) {
            game.removeEventListener(this);
        }

        //output game over message to console
        System.out.println("Game Over");
        System.out.println("Winner: " + winner.type.name());
    }

    private void outputBoardToConsole(DiscType[][] board) {
        System.out.print("-");
        for (int i = 0; i < board.length; i++) {
            System.out.print("－-");
        }
        System.out.print("\n");
        for (int v = 0; v < board.length; v++) {
            System.out.print("|");
            for (int h = 0; h < board.length; h++) {
                if (board[h][v] == DiscType.BLACK) {
                    System.out.print("○");
                } else if (board[h][v] == DiscType.WHITE) {
                    System.out.print("●");
                } else {
                    System.out.print("　");
                }
                System.out.print("|");
            }
            System.out.print("\n");
            System.out.print("-");
            for (int i = 0; i < board.length; i++) {
                System.out.print("－-");
            }
            System.out.print("\n");
        }
    }
}
