package ru.dotaclassic.highlights.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skadistats.clarity.event.Insert;
import skadistats.clarity.model.CombatLogEntry;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.entities.OnEntityPropertyChanged;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.gameevents.OnCombatLogEntry;
import skadistats.clarity.processor.reader.OnTickStart;
import skadistats.clarity.processor.runner.Context;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.MappedFileSource;
import skadistats.clarity.wire.dota.common.proto.DOTAUserMessages;

import java.nio.file.Path;
import java.util.*;

import static ru.dotaclassic.highlights.parser.Utils.formatGameTime;

@UsesEntities
public class HighlightJob {
    private List<ComboSpellDetector> highlightSpells = List.of(
            new ComboSpellDetector("earthshaker_echo_slam"),
            new ComboSpellDetector("enigma_black_hole"),
            new ComboSpellDetector("magnataur_reverse_polarity"),
            new ComboSpellDetector("phoenix_supernova"),
            new ComboSpellDetector("queenofpain_sonic_wave"),
            new ComboSpellDetector("nevermore_requiem")
    );
    @Insert
    private Entities entities;

    private int tick = 0;

    private final ArrayList<HighlightDTO> highlights = new ArrayList<>();

    private final HashMap<String, LinkedList<ReplayTick>> killTimings = new HashMap<>();


    private float m_flPreGameStartTime = 0;

    private final Logger log = LoggerFactory.getLogger(HighlightJob.class.getPackage().getClass());


    private String compileName(String attackerName, boolean isIllusion) {
        return attackerName != null ? attackerName + (isIllusion ? " (illusion)" : "") : "UNKNOWN";
    }


    private static String capitalize(String word) {
        if (word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public static String heroNameToDtClassName(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove "npc_dota_" prefix
        String withoutPrefix = input.replaceFirst("^npc_dota_", "");

        // Split by underscore
        String[] parts = withoutPrefix.split("_");

        // Capitalize each part
        StringBuilder sb = new StringBuilder("DT_DOTA_Unit_");
        for (int i = 0; i < parts.length; i++) {
            sb.append(capitalize(parts[i]));
            if (i < parts.length - 1) {
                sb.append("_");
            }
        }

        return sb.toString();
    }


    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        tick = ctx.getTick();
    }

    @OnEntityPropertyChanged(classPattern = "DT_DOTAGamerulesProxy", propertyPattern = "dota_gamerules_data.m_flGameStartTime")
    public void onPreGameStartTimeChanged(Context ctx, Entity e, FieldPath fp) {
        m_flPreGameStartTime = e.getPropertyForFieldPath(fp);
    }

    @OnCombatLogEntry
    public void onCombatLogEntry(CombatLogEntry cle) {
        float realGameTime = Math.max(0, cle.getTimestamp() - m_flPreGameStartTime);
        var time = formatGameTime(realGameTime);


        var replayTick = new ReplayTick(tick, realGameTime);

        switch (cle.getType()) {
            case DOTA_COMBATLOG_DAMAGE:
                highlightSpells.forEach(csd -> csd.onCombatLogDamage(replayTick, cle));
                break;
            case DOTA_COMBATLOG_DEATH:
                if (cle.getAttackerName().contains("npc_dota_neutral_") && (cle.isTargetHero() && !cle.isTargetIllusion())) {
                    log.info("Neutrla kill! {} by {}", cle.getTargetName(), cle.getAttackerName());
                    // it's a neutral hero kill!
                    killTimings.putIfAbsent("neutral", new LinkedList<>());
                    var heroKills = killTimings.get("neutral");
                    heroKills.add(replayTick);
                }

                if (!cle.isAttackerHero() || !cle.isTargetHero() || cle.isTargetIllusion()) {
                    return;
                }

                if (Objects.equals(cle.getTargetName(), cle.getAttackerName())) {
                    return;
                }

                var hackedName = heroNameToDtClassName(cle.getAttackerName());

                var heroEnt = entities.getByDtName(
                        hackedName
                );

                if (heroEnt != null) {
                    Integer hp = heroEnt.getProperty("m_iHealth");
                    Integer maxHp = heroEnt.getProperty("m_iMaxHealth");

                    var healthPercentage = (float) hp / maxHp;

                    if (healthPercentage < 0.05 && !hackedName.contains("Techies")) {
                        log.info("{} LOW HP KILL!? {} {}", time, hackedName, hp);
                        highlights.add(new HighlightDTO(tick, realGameTime, cle.getAttackerName(), HighlightType.LOW_HP_KILL, "Убийство на лоу хп"));
                    }
                }

                killTimings.putIfAbsent(cle.getAttackerName(), new LinkedList<>());
                var heroKills = killTimings.get(cle.getAttackerName());

                heroKills.add(replayTick);
                break;
            default:
                DOTAUserMessages.DOTA_COMBATLOG_TYPES type = cle.getType();
                break;

        }
    }

    private void multikills() {
        killTimings.forEach((hero, killTimings) -> {
            float maxStreakInterval = 19;
            ReplayTick streakStartTime = new ReplayTick(0, 0);
            ReplayTick streakLastTime = streakStartTime;
            int streak = 0;


            for (var killTiming : killTimings) {
                boolean isStreakGoing = Math.abs(killTiming.time() - streakLastTime.time()) < maxStreakInterval;
                if (isStreakGoing) {
                    streak += 1;
                    streakLastTime = killTiming;
                } else {
                    if (streak > 2) {
                        highlights.add(
                                new HighlightDTO(streakStartTime.tick(),
                                        streakStartTime.time(),
                                        hero,
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
                        new HighlightDTO(streakStartTime.tick(),
                                streakStartTime.time(),
                                hero,
                                HighlightType.MULTIKILL,
                                "Мультикилл: %d героев".formatted(streak))
                );
            }
        });

    }


    private void quickKillsHighlights() {
        killTimings.forEach((hero, killTimings) -> {
            float maxStreakInterval = 2;
            ReplayTick streakStartTime = new ReplayTick(0, 0);
            int streak = 0;
            for (var killTiming : killTimings) {
                boolean isStreakGoing = Math.abs(killTiming.time() - streakStartTime.time()) < maxStreakInterval;
                if (!isStreakGoing) {
                    // Streak is over!!
                    if (streak > 1) {
                        highlights.add(
                                new HighlightDTO(streakStartTime.tick(),
                                        streakStartTime.time(),
                                        hero,
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
                        new HighlightDTO(streakStartTime.tick(),
                                streakStartTime.time(),
                                hero,
                                HighlightType.QUICK_MULTIKILL,
                                "Быстрый мультикилл: %d героев".formatted(streak))
                );
            }

        });

    }

    private void run(Path file) throws Exception {
        long tStart = System.currentTimeMillis();
        new SimpleRunner(new MappedFileSource(file)).runWith(this);
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