package com.cedexis.androidradar;

import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by jrosas on 8/21/15.
 */
public class RadarProvider {
    private static final String PROVIDER_DOMAIN = "radar.cedexis.com";
    private static final String TAG = RadarProvider.class.getSimpleName();

    private int _ownerZoneId;
    private int _ownerCustomerId;
    private int _providerId;
    public RadarProbe[] _probes;

    public RadarProbe[] getProbes() {
        return _probes;
    }

    private RadarSession _session;

    public int getOwnerZoneId() {
        return _ownerZoneId;
    }

    public int getOwnerCustomerId() {
        return _ownerCustomerId;
    }

    public int getProviderId() {
        return _providerId;
    }

    public RadarProvider(JSONObject providerObject, RadarSession session) throws JSONException {
        _session = session;

        JSONObject providerData = providerObject.getJSONObject("p");
        JSONObject temp = providerData.getJSONObject("p");
        JSONObject probesData;
        if (temp.has("a")) {
            probesData = temp.getJSONObject("a");
        } else if (temp.has("b")) {
            probesData = temp.getJSONObject("b");
        } else {
            return;
        }

        _ownerZoneId = providerData.getInt("z");
        _ownerCustomerId = providerData.getInt("c");
        _providerId = providerData.getInt("i");

        List<RadarProbe> probeList = new ArrayList<>();
        if (probesData.has("a")) {
            RadarProbe probe = new RadarProbe(session, probesData.getJSONObject("a"), ProbeType.COLD, this);
            probeList.add(probe);
        }
        if (probesData.has("b")) {
            RadarProbe probe = new RadarProbe(session, probesData.getJSONObject("b"), ProbeType.RTT, this);
            probeList.add(probe);
        }
        if (probesData.has("c")) {
            RadarProbe probe = new RadarProbe(session, probesData.getJSONObject("c"), ProbeType.THROUGHPUT, this);
            probeList.add(probe);
        }
        _probes = probeList.toArray(new RadarProbe[probeList.size()]);
    }

    public void process(List<Pair<String, String>> progressData) throws JSONException {
        for(RadarProbe probe : _probes) {
            if (probe.getProbeType() == ProbeType.THROUGHPUT && !testThroughputSampleRate(_session)) {
                break;
            }
            if (!probe.measure(progressData)) {
                break;
            }
        }
    }

    public static RadarProvider[] gatherRadarProviders(RadarSession session, RadarSessionProperties sessionProperties) {
        List<RadarProvider> providers = new ArrayList<RadarProvider>();
        String[] protocols = {"http", "https"};
        for (int i = 0; i < protocols.length; i++) {
            try {
                URL url = new URL(makeProvidersRequestUrl(protocols[i], sessionProperties));
                //Log.v(TAG, String.format("Providers URL: %s", url));
                String response = RadarSessionTask.makeHttpRequest(url);
                JSONArray providersData = new JSONArray(response);
                for (int j = 0; j < providersData.length(); j++) {
                    providers.add(new RadarProvider(providersData.getJSONObject(j), session));
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return providers.toArray(new RadarProvider[providers.size()]);
    }

    public static String makeProvidersRequestUrl(String protocol, RadarSessionProperties sessionProperties) {
        StringBuilder result = new StringBuilder(protocol);
        result.append("://");
        result.append(PROVIDER_DOMAIN);
        result.append("/");
        result.append(sessionProperties.get_requestorZoneId());
        result.append("/");
        result.append(sessionProperties.get_requestorCustomerId());
        result.append("/radar/");
        result.append(1 + new SecureRandom().nextInt(Integer.MAX_VALUE));
        result.append("/");
        result.append(UUID.randomUUID().toString());
        result.append("/providers.json?imagesok=1");
        result.append("&t=1");
        return result.toString();
    }

    public static boolean testThroughputSampleRate(RadarSession session) {
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

    public static boolean testPercentage(double pct) {
        double temp = new Random().nextDouble();
        return temp < pct;
    }
}
