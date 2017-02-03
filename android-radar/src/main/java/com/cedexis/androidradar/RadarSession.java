package com.cedexis.androidradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadarSession {
    // Constants:
    private static final String TAG = RadarSession.class.getSimpleName();
    private static final String TAG_IMPACT = "RadarSessionTask.impact";
    private static final int MAJOR_VERSION = 3;
    private static final int MINOR_VERSION = 0;
    private static final String INIT_DOMAIN = "init.cedexis-radar.net";
    public static final String REPORT_DOMAIN = "rpt.cedexis.com";

    // Private members
    private int _transactionId;
    private long _sessionTimestamp;
    private String _requestSignature;
    private RadarSessionProperties _sessionProperties;
    private String _networkType;
    private String _networkSubtype;
    private Boolean _isCancelled = false;

    public int getTransactionId() {
        return _transactionId;
    }

    public long getSessionTimestamp() {
        return _sessionTimestamp;
    }

    public String getRequestSignature() {
        return _requestSignature;
    }

    public void setRequestSignature(String requestSignature) {
        this._requestSignature = requestSignature;
    }

    public int getRequestorZoneId() {
        return _sessionProperties.getRequestorZoneId();
    }

    public int getRequestorCustomerId() {
        return _sessionProperties.getRequestorCustomerId();
    }

    public String getNetworkType() {
        return _networkType;
    }

    public String getNetworkSubtype() {
        return _networkSubtype;
    }

    public RadarSessionProperties getSessionProperties() {
        return _sessionProperties;
    }

    public RadarSession(int transactionId, long sessionTimestamp, RadarSessionProperties sessionProperties, String networkType, String networkSubtype) {
        _transactionId = transactionId;
        _sessionTimestamp = sessionTimestamp;
        _sessionProperties = sessionProperties;
        _networkType = networkType;
        _networkSubtype = networkSubtype;
    }

    public static String makeHttpRequest(URL url, List<Pair<String, String>> headers, int connectTimeout, int readTimeout) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (0 < connectTimeout) {
            connection.setConnectTimeout(connectTimeout);
        }
        if (0 < readTimeout) {
            connection.setReadTimeout(readTimeout);
        }
        if (null != headers) {
            for (Pair<String, String> current : headers) {
                connection.setRequestProperty(current.first, current.second);
            }
        }
        InputStream in = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        in.close();
        return result.toString();
    }

    public static String makeHttpRequest(URL url, List<Pair<String, String>> headers) throws IOException {
        return makeHttpRequest(url, headers, -1, -1);
    }

    public static String makeHttpRequest(URL url) throws IOException {
        return makeHttpRequest(url, null, -1, -1);
    }

    public static RadarSession initializeRadarSession(RadarSessionProperties sessionProperties, Context context) {

        // Determine network status
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        // Both mobile and wifi may be null
        android.net.NetworkInfo networkInUse;
        if ((null != wifi) && wifi.isConnectedOrConnecting()) {
            networkInUse = wifi;
        } else if ((null != mobile) && mobile.isConnectedOrConnecting()) {
            networkInUse = mobile;
        } else {
            return null;
        }
        //Log.d(TAG, String.format("Network type name: %s", networkInUse.getTypeName()));
        //Log.d(TAG, String.format("Network subtype name: %s", networkInUse.getSubtypeName()));

        RadarSession result = new RadarSession(
                1 + new SecureRandom().nextInt(Integer.MAX_VALUE),
                System.currentTimeMillis() / 1000L,
                sessionProperties,
                networkInUse.getTypeName(),
                networkInUse.getSubtypeName()
        );

        StringBuilder seed = new StringBuilder();
        seed.append("i1-an-");
        seed.append(MAJOR_VERSION);
        seed.append("-");
        seed.append(MINOR_VERSION);
        seed.append("-");
        seed.append(sessionProperties.getRequestorZoneId());
        seed.append("-");
        seed.append(sessionProperties.getRequestorCustomerId());
        seed.append("-");
        seed.append(result.getTransactionId());
        seed.append("-i");

        StringBuilder urlString = new StringBuilder("http://");
        urlString.append(seed.toString());
        urlString.append(".");
        urlString.append(INIT_DOMAIN);
        urlString.append("/i1/");
        urlString.append(result.getSessionTimestamp());
        urlString.append("/");
        urlString.append(result.getTransactionId());
        urlString.append("/json?seed=");
        urlString.append(seed.toString());
        //Log.v(TAG, String.format("Init URL: %s", urlString));

        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(Pair.create("cedexis-android-network-type", result.getNetworkType()));
        headers.add(Pair.create("cedexis-android-network-subtype", result.getNetworkSubtype()));

        try {
            URL initUrl = new URL(urlString.toString());
            String initResponse = RadarSession.makeHttpRequest(initUrl, headers);
            //Log.d(TAG, initResponse);
            JSONObject json = new JSONObject(initResponse);
            result.setRequestSignature(json.getString("a"));

            Log.d(TAG, "RequestSignature: " + result.getRequestSignature());

            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void sendImpactReport() throws JSONException {
        RadarImpactProperties impact = this.getSessionProperties().getImpactProperties();
        if (null != impact) {
            JSONObject performanceTestResult = getImpactPerformanceValue(impact.getPerformanceTestUrl());
            if (!performanceTestResult.getString("result").equals("success")) {
                Log.w(TAG_IMPACT, performanceTestResult.toString());
                return;
            }
            JSONObject blob = new JSONObject();
            String temp = impact.getSessionId();
            if (null != temp) {
                blob.put("sessionID", temp);
            }
            temp = impact.getCategory();
            if (null != temp) {
                blob.put("category", temp);
            }
            HashMap<String, Object> tuples = impact.getKpiTuples();
            if (0 < tuples.size()) {
                JSONArray kpi = new JSONArray();
                for (Map.Entry<String, Object> pair : tuples.entrySet()) {
                    JSONArray tuple = new JSONArray();
                    tuple.put(pair.getKey());
                    tuple.put(pair.getValue());
                    kpi.put(tuple);
                }
                blob.put("kpi", kpi);
            }
            String blobAsString = blob.toString();
            //Log.d(TAG_IMPACT, blobAsString);
            byte[] blobAsBytes;
            try {
                blobAsBytes = blobAsString.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return;
            }

            String encodedString = Base64.encodeToString(blobAsBytes, Base64.URL_SAFE | Base64.NO_WRAP);
            String impactString = String.format("impact_kpi:%s", encodedString);
            //Log.d(TAG_IMPACT, impactString);

            try {
                sendImpactReport2(performanceTestResult.getInt("value"), impactString);
            } catch(ProtocolException e) {
                // Swallow
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    public void sendImpactReport2(int value, String impactString) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://");
        urlBuilder.append(REPORT_DOMAIN);
        //urlBuilder.append("/n1/0/1/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/");
        urlBuilder.append("/n1/0/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/");
        urlBuilder.append(value + 1);
        urlBuilder.append("/");
        urlBuilder.append(this.getRequestSignature());
        urlBuilder.append("/");
        urlBuilder.append(impactString);
        urlBuilder.append("/0");
        URL url = new URL(urlBuilder.toString());
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(Pair.create("cedexis-android-network-type", this.getNetworkType()));
        headers.add(Pair.create("cedexis-android-network-subtype", this.getNetworkSubtype()));
        headers.add(Pair.create("connection", "close"));
        //Log.d(TAG_IMPACT, String.format("Impact report URL: %s", url));
        RadarSession.makeHttpRequest(url, headers);
    }

    public static JSONObject getImpactPerformanceValue(String performanceTestUrl) throws JSONException {
        JSONObject result = new JSONObject();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(performanceTestUrl);
            long startedAt = new Date().getTime();
            connection = (HttpURLConnection) url.openConnection();
            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int n;
            OutputStream output = new ByteArrayOutputStream();
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
            }
            long elapsed = new Date().getTime() - startedAt;
            result.put("result", "success");
            result.put("value", elapsed);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            result.put("result", "error");
            result.put("exceptionMessage", e.getMessage());
            return result;
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "RadarSession{" +
                "_transactionId=" + _transactionId +
                ", _sessionTimestamp=" + _sessionTimestamp +
                ", _requestSignature='" + _requestSignature + '\'' +
                '}';
    }

    public void run() {
        try {
            this.sendImpactReport();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RadarProvider[] providers = RadarProvider.gatherRadarProviders(this, _sessionProperties);
        Log.d(TAG, providers.toString());

        for (int providerIndex = 0; providerIndex < providers.length; providerIndex++) {
            if (_isCancelled) {
                Log.d(TAG, "Session cancelled");
                return;
            }
            try {
                providers[providerIndex].process();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "Session complete");
    }

    public void stop() {
        _isCancelled = true;
    }
}
