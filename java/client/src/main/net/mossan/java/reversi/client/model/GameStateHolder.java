package net.mossan.java.reversi.client.model;

import net.mossan.java.reversi.common.message.response.GameState;
import net.mossan.java.reversi.common.model.Game;

public final class GameStateHolder extends Game {
    public GameStateHolder(GameState state) {
        super();
        this.updateFromState(state);
    }

    public void updateFromState(GameState state) {
        // Parameters Check
        assert state.board.length > 0 && state.board.length % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";
        assert state.board.length == state.board[0].length : "board is not square.";
        if (state.turn != null) {
            assert state.winner == null : "There must be no winners in games that are someone's turn.";
        } else {
            assert state.winner != null : "Nobody turn the game must have a winner.";
        }

        // Update state
        this.board = state.board;
        this.currentTurn = state.turn;
        this.winner = state.winner;
    }
}
