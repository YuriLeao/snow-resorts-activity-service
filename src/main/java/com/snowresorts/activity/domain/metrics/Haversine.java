package com.snowresorts.activity.domain.metrics;

/** Great-circle distance helper. Pure math, no framework dependencies. */
public final class Haversine {

    /** Mean Earth radius in metres (IUGG). */
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private Haversine() {
    }

    /** @return the great-circle distance in metres between two WGS84 coordinates. */
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sinLat = Math.sin(dLat / 2);
        double sinLng = Math.sin(dLng / 2);
        double a = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLng * sinLng;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }
}
