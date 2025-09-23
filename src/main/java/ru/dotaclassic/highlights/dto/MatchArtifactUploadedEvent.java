package ru.dotaclassic.highlights.dto;

public record MatchArtifactUploadedEvent(
    int matchId,
    MatchmakingMode lobbyType,
    MatchArtifactType artifactType,
    String bucket,
    String key
) {}
