package com.cedexis.mobileradarlib.http;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.cedexis.mobileradarlib.DeviceStateChecker;
import com.cedexis.mobileradarlib.IPostReportHandler;
import com.cedexis.mobileradarlib.InitResult;
import com.cedexis.mobileradarlib.ReportData;
import com.cedexis.mobileradarlib.ReportHandler;

public class RadarHttpSessionManager {
    
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY = 1000;
    private static final List<String> PROBE_TYPES_TO_MEASURE = new ArrayList<String>(
            Arrays.asList(
                    "v1",
                    "custom-page",
                    "custom-js",
                    "custom-img"));
    
    private ExecutorService _threadPool;
    private Application _app;
    private int _zoneId;
    private int _customerId;
    private String _initHost;
    private String _reportHost;
    private String _probeServerHost;
    private String _agentName;
    private String _agentVersion;
    List<IPostReportHandler> _postReportHandlers;
    
    public RadarHttpSessionManager(
            Application context,
            int zoneId,
            int customerId,
            String agentName,
            String agentVersion) {
        this(context,
                zoneId,
                customerId,
                "init.cedexis-radar.net",
                "report.init.cedexis-radar.net",
                "probes.cedexis.com",
                agentName,
                agentVersion);
    }
    
    public RadarHttpSessionManager(
            Application context,
            int zoneId,
            int customerId,
            String initHost,
            String reportHost,
            String probeServerHost,
            String agentName,
            String agentVersion) {
        this._app = context;
        this._zoneId = zoneId;
        this._customerId = customerId;
        this._initHost = initHost;
        this._reportHost = reportHost;
        this._probeServerHost = probeServerHost;
        this._postReportHandlers = new ArrayList<IPostReportHandler>();
        this._agentName = agentName;
        this._agentVersion = agentVersion;
    }
    
    /**
     * Enable caller to register a function to be called after a report is sent.
     * 
     * @param handler
     */
    public void addPostReportHandler(IPostReportHandler handler) {
        if (!this._postReportHandlers.contains(handler)) {
            this._postReportHandlers.add(handler);
        }
    }
    
    /**
     * Remove a callback registered with RadarHttpSession.addPostReportHandler.
     * 
     * @param handler
     */
    public void removePostReportHandler(IPostReportHandler handler) {
        this._postReportHandlers.remove(handler);
    }
    
    private ExecutorService getThreadPool() {
        if (null == this._threadPool) {
            this._threadPool = Executors.newSingleThreadExecutor();
        }
        return this._threadPool;
    }
    
    public void queueSession() {
        this.getThreadPool().execute(new Runnable() {
            
            private String queryProbeServer(List<String> providerIds) {
                StringBuilder probeServerURL = new StringBuilder();
                probeServerURL.append("http://");
                probeServerURL.append(RadarHttpSessionManager.this._probeServerHost);
                probeServerURL.append("?");
                probeServerURL.append(String.format("z=%d", RadarHttpSessionManager.this._zoneId));
                probeServerURL.append(String.format("&c=%d", RadarHttpSessionManager.this._customerId));
                probeServerURL.append("&fmt=json");
                probeServerURL.append("&m=1");
                if (!providerIds.isEmpty()) {
                    probeServerURL.append("&i=" + TextUtils.join(",", providerIds));
                }
                if (DeviceStateChecker.isOnWifi(RadarHttpSessionManager.this._app)) {
                    probeServerURL.append("&allowThroughput=1");
                }
                else {
                    probeServerURL.append("&allowThroughput=1");
                }
                probeServerURL.append(String.format("&rnd=%s", UUID.randomUUID().toString()));
                
                URL url;
                try {
                    url = new URL(probeServerURL.toString());
                    int tries = 0;
                    while (RadarHttpSessionManager.MAX_RETRIES > tries) {
                        tries++;
                        HttpURLConnection connection;
                        try {
                            connection = (HttpURLConnection)url.openConnection();
                            boolean doRetry = false;
                            try {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(
                                                connection.getInputStream()));
                                StringBuilder content = new StringBuilder();
                                String line;
                                while (null != (line = reader.readLine())) {
                                    content.append(line);
                                }
                                reader.close();
                                return content.toString();
                            }
                            catch (FileNotFoundException e) {
                                //e.printStackTrace();
                                doRetry = true;
                            }
                            finally {
                                connection.disconnect();
                            }
                            
                            if (doRetry) {
                                try {
                                    Thread.sleep(RadarHttpSessionManager.RETRY_DELAY);
                                }
                                catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            public void run() {
                if (DeviceStateChecker.okToMeasure(RadarHttpSessionManager.this._app)) {
                    // Perform an init request
                    InitResult initResult = InitResult.doInit(
                            RadarHttpSessionManager.this._zoneId,
                            RadarHttpSessionManager.this._customerId,
                            RadarHttpSessionManager.this._initHost);
                    
                    boolean keepGoing = true;
                    int downloads = 0;
                    List<String> providerIds = new ArrayList<String>();
                    // Make request to probe server
                    while (keepGoing) {
                        // This is just to protect against something crazy happening
                        if (20 <= downloads) {
                            break;
                        }
                        downloads++;
                        String probeServerResult;
                        if (null != (probeServerResult = this.queryProbeServer(providerIds))) {
                            try {
                                JSONObject json = new JSONObject(probeServerResult);
                                if (json.has("a")) {
                                    String providerType = json.getString("a");
                                    String providerId = json.getJSONObject("p").getString("i");
                                    providerIds.add(providerId);
                                    
                                    // We're already in a background thread so there's
                                    // really no need to use a new one here.  And
                                    // we generally prefer to only do one download
                                    // at a time anyway.
                                    if (providerType.equals("networktype")) {
                                        //new NetworkTypeReportHandler(
                                        //        initResult.getRequestSignature()).run();
                                        ConnectivityManager manager =
                                                (ConnectivityManager)RadarHttpSessionManager
                                                .this._app.getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                                        if (null != networkInfo) {
                                            int networkType = networkInfo.getType();
                                            int subType = networkInfo.getSubtype();
                                            String subTypeName = networkInfo.getSubtypeName();
                                            ReportData data = new NetworkTypeData(
                                                    networkType,
                                                    subType,
                                                    subTypeName);
                                            new ReportHandler(data,
                                                    RadarHttpSessionManager.this._reportHost,
                                                    RadarHttpSessionManager.this._agentName,
                                                    RadarHttpSessionManager.this._agentVersion,
                                                    initResult.getRequestSignature(),
                                                    RadarHttpSessionManager.this._postReportHandlers)
                                                    .run();
                                        }
                                    }
                                    else if (providerType.equals("probe")) {
                                        this.measureRemoteProbe(
                                            json,
                                            initResult.getRequestSignature());
                                    }
                                    else {
                                        keepGoing = false;
                                    }
                                }
                                else {
                                    keepGoing = false;
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                                keepGoing = false;
                            }
                        }
                        else {
                            keepGoing = false;
                        }
                    }
                }
            }
            
            private void measureRemoteProbe(JSONObject json, String requestSignature) {
                try {
                    JSONObject provider = json.getJSONObject("p");
                    int providerOwnerZoneId = Integer.parseInt(provider.getString("z"));
                    int providerOwnerCustomerId = Integer.parseInt(provider.getString("c"));
                    int providerId = Integer.parseInt(provider.getString("i"));
                    boolean cacheBusting = Boolean.parseBoolean(provider.getString("b"));
                    
                    JSONArray probes = provider.getJSONArray("p");
                    for (int i = 0; i < probes.length(); i++) {
                        JSONObject current = probes.getJSONObject(i);
                        String probeType = current.getString("a");
                        if (!RadarHttpSessionManager.PROBE_TYPES_TO_MEASURE.contains(probeType)) {
                            continue;
                        }
                        int probeTypeNum = current.getInt("t");
                        String rawUrl = current.getString("u");
                        StringBuilder url = new StringBuilder();
                        url.append(rawUrl);
                        if (cacheBusting) {
                            if (rawUrl.contains("?")) {
                                url.append("&");
                            }
                            else {
                                url.append("?");
                            }
                            url.append(String.format("rnd=%s", UUID.randomUUID().toString()));
                        }
                        
                        try {
                            long start = new Date().getTime();
                            InputStream stream = new URL(url.toString()).openStream();
                            byte[] buffer = new byte[1024];
                            while (0 < stream.read(buffer)) {
                                // Just swallow the data
                            }
                            long elapsed = new Date().getTime() - start;
                            stream.close();
                            
                            long measurement = elapsed;
                            // Calculate KBPS if this is a throughput measurement
                            if ((23 == probeTypeNum) || (30 == probeTypeNum)) {
                                if (!current.has("s")) {
                                    return;
                                }
                                double kbps = 8 * 1000 * current.getInt("s") / (double)elapsed;
                                measurement = (int)kbps;
                            }
                            
                            // Send the report synchronously
                            ReportData data = new RemoteProbingData(
                                    providerOwnerZoneId,
                                    providerOwnerCustomerId,
                                    providerId,
                                    probeTypeNum,
                                    0,
                                    measurement);
                            new ReportHandler(
                                    data,
                                    RadarHttpSessionManager.this._reportHost,
                                    RadarHttpSessionManager.this._agentName,
                                    RadarHttpSessionManager.this._agentVersion,
                                    requestSignature,
                                    RadarHttpSessionManager.this._postReportHandlers).run();
                        }
                        catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
