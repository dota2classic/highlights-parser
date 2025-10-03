package ru.dotaclassic.highlights.parser.algorithm;

import ru.dotaclassic.highlights.parser.ReplayTick;

public interface ClusteringGameEvent {
    String attacker();

    ReplayTick tick();

    String target();
}
