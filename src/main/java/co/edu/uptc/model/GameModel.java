package co.edu.uptc.model;

import co.edu.uptc.dto.BallSnapshot;
import co.edu.uptc.dto.GameSnapshot;
import co.edu.uptc.interfaces.ModelInterface;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameModel implements ModelInterface {
    public static final int GRID_WIDTH = 60;
    public static final int GRID_HEIGHT = 40;
    public static final int PADDLE_HEIGHT = 7;
    public static final int PADDLE_X = GRID_WIDTH - 1;

    private static final int CENTER_X = GRID_WIDTH / 2;
    private static final int CENTER_Y = GRID_HEIGHT / 2;
    private static final int MIN_SPEED_MS = 5;
    private static final int BALL_DELAY_STEP_MS = 1200;

    private final List<BallState> balls = new ArrayList<>();
    private final Object stateLock = new Object();
    private final Random random = new Random();
    private final AtomicInteger bounceCount = new AtomicInteger();

    private volatile boolean running;
    private volatile int speedMs = 60;
    private volatile int paddleY;
    private volatile int desiredBallCount = 1;
    private int ballDelayIndex;
    private LocalTime startTime;
    private Duration elapsedAtStop = Duration.ZERO;

    @Override
    public void resetGame() {
        stopAllBalls();
        clearBalls();
        initState();
        addBalls(desiredBallCount);
    }

    @Override
    public void addBalls(int count) {
        if (!running || count <= 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            addBall(createBall());
        }
    }

    @Override
    public void setBallCount(int count) {
        int sanitized = Math.max(1, count);
        desiredBallCount = sanitized;
        if (!running) {
            return;
        }
        int current = currentBallCount();
        if (sanitized > current) {
            addBalls(sanitized - current);
        } else if (sanitized < current) {
            removeBalls(current - sanitized);
        }
    }

    @Override
    public void setSpeedMs(int speedMs) {
        this.speedMs = Math.max(MIN_SPEED_MS, speedMs);
    }

    @Override
    public int getSpeedMs() {
        return speedMs;
    }

    @Override
    public void movePaddle(int delta) {
        if (!running) {
            return;
        }
        synchronized (stateLock) {
            paddleY = clampPaddle(paddleY + delta);
        }
    }

    @Override
    public GameSnapshot getSnapshot() {
        List<BallSnapshot> ballSnapshots = snapshotBalls();
        boolean runningSnapshot = running;
        return snapshotState(ballSnapshots, runningSnapshot);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    void advanceBall(BallState ball) {
        if (!running) {
            return;
        }
        int x = ball.getX();
        int y = ball.getY();
        int dx = ball.getDx();
        int dy = ball.getDy();
        int nextY = resolveVertical(y, dy);
        dy = resolveVerticalDirection(y, dy);
        int nextX = x + dx;
        int resolvedDx = resolveHorizontal(nextX, nextY, dx);
        nextX = x + resolvedDx;
        if (nextX >= GRID_WIDTH) {
            ball.advanceTo(GRID_WIDTH - 1, nextY);
            stopAllBalls();
            return;
        }
        ball.setDirection(resolvedDx, dy);
        ball.advanceTo(nextX, nextY);
    }

    private int resolveVertical(int y, int dy) {
        int nextY = y + dy;
        if (nextY < 0 || nextY >= GRID_HEIGHT) {
            return y - dy;
        }
        return nextY;
    }

    private int resolveVerticalDirection(int y, int dy) {
        int nextY = y + dy;
        if (nextY < 0 || nextY >= GRID_HEIGHT) {
            return -dy;
        }
        return dy;
    }

    private int resolveHorizontal(int nextX, int nextY, int dx) {
        int resolvedDx = dx;
        if (nextX < 0) {
            resolvedDx = -dx;
        }
        if (isPaddleHit(nextX, nextY, dx)) {
            resolvedDx = -Math.abs(resolvedDx);
            bounceCount.incrementAndGet();
        }
        return resolvedDx;
    }

    private boolean isPaddleHit(int nextX, int nextY, int dx) {
        if (dx <= 0 || nextX != PADDLE_X) {
            return false;
        }
        int top = paddleY;
        return nextY >= top && nextY < top + PADDLE_HEIGHT;
    }

    private void initState() {
        bounceCount.set(0);
        paddleY = CENTER_Y - (PADDLE_HEIGHT / 2);
        ballDelayIndex = 0;
        synchronized (stateLock) {
            startTime = LocalTime.now();
            elapsedAtStop = Duration.ZERO;
        }
        running = true;
    }

    private BallState createBall() {
        int dy = random.nextBoolean() ? 1 : -1;
        return new BallState(CENTER_X, CENTER_Y, -1, dy);
    }

    private void addBall(BallState ball) {
        int delayMs = nextBallDelayMs();
        synchronized (balls) {
            balls.add(ball);
        }
        Thread thread = new Thread(new BallTask(this, ball, delayMs), buildBallName());
        thread.setDaemon(true);
        thread.start();
    }

    private int nextBallDelayMs() {
        int delay = (ballDelayIndex % 4) * BALL_DELAY_STEP_MS;
        ballDelayIndex++;
        return delay;
    }

    private String buildBallName() {
        return "Ball-" + System.nanoTime();
    }

    private void stopAllBalls() {
        if (!running && balls.isEmpty()) {
            return;
        }
        if (running) {
            elapsedAtStop = elapsedFromStart();
        }
        running = false;
        synchronized (balls) {
            for (BallState ball : balls) {
                ball.deactivate();
            }
        }
    }

    private Duration elapsedFromStart() {
        synchronized (stateLock) {
            if (startTime == null) {
                return Duration.ZERO;
            }
            return Duration.between(startTime, LocalTime.now());
        }
    }

    private void clearBalls() {
        synchronized (balls) {
            balls.clear();
        }
    }

    private int currentBallCount() {
        synchronized (balls) {
            return balls.size();
        }
    }

    private void removeBalls(int count) {
        synchronized (balls) {
            for (int i = 0; i < count && !balls.isEmpty(); i++) {
                BallState ball = balls.remove(balls.size() - 1);
                ball.deactivate();
            }
        }
    }

    private List<BallSnapshot> snapshotBalls() {
        List<BallSnapshot> snapshots = new ArrayList<>();
        synchronized (balls) {
            for (BallState ball : balls) {
                snapshots.add(ball.snapshot());
            }
        }
        return List.copyOf(snapshots);
    }

    private GameSnapshot snapshotState(List<BallSnapshot> ballsSnapshot, boolean runningSnapshot) {
        int paddleSnapshot;
        LocalTime startSnapshot;
        Duration elapsedSnapshot;
        synchronized (stateLock) {
            paddleSnapshot = paddleY;
            startSnapshot = startTime;
            elapsedSnapshot = runningSnapshot && startSnapshot != null
                    ? Duration.between(startSnapshot, LocalTime.now())
                    : elapsedAtStop;
        }
        return new GameSnapshot(ballsSnapshot, paddleSnapshot, runningSnapshot,
                bounceCount.get(), startSnapshot, elapsedSnapshot);
    }

    private int clampPaddle(int value) {
        int max = GRID_HEIGHT - PADDLE_HEIGHT;
        if (value < 0) {
            return 0;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
