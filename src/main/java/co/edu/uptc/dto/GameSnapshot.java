package co.edu.uptc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GameSnapshot {
    private final List<BallSnapshot> balls;
    private final int paddleY;
    private final boolean running;
    private final LocalTime startTime;
    private final Duration elapsed;
    private final int speedLevel;
    private final int maxSpeedLevels;
    private final int maxBalls;
}
