package com.tnsfrp;

import static org.junit.jupiter.api.Assertions.*;

import com.tnsfrp.util.GeoUtil;
import org.junit.jupiter.api.Test;

class GeoUtilTest {
    @Test
    void sameDistanceIsZero() {
        assertEquals(0.0, GeoUtil.distanceKm(13.08, 80.27, 13.08, 80.27), 0.0001);
    }

    @Test
    void oneDegreeOfLongitudeAtEquatorIsAbout111Km() {
        assertEquals(111.19, GeoUtil.distanceKm(0, 0, 0, 1), 0.1);
    }

    @Test
    void chennaiToCoimbatoreIsRoughly430Km() {
        double d = GeoUtil.distanceKm(13.0827, 80.2707, 11.0168, 76.9558);
        assertTrue(d > 420 && d < 445, "distance was " + d);
    }

    @Test
    void distanceIsSymmetric() {
        assertEquals(GeoUtil.distanceKm(9.92, 78.12, 10.79, 78.69), GeoUtil.distanceKm(10.79, 78.69, 9.92, 78.12), 1e-9);
    }

    @Test
    void invalidCoordinatesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> GeoUtil.distanceKm(91, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> GeoUtil.distanceKm(0, 181, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> GeoUtil.distanceKm(Double.NaN, 0, 0, 0));
        assertFalse(GeoUtil.isValidCoordinate(0, -200));
    }
}
