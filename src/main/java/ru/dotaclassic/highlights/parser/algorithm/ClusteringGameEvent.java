package ru.dotaclassic.highlights.parser.algorithm;

import ru.dotaclassic.highlights.parser.model.ReplayTick;

public interface ClusteringGameEvent {
    String attacker();

    ReplayTick tick();

    String target();
}
