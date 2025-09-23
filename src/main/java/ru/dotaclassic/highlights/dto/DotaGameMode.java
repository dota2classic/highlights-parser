package ru.dotaclassic.highlights.dto;

public enum DotaGameMode {
    ALLPICK(1),
    CAPTAINS_MODE(2),
    RANDOM_DRAFT(3),
    SINGLE_DRAFT(4),
    ALL_RANDOM(5),
    INTRO(6),
    DIRETIDE(7),
    REVERSE_CAPTAINS_MODE(8),
    GREEVILING(9),
    TUTORIAL(10),
    MID_ONLY(11),
    LEAST_PLAYED(12),
    LIMITED_HEROES(13),
    BALANCED_DRAFT(17),
    ABILITY_DRAFT(18),
    ALL_RANDOM_DEATH_MATCH(20),
    SOLOMID(21),
    RANKED_AP(22);

    private final int code;

    DotaGameMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // for mapping int → enum
    public static DotaGameMode fromCode(int code) {
        for (DotaGameMode mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown DotaGameMode code: " + code);
    }
}
