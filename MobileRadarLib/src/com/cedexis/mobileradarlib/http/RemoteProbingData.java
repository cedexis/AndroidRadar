package com.cedexis.mobileradarlib.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cedexis.mobileradarlib.ReportData;

public class RemoteProbingData extends ReportData {
    int _zoneId;
    int _customerId;
    int _providerId;
    int _probeTypeNum;
    int _responseCode;
    long _measurement;
    
    public RemoteProbingData(int zoneId, int customerId, int providerId,
            int probeTypeNum, int responseCode, long measurement) {
        this._zoneId = zoneId;
        this._customerId = customerId;
        this._providerId = providerId;
        this._probeTypeNum = probeTypeNum;
        this._responseCode = responseCode;
        this._measurement = measurement;
    }
    
    @Override
    public List<String> getReportElements(String requestSignature) {
        List<String> result = new ArrayList<String>();
        result.add("f1");
        result.add(requestSignature);
        result.add(String.format("%d", this._zoneId));
        result.add(String.format("%d", this._customerId));
        result.add(String.format("%d", this._providerId));
        result.add(String.format("%d", this._probeTypeNum));
        result.add(String.format("%d", this._responseCode));
        result.add(String.format("%d", this._measurement));
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "RemoteProbingData (%d, %d, %d, %d, %d, %d)",
                this._zoneId,
                this._customerId,
                this._providerId,
                this._probeTypeNum,
                this._responseCode,
                this._measurement);
    }

}
