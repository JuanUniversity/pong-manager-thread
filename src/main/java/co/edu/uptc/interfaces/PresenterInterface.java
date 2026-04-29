package co.edu.uptc.interfaces;

import co.edu.uptc.dto.GameSnapshot;

public interface PresenterInterface {
    void setModel(ModelInterface model);

    void onStart();

    void onReset();

    void onBallCountChange(int count);

    void onSpeedChange(int speedMs);

    void onMovePaddle(int delta);

    GameSnapshot getSnapshot();
}
