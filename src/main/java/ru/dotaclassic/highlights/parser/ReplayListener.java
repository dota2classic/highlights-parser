package ru.dotaclassic.highlights.parser;

import ru.dotaclassic.highlights.parser.model.HighlightDTO;
import ru.dotaclassic.highlights.parser.model.ReplayTick;
import skadistats.clarity.model.CombatLogEntry;
import skadistats.clarity.model.Entity;
import skadistats.clarity.wire.dota.common.proto.DOTAModifiers;

import java.util.Collection;

public interface ReplayListener {

    default void onCombatLogDamage(ReplayTick tick, CombatLogEntry cle) {
    }

    default void onModifierEvent(ReplayTick tick,
                                 String modifierName,
                                 Entity caster,
                                 Entity target,
                                 Entity ability,
                                 DOTAModifiers.CDOTAModifierBuffTableEntry entry) {
    }


    Collection<HighlightDTO> getHighlights();
}
