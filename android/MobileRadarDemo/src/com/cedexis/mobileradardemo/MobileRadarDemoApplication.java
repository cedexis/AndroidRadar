package com.cedexis.mobileradardemo;

import java.util.Date;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;

import com.cedexis.mobileradarlib.IProvidesBatteryStatus;
import com.cedexis.mobileradarlib.Radar;

public class MobileRadarDemoApplication
    extends Application
    implements IProvidesBatteryStatus {
    
    private Radar _radar;
    private long _onCreateTimestamp;
    private int _batteryStatus;
    private boolean _isCharging;
    private float _batteryLevel;
    
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
    
    @Override
    public void onCreate() {
        // This is the earliest opportunity to run code, before any activity
        // has been created.  Here we simply grab a timestamp, which we'll
        // use to initiate the RUM session later.
        this._onCreateTimestamp = new Date().getTime();
        
        // Register a broadcast receiver to detect changes in battery level
        this.registerReceiver(this._batteryInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        
        // Make sure to call the base class version
        super.onCreate();
    }
    
    private void createRadar() {
        synchronized(this) {
            if (null == this._radar) {
                SharedPreferences settings = PreferenceManager
                        .getDefaultSharedPreferences(this);
                
                int zoneId = Integer.parseInt(settings.getString(
                        "zoneId",
                        this.getString(R.string.default_zone_id)));
                
                int customerId = Integer.parseInt(settings.getString(
                        "customerId",
                        this.getString(R.string.default_customer_id)));
                
                this._radar = Radar.createRadar(
                        this,
                        zoneId,
                        customerId,
                        this.getString(R.string.radar_client_name),
                        this.getString(R.string.radar_client_version),
                        this._onCreateTimestamp);
            }
        }
    }
    
    public Radar getRadar() {
        synchronized(this) {
            if (null == this._radar) {
                this.createRadar();
            }
        }
        return this._radar;
    }
    
    public long getOnCreateTimestamp() {
        return this._onCreateTimestamp;
    }
    
    public int getBatteryStatus() {
        return this._batteryStatus;
    }
    
    
    public boolean isBatteryCharging() {
        return this._isCharging;
    }
    
    public float getBatteryLevel() {
        return this._batteryLevel;
    }
    
    public float getMinimumBatteryLevel() {
        return (float)0.4;
    }
    
    public float getMinimumBatteryLevelWhenCharging() {
        return (float)0.2;
    }
    
    public void restartRadar() {
        synchronized(this) {
            this._onCreateTimestamp = new Date().getTime();
            this._radar = null;
            this.createRadar();
        }
    }
    
}
