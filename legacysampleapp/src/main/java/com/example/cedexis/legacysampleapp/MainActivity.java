package com.example.cedexis.legacysampleapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cedexis.androidradar.RadarService;
import com.cedexis.androidradar.RadarSessionProperties;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        Intent radarService = new Intent(this, RadarService.class);
//        radarService.putExtra( RadarService.EXTRA_SESSION_PROPERTIES, new RadarSessionProperties(1, 10660));
        startService(radarService);
        super.onResume();
    }
}
