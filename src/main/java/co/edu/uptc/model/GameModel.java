package co.edu.uptc.model;

import co.edu.uptc.dto.BallSnapshot;
import co.edu.uptc.dto.GameSnapshot;
import co.edu.uptc.interfaces.ModelInterface;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel implements ModelInterface {
    public static final int GRID_WIDTH = 60;
    public static final int GRID_HEIGHT = 40;
    public static final int PADDLE_HEIGHT = 7;
    public static final int PADDLE_X = GRID_WIDTH - 1;

    private static final int CENTER_X = GRID_WIDTH / 2;
    private static final int CENTER_Y = GRID_HEIGHT / 2;
    private static final int BALL_DELAY_STEP_MS = 1200;
    private static final int SPEED_RAMP_INTERVAL_SEC = 5;
    private static final int[] SPEED_LEVELS_MS = {90, 75, 60, 50, 40};
    private static final int MAX_SPEED_LEVEL = SPEED_LEVELS_MS.length - 1;
    private static final int MAX_BALLS = 5;

    private final List<BallState> balls = new ArrayList<>();
    private final Object stateLock = new Object();
    private final Object pauseLock = new Object();
    private final Random random = new Random();
    private int nextBallId = 1;

    private volatile boolean running;
    private volatile boolean paused;
    private volatile int speedLevelIndex = 2;
    private volatile int paddleY;
    private volatile int desiredBallCount = 1;
    private int ballDelayIndex;
    private LocalTime startTime;
    private Duration elapsedAtStop = Duration.ZERO;
    private Duration pausedDuration = Duration.ZERO;
    private LocalTime pauseStart;

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
        int allowed = Math.min(count, MAX_BALLS - currentBallCount());
        if (allowed <= 0) {
            return;
        }
        for (int i = 0; i < allowed; i++) {
            addBall(createBall());
        }
    }

    @Override
    public void setBallCount(int count) {
        int sanitized = Math.max(1, Math.min(MAX_BALLS, count));
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
        speedLevelIndex = findClosestSpeedLevel(speedMs);
    }

    @Override
    public void increaseSpeed() {
        speedLevelIndex = Math.min(MAX_SPEED_LEVEL, speedLevelIndex + 1);
    }

    @Override
    public void decreaseSpeed() {
        speedLevelIndex = Math.max(0, speedLevelIndex - 1);
    }

    @Override
    public int getSpeedMs() {
        return computeEffectiveSpeed();
    }

    @Override
    public void movePaddle(int delta) {
        if (!running || paused) {
            return;
        }
        synchronized (stateLock) {
            paddleY = clampPaddle(paddleY + delta);
        }
    }

    @Override
    public void togglePause() {
        if (!running) {
            return;
        }
        if (paused) {
            resume();
        } else {
            pause();
        }
    }

    @Override
    public boolean isPaused() {
        return paused;
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

        int resolvedDx = resolveHorizontal(ball, nextX, nextY, dx);
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

    private int resolveHorizontal(BallState ball, int nextX, int nextY, int dx) {
        int resolvedDx = dx;
        if (nextX < 0) {
            resolvedDx = -dx;
        }
        if (isPaddleHit(nextX, nextY, dx)) {
            resolvedDx = -Math.abs(resolvedDx);
            ball.incrementBounceCount();
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
        nextBallId = 1;
        paddleY = CENTER_Y - (PADDLE_HEIGHT / 2);
        ballDelayIndex = 0;
        paused = false;
        pausedDuration = Duration.ZERO;
        pauseStart = null;
        synchronized (stateLock) {
            startTime = LocalTime.now();
            elapsedAtStop = Duration.ZERO;
        }
        running = true;
    }

    private BallState createBall() {
        int dy = random.nextBoolean() ? 1 : -1;
        return new BallState(nextBallId++, CENTER_X, CENTER_Y, -1, dy);
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
            elapsedAtStop = currentElapsed();
        }
        running = false;
        resume();
        synchronized (balls) {
            for (BallState ball : balls) {
                ball.deactivate();
            }
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
                    ? currentElapsed()
                    : elapsedAtStop;
        }
        int effectiveSpeedLevel = computeEffectiveSpeedLevel();
        return new GameSnapshot(ballsSnapshot, paddleSnapshot, runningSnapshot,
                startSnapshot, elapsedSnapshot, effectiveSpeedLevel, MAX_SPEED_LEVEL + 1, MAX_BALLS);
    }

    void waitIfPaused() {
        synchronized (pauseLock) {
            while (paused && running) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void pause() {
        if (paused) {
            return;
        }
        paused = true;
        synchronized (stateLock) {
            pauseStart = LocalTime.now();
        }
    }

    private void resume() {
        if (!paused) {
            return;
        }
        synchronized (stateLock) {
            if (pauseStart != null) {
                pausedDuration = pausedDuration.plus(Duration.between(pauseStart, LocalTime.now()));
            }
            pauseStart = null;
        }
        paused = false;
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    private int computeEffectiveSpeed() {
        Duration elapsed = currentElapsed();
        long steps = elapsed.getSeconds() / SPEED_RAMP_INTERVAL_SEC;
        int rampLevels = (int) steps;
        int effectiveIndex = Math.min(MAX_SPEED_LEVEL, speedLevelIndex + rampLevels);
        return SPEED_LEVELS_MS[effectiveIndex];
    }

    private int computeEffectiveSpeedLevel() {
        Duration elapsed = currentElapsed();
        long steps = elapsed.getSeconds() / SPEED_RAMP_INTERVAL_SEC;
        int rampLevels = (int) steps;
        int effectiveIndex = Math.min(MAX_SPEED_LEVEL, speedLevelIndex + rampLevels);
        return effectiveIndex + 1;
    }

    private int findClosestSpeedLevel(int speedMs) {
        int bestIndex = 0;
        int bestDiff = Math.abs(SPEED_LEVELS_MS[0] - speedMs);
        for (int i = 1; i < SPEED_LEVELS_MS.length; i++) {
            int diff = Math.abs(SPEED_LEVELS_MS[i] - speedMs);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private Duration currentElapsed() {
        synchronized (stateLock) {
            if (startTime == null) {
                return Duration.ZERO;
            }
            Duration elapsed = Duration.between(startTime, LocalTime.now());
            Duration pausedTotal = pausedDuration;
            if (paused && pauseStart != null) {
                pausedTotal = pausedTotal.plus(Duration.between(pauseStart, LocalTime.now()));
            }
            Duration result = elapsed.minus(pausedTotal);
            return result.isNegative() ? Duration.ZERO : result;
        }
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
