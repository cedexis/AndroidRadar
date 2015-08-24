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

    public static final String TAG = RadarSessionTask.class.getSimpleName();
    private static final String TAG_IMPACT = "RadarSessionTask.impact";
    private static final int MAJOR_VERSION = 3;
    private static final int MINOR_VERSION = 0;
    private String _initDomain = "init.cedexis-radar.net";
    //private String _providersDomain = "radar.test.wildlemur.com";
    public static final String REPORT_DOMAIN = "rpt.cedexis.com";
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
                    sendImpactReport(session);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RadarProvider[] providers = RadarProvider.gatherRadarProviders(session, params[i]);
                Log.d(TAG, providers.toString());
                List<Pair<String, String>> progressData = new ArrayList<>();
                progressData.add(Pair.create("providerCount", String.format("%d", providers.length)));
                publishProgress(new RadarSessionProgress("gotProviders", progressData));
                for (int providerIndex = 0; providerIndex < providers.length; providerIndex++) {
                    processProvider(session, providers[providerIndex]);
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
        urlBuilder.append(REPORT_DOMAIN);
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
        return result.toString();
    }

    public static String makeHttpRequest(URL url, List<Pair<String, String>> headers) throws IOException {
        return makeHttpRequest(url, headers, -1, -1);
    }

    public static String makeHttpRequest(URL url) throws IOException {
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

    /**
     * Builds the probes from the provider object, perform the measurements and publishes progress
     * @param session
     * @param provider
     */
    private void processProvider(RadarSession session, RadarProvider provider) {
            List<Pair<String, String>> progressData = new ArrayList<>();
            progressData.add(Pair.create("providerOwnerZoneId", Integer.toString(provider.getOwnerZoneId())));
            progressData.add(Pair.create("providerOwnerCustomerId", Integer.toString(provider.getOwnerCustomerId())));
            progressData.add(Pair.create("providerId", Integer.toString(provider.getProviderId())));
        try {
            provider.process(progressData);
            publishProgress(new RadarSessionProgress("finishedProvider", progressData));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
