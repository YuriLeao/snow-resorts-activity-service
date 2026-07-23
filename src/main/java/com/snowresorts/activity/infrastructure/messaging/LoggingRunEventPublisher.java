package com.snowresorts.activity.infrastructure.messaging;

import com.snowresorts.activity.domain.port.RunEventPublisher;
import com.snowresorts.contracts.events.RunCompletedEvent;
import com.snowresorts.security.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Dev/no-op publisher: logs the {@link RunCompletedEvent} instead of fanning out to SNS/SQS.
 * Production swaps this for an SNS adapter behind the same {@link RunEventPublisher} port.
 */
@Component
public class LoggingRunEventPublisher implements RunEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingRunEventPublisher.class);

    @Override
    public void publish(RunCompletedEvent event) {
        StructuredLogger.of(log).info("run_completed_event", "succeeded", "published",
                "event_type", event.type(),
                "event_id", event.eventId(),
                "run_id", event.runId(),
                "user_id", event.userId(),
                "max_speed_kmh", event.maxSpeedKmh(),
                "distance_m", event.distanceM(),
                "duration_sec", event.durationSec());
    }
}
