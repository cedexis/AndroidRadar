package com.cedexis.mobileradarlib.rum;

public abstract class RUMReport extends RUMData {
    private int _reportId;
    
    public RUMReport(int reportId, long timestamp) {
        super(timestamp);
        this._reportId = reportId;
    }
    
    public int getReportId() {
        return this._reportId;
    }
}
