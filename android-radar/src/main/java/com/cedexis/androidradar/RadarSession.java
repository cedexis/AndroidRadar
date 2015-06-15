package com.cedexis.androidradar;

/**
 * Created by jacob on 10/06/15.
 */
public class RadarSession {
    private int _transactionId;
    private long _sessionTimestamp;
    private String _requestSignature;
    private RadarSessionProperties _sessionProperties;
    private String _networkType;
    private String _networkSubtype;

    public RadarSession(int transactionId, long sessionTimestamp, RadarSessionProperties sessionProperties, String networkType, String networkSubtype) {
        _transactionId = transactionId;
        _sessionTimestamp = sessionTimestamp;
        _sessionProperties = sessionProperties;
        _networkType = networkType;
        _networkSubtype = networkSubtype;
    }

    public int get_transactionId() {
        return _transactionId;
    }

    public long get_sessionTimestamp() {
        return _sessionTimestamp;
    }

    public String get_requestSignature() {
        return _requestSignature;
    }

    public void set_requestSignature(String _requestSignature) {
        this._requestSignature = _requestSignature;
    }

    public int get_requestorZoneId() {
        return _sessionProperties.get_requestorZoneId();
    }

    public int get_requestorCustomerId() {
        return _sessionProperties.get_requestorCustomerId();
    }

    public String get_networkType() {
        return _networkType;
    }

    public String get_networkSubtype() {
        return _networkSubtype;
    }

    public RadarSessionProperties get_sessionProperties() {
        return _sessionProperties;
    }

    @Override
    public String toString() {
        return "RadarSession{" +
                "_transactionId=" + _transactionId +
                ", _sessionTimestamp=" + _sessionTimestamp +
                ", _requestSignature='" + _requestSignature + '\'' +
                '}';
    }
}
