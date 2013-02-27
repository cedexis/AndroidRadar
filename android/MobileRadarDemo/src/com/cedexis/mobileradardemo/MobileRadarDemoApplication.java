package com.cedexis.mobileradardemo;

import java.util.Date;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.cedexis.mobileradarlib.IProvidesBatteryStatus;
import com.cedexis.mobileradarlib.http.RadarHttpSessionManager;
import com.cedexis.mobileradarlib.rum.RadarRUMSession;

public class MobileRadarDemoApplication extends Application implements IProvidesBatteryStatus {
    
    private RadarRUMSession _radarRUM;
    private RadarHttpSessionManager _radarHttp;
    private BroadcastReceiver _batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1); 
            MobileRadarDemoApplication.this._batteryStatus = status;
            MobileRadarDemoApplication.this._isCharging =
                    ((status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                     (status == BatteryManager.BATTERY_STATUS_FULL));
            
            // Calculate the battery level
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            MobileRadarDemoApplication.this._batteryLevel = level / (float)scale;
        }
    };
    
    public RadarRUMSession getRadarRUM() {
        return this._radarRUM;
    }
    
    public RadarHttpSessionManager getRadarHttp() {
        if (null == this._radarHttp) {
            this._radarHttp = new RadarHttpSessionManager(
                    this, // application-level context
                    1, // zone id
                    10660); // customer id
        }
        return this._radarHttp;
    }
    
    private int _batteryStatus;
    public int getBatteryStatus() {
        return this._batteryStatus;
    }
    
    private boolean _isCharging;
    public boolean isBatteryCharging() {
        return this._isCharging;
    }
    
    private float _batteryLevel;
    public float getBatteryLevel() {
        return this._batteryLevel;
    }
    
    public float getMinimumBatteryLevel() {
        return (float)0.4;
    }
    
    public float getMinimumBatteryLevelWhenCharging() {
        return (float)0.2;
    }
    
    @Override
    public void onCreate() {
        // This is the earliest opportunity to run code, before any activity
        // has been created. All we need to do here is create the Radar RUM
        // session object. Nothing is being reported here. The main reason to
        // do this here is to get the timestamp, but you could just save the
        // timestamp and create the object later if preferred.
        this._radarRUM = new RadarRUMSession(
                this, // application-level context
                new Date().getTime(), // app start timestamp
                1, // zone id
                10660); // customer id
        
        // Register a broadcast receiver to detect changes in battery level
        this.registerReceiver(this._batteryInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        
        // Make sure to call the base class version
        super.onCreate();
    }
    
}
