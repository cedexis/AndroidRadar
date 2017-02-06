package com.cedexis.simpleradardemo;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.cedexis.androidradar.Cedexis;
import com.cedexis.androidradar.Radar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button radarButton;
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
        radar = Cedexis.radar(this);

        radarButton = (Button) findViewById(R.id.radar_button);
        radarButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        radar.start(_requestorZoneId, _requestorCustomerId);
    }
}