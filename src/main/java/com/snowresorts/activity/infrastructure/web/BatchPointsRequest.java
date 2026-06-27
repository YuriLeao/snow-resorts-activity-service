package com.snowresorts.activity.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** A batch of GPS samples (online stream or offline sync). */
public record BatchPointsRequest(
        @NotEmpty(message = "points must not be empty")
        @Valid
        List<TrackPointDto> points) {
}
