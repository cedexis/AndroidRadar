package com.cedexis.simpleradardemo;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.cedexis.androidradar.Cedexis;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button radarButton;
    private int requestorZoneId = 1;
    private int requestorCustomerId = 22746;
    private Cedexis cedexis;

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
        cedexis = Cedexis.init(this);

        radarButton = (Button) findViewById(R.id.radar_button);
        radarButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // New SDK Usage
        cedexis.start(requestorZoneId, requestorCustomerId);


        // Old SDK Usage
//        RadarImpactProperties impactProperties = new RadarImpactProperties(UUID.randomUUID().toString());
//        impactProperties.setPerformanceTestUrl("http://www.cedexis.com/images/homepage/portal-bg-1.jpg");
//        impactProperties.setCategory("cart");
//        impactProperties.addKpi("value", 12.34);
//        impactProperties.addKpi("first kpi", 1);
//        impactProperties.addKpi("second kpi", "abc");
//        impactProperties.addKpi("third kpi", true);
//
//        RadarSessionProperties radarSessionProperties =
//                new RadarSessionProperties(requestorZoneId, requestorCustomerId,
//                        impactProperties, 1, 0.5);
//
//        Intent radarService = new Intent(this, RadarService.class);
//        radarService.putExtra(RadarService.EXTRA_SESSION_PROPERTIES, radarSessionProperties);
//        startService(radarService);

    }
}