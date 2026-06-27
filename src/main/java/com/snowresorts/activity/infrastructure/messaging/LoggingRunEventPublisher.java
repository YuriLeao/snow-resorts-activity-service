package com.snowresorts.activity.infrastructure.messaging;

import com.snowresorts.activity.domain.port.RunEventPublisher;
import com.snowresorts.contracts.events.RunCompletedEvent;
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
        log.info("Published {} eventId={} runId={} userId={} maxSpeed={}km/h distance={}m duration={}s",
                event.type(), event.eventId(), event.runId(), event.userId(),
                event.maxSpeedKmh(), event.distanceM(), event.durationSec());
    }
}
