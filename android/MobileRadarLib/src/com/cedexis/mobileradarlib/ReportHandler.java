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

public class ReportHandler implements Runnable {
    
    private static final String TAG = "ReportHandler";
    private ReportData _reportData;
    private String _reportHost;
    private String _agentName;
    private String _agentVersion;
    private String _requestSignature;
    private List<IPostReportHandler> _postReportHandlers;
    
    public ReportHandler(ReportData reportObject,
            String reportHost,
            String agentName,
            String agentVersion,
            String requestSignature,
            List<IPostReportHandler> postReportHandlers) {
        this._reportData = reportObject;
        this._reportHost = reportHost;
        this._agentName = agentName;
        this._agentVersion = agentVersion;
        this._requestSignature = requestSignature;
        this._postReportHandlers = postReportHandlers;
    }
    
    protected String getRequestSignature() {
        return this._requestSignature;
    }
    
    @Override
    public void run() {
        Log.d(TAG, "Sending report to " + this._reportHost);
        StringBuilder temp = new StringBuilder();
        temp.append("http://");
        temp.append(this._reportHost);
        List<String> elements = this._reportData.getReportElements(this._requestSignature);
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
                    connection.setRequestProperty(
                            "User-Agent",
                            String.format(
                                    "Radar Mobile Client/0.0.1 (Android) %s/%s",
                                    this._agentName,
                                    this._agentVersion));
                    InputStream in = connection.getInputStream();
                    InputStreamReader rawReader = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(rawReader);
                    String line;
                    while (null != (line = reader.readLine())) {
                        Log.d(TAG, line);
                    }
                    if (null != this._postReportHandlers) {
                        for (IPostReportHandler handler: this._postReportHandlers) {
                            handler.execute(this._reportData);
                        }
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
