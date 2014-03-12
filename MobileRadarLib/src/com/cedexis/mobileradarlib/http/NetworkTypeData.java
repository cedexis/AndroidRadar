package com.cedexis.mobileradarlib.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cedexis.mobileradarlib.ReportData;

public class NetworkTypeData extends ReportData {
    private int _networkType;
    private int _subType;
    private String _subTypeName;
    
    public NetworkTypeData(int networkType, int subType, String subTypeName) {
        this._networkType = networkType;
        this._subType = subType;
        this._subTypeName = subTypeName;
    }
    
    @Override
    public List<String> getReportElements(String requestSignature) {
        List<String> result = new ArrayList<String>();
        result.add("f3");
        result.add(String.format("%d", this._networkType));
        result.add(String.format("%d", this._subType));
        result.add(requestSignature);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.getDefault(),
                "NetworkTypeData (%d, %d, %s)",
                this._networkType,
                this._subType,
                this._subTypeName);
    }

}
