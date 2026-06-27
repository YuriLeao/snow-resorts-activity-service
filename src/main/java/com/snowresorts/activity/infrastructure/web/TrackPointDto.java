package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.domain.model.TrackPoint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/** A single GPS sample from the mobile client. */
public record TrackPointDto(
        @NotNull(message = "recordedAt is required")
        Instant recordedAt,

        @NotNull(message = "lat is required")
        @DecimalMin(value = "-90.0", message = "lat must be >= -90")
        @DecimalMax(value = "90.0", message = "lat must be <= 90")
        Double lat,

        @NotNull(message = "lng is required")
        @DecimalMin(value = "-180.0", message = "lng must be >= -180")
        @DecimalMax(value = "180.0", message = "lng must be <= 180")
        Double lng,

        Double altitude,
        Double speedKmh,
        Double inclination) {

    public TrackPoint toDomain() {
        return new TrackPoint(recordedAt, lat, lng, altitude, speedKmh);
    }
}
