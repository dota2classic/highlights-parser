package ru.dotaclassic.highlights.dto;

public enum MatchmakingMode {
    RANKED(0),
    UNRANKED(1),
    SOLOMID(2),
    DIRETIDE(3),
    GREEVILING(4),
    ABILITY_DRAFT(5),
    TOURNAMENT(6),
    BOTS(7),
    HIGHROOM(8),
    TOURNAMENT_SOLOMID(9),
    CAPTAINS_MODE(10),
    LOBBY(11),
    BOTS_2X2(12),
    TURBO(13);

    private final int code;

    MatchmakingMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    // mapping int → enum
    public static MatchmakingMode fromCode(int code) {
        for (MatchmakingMode mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown MatchmakingMode code: " + code);
    }
}
