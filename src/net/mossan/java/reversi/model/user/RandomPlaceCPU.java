package net.mossan.java.reversi.model.user;

import net.mossan.java.reversi.model.DiscType;

import java.security.SecureRandom;
import java.util.List;

public class RandomPlaceCPU extends User {
    private SecureRandom random = new SecureRandom();

    @Override
    public void notifyTurn(DiscType[][] board, List<List<int[]>> placeableCellList, boolean isOtherPlayerSkipped) {
        int choice = random.nextInt(placeableCellList.size());

        int horizontal = placeableCellList.get(choice).get(0)[0];
        int vertical = placeableCellList.get(choice).get(0)[1];

        new Thread(() -> {
            try {
                Thread.sleep(1000 + random.nextInt(4000));
                game.placeDisc(this, horizontal, vertical);
            } catch (InterruptedException ignore) {
            }
        }).start();
    }
}
