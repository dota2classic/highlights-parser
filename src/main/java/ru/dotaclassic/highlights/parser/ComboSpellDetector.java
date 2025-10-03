package ru.dotaclassic.highlights.parser;


import ru.dotaclassic.highlights.parser.algorithm.ClusteringGameEvent;
import ru.dotaclassic.highlights.parser.algorithm.QuickClusterAlgorithm;
import skadistats.clarity.model.CombatLogEntry;

import java.util.ArrayList;
import java.util.List;

public class ComboSpellDetector implements ReplayListener {
    private static final long WINDOW_SECONDS = 5;
    private final HighlightJob job;
    private final int stackCount;


    @Override
    public List<HighlightDTO> getHighlights() {
        var combos = QuickClusterAlgorithm.getHighlights(
                events,
                WINDOW_SECONDS,
                stackCount
        );

        return combos.stream().map(combo -> new HighlightDTO(
                combo.tick(),
                combo.tick(),
                combo.attacker(),
                job.getHeroIndex(combo.attacker()),
                HighlightType.COMBO_SPELL,
                "%s по %d героям".formatted(ability, combo.uniqueTargets().size()))).toList();
    }

    private String ability;
    private ArrayList<ComboSpellEntry> events = new ArrayList<>();


    public ComboSpellDetector(HighlightJob job, String ability, int stackCount) {
        this.ability = ability;
        this.job = job;
        this.stackCount = stackCount;
    }

    @Override
    public void onCombatLogDamage(ReplayTick tick, CombatLogEntry cle) {
        var inflictor = cle.getInflictorName();
        if (inflictor == null || !inflictor.equals(ability)) return;
        if (!cle.isTargetHero() || cle.isTargetIllusion()) return;

        events.add(new ComboSpellEntry(tick, cle.getAttackerName(), cle.getTargetName()));
    }

    private record ComboSpellEntry(ReplayTick tick, String attacker, String target) implements ClusteringGameEvent {
    }
}
