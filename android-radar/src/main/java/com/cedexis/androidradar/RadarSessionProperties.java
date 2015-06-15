package com.cedexis.androidradar;

/**
 * Created by jacob on 10/06/15.
 */
public class RadarSessionProperties {
    private int _requestorZoneId;
    private int _requestorCustomerId;
    private RadarImpactProperties _impactProperties;
    private double _throughputSampleRate;
    private double _throughputSampleRateMobile;

    public RadarSessionProperties(
            int requestorZoneId,
            int requestorCustomerId,
            RadarImpactProperties impactProperties,
            double throughputSampleRate,
            double throughputSampleRateMobile) {
        this._requestorZoneId = requestorZoneId;
        this._requestorCustomerId = requestorCustomerId;
        this._impactProperties = impactProperties;
        this._throughputSampleRate = throughputSampleRate;
        this._throughputSampleRateMobile = throughputSampleRateMobile;
    }

    public RadarSessionProperties(int requestorZoneId, int requestorCustomerId, RadarImpactProperties impactProperties) {
        this(requestorZoneId, requestorCustomerId, impactProperties, 1, 0);
    }

    public RadarSessionProperties(int requestorZoneId, int requestorCustomerId) {
        this(requestorZoneId, requestorCustomerId, null);
    }

    public int get_requestorZoneId() {
        return _requestorZoneId;
    }

    public int get_requestorCustomerId() {
        return _requestorCustomerId;
    }

    public double get_throughputSampleRate() {
        return _throughputSampleRate;
    }

    public double get_throughputSampleRateMobile() {
        return _throughputSampleRateMobile;
    }

    public RadarImpactProperties get_impactProperties() {
        return _impactProperties;
    }
}
