package co.edu.uptc.util;

import java.time.Duration;

public final class TimeFormatter {
    private TimeFormatter() {
    }

    public static String formatElapsed(Duration duration) {
        long seconds = duration == null ? 0 : duration.getSeconds();
        long minutes = seconds / 60;
        long remaining = seconds % 60;
        return String.format("%02d:%02d", minutes, remaining);
    }
}
