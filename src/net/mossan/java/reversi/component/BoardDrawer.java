package net.mossan.java.reversi.component;

import net.mossan.java.reversi.model.Disc;
import net.mossan.java.reversi.model.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class BoardDrawer extends JPanel implements MouseListener {
    private static final Color BOARD_BACKGROUND_COLOR = new Color(40, 128, 40);
    private static final double DISC_DRAW_RATIO = 0.8;

    private final JFrame window;

    private int BOARD_ROWS;
    private int CELL_SIZE;
    private int DISC_SIZE;
    private int DISC_DRAW_MARGIN_IN_CELL;
    private int LEFT_RIGHT_MARGIN;
    private int TOP_BOTTOM_MARGIN;
    private int BOARD_SIZE;
    private Game drawGame;

    public BoardDrawer(Game game, int cell_size, String window_title) {
        assert cell_size > 0 : "cell size must be greater than 0.";

        // Panel Settings
        setOpaque(false);
        addMouseListener(this);

        // Create Window Frame
        this.window = new JFrame();
        this.window.setLocationRelativeTo(null);
        this.window.setResizable(false);
        this.window.setTitle(window_title);
        this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.window.getContentPane().setBackground(Color.black);
        setGame(game, cell_size);
        this.window.getContentPane().add(this);
        this.window.setVisible(true);
    }

    public void setGame(Game game, int cell_size) {
        // Board Settings
        this.drawGame = game;
        this.BOARD_ROWS = game.getBoard().length;
        this.CELL_SIZE = cell_size;
        this.DISC_SIZE = (int) Math.round(CELL_SIZE * DISC_DRAW_RATIO);
        this.DISC_DRAW_MARGIN_IN_CELL = (CELL_SIZE - DISC_SIZE) / 2;
        this.BOARD_SIZE = CELL_SIZE * BOARD_ROWS;

        // Set Window Frame
        this.window.getContentPane().setPreferredSize(new Dimension(CELL_SIZE + BOARD_SIZE, CELL_SIZE + BOARD_SIZE));
        this.window.pack();

        // Calculate Margin
        this.LEFT_RIGHT_MARGIN = (this.window.getContentPane().getWidth() - BOARD_SIZE) / 2;
        this.TOP_BOTTOM_MARGIN = (this.window.getContentPane().getHeight() - BOARD_SIZE) / 2;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Disc[][] drawBoard = drawGame.getBoard();
        Graphics2D g2 = (Graphics2D) g;

        // Set Rendering Options
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill Board Background
        g2.setColor(BOARD_BACKGROUND_COLOR);
        g2.fillRect(LEFT_RIGHT_MARGIN, TOP_BOTTOM_MARGIN, BOARD_SIZE, BOARD_SIZE);

        // Draw Board Lines
        g2.setColor(Color.BLACK);
        for (int i = 1; i < BOARD_ROWS; i++) {
            // Horizontal Lines
            g2.drawLine(
                    LEFT_RIGHT_MARGIN,
                    TOP_BOTTOM_MARGIN + CELL_SIZE * i,
                    LEFT_RIGHT_MARGIN + CELL_SIZE * BOARD_ROWS,
                    TOP_BOTTOM_MARGIN + CELL_SIZE * i
            );

            // Vertical Lines
            g2.drawLine(
                    LEFT_RIGHT_MARGIN + CELL_SIZE * i,
                    TOP_BOTTOM_MARGIN,
                    LEFT_RIGHT_MARGIN + CELL_SIZE * i,
                    TOP_BOTTOM_MARGIN + CELL_SIZE * BOARD_ROWS
            );
        }

        // Draw Discs
        for (int i = 0; i < drawBoard.length; i++) {
            for (int j = 0; j < drawBoard.length; j++) {
                // Disc Color
                Color discDrawColor = null;
                switch (drawBoard[i][j]) {
                    case BLACK:
                        discDrawColor = Color.black;
                        break;
                    case WHITE:
                        discDrawColor = Color.white;
                        break;
                    case NONE:
                    default:
                        discDrawColor = BOARD_BACKGROUND_COLOR;
                        break;
                }

                // Calculate Draw Position
                int x = LEFT_RIGHT_MARGIN + (CELL_SIZE * i) + DISC_DRAW_MARGIN_IN_CELL;
                int y = TOP_BOTTOM_MARGIN + (CELL_SIZE * j) + DISC_DRAW_MARGIN_IN_CELL;

                // Draw Disc
                g2.setColor(discDrawColor);
                g2.fillOval(x, y, DISC_SIZE, DISC_SIZE);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Event is triggered when left button is clicked
        if (drawGame != null && e.getButton() == MouseEvent.BUTTON1) {
            // Identify clicked cell
            Point p = e.getPoint();
            int x = (int) Math.round(p.getX()) - LEFT_RIGHT_MARGIN;
            int y = (int) Math.round(p.getY()) - TOP_BOTTOM_MARGIN;
            if (x >= 0 && x <= BOARD_SIZE && y >= 0 && y <= BOARD_SIZE) {
                int i = x / CELL_SIZE;
                int j = y / CELL_SIZE;

                System.out.println(i + "," + j + " clicked.");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
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
