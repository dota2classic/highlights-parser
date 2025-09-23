package ru.dotaclassic.highlights.dto;

import ru.dotaclassic.highlights.parser.HighlightDTO;

import java.util.List;

public record MatchHighlightsEvent(
        int matchId,
        MatchmakingMode lobbyType,
        List<HighlightDTO> highlights
) {
}
