package co.edu.uptc.dto;

import java.awt.Color;

public enum BallColor {
    CYAN(new Color(0x66D0E6), new Color(0x2D7E9C)),
    MINT(new Color(0x5FD3C0), new Color(0x2C8F80)),
    GOLD(new Color(0xF4B860), new Color(0xB6772A)),
    LIME(new Color(0xA6E06B), new Color(0x6B9E2E));

    private final Color head;
    private final Color trail;

    BallColor(Color head, Color trail) {
        this.head = head;
        this.trail = trail;
    }

    public Color getHead() {
        return head;
    }

    public Color getTrail() {
        return trail;
    }
}
