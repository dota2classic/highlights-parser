package ru.dotaclassic.highlights.parser.model;

public record KillTiming(int tick, float time, String hero) {
    public ReplayTick toTick() {
        return new ReplayTick(tick, time);
    }
}
