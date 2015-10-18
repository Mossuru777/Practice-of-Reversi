package net.mossan.java.reversi.component;

import javax.swing.*;
import java.awt.*;

public class BoardDrawer extends JPanel {
    private final int CELL_SIZE;
    private final int BOARD_ROWS;
    private final int LEFT_RIGHT_MARGIN;
    private final int TOP_BOTTOM_MARGIN;
    private final int BOARD_SIZE;
    private final JFrame window;

    public BoardDrawer(final int board_rows, final int cell_size, final String window_title) {
        // Arguments Check
        assert board_rows > 0 && board_rows % 2 == 0 : "board rows must be greater than 0 and divisible by 2.";
        assert cell_size > 0 : "cell size must be greater than 0.";

        // Board Settings
        this.CELL_SIZE = cell_size;
        this.BOARD_ROWS = board_rows;
        this.BOARD_SIZE = CELL_SIZE * BOARD_ROWS;

        // Create Window Frame
        this.window = new JFrame();
        this.window.getContentPane().add(this);
        this.window.getContentPane().setPreferredSize(new Dimension(CELL_SIZE + BOARD_SIZE, CELL_SIZE + BOARD_SIZE));
        this.window.setLocationRelativeTo(null);
        this.window.setResizable(false);
        this.window.setTitle(window_title);
        this.window.setBackground(Color.BLACK);
        this.window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.window.pack();
        this.window.setVisible(true);

        // Calculate Margin
        this.LEFT_RIGHT_MARGIN = (this.window.getContentPane().getWidth() - BOARD_SIZE) / 2;
        this.TOP_BOTTOM_MARGIN = (this.window.getContentPane().getHeight() - BOARD_SIZE) / 2;
    }

    public void paintComponent(Graphics g) {
        // Create Buffer Image
        Image buffer_image = createImage(this.window.getContentPane().getWidth(), this.window.getContentPane().getHeight());
        Graphics2D buffer_graphics = (Graphics2D) buffer_image.getGraphics();

        // Set Rendering Options
        buffer_graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        buffer_graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fill Board Background
        buffer_graphics.setColor(new Color(40, 128, 40));
        buffer_graphics.fillRect(LEFT_RIGHT_MARGIN, TOP_BOTTOM_MARGIN, BOARD_SIZE, BOARD_SIZE);

        // Draw Board Lines
        buffer_graphics.setColor(Color.BLACK);
        for (int i = 1; i < BOARD_ROWS; i++) {
            // Horizontal Lines
            buffer_graphics.drawLine(
                    LEFT_RIGHT_MARGIN,
                    TOP_BOTTOM_MARGIN + CELL_SIZE * i,
                    LEFT_RIGHT_MARGIN + CELL_SIZE * BOARD_ROWS,
                    TOP_BOTTOM_MARGIN + CELL_SIZE * i
            );

            // Vertical Lines
            buffer_graphics.drawLine(
                    LEFT_RIGHT_MARGIN + CELL_SIZE * i,
                    TOP_BOTTOM_MARGIN,
                    LEFT_RIGHT_MARGIN + CELL_SIZE * i,
                    TOP_BOTTOM_MARGIN + CELL_SIZE * BOARD_ROWS
            );
        }

        // Draw Buffer Image to Panel
        g.drawImage(buffer_image, 0, 0, this);
    }
}
