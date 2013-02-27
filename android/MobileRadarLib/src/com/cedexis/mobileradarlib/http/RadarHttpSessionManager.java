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
import android.util.Log;

import com.cedexis.mobileradarlib.DeviceStateChecker;
import com.cedexis.mobileradarlib.InitResult;
import com.cedexis.mobileradarlib.ReportHandler;

public class RadarHttpSessionManager {
    
    private static final String TAG = "RadarHttpSessionManager";
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY = 1000;
    
    private ExecutorService _threadPool;
    private Application _app;
    private int _zoneId;
    private int _customerId;
    private String _initHost;
    private String _reportHost;
    private String _probeServerHost;
    
    public RadarHttpSessionManager(Application context, int zoneId,
            int customerId) {
        this(context, zoneId, customerId, "init.cedexis-radar.net",
                "report.init.cedexis-radar.net", "probes.cedexis.com");
    }
    
    public RadarHttpSessionManager(Application context, int zoneId,
            int customerId, String initHost, String reportHost,
            String probeServerHost) {
        this._app = context;
        this._zoneId = zoneId;
        this._customerId = customerId;
        this._initHost = initHost;
        this._reportHost = reportHost;
        this._probeServerHost = probeServerHost;
    }
    
    private ExecutorService getThreadPool() {
        if (null == this._threadPool) {
            this._threadPool = Executors.newSingleThreadExecutor();
        }
        return this._threadPool;
    }
    
    public void queueSession() {
        Log.d(TAG, "Queueing a Radar HTTP session");
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
                
                Log.d(TAG, "Opening " + probeServerURL.toString());
                URL url;
                try {
                    url = new URL(probeServerURL.toString());
                    int tries = 0;
                    while (RadarHttpSessionManager.MAX_RETRIES > tries) {
                        if (0 < tries) {
                            Log.d(TAG, "Retrying request to Probe Server");
                        }
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
                                Log.w(TAG, "Oh noes!!! URLConnection response code: " + connection.getResponseCode());
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
                    
                    if (RadarHttpSessionManager.MAX_RETRIES <= tries) {
                        Log.w(TAG, "Gave up on Probe Server");
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
                    Log.d(TAG, "Starting Radar HTTP session");
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
                            Log.w(TAG, String.format("Cancelling Radar HTTP session after %d downloads", downloads));
                            break;
                        }
                        downloads++;
                        String probeServerResult;
                        if (null != (probeServerResult = this.queryProbeServer(providerIds))) {
                            Log.d(TAG, probeServerResult);
                            try {
                                JSONObject json = new JSONObject(probeServerResult);
                                if (json.has("a")) {
                                    String providerType = json.getString("a");
                                    Log.d(TAG, "Provider type: " + providerType);
                                    String providerId = json.getJSONObject("p").getString("i");
                                    Log.d(TAG, "Provider id: " + providerId);
                                    providerIds.add(providerId);
                                    
                                    // We're already in a background thread so there's
                                    // really no need to use a new one here.  And
                                    // we generally prefer to only do one download
                                    // at a time anyway.
                                    if (providerType.equals("networktype")) {
                                        new NetworkTypeReportHandler(
                                                initResult.getRequestSignature()).run();
                                    }
                                    else if (providerType.equals("probe")) {
                                        this.measureRemoteProbe(
                                            json,
                                            initResult.getRequestSignature());
                                    }
                                    else {
                                        Log.e(TAG, "Unexpected provider type: " + providerType);
                                        keepGoing = false;
                                    }
                                }
                                else {
                                    keepGoing = false;
                                    if (json.has("comment")) {
                                        Log.d(TAG, json.getString("comment"));
                                    }
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
                    Log.d(TAG, "Radar HTTP session complete");
                }
            }
            
            private void measureRemoteProbe(JSONObject json, String requestSignature) {
                try {
                    JSONObject requestor = json.getJSONObject("r");
                    JSONObject provider = json.getJSONObject("p");
                    boolean onWifi = DeviceStateChecker.isOnWifi(RadarHttpSessionManager.this._app);
                    int requestorZoneId = Integer.parseInt(requestor.getString("z"));
                    int requestorCustomerId = Integer.parseInt(requestor.getString("c"));
                    int providerOwnerZoneId = Integer.parseInt(provider.getString("z"));
                    int providerOwnerCustomerId = Integer.parseInt(provider.getString("c"));
                    int providerId = Integer.parseInt(provider.getString("i"));
                    boolean cacheBusting = Boolean.parseBoolean(provider.getString("b"));
                    Log.d(TAG, "On WIFI: " + onWifi);
                    Log.d(TAG, "Requestor zone id: " + requestorZoneId);
                    Log.d(TAG, "Requestor customer id: " + requestorCustomerId);
                    Log.d(TAG, "Provider owner zone id: " + providerOwnerZoneId);
                    Log.d(TAG, "Provider owner customer id: " + providerOwnerCustomerId);
                    Log.d(TAG, "Provider id: " + providerId);
                    Log.d(TAG, "Cache busting: " + cacheBusting);
                    
                    JSONArray probes = provider.getJSONArray("p");
                    for (int i = 0; i < probes.length(); i++) {
                        JSONObject current = probes.getJSONObject(i);
                        String probeType = current.getString("a");
                        int probeTypeNum = current.getInt("t");
                        String rawUrl = current.getString("u");
                        Log.d(TAG, "Probe type: " + probeType);
                        Log.d(TAG, "Probe type number: " + probeTypeNum);
                        Log.d(TAG, "Raw URL: " + rawUrl);
                        
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
                        Log.d(TAG, "URL: " + url.toString());
                        
                        try {
                            long start = new Date().getTime();
                            InputStream stream = new URL(url.toString()).openStream();
                            byte[] buffer = new byte[1024];
                            while (0 < stream.read(buffer)) {
                                // Just swallow the data
                            }
                            long elapsed = new Date().getTime() - start;
                            stream.close();
                            Log.d(TAG, String.format("Time elapsed: %d ms", elapsed));
                            
                            long measurement = elapsed;
                            // Calculate KBPS if this is a throughput measurement
                            if ((23 == probeTypeNum) || (30 == probeTypeNum)) {
                                if (!current.has("s")) {
                                    Log.w(TAG, "Missing file size hint");
                                    return;
                                }
                                double kbps = 8 * 1000 * current.getInt("s") / (double)elapsed;
                                Log.d(TAG, "KBPS: " + kbps);
                                measurement = (int)kbps;
                            }
                            
                            // Send the report synchronously
                            new RemoteProbeReportHandler(
                                requestSignature,
                                providerOwnerZoneId,
                                providerOwnerCustomerId,
                                providerId,
                                probeTypeNum,
                                0,
                                measurement).run();
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
    
    class RemoteProbeReportHandler extends ReportHandler {
        
        private int _providerOwnerZoneId;
        private int _providerOwnerCustomerId;
        private int _providerId;
        private int _probeTypeNum;
        private int _responseCode;
        private long _measurement;
        
        public RemoteProbeReportHandler(String requestSignature, int providerOwnerZoneId,
                int providerOwnerCustomerId, int providerId, int probeTypeNum,
                int responseCode, long measurement) {
            super(RadarHttpSessionManager.this._reportHost, requestSignature);
            this._providerOwnerZoneId = providerOwnerZoneId;
            this._providerOwnerCustomerId = providerOwnerCustomerId;
            this._providerId = providerId;
            this._probeTypeNum = probeTypeNum;
            this._responseCode = responseCode;
            this._measurement = measurement;
        }
        @Override
        public List<String> getReportElements() {
            List<String> result = new ArrayList<String>();
            result.add("f1");
            result.add(this.getRequestSignature());
            result.add(String.format("%d", this._providerOwnerZoneId));
            result.add(String.format("%d", this._providerOwnerCustomerId));
            result.add(String.format("%d", this._providerId));
            result.add(String.format("%d", this._probeTypeNum));
            result.add(String.format("%d", this._responseCode));
            result.add(String.format("%d", this._measurement));
            return result;
        }
    }
    
    class NetworkTypeReportHandler extends ReportHandler {
        
        public NetworkTypeReportHandler(String requestSignature) {
            super(RadarHttpSessionManager.this._reportHost, requestSignature);
        }
        
        @Override
        public List<String> getReportElements() {
            List<String> result = new ArrayList<String>();
            result.add("f3");
            
            ConnectivityManager manager = (ConnectivityManager)RadarHttpSessionManager
                    .this._app.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (null == networkInfo) {
                return null;
            }
            
            int networkType = networkInfo.getType();
            int subType = networkInfo.getSubtype();
            Log.d(TAG, String.format("Network info: %d, %d, %s", networkType, subType, networkInfo.getSubtypeName()));
            result.add(String.format("%d", networkType));
            result.add(String.format("%d", subType));
            result.add(this.getRequestSignature());
            return result;
        }
    }
}
