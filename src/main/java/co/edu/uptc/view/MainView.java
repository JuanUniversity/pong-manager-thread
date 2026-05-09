package co.edu.uptc.view;

import co.edu.uptc.config.PropertiesManager;
import co.edu.uptc.dto.BallSnapshot;
import co.edu.uptc.dto.GameSnapshot;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.util.TimeFormatter;
import co.edu.uptc.view.util.UIConstants;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainView extends JFrame implements ViewInterface {
    private static final PropertiesManager PROPERTIES = PropertiesManager.getInstance();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss");

    private static final UIConstants UI = new UIConstants();

    private PresenterInterface presenter;
    private BoardPanel boardPanel;
    private JLabel startValue;
    private JLabel elapsedValue;
    private JPanel ballStatsPanel;
    private Timer uiTimer;
    private boolean upPressed;
    private boolean downPressed;
    private int paddleVelocity;
    private boolean lastRunning = true;

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

        startValue = createValueLabel("--:--:--");
        elapsedValue = createValueLabel("00:00:00");
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
        ballStatsPanel = buildBallStatsPanel();
        return new PanelBuilder()
                .add(buildSectionTitle(PROPERTIES.getMessage("label.bounces")))
                .add(ballStatsPanel)
                .addSpacing(12)
                .add(buildInfoRow(PROPERTIES.getMessage("label.startTime"), startValue))
                .addSpacing(12)
                .add(buildInfoRow(PROPERTIES.getMessage("label.elapsed"), elapsedValue))
                .addSpacing(12)
                .add(buildSectionTitle(PROPERTIES.getMessage("label.controls")))
                .add(buildControlLine(PROPERTIES.getMessage("label.control.move")))
                .add(buildControlLine(PROPERTIES.getMessage("label.control.speed")))
                .add(buildControlLine(PROPERTIES.getMessage("label.control.pause")))
                .add(buildControlLine(PROPERTIES.getMessage("label.control.addball")))
                .addGlue()
                .build();
    }

    private JPanel buildBallStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    private JLabel buildSectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setAlignmentX(LEFT_ALIGNMENT);
        return title;
    }

    private JLabel buildControlLine(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
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
        JMenu aboutMenu = new JMenu(PROPERTIES.getMessage("menu.about"));
        aboutMenu.add(buildAboutItem());
        bar.add(aboutMenu);
        return bar;
    }

    private JMenuItem buildAboutItem() {
        JMenuItem item = new JMenuItem(PROPERTIES.getMessage("menu.about.author"));
        item.addActionListener(event -> showAboutDialog());
        return item;
    }

    private void bindKeys() {
        registerKeyAction("moveUp", KeyEvent.VK_UP, -UI.PADDLE_STEP);
        registerKeyAction("moveDown", KeyEvent.VK_DOWN, UI.PADDLE_STEP);
        registerAction("speedUpPlus", KeyStroke.getKeyStroke('+'), this::increaseSpeed);
        registerAction("speedUpAdd", KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), this::increaseSpeed);
        registerAction("speedUpEq", KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.SHIFT_DOWN_MASK),
                this::increaseSpeed);
        registerAction("speedDownMinus", KeyStroke.getKeyStroke('-'), this::decreaseSpeed);
        registerAction("speedDownSub", KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), this::decreaseSpeed);
        registerAction("togglePause", KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), this::togglePause);
        registerAction("addBall", KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), this::addBall);
    }

    private void registerAction(String name, KeyStroke keyStroke, Runnable action) {
        InputMap inputMap = boardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = boardPanel.getActionMap();
        inputMap.put(keyStroke, name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
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

    private void increaseSpeed() {
        if (presenter != null) {
            presenter.onIncreaseSpeed();
        }
    }

    private void decreaseSpeed() {
        if (presenter != null) {
            presenter.onDecreaseSpeed();
        }
    }

    private void togglePause() {
        if (presenter != null) {
            presenter.onTogglePause();
        }
    }

    private void addBall() {
        if (presenter != null) {
            presenter.onAddBall();
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
        handleLoss(snapshot);
    }

    private void handleLoss(GameSnapshot snapshot) {
        if (lastRunning && !snapshot.isRunning()) {
            JOptionPane.showMessageDialog(this,
                    PROPERTIES.getMessage("message.lose"),
                    PROPERTIES.getMessage("dialog.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
        lastRunning = snapshot.isRunning();
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                PROPERTIES.getMessage("menu.about.author"),
                PROPERTIES.getMessage("menu.about"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateLabels(GameSnapshot snapshot) {
        startValue.setText(formatStartTime(snapshot.getStartTime()));
        elapsedValue.setText(TimeFormatter.formatElapsed(snapshot.getElapsed()));
        updateBallStats(snapshot.getBalls());
    }

    private void updateBallStats(List<BallSnapshot> balls) {
        ballStatsPanel.removeAll();
        List<BallSnapshot> sorted = new ArrayList<>(balls);
        sorted.sort(Comparator.comparingInt(BallSnapshot::getId));
        for (BallSnapshot ball : sorted) {
            String label = PROPERTIES.getMessage("label.ball", ball.getId());
            JLabel row = new JLabel(label + ": " + ball.getBounceCount());
            row.setAlignmentX(LEFT_ALIGNMENT);
            ballStatsPanel.add(row);
        }
        ballStatsPanel.revalidate();
        ballStatsPanel.repaint();
    }

    private String formatStartTime(LocalTime time) {
        if (time == null) {
            return "--:--:--";
        }
        return TIME_FORMAT.format(time);
    }
}