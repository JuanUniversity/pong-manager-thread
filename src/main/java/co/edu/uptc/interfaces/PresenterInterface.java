package co.edu.uptc.interfaces;

import co.edu.uptc.dto.GameSnapshot;

public interface PresenterInterface {
    public void setModel(ModelInterface model);

    public void onStart();

    public void onReset();

    public void onBallCountChange(int count);

    public void onAddBall();

    public void onSpeedChange(int speedMs);

    public void onIncreaseSpeed();

    public void onDecreaseSpeed();

    public void onMovePaddle(int delta);

    public void onTogglePause();

    public GameSnapshot getSnapshot();
}