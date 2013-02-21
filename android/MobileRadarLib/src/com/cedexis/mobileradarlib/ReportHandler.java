package com.cedexis.mobileradarlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import android.util.Log;

public abstract class ReportHandler implements IReportHandler {
    
    private static final String TAG = "ReportHandler";
    private String _reportHost;
    private String _requestSignature;
    
    public ReportHandler(String reportHost, String requestSignature) {
        this._reportHost = reportHost;
        this._requestSignature = requestSignature;
    }
    
    public abstract List<String> getReportElements();
    
    protected String getRequestSignature() {
        return this._requestSignature;
    }
    
    @Override
    public void run() {
        Log.d(TAG, "Sending report to " + this._reportHost);
        StringBuilder temp = new StringBuilder();
        temp.append("http://");
        temp.append(this._reportHost);
        List<String> elements = this.getReportElements();
        if (null == elements) {
            Log.w(TAG, "No report elements.");
            return;
        }
        for (String item: elements) {
            try {
                temp.append(String.format("/%s", URLEncoder.encode(item, "UTF-8")));
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return;
            }
        }
        temp.append(String.format("?rnd=%s", UUID.randomUUID().toString()));
        Log.d(TAG, temp.toString());
        
        URL url;
        try {
            url = new URL(temp.toString());
            try {
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                try {
                    InputStream in = connection.getInputStream();
                    InputStreamReader rawReader = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(rawReader);
                    String line;
                    while (null != (line = reader.readLine())) {
                        Log.d(TAG, line);
                    }
                }
                finally {
                    connection.disconnect();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
