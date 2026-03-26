package com.e103.ohmyguide.domain.guide.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterManager {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 6000000L; // 600초

    public SseEmitter create(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.debug("SSE emitter completed: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.debug("SSE emitter timed out: userId={}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId);
            log.debug("SSE emitter error: userId={}", userId);
        });

        emitters.put(userId, emitter);
        return emitter;
    }

    public void send(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.warn("No SSE emitter found for userId={}", userId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("navigation")
                    .data(data));
            emitter.complete();
        } catch (Exception e) {
            emitters.remove(userId);
            log.error("Failed to send SSE event: userId={}", userId, e);
        }
    }
}
