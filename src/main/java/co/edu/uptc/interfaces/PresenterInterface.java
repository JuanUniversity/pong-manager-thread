package co.edu.uptc.interfaces;

import co.edu.uptc.dto.GameSnapshot;

public interface PresenterInterface {
    void setModel(ModelInterface model);

    void onStart();

    void onReset();

    void onBallCountChange(int count);

    void onAddBall();

    void onSpeedChange(int speedMs);

    void onIncreaseSpeed();

    void onDecreaseSpeed();

    void onMovePaddle(int delta);

    void onTogglePause();

    GameSnapshot getSnapshot();
}
