package net.mossan.java.reversi.client.boarddrawer.guiboarddrawer;

import net.mossan.java.reversi.client.boarddrawer.BoardDrawer;
import net.mossan.java.reversi.common.jsonExchange.SeatRequest;
import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;
import net.mossan.java.reversi.common.model.PlayerType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GUIBoardDrawer extends JFrame implements BoardDrawer {
    private final int cellSize;
    private final String windowTitle;
    private final Consumer<SeatRequest> seatRequestConsumer;

    private JFrame window;
    private BoardPanel boardPanel;
    private GameInfoPanel gameInfoPanel;

    private Game drawGame = null;
    private String[] seatedPlayerNames = null;
    private PlayerType[][] seatAvailabilities = null;
    private @Nullable DiscType currentDiscType = null;
    private @Nullable Consumer<PlaceableCell> placeCell = null;

    public GUIBoardDrawer(int cellSize, String windowTitle, Consumer<SeatRequest> seatRequestConsumer) {
        assert cellSize > 0 : "cell size must be greater than 0.";

        this.cellSize = cellSize;
        this.windowTitle = windowTitle;
        this.seatRequestConsumer = seatRequestConsumer;

        this.setLayout(null);
    }

    // ObserverEventListener (inherit from BoardDrawer)
    @Override
    public void boardUpdated(Game game) {
        this.drawGame = game;
        if (this.window == null) {
            this.createWindow();
        } else {
            this.boardPanel.repaint();
            this.gameInfoPanel.updateState();
        }
    }

    // PlayerEventListener (inherit from BoardDrawer)
    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        this.drawGame = game;
        this.placeCell = placeCell;
        this.boardPanel.repaint();
        this.gameInfoPanel.updateState();
    }

    // RoomEventListener (inherit from BoardDrawer)
    @Override
    public void onSuccessSeatRequest(SeatRequest seatRequest) {
        if (seatRequest.playerType == PlayerType.NetworkPlayer) {
            this.currentDiscType = seatRequest.discType;
        }
        this.boardPanel.repaint();
        this.gameInfoPanel.updateState();
    }

    @Override
    public void seatStatusUpdated(UUID[] seatedPlayerUUIDs, String[] seatedPlayerNames, PlayerType[][] seatAvailabilities) {
        this.seatedPlayerNames = seatedPlayerNames;
        this.seatAvailabilities = seatAvailabilities;
        if (this.window != null) {
            this.boardPanel.repaint();
            this.gameInfoPanel.updateState();
        }
    }

    private void createWindow() {
        assert this.window == null;
        assert this.drawGame != null;

        // Calculate sizes
        final int boardWidthHeight = this.cellSize * this.drawGame.getBoardRows();
        final int boardMargin = (int) (this.cellSize * 0.55);
        final int gameInfoPanelWidth = (int) (this.cellSize * 0.75 * 5);
        final int windowWidth = 2 * boardMargin + boardWidthHeight + gameInfoPanelWidth;
        final int windowHeight = 2 * boardMargin + boardWidthHeight;

        // Create Window Frame
        this.window = new JFrame();
        this.window.setLocationRelativeTo(null);
        this.window.setLayout(null);
        this.window.setResizable(false);
        this.window.setTitle(this.windowTitle);
        this.window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                synchronized (GUIBoardDrawer.this) {
                    GUIBoardDrawer.this.notifyAll();
                }
            }
        });
        this.window.getContentPane().setBackground(Color.black);
        this.window.getContentPane().setPreferredSize(new Dimension(windowWidth, windowHeight));
        this.window.pack();

        // Create Supplier
        Supplier<Game> gameSupplier = () -> this.drawGame;
        Supplier<Boolean> isMyTurnSupplier = () -> this.drawGame.getCurrentTurn() == this.currentDiscType;
        Supplier<PlayerType[][]> seatAvailabilitySupplier = () -> this.seatAvailabilities;

        // Add BoardPanel
        this.boardPanel = new BoardPanel(this.cellSize, gameSupplier, this::tryPlaceCell);
        this.boardPanel.setBounds(boardMargin, boardMargin, boardWidthHeight, boardWidthHeight);
        this.window.getContentPane().add(this.boardPanel);

        // Add GameInfoPanel
        this.gameInfoPanel = new GameInfoPanel(gameSupplier, isMyTurnSupplier, seatAvailabilitySupplier, this.seatRequestConsumer);
        this.gameInfoPanel.setBounds(2 * boardMargin + boardWidthHeight, 0, gameInfoPanelWidth, windowHeight);
        this.window.getContentPane().add(this.gameInfoPanel);

        this.window.setVisible(true);
    }

    private void tryPlaceCell(int horizontal, int vertical) {
        DiscType currentTurn = this.drawGame.getCurrentTurn();
        if (this.placeCell == null || this.drawGame == null || currentTurn == null || currentTurn != this.currentDiscType)
            return;

        this.drawGame.getPlaceableCellsList(this.drawGame.getCurrentTurn())
                .stream()
                .filter(p -> p.placePoint[0] == horizontal && p.placePoint[1] == vertical)
                .findFirst()
                .ifPresent(p -> {
                    this.placeCell.accept(p);
                    this.placeCell = null;
                });
    }
}

