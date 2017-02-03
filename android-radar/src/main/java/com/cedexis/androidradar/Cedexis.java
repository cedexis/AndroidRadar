package com.cedexis.androidradar;

/**
 * Cedexis class to initialize SDK objects
 */
public final class Cedexis {

    public static Radar radar(int zoneId, int customerId) {
        return new RadarWebView(zoneId, customerId);
    }

}
