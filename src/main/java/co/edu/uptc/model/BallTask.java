package co.edu.uptc.model;

public class BallTask implements Runnable {
    private final GameModel model;
    private final BallState state;
    private final int initialDelayMs;

    public BallTask(GameModel model, BallState state, int initialDelayMs) {
        this.model = model;
        this.state = state;
        this.initialDelayMs = Math.max(0, initialDelayMs);
    }

    @Override
    public void run() {
        if (!sleepInitialDelay()) {
            return;
        }
        while (state.isActive() && model.isRunning()) {
            model.advanceBall(state);
            sleepForSpeed();
        }
    }

    private boolean sleepInitialDelay() {
        if (initialDelayMs <= 0) {
            return true;
        }
        try {
            Thread.sleep(initialDelayMs);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            state.deactivate();
            return false;
        }
    }

    private void sleepForSpeed() {
        try {
            Thread.sleep(model.getSpeedMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            state.deactivate();
        }
    }
}
