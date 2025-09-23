package ru.dotaclassic.highlights.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.dotaclassic.highlights.RabbitMQConfig;
import ru.dotaclassic.highlights.dto.MatchHighlightsEvent;

@Component
public class ArtifactPublisher {
    private static final Logger log = LoggerFactory.getLogger(ArtifactPublisher.class);

    public static final String MATCH_HIGHLIGHTS_ROUTING_KEY = "MatchHighlightsEvent";


    private final RabbitTemplate rabbitTemplate;

    public ArtifactPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void publishHighlights(MatchHighlightsEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, MATCH_HIGHLIGHTS_ROUTING_KEY, event);
        log.info("Published highlights!");
    }

}
