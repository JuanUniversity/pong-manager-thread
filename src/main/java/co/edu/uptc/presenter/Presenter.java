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
    public void onSpeedChange(int speedMs) {
        model.setSpeedMs(speedMs);
    }

    @Override
    public void onMovePaddle(int delta) {
        model.movePaddle(delta);
    }

    @Override
    public GameSnapshot getSnapshot() {
        return model.getSnapshot();
    }
}
