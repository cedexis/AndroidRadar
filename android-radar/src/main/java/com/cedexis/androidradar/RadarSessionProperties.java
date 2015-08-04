package com.cedexis.androidradar;

/**
 * Allows the user to configure various properties of the Radar session.
 */
public class RadarSessionProperties {
    private int _requestorZoneId;
    private int _requestorCustomerId;
    private RadarImpactProperties _impactProperties;
    private double _throughputSampleRate;
    private double _throughputSampleRateMobile;

    /**
     * This constructor allows the developer to specify most aspects of the Radar session.
     *
     * @param requestorZoneId The Cedexis Zone ID of the customer. Usually 1.
     * @param requestorCustomerId The Cedexis Customer ID.
     * @param impactProperties This property is reserved for future use and should be set to `null`.
     * @param throughputSampleRate The percentage at which to downsample throughput measurements when not on a mobile network (e.g. on WiFi).  Specify a decimal number from 0 to 1.
     * @param throughputSampleRateMobile The percentage at which to downsample throughput measurements on mobile networks.  Specify a decimal number from 0 to 1.
     */
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

    /**
     * @param requestorZoneId The Cedexis Zone ID of the customer. Usually 1.
     * @param requestorCustomerId The Cedexis Customer ID.
     * @param impactProperties This property is reserved for future use and should be set to `null`.
     */
    public RadarSessionProperties(int requestorZoneId, int requestorCustomerId, RadarImpactProperties impactProperties) {
        this(requestorZoneId, requestorCustomerId, impactProperties, 1, 0);
    }

    /**
     * Use this constructor for the most basic scenario with default settings.
     *
     * @param requestorZoneId The Cedexis Zone ID of the customer. Usually 1.
     * @param requestorCustomerId The Cedexis Customer ID.
     */
    public RadarSessionProperties(int requestorZoneId, int requestorCustomerId) {
        this(requestorZoneId, requestorCustomerId, null);
    }

    /**
     * TODO
     *
     * @return
     */
    public int get_requestorZoneId() {
        return _requestorZoneId;
    }

    /**
     * TODO
     *
     * @return
     */
    public int get_requestorCustomerId() {
        return _requestorCustomerId;
    }

    /**
     * TODO
     *
     * @return
     */
    public double get_throughputSampleRate() {
        return _throughputSampleRate;
    }

    /**
     * TODO
     *
     * @return
     */
    public double get_throughputSampleRateMobile() {
        return _throughputSampleRateMobile;
    }

    /**
     * TODO
     *
     * @return
     */
    public RadarImpactProperties get_impactProperties() {
        return _impactProperties;
    }
}
