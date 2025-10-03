package ru.dotaclassic.highlights.parser.model;

public record ReplayTick(int tick, float time) {

    public KillTiming kill(String hero) {
        return new KillTiming(tick, time, hero);
    }
}
