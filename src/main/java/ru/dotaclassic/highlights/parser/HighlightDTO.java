package ru.dotaclassic.highlights.parser;

public record HighlightDTO(
        int tick,
        float gameTime,
        String hero,
        int heroIndex,
        HighlightType type,
        String comment
) implements Comparable<HighlightDTO> {

    @Override
    public int compareTo(HighlightDTO o) {
        return Float.compare(this.gameTime, o.gameTime);
    }
}
