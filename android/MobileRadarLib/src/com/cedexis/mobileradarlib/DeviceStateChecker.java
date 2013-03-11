package com.cedexis.mobileradarlib;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DeviceStateChecker {
    
    public static final String TAG = "DeviceStateChecker";
    
    public static boolean okToMeasure(Application app) {
        ConnectivityManager connectivity = (ConnectivityManager)app
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeConnection = connectivity
                .getActiveNetworkInfo();
        
        // Skip if there's no active connection
        if (null == activeConnection) {
            return false;
        }
        
        if (!activeConnection.isConnected()) {
            return false;
        }
        
        // Skip if roaming
        if (activeConnection.isRoaming()) {
            return false;
        }
        
        // Check the battery state
        IProvidesBatteryStatus batteryStatusProvider = (IProvidesBatteryStatus)app;
        float batteryLevel = batteryStatusProvider.getBatteryLevel();
        boolean isBatteryCharging = batteryStatusProvider.isBatteryCharging();
        
        // Skip if below a certain battery level when charging
        if (isBatteryCharging) {
            if (batteryLevel < batteryStatusProvider.getMinimumBatteryLevelWhenCharging()) {
                return false;
            }
        }
        // Skip if below a certain battery level when NOT charging
        else if (batteryLevel < batteryStatusProvider.getMinimumBatteryLevel()) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isOnWifi(Application app) {
        ConnectivityManager connectivity = (ConnectivityManager)app
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeConnection = connectivity.getActiveNetworkInfo();
        if (null == activeConnection) {
            return false;
        }
        return ConnectivityManager.TYPE_WIFI == activeConnection.getType();
    }
}
