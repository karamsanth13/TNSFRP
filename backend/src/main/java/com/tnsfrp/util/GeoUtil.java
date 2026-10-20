package com.tnsfrp.util;

/** Distance helper using the Haversine formula (no external map service needed). */
public final class GeoUtil {
    public static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtil() {}

    public static boolean isValidCoordinate(double lat, double lon) {
        return !Double.isNaN(lat) && !Double.isNaN(lon)
                && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    /** Great-circle distance between two points in kilometres. */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        if (!isValidCoordinate(lat1, lon1) || !isValidCoordinate(lat2, lon2)) {
            throw new IllegalArgumentException("Invalid latitude/longitude");
        }
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
