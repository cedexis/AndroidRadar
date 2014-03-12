package com.cedexis.mobileradarlib.rum;

import com.cedexis.mobileradarlib.ReportData;

abstract class RUMData extends ReportData {
    private long _timestamp;
    
    public RUMData(long timestamp) {
        this._timestamp = timestamp;
    }
    
    public long getTimestamp() {
        return this._timestamp;
    }
}
