package com.cedexis.androidradar;

public class RadarScheme {
    public static RadarScheme HTTPS = new RadarScheme("https");
    public static RadarScheme HTTP = new RadarScheme("http");
    private final String _protocolString;

    private RadarScheme(String protocol) {
        _protocolString = protocol;
    }

    @Override
    public String toString() {
        return _protocolString;
    }
}
