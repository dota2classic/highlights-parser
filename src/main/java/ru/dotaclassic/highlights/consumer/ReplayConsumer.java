package ru.dotaclassic.highlights.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.dotaclassic.highlights.RabbitMQConfig;
import ru.dotaclassic.highlights.dto.MatchArtifactType;
import ru.dotaclassic.highlights.dto.MatchArtifactUploadedEvent;
import ru.dotaclassic.highlights.dto.MatchHighlightsEvent;
import ru.dotaclassic.highlights.dto.MatchmakingMode;
import ru.dotaclassic.highlights.parser.HighlightJob;

import static ru.dotaclassic.highlights.parser.Utils.formatGameTime;

@Component
public class ReplayConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReplayConsumer.class);

    private final FileDownloader fileDownloader;
    private final ArtifactPublisher artifactPublisher;

    public ReplayConsumer(FileDownloader fileDownloader, ArtifactPublisher artifactPublisher) {
        this.fileDownloader = fileDownloader;
        this.artifactPublisher = artifactPublisher;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void listen(MatchArtifactUploadedEvent event) {
        if (event.artifactType() != MatchArtifactType.REPLAY) {
            return;
        }

        if (event.lobbyType() != MatchmakingMode.UNRANKED) {
            log.info("Skipping processing matchId {}: not an unranked match", event.matchId());
            return;
        }

        try {
            var url = "https://cdn.dotaclassic.ru/%s/%s".formatted(event.bucket(), event.key());
            var isZipped = event.matchId() > 49295 && event.matchId() < 1_000_000;
            if (!isZipped) {
                url = url.replace(".zip", "");
            }
            log.info("Downloading replay from {}", url);

            fileDownloader.getReplay(url, !isZipped, replay -> {
                log.info("Downloaded replay: {}", replay.getFileName());
                var highlights = new HighlightJob().getHighlights(replay);

                highlights.stream().sorted().forEach(highlight -> {
                    log.info("{} {} {}: {} ({})", highlight.start().tick(), formatGameTime(highlight.start().time()), highlight.hero(), highlight.type(), highlight.comment());
                });
                log.info("Done getting highlights!");

                artifactPublisher.publishHighlights(
                        new MatchHighlightsEvent(
                                event.matchId(),
                                event.lobbyType(),
                                highlights
                        )
                );

            });
        } catch (Exception e) {
            log.error("There was an error processing replay!", e);
        }
    }
}
