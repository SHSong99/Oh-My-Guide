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
            emitters.remove(userId, emitter);
            log.warn("SSE emitter completed: userId={}", userId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId, emitter);
            log.warn("SSE emitter timed out: userId={}", userId);
        });
        emitter.onError(e -> {
            emitters.remove(userId, emitter);
            log.warn("SSE emitter error: userId={}", userId, e);
        });

        emitters.put(userId, emitter);
        return emitter;
    }

    public void sendConnected(Long userId) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
            log.info("Sent connected event: userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to send connected event: userId={}", userId, e);
        }
    }

    public void send(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);

        log.info("SSE emitter started: userId={}, data={}!!!!!", userId, data);
        if (emitter == null) {
            log.warn("No SSE emitter found for userId={}", userId);
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("navigation")
                    .data(data));
        } catch (Exception e) {
            emitters.remove(userId, emitter);
            log.error("Failed to send SSE event: userId={}", userId, e);
        }
    }
}
