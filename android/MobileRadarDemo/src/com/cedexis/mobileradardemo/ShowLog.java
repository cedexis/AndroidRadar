package com.cedexis.mobileradardemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ShowLog extends Activity {
    
    private static final String TAG = "ShowLog";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.show_log);
        
        ((MobileRadarDemoApplication)this.getApplication())
            .getRadarRUM().reportEvent(
                "onCreate",
                RadarRUMTags.ShowLogPage.getValue() |
                RadarRUMTags.Miscellaneous.getValue());
        
        this.loadLogText();
        
        ((MobileRadarDemoApplication)this.getApplication())
            .getRadarRUM().reportEvent(
                "text loaded", // spaces are okay
                RadarRUMTags.ShowLogPage.getValue() |
                RadarRUMTags.Miscellaneous.getValue());
    }

    private void loadLogText() {
        TextView textView = (TextView)this.findViewById(R.id.log_text);
        boolean first = true;
        Pattern pattern = Pattern.compile("[D]/(RadarRUMSession|"
                + "MobileRadarDemo|InitHandler|ReportHandler)");
        try {
            Process proc = Runtime.getRuntime().exec("logcat -d -v time");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while (null != (line = reader.readLine())) {
                // Only include lines we're interested in
                if (pattern.matcher(line).find()) {
                    if (!first) {
                        log.append("\n");
                    }
                    first = false;
                    log.append(line);
                }
            }
            textView.setText(log.toString());
        }
        catch (IOException e) {
            Log.w(TAG, "Caught IOException: " + e.toString());
            e.printStackTrace();
        }
    }
    
}
