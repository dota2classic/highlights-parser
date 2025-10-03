package ru.dotaclassic.highlights;

import org.junit.jupiter.api.Test;
import ru.dotaclassic.highlights.parser.HighlightJob;
import ru.dotaclassic.highlights.parser.model.HighlightType;
import ru.dotaclassic.highlights.parser.Utils;

import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HighlightsJobTest {

    @Test
    void testNeutralDoubleKill() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/neutral_double_kill.dem"));
        var neutralDoubleKill = highlights.stream().filter(t -> t.type() == HighlightType.QUICK_MULTIKILL && Objects.equals(t.hero(), "neutral")).findFirst();
        assertTrue(neutralDoubleKill.isPresent());
        assertEquals("neutral", neutralDoubleKill.get().hero());
    }

    @Test
    void testUltraKill() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/neutral_double_kill.dem"));
        var ultraKill = highlights.stream().filter(t -> t.type() == HighlightType.MULTIKILL && Objects.equals(t.hero(), "npc_dota_hero_templar_assassin") && t.comment().contains("4")).findFirst();
        assertTrue(ultraKill.isPresent());
        assertEquals("npc_dota_hero_templar_assassin", ultraKill.get().hero());
    }

    @Test
    void testLowHPKill() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/neutral_double_kill.dem"));
        var ultraKill = highlights.stream().filter(t -> t.type() == HighlightType.LOW_HP_KILL).findFirst();
        assertTrue(ultraKill.isPresent());
        assertEquals("npc_dota_hero_beastmaster", ultraKill.get().hero());
    }

    @Test
    void testShakerMultiEchoSlam() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/shaker.dem"));
        var echoSlam = highlights.stream().filter(t -> t.type() == HighlightType.COMBO_SPELL && Objects.equals(t.hero(), "npc_dota_hero_earthshaker")).findFirst();
        assertTrue(echoSlam.isPresent());
        assertEquals("npc_dota_hero_earthshaker", echoSlam.get().hero());
        assertTrue(echoSlam.get().comment().contains("по 3 героям"));
    }


    @Test
    void testPlayerIndex() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/52089.dem"));
        var lowHpKill = highlights.stream().filter(t -> t.type() == HighlightType.LOW_HP_KILL && Objects.equals(t.hero(), "npc_dota_hero_phoenix")).findFirst();
        assertTrue(lowHpKill.isPresent());
        assertEquals(5, lowHpKill.get().heroIndex());
    }

    @Test
    void testTechiesKills() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/techies.dem"));
        highlights.forEach(highlight -> {
            System.out.printf("%s %s: %s%n", Utils.formatGameTime(highlight.start().time()), highlight.hero(), highlight.comment());
        });
    }


    @Test
    void testModifiersAxe() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/axe.dem"));
        assertEquals("modifier_axe_berserkers_call по 2 героям", highlights.getFirst().comment());
    }

    @Test
    void testModifiersVoid() {
        var highlights = new HighlightJob().getHighlights(Paths.get("test/void.dem"));
        highlights.forEach(highlight -> {
            System.out.printf("%s %s%n", Utils.formatGameTime(highlight.start().time()), highlight.comment());
        });
    }
}
