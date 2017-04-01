package net.mossan.java.reversi.model.user;

import net.mossan.java.reversi.model.DiscType;

import java.security.SecureRandom;
import java.util.List;

public class LongestPlaceCPU extends User {
    private SecureRandom random = new SecureRandom();

    @Override
    public void notifyTurn(DiscType[][] board, List<List<int[]>> placeableCellList, boolean isOtherPlayerSkipped) {
        List<int[]> max = null;
        for (List<int[]> p : placeableCellList) {
            if (max == null || p.size() > max.size()) {
                max = p;
            }
        }

        int horizontal = max.get(0)[0];
        int vertical = max.get(0)[1];

        new Thread(() -> {
            try {
                Thread.sleep(1000 + random.nextInt(4000));
                game.placeDisc(this, horizontal, vertical);
            } catch (InterruptedException ignore) {
            }
        }).start();
    }
}
