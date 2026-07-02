package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.domain.model.TrackPoint;
import java.time.Instant;

/** GPS sample returned by replay and track-point read APIs. */
public record TrackPointResponse(
        Instant recordedAt,
        double lat,
        double lng,
        Double altitude,
        Double speedKmh) {

    public static TrackPointResponse from(TrackPoint point) {
        return new TrackPointResponse(
                point.recordedAt(), point.lat(), point.lng(), point.altitude(), point.speedKmh());
    }
}
