package com.snowresorts.activity.infrastructure.web;

import com.snowresorts.activity.domain.model.TrackPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GeoJSON {@code Feature} with a {@code LineString} geometry for client-side replay rendering.
 * Coordinates follow the GeoJSON order {@code [lng, lat, altitude?]}.
 * {@link #points} carries the full GPS track for geometry matching on the client.
 */
public record ReplayResponse(
        String type,
        UUID runId,
        Geometry geometry,
        int pointCount,
        List<TrackPointResponse> points) {

    public static ReplayResponse of(UUID runId, List<TrackPoint> trackPoints) {
        List<List<Double>> coordinates = new ArrayList<>(trackPoints.size());
        List<TrackPointResponse> points = new ArrayList<>(trackPoints.size());
        for (TrackPoint p : trackPoints) {
            List<Double> coord = new ArrayList<>(3);
            coord.add(p.lng());
            coord.add(p.lat());
            if (p.altitude() != null) {
                coord.add(p.altitude());
            }
            coordinates.add(coord);
            points.add(TrackPointResponse.from(p));
        }
        return new ReplayResponse("Feature", runId,
                new Geometry("LineString", coordinates), trackPoints.size(), points);
    }

    public record Geometry(String type, List<List<Double>> coordinates) {
    }
}
