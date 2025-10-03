package ru.dotaclassic.highlights.parser.algorithm;

import ru.dotaclassic.highlights.parser.ReplayTick;

import java.util.List;

public record EventCluster<T extends ClusteringGameEvent>(
        ReplayTick tick,
        List<String> uniqueTargets,
        String attacker
) {
}
