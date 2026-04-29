package co.edu.uptc.view;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class PanelBuilder {
    private final JPanel panel;

    public PanelBuilder() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }

    public PanelBuilder(LayoutManager layout) {
        panel = new JPanel(layout);
    }

    public PanelBuilder add(Component comp) {
        panel.add(comp);
        return this;
    }

    public PanelBuilder add(Component comp, Object constraints) {
        panel.add(comp, constraints);
        return this;
    }

    public PanelBuilder addSpacing(int height) {
        panel.add(Box.createVerticalStrut(height));
        return this;
    }

    public PanelBuilder addGlue() {
        panel.add(Box.createVerticalGlue());
        return this;
    }

    public PanelBuilder setBorder(int top, int left, int bottom, int right) {
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return this;
    }

    public PanelBuilder setPreferredSize(Dimension size) {
        panel.setPreferredSize(size);
        return this;
    }

    public JPanel build() {
        return panel;
    }
}
