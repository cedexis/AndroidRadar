package com.cedexis.mobileradarlib;

import java.util.List;

public abstract class ReportData {
    public abstract List<String> getReportElements(String requestSignature);
}
