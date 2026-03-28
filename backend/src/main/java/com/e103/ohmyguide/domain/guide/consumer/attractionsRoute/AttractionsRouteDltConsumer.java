package com.e103.ohmyguide.domain.guide.consumer.attractionsRoute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Profile("attractions-route-consumer")
@Slf4j
@Service
public class AttractionsRouteDltConsumer {

    @DltHandler
    @KafkaListener(topics = "user-go-log.dlt", groupId = "attractions-route-dlt-group")
    public void handleDlt(String message) {
        log.error("[AttractionsRoute DLT] All retries exhausted. message={}", message);
    }
}
