package com.snowresorts.activity.domain.port;

import com.snowresorts.contracts.events.RunCompletedEvent;

/**
 * Outbound port for publishing domain events. In AWS this fans out to SNS/SQS; in dev a
 * logging adapter records the event so the rest of the flow stays identical.
 */
public interface RunEventPublisher {

    void publish(RunCompletedEvent event);
}
