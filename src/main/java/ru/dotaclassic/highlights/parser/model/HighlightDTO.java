package ru.dotaclassic.highlights.parser.model;

public record HighlightDTO(
        ReplayTick start,
        ReplayTick finish,
        String hero,
        int heroIndex,
        HighlightType type,
        String comment
) implements Comparable<HighlightDTO> {

    @Override
    public int compareTo(HighlightDTO o) {
        return Float.compare(this.start.time(), o.start.time());
    }
}
