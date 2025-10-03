package ru.dotaclassic.highlights.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dotaclassic.highlights.parser.algorithm.ClusteringGameEvent;
import ru.dotaclassic.highlights.parser.algorithm.QuickClusterAlgorithm;
import ru.dotaclassic.highlights.parser.model.HighlightDTO;
import ru.dotaclassic.highlights.parser.model.HighlightType;
import ru.dotaclassic.highlights.parser.model.ReplayTick;
import skadistats.clarity.model.Entity;
import skadistats.clarity.wire.dota.common.proto.DOTAModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.dotaclassic.highlights.parser.Utils.dtClassNameToHeroName;
import static ru.dotaclassic.highlights.parser.Utils.formatGameTime;

public class ComboModifierDetector implements ReplayListener {

    private static final long WINDOW_SECONDS = 1;
    private final Logger log = LoggerFactory.getLogger(ComboModifierDetector.class.getPackage().getClass());
    private final HighlightJob job;
    private final int stackCount;
    private String modifier;
    private ArrayList<ComboSpellEntry> events = new ArrayList<>();

    public ComboModifierDetector(HighlightJob job, String modifier, int stackCount) {
        this.modifier = modifier;
        this.job = job;
        this.stackCount = stackCount;
    }

    @Override
    public void onModifierEvent(ReplayTick tick,
                                String modifierName,
                                Entity caster,
                                Entity target,
                                Entity ability,
                                DOTAModifiers.CDOTAModifierBuffTableEntry entry) {
        if (!Objects.equals(modifier, modifierName)) return;
        if (!target.getDtClass().getDtName().toLowerCase().contains("hero")) return;
        // Only heroes here
        var time = formatGameTime(tick.time());

        if (target.getProperty("m_iTeamNum") == caster.getProperty("m_iTeamNum")) {
            return;
        }


        var cse = new ComboSpellEntry(tick,
                dtClassNameToHeroName(caster.getDtClass().getDtName()),
                dtClassNameToHeroName(target.getDtClass().getDtName()));
        events.add(cse);
    }

    public List<HighlightDTO> getHighlights() {


        var combos = QuickClusterAlgorithm.getHighlights(
                events,
                WINDOW_SECONDS,
                stackCount
        );

        return combos.stream().map(combo -> new HighlightDTO(
                        combo.tick(),
                        combo.tick(),
                        modifier,
                        job.getHeroIndex(combo.attacker()),
                        HighlightType.COMBO_SPELL,
                        "%s по %d героям".formatted(modifier, combo.uniqueTargets().size())))
                .toList();
    }

    private record ComboSpellEntry(ReplayTick tick, String attacker, String target) implements ClusteringGameEvent {
    }
}
