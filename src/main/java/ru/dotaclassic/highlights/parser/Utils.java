package ru.dotaclassic.highlights.parser;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    private static final DateTimeFormatter GAMETIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");


    public static String formatGameTime(float time) {
        Duration gameTimeMillis = Duration.ofMillis((int) (1000.0f * time));

        LocalTime gameTime = LocalTime.MIDNIGHT.plus(gameTimeMillis);

        return "[" + GAMETIME_FORMATTER.format(gameTime) + "]";
    }
}
