package com.cedexis.mobileradarlib;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DeviceStateChecker {
    
    public static final String TAG = "DeviceStateChecker";
    
    public static boolean okToMeasure(Application app) {
        ConnectivityManager connectivity = (ConnectivityManager)app
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeConnection = connectivity
                .getActiveNetworkInfo();
        
        // Skip if there's no active connection
        if (null == activeConnection) {
            Log.d(TAG, "No active connection...skipping");
            return false;
        }
        
        if (!activeConnection.isConnected()) {
            Log.d(TAG, "Not connected...skipping");
            return false;
        }
        
        // Skip if roaming
        if (activeConnection.isRoaming()) {
            Log.d(TAG, "Roaming...skipping");
            return false;
        }
        
        // Check the battery state
        IProvidesBatteryStatus batteryStatusProvider = (IProvidesBatteryStatus)app;
        int batteryStatus = batteryStatusProvider.getBatteryStatus();
        Log.d(TAG, "Battery info: " + batteryStatus);
        float batteryLevel = batteryStatusProvider.getBatteryLevel();
        Log.d(TAG, "Battery level: " + batteryLevel);
        boolean isBatteryCharging = batteryStatusProvider.isBatteryCharging();
        Log.d(TAG, "Battery charging: " + isBatteryCharging);
        
        // Skip if below a certain battery level when charging
        if (isBatteryCharging) {
            if (batteryLevel < batteryStatusProvider.getMinimumBatteryLevelWhenCharging()) {
                Log.d(TAG, "Battery below minimum charging level...skipping");
                return false;
            }
        }
        // Skip if below a certain battery level when NOT charging
        else if (batteryLevel < batteryStatusProvider.getMinimumBatteryLevel()) {
            Log.d(TAG, "Battery below minimum non-charging level...skipping");
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
