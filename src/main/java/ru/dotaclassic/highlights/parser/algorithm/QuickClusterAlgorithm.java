package ru.dotaclassic.highlights.parser.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuickClusterAlgorithm {
    public static <T extends ClusteringGameEvent> List<EventCluster<T>> getHighlights(List<T> events, long windowSeconds, int stackThreshold) {
        var highlights = new ArrayList<EventCluster<T>>();
        for (int i = 0; i < events.size(); i++) {
            var start = events.get(i);

            Set<String> uniqueTargets = new HashSet<>();
            uniqueTargets.add(start.target());

            var windowStart = start.tick().time();

            for (; i < events.size(); i++) {
                var next = events.get(i);
                if (next.tick().time() - windowStart <= windowSeconds) {
                    uniqueTargets.add(next.target());
                } else {
                    break; // outside time window
                }
            }

            if (uniqueTargets.size() >= stackThreshold) {
                highlights.add(
                        new EventCluster<>(
                                start.tick(),
                                uniqueTargets.stream().toList(),
                                start.attacker()
                        )
                );
            }
        }
        return highlights;

    }
}
