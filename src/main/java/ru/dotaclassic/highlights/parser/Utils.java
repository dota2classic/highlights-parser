package ru.dotaclassic.highlights.parser;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Utils {
    private static final DateTimeFormatter GAMETIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static final Map<String, Integer> heroNameToId = Map.ofEntries(
            Map.entry("npc_dota_hero_antimage", 1),
            Map.entry("npc_dota_hero_axe", 2),
            Map.entry("npc_dota_hero_bane", 3),
            Map.entry("npc_dota_hero_bloodseeker", 4),
            Map.entry("npc_dota_hero_crystal_maiden", 5),
            Map.entry("npc_dota_hero_drow_ranger", 6),
            Map.entry("npc_dota_hero_earthshaker", 7),
            Map.entry("npc_dota_hero_juggernaut", 8),
            Map.entry("npc_dota_hero_mirana", 9),
            Map.entry("npc_dota_hero_nevermore", 11),
            Map.entry("npc_dota_hero_morphling", 10),
            Map.entry("npc_dota_hero_phantom_lancer", 12),
            Map.entry("npc_dota_hero_puck", 13),
            Map.entry("npc_dota_hero_pudge", 14),
            Map.entry("npc_dota_hero_razor", 15),
            Map.entry("npc_dota_hero_sand_king", 16),
            Map.entry("npc_dota_hero_storm_spirit", 17),
            Map.entry("npc_dota_hero_sven", 18),
            Map.entry("npc_dota_hero_tiny", 19),
            Map.entry("npc_dota_hero_vengefulspirit", 20),
            Map.entry("npc_dota_hero_windrunner", 21),
            Map.entry("npc_dota_hero_zuus", 22),
            Map.entry("npc_dota_hero_kunkka", 23),
            Map.entry("npc_dota_hero_lina", 25),
            Map.entry("npc_dota_hero_lich", 31),
            Map.entry("npc_dota_hero_lion", 26),
            Map.entry("npc_dota_hero_shadow_shaman", 27),
            Map.entry("npc_dota_hero_slardar", 28),
            Map.entry("npc_dota_hero_tidehunter", 29),
            Map.entry("npc_dota_hero_witch_doctor", 30),
            Map.entry("npc_dota_hero_riki", 32),
            Map.entry("npc_dota_hero_enigma", 33),
            Map.entry("npc_dota_hero_tinker", 34),
            Map.entry("npc_dota_hero_sniper", 35),
            Map.entry("npc_dota_hero_necrolyte", 36),
            Map.entry("npc_dota_hero_warlock", 37),
            Map.entry("npc_dota_hero_beastmaster", 38),
            Map.entry("npc_dota_hero_queenofpain", 39),
            Map.entry("npc_dota_hero_venomancer", 40),
            Map.entry("npc_dota_hero_faceless_void", 41),
            Map.entry("npc_dota_hero_skeleton_king", 42),
            Map.entry("npc_dota_hero_death_prophet", 43),
            Map.entry("npc_dota_hero_phantom_assassin", 44),
            Map.entry("npc_dota_hero_pugna", 45),
            Map.entry("npc_dota_hero_templar_assassin", 46),
            Map.entry("npc_dota_hero_viper", 47),
            Map.entry("npc_dota_hero_luna", 48),
            Map.entry("npc_dota_hero_dragon_knight", 49),
            Map.entry("npc_dota_hero_dazzle", 50),
            Map.entry("npc_dota_hero_rattletrap", 51),
            Map.entry("npc_dota_hero_leshrac", 52),
            Map.entry("npc_dota_hero_furion", 53),
            Map.entry("npc_dota_hero_life_stealer", 54),
            Map.entry("npc_dota_hero_dark_seer", 55),
            Map.entry("npc_dota_hero_clinkz", 56),
            Map.entry("npc_dota_hero_omniknight", 57),
            Map.entry("npc_dota_hero_enchantress", 58),
            Map.entry("npc_dota_hero_huskar", 59),
            Map.entry("npc_dota_hero_night_stalker", 60),
            Map.entry("npc_dota_hero_broodmother", 61),
            Map.entry("npc_dota_hero_bounty_hunter", 62),
            Map.entry("npc_dota_hero_weaver", 63),
            Map.entry("npc_dota_hero_jakiro", 64),
            Map.entry("npc_dota_hero_batrider", 65),
            Map.entry("npc_dota_hero_chen", 66),
            Map.entry("npc_dota_hero_spectre", 67),
            Map.entry("npc_dota_hero_doom_bringer", 69),
            Map.entry("npc_dota_hero_ancient_apparition", 68),
            Map.entry("npc_dota_hero_ursa", 70),
            Map.entry("npc_dota_hero_spirit_breaker", 71),
            Map.entry("npc_dota_hero_gyrocopter", 72),
            Map.entry("npc_dota_hero_alchemist", 73),
            Map.entry("npc_dota_hero_invoker", 74),
            Map.entry("npc_dota_hero_silencer", 75),
            Map.entry("npc_dota_hero_obsidian_destroyer", 76),
            Map.entry("npc_dota_hero_lycan", 77),
            Map.entry("npc_dota_hero_brewmaster", 78),
            Map.entry("npc_dota_hero_shadow_demon", 79),
            Map.entry("npc_dota_hero_lone_druid", 80),
            Map.entry("npc_dota_hero_chaos_knight", 81),
            Map.entry("npc_dota_hero_meepo", 82),
            Map.entry("npc_dota_hero_treant", 83),
            Map.entry("npc_dota_hero_ogre_magi", 84),
            Map.entry("npc_dota_hero_undying", 85),
            Map.entry("npc_dota_hero_rubick", 86),
            Map.entry("npc_dota_hero_disruptor", 87),
            Map.entry("npc_dota_hero_nyx_assassin", 88),
            Map.entry("npc_dota_hero_naga_siren", 89),
            Map.entry("npc_dota_hero_keeper_of_the_light", 90),
            Map.entry("npc_dota_hero_wisp", 91),
            Map.entry("npc_dota_hero_visage", 92),
            Map.entry("npc_dota_hero_slark", 93),
            Map.entry("npc_dota_hero_medusa", 94),
            Map.entry("npc_dota_hero_troll_warlord", 95),
            Map.entry("npc_dota_hero_centaur", 96),
            Map.entry("npc_dota_hero_magnataur", 97),
            Map.entry("npc_dota_hero_shredder", 98),
            Map.entry("npc_dota_hero_bristleback", 99),
            Map.entry("npc_dota_hero_tusk", 100),
            Map.entry("npc_dota_hero_skywrath_mage", 101),
            Map.entry("npc_dota_hero_abaddon", 102),
            Map.entry("npc_dota_hero_elder_titan", 103),
            Map.entry("npc_dota_hero_legion_commander", 104),
            Map.entry("npc_dota_hero_ember_spirit", 106),
            Map.entry("npc_dota_hero_earth_spirit", 107),
            Map.entry("npc_dota_hero_terrorblade", 109),
            Map.entry("npc_dota_hero_phoenix", 110),
            Map.entry("npc_dota_hero_oracle", 111),
            Map.entry("npc_dota_hero_techies", 105),
            Map.entry("npc_dota_hero_winter_wyvern", 112),
            Map.entry("npc_dota_hero_arc_warden", 113)
    );


    public static String formatGameTime(float time) {
        Duration gameTimeMillis = Duration.ofMillis((int) (1000.0f * time));

        LocalTime gameTime = LocalTime.MIDNIGHT.plus(gameTimeMillis);

        return "[" + GAMETIME_FORMATTER.format(gameTime) + "]";
    }


    public static int heroIdByName(String name) {
        return heroNameToId.getOrDefault(name, -1);
    }
}
