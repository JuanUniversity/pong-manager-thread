package co.edu.uptc.util;

import java.time.Duration;

public final class TimeFormatter {
    private TimeFormatter() {
    }

    public static String formatElapsed(Duration duration) {
        long seconds = duration == null ? 0 : duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remaining = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remaining);
    }
}
