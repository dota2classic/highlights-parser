package ru.dotaclassic.highlights.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dotaclassic.highlights.parser.model.HighlightDTO;
import ru.dotaclassic.highlights.parser.model.HighlightType;
import ru.dotaclassic.highlights.parser.model.KillTiming;
import ru.dotaclassic.highlights.parser.model.ReplayTick;
import skadistats.clarity.event.Insert;
import skadistats.clarity.model.CombatLogEntry;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.model.StringTable;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.OnEntityPropertyChanged;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.modifiers.OnModifierTableEntry;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.processor.stringtables.StringTables;
import skadistats.clarity.processor.stringtables.UsesStringTable;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.wire.dota.common.proto.DOTAModifiers;
import skadistats.clarity.wire.dota.common.proto.DOTAUserMessages;

import java.nio.file.Path;
import java.util.*;

import static ru.dotaclassic.highlights.parser.Utils.*;

@UsesEntities
@UsesStringTable("*")
//@UsesStringTable("EntityNames")
public class HighlightJob {

    private static Map<String, String> attackerMapping = Map.of(
            "npc_dota_techies_land_mine", "npc_dota_hero_techies",
            "npc_dota_techies_remote_mine", "npc_dota_hero_techies"
    );

    private List<ReplayListener> highlightSpells = List.of(
            // Abilities

            new ComboSpellDetector(this, "earthshaker_echo_slam", 3),
            new ComboSpellDetector(this, "enigma_black_hole", 2),
            new ComboSpellDetector(this, "phoenix_supernova", 4),
            new ComboSpellDetector(this, "queenofpain_sonic_wave", 3),
            new ComboSpellDetector(this, "nevermore_requiem", 4),

            new ComboSpellDetector(this, "magnataur_reverse_polarity", 3),
            new ComboSpellDetector(this, "magnataur_skewer", 2),

            new ComboSpellDetector(this, "antimage_mana_void", 3),
            new ComboSpellDetector(this, "alchemist_unstable_concoction", 3),
            new ComboSpellDetector(this, "crystal_maiden_freezing_field", 3),
            new ComboSpellDetector(this, "dark_seer_vacuum", 3),
            new ComboSpellDetector(this, "disruptor_static_storm", 3),
            new ComboSpellDetector(this, "gyrocopter_call_down", 3),
            new ComboSpellDetector(this, "kunkka_ghostship", 3),
            new ComboSpellDetector(this, "obsidian_destroyer_sanity_eclipse", 3),
            new ComboSpellDetector(this, "puck_dream_coil", 3),
            new ComboSpellDetector(this, "sandking_epicenter", 3),


            // Modifiers
            new ComboModifierDetector(this, "modifier_faceless_void_chronosphere_freeze", 3),
            new ComboModifierDetector(this, "modifier_axe_berserkers_call", 3)
    );

    @Insert
    private Context ctx;

    @Insert
    private Entities entities;

    private int tick = 0;

    private final ArrayList<HighlightDTO> highlights = new ArrayList<>();

    private final HashMap<String, LinkedList<KillTiming>> killTimings = new HashMap<>();


    private float m_flPreGameStartTime = 0;

    private final Logger log = LoggerFactory.getLogger(HighlightJob.class.getPackage().getClass());


    private String compileName(String attackerName, boolean isIllusion) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") : "UNKNOWN";
    }




    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        tick = ctx.getTick();
    }

    @OnEntityPropertyChanged(classPattern = "DT_DOTAGamerulesProxy", propertyPattern = "dota_gamerules_data.m_flGameStartTime")
    public void onPreGameStartTimeChanged(Context ctx, Entity e, FieldPath fp) {
        m_flPreGameStartTime = e.getPropertyForFieldPath(fp);
    }

    @OnModifierTableEntry()
    public void onModifierEntry(DOTAModifiers.CDOTAModifierBuffTableEntry e) {
        if (!e.hasCreationTime()) return;
        if (!e.hasCaster()) return;

        float realGameTime = Math.max(0, e.getCreationTime() - m_flPreGameStartTime);
        var replayTick = new ReplayTick(tick, realGameTime);

        StringTables stringTables = ctx.getProcessor(StringTables.class);
        StringTable baselines = stringTables.forName("ModifierNames");


        try {
            var name = baselines.getNameByIndex(e.getModifierClass());

            var caster = entities.getByHandle(e.getCaster());

            var hero = entities.getByHandle(e.getParent());


            highlightSpells.forEach(listener -> listener.onModifierEvent(
                    replayTick,
                    name,
                    caster,
                    hero,
                    entities.getByHandle(e.getAbility()),
                    e
            ));
        } catch (Exception exc) {
            log.warn("Exception:", exc);
        }
    }

    @OnCombatLogEntry
    public void onCombatLogEntry(CombatLogEntry cle) {
        float realGameTime = Math.max(0, cle.getTimestamp() - m_flPreGameStartTime);
        var time = formatGameTime(realGameTime);

        var replayTick = new ReplayTick(tick, realGameTime);

        var isAttackerHero = cle.isAttackerHero();


        var attackerName = cle.getAttackerName();
        if (attackerName != null && attackerMapping.containsKey(attackerName)) {
            attackerName = attackerMapping.get(attackerName);
            isAttackerHero = true;
        }


        switch (cle.getType()) {
            case DOTA_COMBATLOG_DAMAGE:
                highlightSpells.forEach(csd -> csd.onCombatLogDamage(replayTick, cle));
                break;
            case DOTA_COMBATLOG_DEATH:
                assert attackerName != null;

                if (attackerName.contains("npc_dota_neutral_") && (cle.isTargetHero() && !cle.isTargetIllusion())) {
                    log.info("Neutrla kill! {} by {}", cle.getTargetName(), attackerName);
                    // it's a neutral hero kill!
                    killTimings.putIfAbsent("neutral", new LinkedList<>());
                    var heroKills = killTimings.get("neutral");
                    heroKills.add(replayTick.kill(cle.getTargetName()));
                }

                if (!isAttackerHero || !cle.isTargetHero() || cle.isTargetIllusion()) {
                    return;
                }

                if (Objects.equals(cle.getTargetName(), attackerName)) {
                    return;
                }

                var hackedName = heroNameToDtClassName(attackerName);

                var heroEnt = entities.getByDtName(
                        hackedName
                );

                if (heroEnt != null) {
                    Integer hp = heroEnt.getProperty("m_iHealth");
                    Integer maxHp = heroEnt.getProperty("m_iMaxHealth");

                    var healthPercentage = (float) hp / maxHp;

                    if (healthPercentage < 0.05 && !hackedName.contains("Techies")) {
                        highlights.add(
                                new HighlightDTO(
                                        replayTick,
                                        replayTick,
                                        attackerName,
                                        getHeroIndex(attackerName),
                                        HighlightType.LOW_HP_KILL,
                                        "Убийство на лоу хп")
                        );
                    }
                }

                killTimings.putIfAbsent(attackerName, new LinkedList<>());
                var heroKills = killTimings.get(attackerName);

                heroKills.add(replayTick.kill(cle.getTargetName()));
                break;
            default:
                DOTAUserMessages.DOTA_COMBATLOG_TYPES type = cle.getType();
                break;

        }
    }

    public int getHeroIndex(String heroName) {
        Entity ps = entities.getByDtName("DT_DOTA_PlayerResource");
        for (int i = 0; i < 10; i++) {
            Integer some = ps.getProperty("m_nSelectedHeroID." + String.format("%04d", i));
            if (some == heroIdByName(heroName)) {
                return i;
            }
        }

        return -1;
    }

    private void multikills() {
        killTimings.forEach((hero, killTimings) -> {
            float maxStreakInterval = 19;
            KillTiming streakStartTime = new KillTiming(0, 0, "");
            KillTiming streakLastTime = streakStartTime;
            int streak = 0;


            for (var killTiming : killTimings) {
                boolean isStreakGoing = Math.abs(killTiming.time() - streakLastTime.time()) < maxStreakInterval;

                if (isStreakGoing) {
                    streak += 1;
                    streakLastTime = killTiming;
                } else {
                    if (streak > 2) {
                        highlights.add(
                                new HighlightDTO(
                                        streakStartTime.toTick(),
                                        streakLastTime.toTick(),
                                        hero,
                                        getHeroIndex(hero),
                                        HighlightType.MULTIKILL,
                                        "Мультикилл: %d героев".formatted(streak))
                        );
                    }
                    streakLastTime = killTiming;
                    streakStartTime = killTiming;
                    streak = 1;
                }
            }

            // do we still have a streak?
            if (streak > 2) {
                highlights.add(
                        new HighlightDTO(
                                streakStartTime.toTick(),
                                streakLastTime.toTick(),
                                hero,
                                getHeroIndex(hero),
                                HighlightType.MULTIKILL,
                                "Мультикилл: %d героев".formatted(streak))
                );
            }
        });

    }


    private void quickKillsHighlights() {
        killTimings.forEach((hero, killTimings) -> {
            float maxStreakInterval = 2;
            KillTiming streakStartTime = new KillTiming(0, 0, "");
            int streak = 0;

            String focusHero = hero;
            for (var killTiming : killTimings) {
                focusHero = Objects.equals(hero, "npc_dota_hero_techies") ? killTiming.hero() : hero;

                boolean isStreakGoing = Math.abs(killTiming.time() - streakStartTime.time()) < maxStreakInterval;
                if (!isStreakGoing) {
                    // Streak is over!!
                    if (streak > 1) {
                        highlights.add(
                                new HighlightDTO(
                                        streakStartTime.toTick(),
                                        streakStartTime.toTick(),
                                        hero,
                                        getHeroIndex(focusHero),
                                        HighlightType.QUICK_MULTIKILL,
                                        "Быстрый мультикилл: %d героев".formatted(streak))
                        );
                    }
                    streakStartTime = killTiming;
                    streak = 1;
                } else {
                    streak += 1;
                }
            }

            // do we still have a streak?
            if (streak > 1) {
                highlights.add(
                        new HighlightDTO(
                                streakStartTime.toTick(),
                                streakStartTime.toTick(),
                                hero,
                                getHeroIndex(focusHero),
                                HighlightType.QUICK_MULTIKILL,
                                "Быстрый мультикилл: %d героев".formatted(streak))
                );
            }

        });

    }

    private void run(Path file) throws Exception {
        long tStart = System.currentTimeMillis();
        var runner = new SimpleRunner(new MappedFileSource(file));
        runner.runWith(this);
        long tMatch = System.currentTimeMillis() - tStart;
        log.info("total time taken: {}s", (tMatch) / 1000.0);

        quickKillsHighlights();
        multikills();

        highlightSpells.forEach(t -> highlights.addAll(t.getHighlights()));
    }


    public List<HighlightDTO> getHighlights(Path file) {
        try {
            var job = new HighlightJob();
            job.run(file);
            return job.highlights;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}