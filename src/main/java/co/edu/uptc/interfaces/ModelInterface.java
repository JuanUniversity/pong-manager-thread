package co.edu.uptc.interfaces;

import co.edu.uptc.dto.GameSnapshot;

public interface ModelInterface {
    public void resetGame();

    public void addBalls(int count);

    public void setBallCount(int count);

    public void setSpeedMs(int speedMs);

    public void increaseSpeed();

    public void decreaseSpeed();

    public int getSpeedMs();

    public void movePaddle(int delta);

    public void togglePause();

    public boolean isPaused();

    public GameSnapshot getSnapshot();

    public boolean isRunning();
}