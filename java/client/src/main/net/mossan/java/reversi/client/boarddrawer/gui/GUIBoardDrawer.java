package net.mossan.java.reversi.client.boarddrawer.gui;

import net.mossan.java.reversi.client.boarddrawer.BoardDrawer;
import net.mossan.java.reversi.client.model.GameStateHolder;
import net.mossan.java.reversi.common.message.request.SeatRequest;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;
import net.mossan.java.reversi.common.model.PlayerType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GUIBoardDrawer extends BoardDrawer implements WindowListener {
    private final int cellSize;
    private final String windowTitle;

    private JFrame window;
    private BoardPanel boardPanel;
    private GameInfoPanel gameInfoPanel;

    private @Nullable Consumer<PlaceableCell> placeCell = null;

    public GUIBoardDrawer(int cellSize, String windowTitle, Consumer<SeatRequest> seatRequestConsumer, Supplier<Void> onBoardDrawerClosedCallback) {
        super(seatRequestConsumer, onBoardDrawerClosedCallback);
        assert cellSize > 0 : "cell size must be greater than 0.";

        this.cellSize = cellSize;
        this.windowTitle = windowTitle;
    }

    // Override BoardDrawer Methods
    @Override
    public void update(GameStateHolder gameStateHolder) {
        super.update(gameStateHolder);
        if (this.window == null) {
            this.createWindow();
        } else {
            this.boardPanel.repaint();
            this.gameInfoPanel.updateState();
        }
    }

    @Override
    public void onSuccessSeatRequest(SeatRequest seatRequest) {
        super.onSuccessSeatRequest(seatRequest);
        this.boardPanel.repaint();
        this.gameInfoPanel.updateState();
    }

    @Override
    public void notifyLeavingRoom() {
        if (this.window != null) {
            this.boardPanel = null;
            this.gameInfoPanel = null;
            this.window.dispose();
            this.window = null;
        }
        this.onBoardDrawerClosedCallback.get();
    }

    // PlayerEventListener (inherit from BoardDrawer)
    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        this.placeCell = placeCell;
        this.boardPanel.repaint();
        this.gameInfoPanel.updateState();
    }

    private void createWindow() {
        assert this.window == null;

        GameStateHolder gameStateHolder = this.gameStateHolderWeakReference.get();
        assert gameStateHolder != null;

        // Calculate sizes
        final int boardWidthHeight = this.cellSize * gameStateHolder.getBoardRows();
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
        Supplier<@Nullable Game> gameSupplier = () -> this.gameStateHolderWeakReference.get();
        Supplier<Boolean> isMyTurnSupplier = () -> {
            GameStateHolder gsh = this.gameStateHolderWeakReference.get();
            return gsh != null && gsh.getCurrentTurn() != null && gsh.getCurrentTurn() == this.myDiscType;
        };
        Supplier<PlayerType[]> currentTurnSeatAvailabilitySupplier = () -> {
            GameStateHolder gsh = this.gameStateHolderWeakReference.get();
            if (gsh == null || gsh.getCurrentTurn() == null) {
                return new PlayerType[]{};
            } else {
                return gsh.seatAvailabilities[gsh.getCurrentTurn().getInt()];
            }
        };

        // Add BoardPanel
        this.boardPanel = new BoardPanel(this.cellSize, gameSupplier, this::tryPlaceCell);
        this.boardPanel.setBounds(boardMargin, boardMargin, boardWidthHeight, boardWidthHeight);
        this.window.getContentPane().add(this.boardPanel);

        // Add GameInfoPanel
        this.gameInfoPanel = new GameInfoPanel(gameSupplier, isMyTurnSupplier, currentTurnSeatAvailabilitySupplier, this.seatRequestConsumer);
        this.gameInfoPanel.setBounds(2 * boardMargin + boardWidthHeight, 0, gameInfoPanelWidth, windowHeight);
        this.window.getContentPane().add(this.gameInfoPanel);

        // Add Window Events
        this.window.addWindowListener(this);

        this.window.setVisible(true);
    }

    private void tryPlaceCell(int horizontal, int vertical) {
        @Nullable GameStateHolder gameStateHolder = this.gameStateHolderWeakReference.get();
        if (this.placeCell == null || gameStateHolder == null
                || gameStateHolder.getCurrentTurn() == null || gameStateHolder.getCurrentTurn() != this.myDiscType
        ) {
            return;
        }

        gameStateHolder.getPlaceableCellsList(gameStateHolder.getCurrentTurn())
                .stream()
                .filter(p -> p.placePoint[0] == horizontal && p.placePoint[1] == vertical)
                .findFirst()
                .ifPresent(p -> {
                    placeCell.accept(p);
                    placeCell = null;
                });
    }

    // WindowListener
    @Override
    public void windowOpened(WindowEvent e) {
        // ignore
    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.notifyLeavingRoom();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // ignore
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // ignore
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // ignore
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // ignore
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // ignore
    }
}

