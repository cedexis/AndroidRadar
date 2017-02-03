package com.cedexis.simpleradardemo;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.cedexis.androidradar.Cedexis;
import com.cedexis.androidradar.Radar;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String TAG_PROGRESS = "MainActivity.progress";

    Button radarButton;
    ProgressBar _radarSessionProgressBar;
    String _impactSessionId;
    JSONObject _providerNames = null;
    private int _requestorZoneId = 1;
    private int _requestorCustomerId = 22746;
    private Radar radar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Strict Mode for the sample app in order to detect issues on time.
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        setContentView(R.layout.activity_main);
        radar = Cedexis.initRadar(_requestorZoneId, _requestorCustomerId);
        radar.init(this);

        radarButton = (Button) findViewById(R.id.radar_button);
        radarButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        radar.sendRadarEvent();
//        _radarService = new Intent(this, RadarService.class);
//        _radarService.putExtra(RadarService.EXTRA_SESSION_PROPERTIES, radarSessionProperties);
//        startService(_radarService);

    }
}