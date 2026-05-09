package co.edu.uptc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BallSnapshot {
    private final int id;
    private final int x;
    private final int y;
    private final int oldX;
    private final int oldY;
    private final int bounceCount;
}
