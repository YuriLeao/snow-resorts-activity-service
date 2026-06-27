package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.domain.model.TrackPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GeoJSON {@code Feature} with a {@code LineString} geometry for client-side replay rendering.
 * Coordinates follow the GeoJSON order {@code [lng, lat, altitude?]}.
 */
public record ReplayResponse(
        String type,
        UUID runId,
        Geometry geometry,
        int pointCount) {

    public static ReplayResponse of(UUID runId, List<TrackPoint> points) {
        List<List<Double>> coordinates = new ArrayList<>(points.size());
        for (TrackPoint p : points) {
            List<Double> coord = new ArrayList<>(3);
            coord.add(p.lng());
            coord.add(p.lat());
            if (p.altitude() != null) {
                coord.add(p.altitude());
            }
            coordinates.add(coord);
        }
        return new ReplayResponse("Feature", runId,
                new Geometry("LineString", coordinates), points.size());
    }

    public record Geometry(String type, List<List<Double>> coordinates) {
    }
}
