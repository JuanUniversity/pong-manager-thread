package co.edu.uptc.model;

import co.edu.uptc.dto.BallColor;
import co.edu.uptc.dto.BallSnapshot;

public class BallState {
    private final int id;
    private final BallColor color;
    private int x;
    private int y;
    private int oldX;
    private int oldY;
    private int dx;
    private int dy;
    private int bounceCount;
    
    private boolean active;
    
    public BallState(int id, int x, int y, int dx, int dy, BallColor color) {
        this.id = id;
        this.color = color;
        this.x = x;
        this.y = y;
        this.oldX = x;
        this.oldY = y;
        this.dx = dx;
        this.dy = dy;
        this.active = true;
    }

    public synchronized int getId() {
        return id;
    }

    public synchronized BallColor getColor() {
        return color;
    }
    
    public synchronized int getX() {
        return x;
    }

    public synchronized int getY() {
        return y;
    }
    
    public synchronized int getDx() {
        return dx;
    }
    
    public synchronized int getDy() {
        return dy;
    }
    
    public synchronized int getBounceCount() {
        return bounceCount;
    }

    public synchronized void setDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public synchronized void incrementBounceCount() {
        bounceCount++;
    }

    public synchronized void advanceTo(int newX, int newY) {
        oldX = x;
        oldY = y;
        x = newX;
        y = newY;
    }

    public synchronized BallSnapshot snapshot() {
        return new BallSnapshot(id, x, y, oldX, oldY, color, bounceCount);
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void deactivate() {
        active = false;
    }
}
