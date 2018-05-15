package net.mossan.java.reversi.client.boarddrawer;

import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.eventlistener.PlaceableCell;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class GUIBoardDrawer extends JPanel implements MouseListener, BoardDrawerBase {
    private static final Color BOARD_BACKGROUND_COLOR = new Color(40, 128, 40);
    private static final double DISC_DRAW_RATIO = 0.8;
    private static final float PLACEABLE_DISC_DRAW_OPACITY = 0.2f;

    private final int cellSize;
    private final String windowTitle;

    private JFrame window = null;
    private int discSize;
    private int discDrawMarginInCell;
    private int leftRightMargin;
    private int topBottomMargin;
    private int boardSize;
    private @Nullable Game drawGame = null;
    private @Nullable Consumer<PlaceableCell> placeCell = null;

    public GUIBoardDrawer(int cellSize, String windowTitle) {
        assert cellSize > 0 : "cell size must be greater than 0.";

        // Panel Settings
        this.setOpaque(false);
        this.addMouseListener(this);

        this.cellSize = cellSize;
        this.windowTitle = windowTitle;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.window == null || this.drawGame == null) {
            return;
        }

        DiscType[][] drawBoard = this.drawGame.getBoard();
        Graphics2D g2 = (Graphics2D) g;

        // Set Rendering Options
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill Board Background
        g2.setColor(GUIBoardDrawer.BOARD_BACKGROUND_COLOR);
        g2.fillRect(this.leftRightMargin, this.topBottomMargin, this.boardSize, this.boardSize);

        // Draw Board Lines
        g2.setColor(Color.BLACK);
        for (int i = 1; i < this.drawGame.getBoardRows(); i++) {
            // Horizontal Lines
            g2.drawLine(
                    this.leftRightMargin,
                    this.topBottomMargin + this.cellSize * i,
                    this.leftRightMargin + this.cellSize * this.drawGame.getBoardRows(),
                    this.topBottomMargin + this.cellSize * i
            );

            // Vertical Lines
            g2.drawLine(
                    this.leftRightMargin + this.cellSize * i,
                    this.topBottomMargin,
                    this.leftRightMargin + this.cellSize * i,
                    this.topBottomMargin + this.cellSize * this.drawGame.getBoardRows()
            );
        }

        // Draw Discs
        for (int i = 0; i < drawBoard.length; i++) {
            for (int j = 0; j < drawBoard.length; j++) {
                // Determine PlayerEventListener Disc Color
                Color discDrawColor;
                if (drawBoard[i][j] == DiscType.BLACK) {
                    discDrawColor = Color.black;
                } else if (drawBoard[i][j] == DiscType.WHITE) {
                    discDrawColor = Color.white;
                } else {
                    discDrawColor = GUIBoardDrawer.BOARD_BACKGROUND_COLOR;
                }

                // Calculate Draw Position
                int x = this.leftRightMargin + (this.cellSize * i) + this.discDrawMarginInCell;
                int y = this.topBottomMargin + (this.cellSize * j) + this.discDrawMarginInCell;

                // Draw PlayerEventListener Disc
                g2.setColor(discDrawColor);
                g2.fillOval(x, y, this.discSize, this.discSize);
            }
        }

        // Draw Placeable Discs
        if (this.drawGame.getCurrentTurn() != null) {
            float[] rgb = this.drawGame.getCurrentTurn().getColor().getRGBColorComponents(null);
            g2.setColor(new Color(rgb[0], rgb[1], rgb[2], GUIBoardDrawer.PLACEABLE_DISC_DRAW_OPACITY));
            for (PlaceableCell c : this.drawGame.getPlaceableCellsList(this.drawGame.getCurrentTurn())) {
                // Calculate Draw Position
                int x = this.leftRightMargin + (this.cellSize * c.placePoint[0]) + this.discDrawMarginInCell;
                int y = this.topBottomMargin + (this.cellSize * c.placePoint[1]) + this.discDrawMarginInCell;

                // Draw PlayerEventListener Disc
                g2.fillOval(x, y, this.discSize, this.discSize);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Event is triggered when left button is clicked
        if (e.getButton() == MouseEvent.BUTTON1 && this.placeCell != null) {
            // Identify clicked cell
            Point point = e.getPoint();
            int x = (int) Math.round(point.getX()) - leftRightMargin;
            int y = (int) Math.round(point.getY()) - topBottomMargin;
            if (x >= 0 && x <= this.boardSize && y >= 0 && y <= this.boardSize) {
                int horizontal = x / this.cellSize;
                int vertical = y / this.cellSize;
                this.tryPlaceCell(horizontal, vertical);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // BoardDrawerBase
    @Override
    public void boardUpdated(Game game) {
        if (this.window == null) {
            this.createWindow(game);
        } else {
            this.drawGame = game;
            this.repaint();
        }
    }

    @Override
    public void notifyTurn(Game game, Consumer<PlaceableCell> placeCell) {
        this.placeCell = placeCell;
    }

    private void createWindow(Game game) {
        this.drawGame = game;

        if (this.window != null) {
            this.window.dispatchEvent(new WindowEvent(this.window, WindowEvent.WINDOW_CLOSING));
            this.window = null;
        }

        // Create Window Frame
        this.window = new JFrame();
        this.window.setLocationRelativeTo(null);
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

        this.discSize = (int) Math.round(this.cellSize * GUIBoardDrawer.DISC_DRAW_RATIO);
        this.discDrawMarginInCell = (this.cellSize - this.discSize) / 2;
        this.boardSize = this.cellSize * game.getBoardRows();

        // Set Window Frame
        this.window.getContentPane().setPreferredSize(new Dimension(this.cellSize + this.boardSize, this.cellSize + this.boardSize));
        this.window.pack();

        // Calculate Margin
        this.leftRightMargin = (this.window.getContentPane().getWidth() - this.boardSize) / 2;
        this.topBottomMargin = (this.window.getContentPane().getHeight() - this.boardSize) / 2;

        this.window.getContentPane().add(this);
        this.window.setVisible(true);
    }

    private void tryPlaceCell(int horizontal, int vertical) {
        if (this.placeCell == null || this.drawGame == null) return;

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
