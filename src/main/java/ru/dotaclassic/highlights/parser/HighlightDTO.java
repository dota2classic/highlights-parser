package ru.dotaclassic.highlights.parser;

public record HighlightDTO(
        float gameTime,
        String hero,
        HighlightType type,
        String comment
) implements Comparable<HighlightDTO> {

    @Override
    public int compareTo(HighlightDTO o) {
        return Float.compare(this.gameTime, o.gameTime);
    }
}
