package com.cedexis.androidradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.util.HashMap;
import java.util.Map;

public class RadarCommon {

    private final static String WTYPE = "WTYPE";
    public static final String WWAN_TYPE = "WWAN_TYPE";
    public static final String DEVICE_TYPE = "DEVICE_TYPE";

    public String getNetworkType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Notfound";
        }
    }

    private NetworkInfo obtainNetworkInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    Map<String, String> getParameters(Context context) {
        Map<String, String> params = new HashMap<>();
        NetworkInfo networkInfo = obtainNetworkInfo(context);
        String deviceType = context.getResources().getBoolean(R.bool.is_tablet) ? "tablet" : "phone";
        params.put(DEVICE_TYPE, deviceType);
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            // WLAN
            params.put(WTYPE, "WLAN");
        }

        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            // WWAN
            params.put(WTYPE, "WWAN");
            params.put(WWAN_TYPE, getNetworkType(context));
        }
        return params;
    }

}
