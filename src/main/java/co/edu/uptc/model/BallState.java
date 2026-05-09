package co.edu.uptc.model;

import co.edu.uptc.dto.BallSnapshot;

public class BallState {
    private int x;
    private int y;
    private int oldX;
    private int oldY;
    private int dx;
    private int dy;
    private int racketCollisions;
    
    private boolean active;
    
    public BallState(int x, int y, int dx, int dy, int racketCollisions) {
        this.x = x;
        this.y = y;
        this.oldX = x;
        this.oldY = y;
        this.dx = dx;
        this.dy = dy;
        this.active = true;
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
    
    public synchronized int getRacketCollisions() {
        return racketCollisions;
    }

    public synchronized void setDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public synchronized void setRacketCollisions(int racketCollisions) {
        this.racketCollisions = racketCollisions;
    }

    public synchronized void advanceTo(int newX, int newY) {
        oldX = x;
        oldY = y;
        x = newX;
        y = newY;
    }

    public synchronized BallSnapshot snapshot() {
        return new BallSnapshot(x, y, oldX, oldY, racketCollisions);
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void deactivate() {
        active = false;
    }
}
