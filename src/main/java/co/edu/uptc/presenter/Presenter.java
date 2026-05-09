package co.edu.uptc.presenter;

import co.edu.uptc.dto.GameSnapshot;
import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.interfaces.PresenterInterface;

public class Presenter implements PresenterInterface {
    private ModelInterface model;

    @Override
    public void setModel(ModelInterface model) {
        this.model = model;
    }

    @Override
    public void onStart() {
        model.resetGame();
    }

    @Override
    public void onReset() {
        model.resetGame();
    }

    @Override
    public void onBallCountChange(int count) {
        model.setBallCount(count);
    }

    @Override
    public void onAddBall() {
        model.addBalls(1);
    }

    @Override
    public void onSpeedChange(int speedMs) {
        model.setSpeedMs(speedMs);
    }

    @Override
    public void onIncreaseSpeed() {
        model.increaseSpeed();
    }

    @Override
    public void onDecreaseSpeed() {
        model.decreaseSpeed();
    }

    @Override
    public void onMovePaddle(int delta) {
        model.movePaddle(delta);
    }

    @Override
    public void onTogglePause() {
        model.togglePause();
    }

    @Override
    public GameSnapshot getSnapshot() {
        return model.getSnapshot();
    }
}
