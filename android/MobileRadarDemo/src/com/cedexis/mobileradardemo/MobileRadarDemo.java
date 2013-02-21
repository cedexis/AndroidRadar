package com.cedexis.mobileradardemo;
    
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MobileRadarDemo extends Activity {
    
    private final static String TAG = "MobileRadarDemo";
    
    private boolean _resumed = false;
    
    private boolean isResumed() {
        return this._resumed;
    }
    
    private Timer _radarTimer;
    
    private Timer getRadarTimer() {
        if (null == this._radarTimer) {
            this._radarTimer = new Timer();
        }
        return this._radarTimer;
    }
    
    public MobileRadarDemo() {
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Mark the activity as resumed
        this._resumed = true;
        
        // Report when the method fired
        ((MobileRadarDemoApplication)this.getApplication()).getRadarRUM().reportEvent("onResume");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // Report when the method fired
        ((MobileRadarDemoApplication)this.getApplication()).getRadarRUM()
            .reportEvent("onStart");
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        
        // Schedule periodic Radar HTTP sessions.  We do it here because
        // onCreate is only called once for the life of the activity.
        this.getRadarTimer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Only schedule a Radar HTTP session if the activity is in the
                // Resumed state.
                if (MobileRadarDemo.this.isResumed()) {
                    ((MobileRadarDemoApplication)MobileRadarDemo
                        .this.getApplication())
                            .getRadarHttp()
                                .queueSession();
                }
            }
            
        },
        2000, // start in 2 seconds
        15000); // repeat every several seconds
        
        // Report when the method fired
        ((MobileRadarDemoApplication)this.getApplication()).getRadarRUM()
            .reportEvent("onCreate");
        
        // Attach metadata to the RUM session
        // In this example, we attach a fictitious username 
        ((MobileRadarDemoApplication)this.getApplication()).getRadarRUM()
            .reportSetProperty("username", "someusername");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Mark the activity as NOT resumed
        this._resumed = false;
        
        // Report when the method fired
        ((MobileRadarDemoApplication)this.getApplication()).getRadarRUM()
            .reportEvent("onPause");
    }
    
    public void showLog(MenuItem item) {
        Log.d(TAG, "showLog clicked: " + item);
        Intent intent = new Intent(this, ShowLog.class);
        this.startActivity(intent);
    }
}
