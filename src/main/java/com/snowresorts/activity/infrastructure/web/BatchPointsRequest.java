package com.snowresorts.activity.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** A batch of GPS samples (online stream or offline sync). */
public record BatchPointsRequest(
        @NotEmpty(message = "points must not be empty")
        @Size(max = 500, message = "points must contain at most 500 entries")
        @Valid
        List<TrackPointDto> points) {
}
