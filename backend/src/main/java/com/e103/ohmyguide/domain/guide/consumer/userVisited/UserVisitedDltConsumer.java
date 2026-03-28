package com.e103.ohmyguide.domain.guide.consumer.userVisited;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Profile("user-visited-consumer")
@Slf4j
@Service
public class UserVisitedDltConsumer {

    @DltHandler
    @KafkaListener(topics = "user-go-log.dlt", groupId = "user-visited-dlt-group")
    public void handleDlt(String message) {
        log.error("[UserVisited DLT] All retries exhausted. message={}", message);
    }
}
