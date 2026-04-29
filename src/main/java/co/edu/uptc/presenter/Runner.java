package co.edu.uptc.presenter;

import co.edu.uptc.interfaces.ModelInterface;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.model.GameModel;
import co.edu.uptc.view.MainView;

public class Runner {
    private PresenterInterface presenter;
    private ModelInterface model;
    private ViewInterface view;

    private void makeMvp() {
        presenter = new Presenter();
        model = new GameModel();
        view = new MainView();
        presenter.setModel(model);
        view.setPresenter(presenter);
    }

    public void start() {
        makeMvp();
        view.start();
    }
}
