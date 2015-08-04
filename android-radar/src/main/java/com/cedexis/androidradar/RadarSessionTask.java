package com.cedexis.androidradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadarSessionTask extends AsyncTask<RadarSessionProperties, RadarSessionProgress, Void> {

    private static final String TAG = "RadarSessionTask";
    private static final String TAG_IMPACT = "RadarSessionTask.impact";
    private static final int MAJOR_VERSION = 3;
    private static final int MINOR_VERSION = 0;
    private static SecureRandom _random = new SecureRandom();
    private String _initDomain = "init.cedexis-radar.net";
    private String _providersDomain = "radar.cedexis.com";
    //private String _providersDomain = "radar.test.wildlemur.com";
    private String _reportDomain = "rpt.cedexis.com";
    private int _standardTimeout = 4000;
    private Context _context;
    private RadarSessionTaskCaller _caller = null;

    /**
     * The constructor for the RadarSessionTask class.
     *
     * @param context The Context in which the task runs.  Generally the Activity where the task
     *                 is created.
     */
    public RadarSessionTask(Context context) {
        this._context = context;
    }

    public interface RadarSessionTaskCaller {
        public void onProgress(RadarSessionProgress sessionProgress);
    }

    public void set_caller(RadarSessionTaskCaller _caller) {
        this._caller = _caller;
    }

    @Override
    protected Void doInBackground(RadarSessionProperties... params) {
        int countOfParams = params.length;
        for (int i = 0; i < countOfParams; i++) {
            RadarSession session = initializeRadarSession(params[i]);
            if (null != session) {
                //Log.d(TAG, session.toString());
                try {
//                    int j = 100;
//                    while (0 < j--) {
//                        sendImpactReport(session);
//                    }
                    sendImpactReport(session);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray providers = gatherRadarProviders(params[i]);
                Log.d(TAG, providers.toString());
                int countOfProviders = providers.length();
                List<Pair<String, String>> progressData = new ArrayList<>();
                progressData.add(Pair.create("providerCount", String.format("%d", countOfProviders)));
                publishProgress(new RadarSessionProgress("gotProviders", progressData));
                for (int providerIndex = 0; providerIndex < countOfProviders; providerIndex++) {
                    try {
                        processProvider(session, providers.getJSONObject(providerIndex));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
            publishProgress(new RadarSessionProgress("sessionComplete"));
        }
        return null;
    }

    private void sendImpactReport(RadarSession session) throws JSONException {
        RadarImpactProperties impact = session.get_sessionProperties().get_impactProperties();
        if (null != impact) {
            JSONObject performanceTestResult = getImpactPerformanceValue(impact.get_performanceTestUrl());
            if (!performanceTestResult.getString("result").equals("success")) {
                Log.w(TAG_IMPACT, performanceTestResult.toString());
                return;
            }
            JSONObject blob = new JSONObject();
            String temp = impact.get_sessionId();
            if (null != temp) {
                blob.put("sessionID", temp);
            }
            temp = impact.get_category();
            if (null != temp) {
                blob.put("category", temp);
            }
            List<Pair<String, Object>> tuples = impact.get_kpiTuples();
            if (0 < tuples.size()) {
                JSONArray kpi = new JSONArray();
                for (Pair<String, Object> pair : tuples) {
                    JSONArray tuple = new JSONArray();
                    tuple.put(pair.first);
                    tuple.put(pair.second);
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
                sendImpactReport2(session, performanceTestResult.getInt("value"), impactString);
            } catch(ProtocolException e) {
                // Swallow
            } catch (IOException e) {
                Log.d(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    private void sendImpactReport2(RadarSession session, int value, String impactString) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://");
        urlBuilder.append(_reportDomain);
        //urlBuilder.append("/n1/0/1/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/0/");
        urlBuilder.append("/n1/0/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/1/");
        urlBuilder.append(value + 1);
        urlBuilder.append("/");
        urlBuilder.append(session.get_requestSignature());
        urlBuilder.append("/");
        urlBuilder.append(impactString);
        urlBuilder.append("/0");
        URL url = new URL(urlBuilder.toString());
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(Pair.create("cedexis-android-network-type", session.get_networkType()));
        headers.add(Pair.create("cedexis-android-network-subtype", session.get_networkSubtype()));
        headers.add(Pair.create("connection", "close"));
        //Log.d(TAG_IMPACT, String.format("Impact report URL: %s", url));
        makeHttpRequest(url, headers);
    }

    private JSONObject getImpactPerformanceValue(String performanceTestUrl) throws JSONException {
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
    protected void onProgressUpdate(RadarSessionProgress... values) {
        super.onProgressUpdate(values);
        if (null != _caller) {
            int countOfValues = values.length;
            for (int i = 0; i < countOfValues; i++) {
                _caller.onProgress(values[i]);
            }
        }
    }

    private JSONArray gatherRadarProviders(RadarSessionProperties sessionProperties) {
        JSONArray result = new JSONArray();
        String[] protocols = {"http", "https"};
        for (int i = 0; i < protocols.length; i++) {
            try {
                URL url = new URL(makeProvidersRequestUrl(protocols[i], sessionProperties));
                //Log.v(TAG, String.format("Providers URL: %s", url));
                String source = makeHttpRequest(url);
                JSONArray temp = new JSONArray(source);
                for (int j = 0; j < temp.length(); j++) {
                    result.put(temp.get(j));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String makeProvidersRequestUrl(String protocol, RadarSessionProperties sessionProperties) {
        StringBuilder result = new StringBuilder(protocol);
        result.append("://");
        result.append(_providersDomain);
        result.append("/");
        result.append(sessionProperties.get_requestorZoneId());
        result.append("/");
        result.append(sessionProperties.get_requestorCustomerId());
        result.append("/radar/");
        result.append(1 + _random.nextInt(Integer.MAX_VALUE));
        result.append("/");
        result.append(UUID.randomUUID().toString());
        result.append("/providers.json?imagesok=1");
        result.append("&t=1");
        return result.toString();
    }

    private String makeHttpRequest(URL url, List<Pair<String, String>> headers, int connectTimeout, int readTimeout) throws IOException {
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
        return result.toString();
    }

    private String makeHttpRequest(URL url, List<Pair<String, String>> headers) throws IOException {
        return makeHttpRequest(url, headers, -1, -1);
    }

    private String makeHttpRequest(URL url) throws IOException {
        return makeHttpRequest(url, null, -1, -1);
    }

    private RadarSession initializeRadarSession(RadarSessionProperties sessionProperties) {

        // Determine network status
        ConnectivityManager connMgr = (ConnectivityManager) this._context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                1 + _random.nextInt(Integer.MAX_VALUE),
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
        seed.append(sessionProperties.get_requestorZoneId());
        seed.append("-");
        seed.append(sessionProperties.get_requestorCustomerId());
        seed.append("-");
        seed.append(result.get_transactionId());
        seed.append("-i");

        StringBuilder urlString = new StringBuilder("http://");
        urlString.append(seed.toString());
        urlString.append(".");
        urlString.append(_initDomain);
        urlString.append("/i1/");
        urlString.append(result.get_sessionTimestamp());
        urlString.append("/");
        urlString.append(result.get_transactionId());
        urlString.append("/json?seed=");
        urlString.append(seed.toString());
        //Log.v(TAG, String.format("Init URL: %s", urlString));

        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(Pair.create("cedexis-android-network-type", result.get_networkType()));
        headers.add(Pair.create("cedexis-android-network-subtype", result.get_networkSubtype()));

        try {
            URL initUrl = new URL(urlString.toString());
            String initResponse = makeHttpRequest(initUrl, headers);
            //Log.d(TAG, initResponse);
            JSONObject json = new JSONObject(initResponse);
            result.set_requestSignature(json.getString("a"));


            List<Pair<String, String>> progressData = new ArrayList<>();
            progressData.add(Pair.create("requestSignature", result.get_requestSignature()));
            publishProgress(new RadarSessionProgress("init", progressData));

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void processProvider(RadarSession session, JSONObject providerObject) {
        //Log.d(TAG, providerObject.toString());
        try {
            JSONObject providerData = providerObject.getJSONObject("p");
            JSONObject temp = providerData.getJSONObject("p");
            JSONObject probes;
            if (temp.has("a")) {
                probes = temp.getJSONObject("a");
            } else if (temp.has("b")) {
                probes = temp.getJSONObject("b");
            } else {
                return;
            }

            //Log.d(TAG, probes.toString());
            boolean keepGoing = true;
            List<Pair<String, String>> progressData = new ArrayList<>();
            progressData.add(Pair.create("providerOwnerZoneId", providerData.getString("z")));
            progressData.add(Pair.create("providerOwnerCustomerId", providerData.getString("c")));
            progressData.add(Pair.create("providerId", providerData.getString("i")));
            if (probes.has("a")) {
                keepGoing = measureProbe(session, 1, providerData, probes.getJSONObject("a"), progressData);
            }
            if (keepGoing && probes.has("b")) {
                keepGoing = measureProbe(session, 0, providerData, probes.getJSONObject("b"), progressData);
            }
            if (keepGoing && probes.has("c") && testThroughputSampleRate(session)) {
                measureProbe(session, 14, providerData, probes.getJSONObject("c"), progressData);
            }
            publishProgress(new RadarSessionProgress("finishedProvider", progressData));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean testThroughputSampleRate(RadarSession session) {
        RadarSessionProperties sessionProperties = session.get_sessionProperties();
        double pct;
        Log.d(TAG, String.format("Network type: %s %s", session.get_networkType(), session.get_networkSubtype()));
        if (session.get_networkType().equalsIgnoreCase("mobile")) {
            pct = sessionProperties.get_throughputSampleRateMobile();
        } else {
            pct = sessionProperties.get_throughputSampleRate();
        }
        return testPercentage(pct);
    }

    private boolean testPercentage(double pct) {
        double temp = new Random().nextDouble();
        return temp < pct;
    }

    private boolean measureProbe(RadarSession session, int probeTypeId, JSONObject providerData,
                                 JSONObject probe,
                                 List<Pair<String, String>> progressData) throws JSONException {
        //Log.d(TAG, probe.toString());
        URL url = null;
        try {
            url = new URL(makeProbeUrl(session, probeTypeId, providerData, probe));
        } catch (MalformedURLException | RadarClientException e) {
            e.printStackTrace();
            return false;
        }
        //Log.v(TAG, String.format("Probe URL: %s", url));

        int resultCode = 0;
        long measurement;

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
//            connection.setConnectTimeout(10);
//            connection.setReadTimeout(10);
            Date startedAt = new Date();
            Date endedAt;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            switch (probeTypeId) {
                case 1:
                case 0:
                    reader.readLine();
                    endedAt = new Date();
                    break;
                case 14:
                    while (null != reader.readLine()) {
                        continue;
                    }
                    endedAt = new Date();
                    break;
                default:
                    throw new RadarClientException(String.format("Unexpected probe type id: %d", probeTypeId));
            }
            long elapsed = endedAt.getTime() - startedAt.getTime();
            //Log.d(TAG, String.format("Elapsed: %d", elapsed));
            if (elapsed <= _standardTimeout) {
                if (14 == probeTypeId) {
                    measurement = calculateThroughput(elapsed, probe);
                } else {
                    measurement = elapsed;
                }
            } else {
                resultCode = 1;
                measurement = 0;
            }
            //Log.d(TAG, String.format("Measurement: %d", measurement));
        } catch (IOException e) {
            resultCode = 1;
            measurement = 0;
        } catch (RadarClientException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }

        URL reportUrl = null;
        try {
            reportUrl = new URL(makeReportUrl(session, probeTypeId, providerData, measurement, resultCode));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, String.format("Report URL: %s", reportUrl));
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(Pair.create("cedexis-android-network-type", session.get_networkType()));
        headers.add(Pair.create("cedexis-android-network-subtype", session.get_networkSubtype()));
        try {
            makeHttpRequest(reportUrl, headers);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        String probeType = "unknown";
        String units = "ms";
        switch (probeTypeId) {
            case 1:
                probeType = "connect";
                break;
            case 0:
                probeType = "rtt";
                break;
            case 14:
                probeType = "throughput";
                units = "kbps";
                break;
        }
        String measurementKey = String.format("measurement.%s", probeType);
        if (0 != resultCode) {
            progressData.add(Pair.create(measurementKey, "error"));
            return false;
        }
        progressData.add(Pair.create(measurementKey, String.format("%d %s", measurement, units)));
        return true;
    }

    private long calculateThroughput(long elapsed, JSONObject probe) throws JSONException {
        String url = probe.getString("u");
        Pattern p = Pattern.compile("-(\\d+)kb", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(url);
        // Eventually this number will be sent with the provider configuration, but for now we
        // must get it from the filename
        int fileSize = 100;
        if (m.find()) {
            fileSize = Integer.parseInt(m.group(1));
        }
        return Math.round(Math.floor(8 * 1000 * fileSize / elapsed));
    }

    private String makeProbeUrl(RadarSession session, int probeTypeId, JSONObject providerData, JSONObject probe)
            throws RadarClientException, JSONException {
        String baseUrl = probe.getString("u");
        int objectType = probe.getInt("t");
        if (9 == objectType) {
            throw new RadarClientException("DNS measurement type not implemented");
        }
        StringBuilder queryString = new StringBuilder("?rnd=");
        queryString.append(probeTypeId);
        queryString.append("-");
        queryString.append(session.get_requestorZoneId());
        queryString.append("-");
        queryString.append(session.get_requestorCustomerId());
        queryString.append("-");
        queryString.append(providerData.getInt("z"));
        queryString.append("-");
        queryString.append(providerData.getInt("c"));
        queryString.append("-");
        queryString.append(providerData.getInt("i"));
        queryString.append("-");
        queryString.append(session.get_transactionId());
        queryString.append("-");
        queryString.append(session.get_requestSignature());
        return baseUrl + queryString.toString();
    }

    private String makeReportUrl(RadarSession session, int probeTypeId, JSONObject providerData, long measurement, int resultCode) throws JSONException {
        StringBuilder result = new StringBuilder("http://");
        result.append(_reportDomain);
        result.append("/f1/");
        result.append(session.get_requestSignature());
        result.append("/");
        result.append(providerData.getInt("z"));
        result.append("/");
        result.append(providerData.getInt("c"));
        result.append("/");
        result.append(providerData.getInt("i"));
        result.append("/");
        result.append(probeTypeId);
        result.append("/");
        result.append(resultCode);
        result.append("/");
        result.append(measurement);
        return result.toString();
    }
}
