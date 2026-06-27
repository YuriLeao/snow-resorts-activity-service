package com.snowresorts.activity.infrastructure.web;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Minimal in-memory idempotency guard for the offline-sync {@code PATCH /runs/{id}/points} endpoint
 * (recommendation #5). The first time a non-blank {@code Idempotency-Key} is seen it is registered
 * and the batch is processed; replays of the same key are skipped so a flaky mobile reconnect cannot
 * duplicate GPS points.
 *
 * <p>Scope is per-instance and unbounded for the MVP; production replaces this with Redis (or a DB
 * unique constraint) so the guard survives restarts and works across replicas.
 */
@Component
public class IdempotencyStore {

    private final Set<String> seen = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** @return {@code true} if this key was not seen before (i.e. the caller should process it). */
    public boolean register(String key) {
        return seen.add(key);
    }
}
