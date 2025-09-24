package ru.dotaclassic.highlights.parser;

import skadistats.clarity.model.CombatLogEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComboSpellDetector {
    private static final long WINDOW_SECONDS = 5;
    private final HighlightJob job;


    private record ComboSpellEntry(ReplayTick tick, String attacker, String target) {
    }

    private String ability;
    private ArrayList<ComboSpellEntry> events = new ArrayList<>();


    public ComboSpellDetector(HighlightJob job, String ability) {
        this.ability = ability;
        this.job = job;
    }

    public void onCombatLogDamage(ReplayTick tick, CombatLogEntry cle) {
        var inflictor = cle.getInflictorName();
        if (inflictor == null || !inflictor.equals(ability)) return;
        if (!cle.isTargetHero() || cle.isTargetIllusion()) return;

        events.add(new ComboSpellEntry(tick, cle.getAttackerName(), cle.getTargetName()));
    }

    public List<HighlightDTO> getHighlights() {
        var highlights = new ArrayList<HighlightDTO>();
        for (int i = 0; i < events.size(); i++) {
            var start = events.get(i);
            Set<String> uniqueTargets = new HashSet<>();
            uniqueTargets.add(start.target());

            var windowStart = start.tick.time();

            for (; i < events.size(); i++) {
                var next = events.get(i);
                if (next.tick.time() - windowStart <= WINDOW_SECONDS) {
                    uniqueTargets.add(next.target);
                } else {
                    break; // outside time window
                }
            }

            if (uniqueTargets.size() >= 2) {
                highlights.add(new HighlightDTO(start.tick().tick(), start.tick().time(), start.attacker, job.getHeroIndex(start.attacker), HighlightType.COMBO_SPELL, "%s по %d героям".formatted(ability, uniqueTargets.size())));
            }
        }
        return highlights;
    }
}
