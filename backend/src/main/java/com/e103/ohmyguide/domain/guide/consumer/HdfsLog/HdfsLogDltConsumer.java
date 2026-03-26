package com.e103.ohmyguide.domain.guide.consumer.HdfsLog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Profile("hdfs-log-consumer")
@Slf4j
@Service
public class HdfsLogDltConsumer {

    @DltHandler
    @KafkaListener(topics = "user-go-log.dlt", groupId = "hdfs-log-dlt-group")
    public void handleDlt(String message) {
        log.error("[HdfsLog DLT] All retries exhausted. message={}", message);
    }
}
