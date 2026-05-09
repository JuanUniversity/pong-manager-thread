package co.edu.uptc.interfaces;

import co.edu.uptc.dto.GameSnapshot;

public interface ModelInterface {
    void resetGame();

    void addBalls(int count);

    void setBallCount(int count);

    void setSpeedMs(int speedMs);

    void increaseSpeed();

    void decreaseSpeed();

    int getSpeedMs();

    void movePaddle(int delta);

    void togglePause();

    boolean isPaused();

    GameSnapshot getSnapshot();

    boolean isRunning();
}
