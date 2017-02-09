// Copyright 2016 Cedexis
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

package com.cedexis.androidradar;

import java.io.Serializable;
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

    public String getSessionId() {
        return _sessionId;
    }

    public void setCategory(String value) {
        _category = value;
    }

    public String getCategory() {
        return _category;
    }

    public HashMap<String, Object> getKpiTuples() {
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

    public void setPerformanceTestUrl(String performanceTestUrl) {
        this._performanceTestUrl = performanceTestUrl;
    }

    public String getPerformanceTestUrl() {
        return _performanceTestUrl;
    }
}
