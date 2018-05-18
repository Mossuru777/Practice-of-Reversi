package net.mossan.java.reversi.client.boarddrawer.guiboarddrawer;

import net.mossan.java.reversi.common.jsonExchange.SeatRequest;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlayerType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

class GameInfoPanel extends JPanel {
    private final Supplier<Game> gameSupplier;
    private final Supplier<Boolean> isMyTurnSupplier;
    private final Supplier<PlayerType[][]> seatAvailabilitySupplier;
    private final BiConsumer<DiscType, PlayerType> seatRequestIssuer;

    private final JLabel turnLabel = new JLabel();
    private final JLabel messageLabel = new JLabel();

    private final JButton seatMeButton = new JButton("Me");
    private final JButton seatLongestPlaceCPUButton = new JButton("LongestPlaceCPU");
    private final JButton seatRandomPlaceCPUButton = new JButton("RandomPlaceCPU");
    private @Nullable ActionListener seatMeButtonAction = null;
    private @Nullable ActionListener seatLongestPlaceCPUButtonAction = null;
    private @Nullable ActionListener seatRandomPlaceCPUButtonAction = null;

    GameInfoPanel(Supplier<Game> gameSupplier, Supplier<Boolean> isMyTurnSupplier, Supplier<PlayerType[][]> seatAvailabilitySupplier, Consumer<SeatRequest> seatRequestConsumer) {
        this.gameSupplier = gameSupplier;
        this.isMyTurnSupplier = isMyTurnSupplier;
        this.seatAvailabilitySupplier = seatAvailabilitySupplier;
        this.seatRequestIssuer = (currentTurn, playerType) -> {
            if (currentTurn == null) return;
            seatRequestConsumer.accept(new SeatRequest(currentTurn, playerType));
        };

        this.setOpaque(true);
        this.setBackground(Color.darkGray);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        Font turnLabelFont = new Font(Font.SERIF, Font.PLAIN, 20);
        this.turnLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        this.turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.turnLabel.setFont(turnLabelFont);
        this.turnLabel.setForeground(Color.white);
        this.add(this.turnLabel);

        Font messageLabelFont = new Font(Font.SERIF, Font.PLAIN, 22);
        this.messageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        this.messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.messageLabel.setFont(messageLabelFont);
        this.messageLabel.setForeground(Color.yellow);
        this.add(this.messageLabel);

        this.seatMeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(this.seatMeButton);

        this.seatLongestPlaceCPUButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(this.seatLongestPlaceCPUButton);

        this.seatRandomPlaceCPUButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(this.seatRandomPlaceCPUButton);

        this.updateState();
    }

    void updateState() {
        Game drawGame = this.gameSupplier.get();
        if (drawGame == null) return;

        DiscType currentTurn = drawGame.getCurrentTurn();
        if (currentTurn != null) {
            String yourTurn = this.isMyTurnSupplier.get() ? " (YOUR TURN)" : "";
            this.turnLabel.setText(String.format("Turn: %s%s", currentTurn.toString(), yourTurn));
        } else {
            this.turnLabel.setText("");
        }

        DiscType winner = drawGame.getWinner();
        if (winner != null) {
            this.messageLabel.setText(String.format("Winner: %s", winner.toString()));
        } else {
            this.messageLabel.setText("");
        }

        PlayerType[] seatAvailability = currentTurn == null ? new PlayerType[]{} : this.seatAvailabilitySupplier.get()[currentTurn.getInt()];

        if (this.seatMeButtonAction != null) {
            this.seatMeButton.removeActionListener(this.seatMeButtonAction);
            this.seatMeButtonAction = null;
        }
        if (Arrays.stream(seatAvailability).anyMatch(t -> t == PlayerType.NetworkPlayer)) {
            this.seatMeButton.setEnabled(true);
            this.seatMeButtonAction = e -> GameInfoPanel.this.seatRequestIssuer.accept(currentTurn, PlayerType.NetworkPlayer);
            this.seatMeButton.addActionListener(this.seatMeButtonAction);
        } else {
            this.seatMeButton.setEnabled(false);
        }

        if (this.seatLongestPlaceCPUButtonAction != null) {
            this.seatLongestPlaceCPUButton.removeActionListener(this.seatLongestPlaceCPUButtonAction);
            this.seatLongestPlaceCPUButtonAction = null;
        }
        if (Arrays.stream(seatAvailability).anyMatch(t -> t == PlayerType.NetworkPlayer)) {
            this.seatLongestPlaceCPUButton.setEnabled(true);
            this.seatLongestPlaceCPUButtonAction = e -> GameInfoPanel.this.seatRequestIssuer.accept(currentTurn, PlayerType.LongestPlaceCPU);
            this.seatLongestPlaceCPUButton.addActionListener(this.seatLongestPlaceCPUButtonAction);
        } else {
            this.seatLongestPlaceCPUButton.setEnabled(false);
        }

        if (this.seatRandomPlaceCPUButtonAction != null) {
            this.seatRandomPlaceCPUButton.removeActionListener(this.seatRandomPlaceCPUButtonAction);
            this.seatRandomPlaceCPUButtonAction = null;
        }
        if (Arrays.stream(seatAvailability).anyMatch(t -> t == PlayerType.NetworkPlayer)) {
            this.seatRandomPlaceCPUButton.setEnabled(true);
            this.seatRandomPlaceCPUButtonAction = e -> GameInfoPanel.this.seatRequestIssuer.accept(currentTurn, PlayerType.RandomPlaceCPU);
            this.seatRandomPlaceCPUButton.addActionListener(this.seatRandomPlaceCPUButtonAction);
        } else {
            this.seatRandomPlaceCPUButton.setEnabled(false);
        }
    }
}
