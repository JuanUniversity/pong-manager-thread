package co.edu.uptc.interfaces;

import co.edu.uptc.dto.GameSnapshot;

public interface ModelInterface {
    String exec() throws Exception;

    void resetGame();

    void addBalls(int count);

    void setBallCount(int count);

    void setSpeedMs(int speedMs);

    int getSpeedMs();

    void movePaddle(int delta);

    GameSnapshot getSnapshot();

    boolean isRunning();
}
