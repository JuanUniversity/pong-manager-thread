package co.edu.uptc.view;

import co.edu.uptc.config.PropertiesManager;
import co.edu.uptc.dto.GameSnapshot;
import co.edu.uptc.interfaces.PresenterInterface;
import co.edu.uptc.interfaces.ViewInterface;
import co.edu.uptc.util.TimeFormatter;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainView extends JFrame implements ViewInterface {
    private static final int UI_REFRESH_MS = 30;
    private static final int PADDLE_STEP = 1;
    private static final int INFO_PANEL_WIDTH = 250;


    private static final Color RESET_BG = new Color(0x55958D);

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final PropertiesManager PROPERTIES = PropertiesManager.getInstance();

    private static final String KEY_APP_TITLE = "app.title";
    private static final String KEY_PANEL_TITLE = "panel.title";
    private static final String KEY_LABEL_BOUNCES = "label.bounces";
    private static final String KEY_LABEL_START = "label.startTime";
    private static final String KEY_LABEL_ELAPSED = "label.elapsed";
    private static final String KEY_BUTTON_RESET = "button.reset";
    private static final String KEY_MENU_MAIN = "menu.main";
    private static final String KEY_MENU_SPEED = "menu.speed";
    private static final String KEY_MENU_BALLS = "menu.balls";
    private static final String KEY_MENU_BALLS_1 = "menu.balls.1";
    private static final String KEY_MENU_BALLS_2 = "menu.balls.2";
    private static final String KEY_MENU_BALLS_3 = "menu.balls.3";
    private static final String KEY_MENU_SPEED_SLOW = "menu.speed.slow";
    private static final String KEY_MENU_SPEED_MEDIUM = "menu.speed.medium";
    private static final String KEY_MENU_SPEED_FAST = "menu.speed.fast";

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
        setTitle(PROPERTIES.getMessage(KEY_APP_TITLE));
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        panel.setPreferredSize(new Dimension(INFO_PANEL_WIDTH,
                boardPanel.getPreferredSize().height));
        bounceValue = createValueLabel("0");
        startValue = createValueLabel("--:--:--");
        elapsedValue = createValueLabel("00:00");
        panel.add(buildInfoHeader(), BorderLayout.NORTH);
        panel.add(buildInfoCenter(), BorderLayout.CENTER);
        panel.add(buildInfoFooter(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInfoHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(buildInfoTitle());
        header.add(Box.createVerticalStrut(16));
        return header;
    }

    private JPanel buildInfoCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(buildInfoRow(PROPERTIES.getMessage(KEY_LABEL_BOUNCES), bounceValue));
        center.add(Box.createVerticalStrut(12));
        center.add(buildInfoRow(PROPERTIES.getMessage(KEY_LABEL_START), startValue));
        center.add(Box.createVerticalStrut(12));
        center.add(buildInfoRow(PROPERTIES.getMessage(KEY_LABEL_ELAPSED), elapsedValue));
        center.add(Box.createVerticalGlue());
        return center;
    }

    private JPanel buildInfoFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.add(buildResetButton());
        return footer;
    }

    private JLabel buildInfoTitle() {
        JLabel title = new JLabel(PROPERTIES.getMessage(KEY_PANEL_TITLE));
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
        row.setMaximumSize(new Dimension(INFO_PANEL_WIDTH - 32, 48));
        JLabel title = new JLabel(label);
        row.add(title, BorderLayout.NORTH);
        row.add(value, BorderLayout.SOUTH);
        return row;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        return label;
    }

    private JButton buildResetButton() {
        JButton button = new JButton(PROPERTIES.getMessage(KEY_BUTTON_RESET));
        button.setBackground(RESET_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 36));
        button.setPreferredSize(new Dimension(180, 36));
        button.addActionListener(event -> presenter.onReset());
        return button;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu(PROPERTIES.getMessage(KEY_MENU_MAIN));
        menu.add(buildSpeedMenu());
        menu.add(buildBallMenu());
        bar.add(menu);
        return bar;
    }

    private JMenu buildSpeedMenu() {
        JMenu menu = new JMenu(PROPERTIES.getMessage(KEY_MENU_SPEED));
        addSpeedItem(menu, PROPERTIES.getMessage(KEY_MENU_SPEED_SLOW, 90), 90);
        addSpeedItem(menu, PROPERTIES.getMessage(KEY_MENU_SPEED_MEDIUM, 60), 60);
        addSpeedItem(menu, PROPERTIES.getMessage(KEY_MENU_SPEED_FAST, 40), 40);
        return menu;
    }

    private void addSpeedItem(JMenu menu, String label, int speedMs) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(event -> presenter.onSpeedChange(speedMs));
        menu.add(item);
    }

    private JMenu buildBallMenu() {
        JMenu menu = new JMenu(PROPERTIES.getMessage(KEY_MENU_BALLS));
        addBallItem(menu, PROPERTIES.getMessage(KEY_MENU_BALLS_1), 1);
        addBallItem(menu, PROPERTIES.getMessage(KEY_MENU_BALLS_2), 2);
        addBallItem(menu, PROPERTIES.getMessage(KEY_MENU_BALLS_3), 3);
        return menu;
    }

    private void addBallItem(JMenu menu, String label, int count) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(event -> presenter.onBallCountChange(count));
        menu.add(item);
    }

    private void bindKeys() {
        InputMap inputMap = boardPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = boardPanel.getActionMap();
        registerMoveAction(inputMap, actionMap, "moveUp", KeyEvent.VK_UP, -PADDLE_STEP);
        registerMoveAction(inputMap, actionMap, "moveDown", KeyEvent.VK_DOWN, PADDLE_STEP);
    }

    private void registerMoveAction(InputMap inputMap, ActionMap actionMap,
            String name, int keyCode, int delta) {
        String pressName = name + "Pressed";
        String releaseName = name + "Released";
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), pressName);
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), releaseName);
        actionMap.put(pressName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setKeyState(delta, true);
            }
        });
        actionMap.put(releaseName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setKeyState(delta, false);
            }
        });
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
            velocity -= PADDLE_STEP;
        }
        if (downPressed) {
            velocity += PADDLE_STEP;
        }
        paddleVelocity = velocity;
    }

    private void applyPaddleMovement() {
        if (presenter != null && paddleVelocity != 0) {
            presenter.onMovePaddle(paddleVelocity);
        }
    }

    private void startTimer() {
        uiTimer = new Timer(UI_REFRESH_MS, event -> refreshUi());
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
