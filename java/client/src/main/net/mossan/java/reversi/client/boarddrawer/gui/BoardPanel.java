package net.mossan.java.reversi.client.boarddrawer.gui;

import net.mossan.java.reversi.common.model.DiscType;
import net.mossan.java.reversi.common.model.Game;
import net.mossan.java.reversi.common.model.PlaceableCell;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

class BoardPanel extends JPanel implements MouseListener {
    private static final Color BOARD_BACKGROUND_COLOR = new Color(40, 128, 40);
    private static final double DISC_DRAW_RATIO = 0.8;
    private static final float PLACEABLE_DISC_DRAW_OPACITY = 0.2f;

    private final int cellWidthHeight;
    private final Supplier<@Nullable Game> gameSupplier;
    private final BiConsumer<Integer, Integer> tryPlaceCell;

    private final int boardWidthHeight;
    private final int discSize;
    private final int discDrawMarginInCell;

    BoardPanel(int cellWidthHeight, Supplier<@Nullable Game> gameSupplier, BiConsumer<Integer, Integer> tryPlaceCell) {
        this.cellWidthHeight = cellWidthHeight;
        this.gameSupplier = gameSupplier;
        this.tryPlaceCell = tryPlaceCell;

        Game drawGame = this.gameSupplier.get();
        assert drawGame != null;

        this.boardWidthHeight = this.cellWidthHeight * drawGame.getBoardRows();
        this.discSize = (int) Math.round(this.cellWidthHeight * DISC_DRAW_RATIO);
        this.discDrawMarginInCell = (this.cellWidthHeight - this.discSize) / 2;

        this.setOpaque(true);
        this.addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Game drawGame = this.gameSupplier.get();
        if (drawGame == null) return;

        DiscType[][] drawBoard = drawGame.getBoard();
        Graphics2D g2 = (Graphics2D) g;

        // Set Rendering Options
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill Board Background
        g2.setColor(BOARD_BACKGROUND_COLOR);
        g2.fillRect(0, 0, this.boardWidthHeight, this.boardWidthHeight);

        // Draw Board Lines
        g2.setColor(Color.BLACK);
        for (int i = 1; i < drawGame.getBoardRows(); i++) {
            // Horizontal Lines
            g2.drawLine(
                    0,
                    i * this.cellWidthHeight,
                    drawGame.getBoardRows() * this.cellWidthHeight,
                    i * this.cellWidthHeight
            );

            // Vertical Lines
            g2.drawLine(
                    i * this.cellWidthHeight,
                    0,
                    i * this.cellWidthHeight,
                    drawGame.getBoardRows() * this.cellWidthHeight
            );
        }

        // Draw Discs
        for (int i = 0; i < drawBoard.length; i++) {
            for (int j = 0; j < drawBoard.length; j++) {
                // Determine PlayerEventListener Disc Color
                Color discDrawColor;
                if (drawBoard[i][j] == DiscType.Black) {
                    discDrawColor = Color.black;
                } else if (drawBoard[i][j] == DiscType.White) {
                    discDrawColor = Color.white;
                } else {
                    discDrawColor = BOARD_BACKGROUND_COLOR;
                }

                // Calculate Draw Position
                int x = i * this.cellWidthHeight + this.discDrawMarginInCell;
                int y = j * this.cellWidthHeight + this.discDrawMarginInCell;

                // Draw PlayerEventListener Disc
                g2.setColor(discDrawColor);
                g2.fillOval(x, y, this.discSize, this.discSize);
            }
        }

        // Draw Placeable Discs
        if (drawGame.getCurrentTurn() != null) {
            float[] rgb = drawGame.getCurrentTurn().getColor().getRGBColorComponents(null);
            g2.setColor(new Color(rgb[0], rgb[1], rgb[2], PLACEABLE_DISC_DRAW_OPACITY));
            for (PlaceableCell c : drawGame.getPlaceableCellsList(drawGame.getCurrentTurn())) {
                // Calculate Draw Position
                int x = c.placePoint[0] * this.cellWidthHeight + this.discDrawMarginInCell;
                int y = c.placePoint[1] * this.cellWidthHeight + this.discDrawMarginInCell;

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
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Identify clicked cell
            Point point = e.getPoint();
            int x = (int) Math.round(point.getX());
            int y = (int) Math.round(point.getY());
            if (x >= 0 && x <= this.boardWidthHeight && y >= 0 && y <= this.boardWidthHeight) {
                int horizontal = x / this.cellWidthHeight;
                int vertical = y / this.cellWidthHeight;
                this.tryPlaceCell.accept(horizontal, vertical);
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
}

