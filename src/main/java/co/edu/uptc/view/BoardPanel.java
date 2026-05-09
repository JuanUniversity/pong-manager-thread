package co.edu.uptc.view;

import co.edu.uptc.dto.BallColor;
import co.edu.uptc.dto.BallSnapshot;
import co.edu.uptc.dto.GameSnapshot;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

public class BoardPanel extends JPanel {
    private static final int GRID_WIDTH = 60;
    private static final int GRID_HEIGHT = 40;
    private static final int SCALE = 15;
    private static final int PADDLE_HEIGHT = 7;
    private static final int PADDLE_X = GRID_WIDTH - 1;

    private static final Color BOARD_COLOR = new Color(0x242529);
    private static final Color BALL_HEAD = new Color(0x66D0E6);
    private static final Color BALL_TRAIL = new Color(0x2D7E9C);
    private static final Color PADDLE_COLOR = new Color(0xF39C12);

    private volatile GameSnapshot snapshot;

    public BoardPanel() {
        setPreferredSize(new Dimension(GRID_WIDTH * SCALE, GRID_HEIGHT * SCALE));
        setBackground(BOARD_COLOR);
    }

    public void setSnapshot(GameSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameSnapshot current = snapshot;
        if (current == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            drawPaddle(g2, current.getPaddleY());
            drawBalls(g2, current.getBalls(), current.isRunning());
        } finally {
            g2.dispose();
        }
    }

    private void drawPaddle(Graphics2D g2, int paddleY) {
        int x = toPixels(PADDLE_X);
        int y = toPixels(paddleY);
        g2.setColor(PADDLE_COLOR);
        g2.fillRect(x, y, SCALE, PADDLE_HEIGHT * SCALE);
    }

    private void drawBalls(Graphics2D g2, List<BallSnapshot> balls, boolean running) {
        for (BallSnapshot ball : balls) {
            if (running) {
                drawTrailBall(g2, ball);
            } else {
                drawSingleBall(g2, ball);
            }
        }
    }

    private void drawTrailBall(Graphics2D g2, BallSnapshot ball) {
        BallColor color = ball.getColor();
        Color trail = color == null ? BALL_TRAIL : color.getTrail();
        Color head = color == null ? BALL_HEAD : color.getHead();
        drawSquare(g2, ball.getOldX(), ball.getOldY(), trail, 2);
        drawSquare(g2, ball.getX(), ball.getY(), head, 2);
    }

    private void drawSingleBall(Graphics2D g2, BallSnapshot ball) {
        BallColor color = ball.getColor();
        Color head = color == null ? BALL_HEAD : color.getHead();
        drawSquare(g2, ball.getX(), ball.getY(), head, 1);
    }

    private void drawSquare(Graphics2D g2, int unitX, int unitY, Color color, int sizeUnits) {
        int size = sizeUnits * SCALE;
        int x = toPixels(unitX) - ((sizeUnits - 1) * SCALE);
        int y = toPixels(unitY) - ((sizeUnits - 1) * SCALE);
        g2.setColor(color);
        g2.fillOval(Math.max(0, x), Math.max(0, y), size, size);
    }

    private int toPixels(int unit) {
        return unit * SCALE;
    }
}
