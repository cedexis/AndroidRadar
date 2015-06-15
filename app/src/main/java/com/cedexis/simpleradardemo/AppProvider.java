package com.cedexis.simpleradardemo;

public class AppProvider {
    private String _providerId;
    private String _providerName;
    private String _connectTimeText;
    private String _responseTimeText;
    private String _throughputText;

    public AppProvider(String providerId, String providerName) {
        _providerId = providerId;
        _providerName = providerName;
    }

    @Override
    public String toString() {
        return String.format("AppProvider (%s)", _providerName);
    }

    public String get_providerId() {
        return _providerId;
    }

    public void set_throughputText(String _throughputText) {
        this._throughputText = _throughputText;
    }

    public void set_connectTimeText(String _connectTimeText) {
        this._connectTimeText = _connectTimeText;
    }

    public void set_responseTimeText(String _responseTimeText) {
        this._responseTimeText = _responseTimeText;
    }

    public String get_providerName() {
        return _providerName;
    }

    public String get_connectTimeText() {
        return _connectTimeText;
    }

    public String get_responseTimeText() {
        return _responseTimeText;
    }

    public String get_throughputText() {
        return _throughputText;
    }

    public boolean has_connectTimeText() {
        return null != _connectTimeText;
    }

    public boolean has_responseTimeText() {
        return null != _responseTimeText;
    }

    public boolean has_throughputText() {
        return null != _throughputText;
    }
}
