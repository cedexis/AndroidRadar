package com.cedexis.androidradar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * TODO
 */
public class RadarImpactProperties implements Serializable {
    private String _sessionId;
    private String _category;
    private HashMap<String, Object> _kpiTuples = new HashMap<>();
    private String _performanceTestUrl;

    public RadarImpactProperties(String sessionId) {
        this._sessionId = sessionId;
    }

    public String get_sessionId() {
        return _sessionId;
    }

    public void set_category(String value) {
        _category = value;
    }

    public String get_category() {
        return _category;
    }

    public HashMap<String, Object> get_kpiTuples() {
        return _kpiTuples;
    }

    public void addKpi(String name, String value) {
        this.addKpi(name, (Object) value);
    }

    public void addKpi(String name, int value) {
        this.addKpi(name, (Object) value);
    }

    public void addKpi(String name, double value) {
        this.addKpi(name, (Object) value);
    }

    public void addKpi(String name, boolean value) {
        this.addKpi(name, (Object) value);
    }

    private void addKpi(String name, Object value) {
        _kpiTuples.put(name, value);
    }

    public void set_performanceTestUrl(String _performanceTestUrl) {
        this._performanceTestUrl = _performanceTestUrl;
    }

    public String get_performanceTestUrl() {
        return _performanceTestUrl;
    }
}
