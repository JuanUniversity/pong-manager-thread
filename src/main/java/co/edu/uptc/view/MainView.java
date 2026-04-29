package co.edu.uptc.view;

import co.edu.uptc.config.PropertiesManager;
import co.edu.uptc.dto.GameSnapshot;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.util.TimeFormatter;
import co.edu.uptc.view.util.UIConstants;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class MainView extends JFrame implements ViewInterface {
    private static final PropertiesManager PROPERTIES = PropertiesManager.getInstance();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final UIConstants UI = new UIConstants();

    private PresenterInterface presenter;
    private BoardPanel boardPanel;
    private JLabel bounceValue;
    private JLabel startValue;
    private JLabel elapsedValue;
    private Timer uiTimer;
    private boolean upPressed;
    private boolean downPressed;
    private int paddleVelocity;

    @Override
    public void setPresenter(PresenterInterface presenter) {
        this.presenter = presenter;
    }

    @Override
    public void start() {
        SwingUtilities.invokeLater(this::initUi);
    }

    private void initUi() {
        initTheme();
        configureFrame();
        buildContent();
        configureMenu();
        finalizeWindow();
        startUiLoops();
        focusBoard();
        notifyPresenter();
    }

    private void configureFrame() {
        setTitle(PROPERTIES.getMessage("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void buildContent() {
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);
        add(buildInfoPanel(), BorderLayout.EAST);
    }

    private void configureMenu() {
        setJMenuBar(buildMenuBar());
    }

    private void finalizeWindow() {
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void startUiLoops() {
        bindKeys();
        startTimer();
    }

    private void focusBoard() {
        boardPanel.requestFocusInWindow();
    }

    private void notifyPresenter() {
        if (presenter != null) {
            presenter.onStart();
        }
    }

    private void initTheme() {
        try {
            FlatDarculaLaf darculaLaf = new FlatDarculaLaf();
            UIManager.setLookAndFeel(darculaLaf);
        } catch (Exception ex) {
        }
    }

    private JPanel buildInfoPanel() {
        Dimension infoPanelSize = new Dimension(UI.INFO_PANEL_WIDTH,
                boardPanel.getPreferredSize().height);
                
        bounceValue = createValueLabel("0");
        startValue = createValueLabel("--:--:--");
        elapsedValue = createValueLabel("00:00");
        return new PanelBuilder(new BorderLayout())
                .setBorder(UI.INFO_PADDING, UI.INFO_PADDING,
                        UI.INFO_PADDING, UI.INFO_PADDING)
                .setPreferredSize(infoPanelSize)
                .add(buildInfoHeader(), BorderLayout.NORTH)
                .add(buildInfoCenter(), BorderLayout.CENTER)
                .add(buildInfoFooter(), BorderLayout.SOUTH)
                .build();
    }

    private JPanel buildInfoHeader() {
        return new PanelBuilder()
                .add(buildInfoTitle())
                .addSpacing(16)
                .build();
    }

    private JPanel buildInfoCenter() {
        return new PanelBuilder()
                .add(buildInfoRow(PROPERTIES.getMessage("label.bounces"), bounceValue))
                .addSpacing(12)
                .add(buildInfoRow(PROPERTIES.getMessage("label.startTime"), startValue))
                .addSpacing(12)
                .add(buildInfoRow(PROPERTIES.getMessage("label.elapsed"), elapsedValue))
                .addGlue()
                .build();
    }

    private JPanel buildInfoFooter() {
        return new PanelBuilder()
                .add(buildResetButton())
                .build();
    }

    private JLabel buildInfoTitle() {
        JLabel title = new JLabel(PROPERTIES.getMessage("panel.title"));
        title.setAlignmentX(LEFT_ALIGNMENT);
        Font baseFont = title.getFont();
        if (baseFont != null) {
            title.setFont(baseFont.deriveFont(Font.BOLD, 16f));
        }
        return title;
    }

    private JPanel buildInfoRow(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(UI.INFO_PANEL_WIDTH - 32, 48));
        JLabel title = new JLabel(label);
        row.add(title, BorderLayout.NORTH);
        row.add(value, BorderLayout.SOUTH);
        return row;
    }

    private JLabel createValueLabel(String text) {
        return new JLabel(text);
    }

    private JButton buildResetButton() {
        JButton button = new JButton(PROPERTIES.getMessage("button.reset"));
        button.setBackground(UI.RESET_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setMaximumSize(UI.BUTTON_SIZE);
        button.setPreferredSize(UI.BUTTON_SIZE);
        button.addActionListener(event -> presenter.onReset());
        return button;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu(PROPERTIES.getMessage("menu.main"));
        menu.add(buildSpeedMenu());
        menu.add(buildBallMenu());
        bar.add(menu);
        return bar;
    }

    private JMenu buildSpeedMenu() {
        JMenu menu = new JMenu(PROPERTIES.getMessage("menu.speed"));
        addMenuItem(menu, PROPERTIES.getMessage("menu.speed.slow", 90), 90,
                presenter::onSpeedChange);
        addMenuItem(menu, PROPERTIES.getMessage("menu.speed.medium", 60), 60,
                presenter::onSpeedChange);
        addMenuItem(menu, PROPERTIES.getMessage("menu.speed.fast", 40), 40,
                presenter::onSpeedChange);
        return menu;
    }

    private JMenu buildBallMenu() {
        JMenu menu = new JMenu(PROPERTIES.getMessage("menu.balls"));
        addMenuItem(menu, PROPERTIES.getMessage("menu.balls.1"), 1,
                presenter::onBallCountChange);
        addMenuItem(menu, PROPERTIES.getMessage("menu.balls.2"), 2,
                presenter::onBallCountChange);
        addMenuItem(menu, PROPERTIES.getMessage("menu.balls.3"), 3,
                presenter::onBallCountChange);
        return menu;
    }

    private <T> void addMenuItem(JMenu menu, String label, T value, Consumer<T> action) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(e -> action.accept(value));
        menu.add(item);
    }

    private void bindKeys() {
        registerKeyAction("moveUp", KeyEvent.VK_UP, -UI.PADDLE_STEP);
        registerKeyAction("moveDown", KeyEvent.VK_DOWN, UI.PADDLE_STEP);
    }

    private void registerKeyAction(String name, int keyCode, int delta) {
        InputMap inputMap = boardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = boardPanel.getActionMap();

        String pressName = name + "Pressed";
        String releaseName = name + "Released";

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), pressName);
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), releaseName);

        actionMap.put(pressName, createKeyAction(delta, true));
        actionMap.put(releaseName, createKeyAction(delta, false));
    }

    private AbstractAction createKeyAction(int delta, boolean pressed) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setKeyState(delta, pressed);
            }
        };
    }

    private void setKeyState(int delta, boolean pressed) {
        if (delta < 0) {
            upPressed = pressed;
        } else {
            downPressed = pressed;
        }
        updatePaddleVelocity();
        if (pressed) {
            applyPaddleMovement();
        }
    }

    private void updatePaddleVelocity() {
        int velocity = 0;
        if (upPressed) {
            velocity -= UI.PADDLE_STEP;
        }
        if (downPressed) {
            velocity += UI.PADDLE_STEP;
        }
        paddleVelocity = velocity;
    }

    private void applyPaddleMovement() {
        if (presenter != null && paddleVelocity != 0) {
            presenter.onMovePaddle(paddleVelocity);
        }
    }

    private void startTimer() {
        uiTimer = new Timer(UI.REFRESH_MS, event -> refreshUi());
        uiTimer.start();
    }

    private void refreshUi() {
        if (presenter == null) {
            return;
        }
        applyPaddleMovement();
        GameSnapshot snapshot = presenter.getSnapshot();
        boardPanel.setSnapshot(snapshot);
        updateLabels(snapshot);
        boardPanel.repaint();
    }

    private void updateLabels(GameSnapshot snapshot) {
        bounceValue.setText(String.valueOf(snapshot.getBounceCount()));
        startValue.setText(formatStartTime(snapshot.getStartTime()));
        elapsedValue.setText(TimeFormatter.formatElapsed(snapshot.getElapsed()));
    }

    private String formatStartTime(LocalTime time) {
        if (time == null) {
            return "--:--:--";
        }
        return TIME_FORMAT.format(time);
    }
}