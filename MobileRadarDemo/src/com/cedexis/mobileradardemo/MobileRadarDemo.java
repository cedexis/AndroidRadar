package com.cedexis.mobileradardemo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.cedexis.mobileradarlib.IPostReportHandler;
import com.cedexis.mobileradarlib.Radar;
import com.cedexis.mobileradarlib.ReportData;

public class MobileRadarDemo extends Activity {
    
    private final static String OUTER_SLICE_NAME = "Main Page Outer";
    private final static String INNER_SLICE_NAME = "Main Page Inner";
    
    private IPostReportHandler _postReportHandler;
    private Timer _radarTimer = new Timer();
    private boolean _resumed = false;
    
    public MobileRadarDemo() {
        this._postReportHandler = new IPostReportHandler() {
            @Override
            public void execute(final ReportData data) {
                MobileRadarDemo.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update the text view
                        TextView view = (TextView)MobileRadarDemo.this
                                .findViewById(R.id.messages_view);
                        if (null != view) {
                            StringBuilder temp = new StringBuilder(view.getText());
                            temp.insert(0, "\n");
                            temp.insert(0, data);
                            view.setText(temp.toString());
                        }
                    }
                });
            }
        };
    }
    
    private Radar getRadar() {
        MobileRadarDemoApplication app = (MobileRadarDemoApplication)this.getApplication();
        return app.getRadar();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileRadarDemoApplication app = (MobileRadarDemoApplication)this.getApplication();
        Radar radar = this.getRadar();
        
        // Register a handler to fire whenever a RUM report is sent
        radar.addPostReportHandler(this._postReportHandler);
        
        // This is the onCreate method of application's main activity. If
        // the last RUM report id is greater than zero, then the user is
        // "returning" to the app and we need to grab the text that we
        // (probably) saved when the left earlier.
        if (1 > radar.getLastRUMReportId()) {
            // Report when the app started
            radar.reportEvent("App Start", 0, app.getOnCreateTimestamp());
            
            // Attach metadata to the RUM session
            radar.reportSetProperty("device manufacturer", android.os.Build.MANUFACTURER);
            radar.reportSetProperty("device model", android.os.Build.MODEL);
        }
        else {
            this.deserializeText();
        }
        
        // Start Create/Destroy slice
        radar.reportSliceStart(MobileRadarDemo.OUTER_SLICE_NAME);
        
        // Report when the method fired
        radar.reportEvent("onCreate",
                RadarRUMTags.MainPage.getValue() |
                RadarRUMTags.Miscellaneous.getValue());
        
        // Start the Radar timer
        this._radarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (MobileRadarDemo.this._resumed) {
                    MobileRadarDemoApplication app =
                            (MobileRadarDemoApplication)MobileRadarDemo
                            .this.getApplication();
                    app.getRadar().queueHttpSession();
                }
            }
        },
        2000, // start in a couple seconds
        60000); // repeat every minute or so
    }
    
    @Override
    protected void onDestroy() {
        Radar radar = this.getRadar();
        
        // End outer slice
        radar.reportSliceEnd(MobileRadarDemo.OUTER_SLICE_NAME);
        
        // Serialize the textview text to file
        this.serializeText();
        
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Begin a new Radar RUM slice
        Radar radar = this.getRadar();
        
        // Make sure the current Radar object has our post-report handler
        // registered. Adding the same handler multiple times is okay.
        radar.addPostReportHandler(this._postReportHandler);
        
        // Start inner slice
        radar.reportSliceStart(MobileRadarDemo.INNER_SLICE_NAME);
        
        // Report when the method fired
        int reportId = radar.reportEvent("onResume",
                RadarRUMTags.MainPage.getValue() |
                RadarRUMTags.Miscellaneous.getValue());
        
        // Attach metadata to the report
        String[] colors = new String[] { "red", "blue", "green", "orange", "purple" };
        int randomColorIndex = new Random().nextInt(colors.length);
        radar.reportSetProperty("color", colors[randomColorIndex], reportId);
        
        // Restart Radar remote probing
        this._resumed = true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Stop Radar remote probing
        this._resumed = false;
        
        // Report when the method fired
        Radar radar = this.getRadar();
        radar.reportEvent("onPause",
                RadarRUMTags.MainPage.getValue() |
                RadarRUMTags.Miscellaneous.getValue());
        
        // End Radar RUM slice
        radar.reportSliceEnd(MobileRadarDemo.INNER_SLICE_NAME);
    }
    
    private void deserializeText() {
        if (Arrays.asList(this.fileList()).contains(this.getString(R.string.save_file_name))) {
            TextView view = (TextView)this.findViewById(R.id.messages_view);
            if (null != view) {
                try {
                    FileInputStream f = this.openFileInput(this.getString(R.string.save_file_name));
                    try {
                        ObjectInputStream s = new ObjectInputStream(f);
                        try {
                            view.setText(s.readObject().toString());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        finally {
                            s.close();
                        }
                    } catch (StreamCorruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    // Not worried about this
                }
                
                // Get rid of the file now.  We'll create it again if needed.
                this.deleteFile(this.getString(R.string.save_file_name));
            }
        }
    }
    
    private void serializeText() {
        TextView view = (TextView)this.findViewById(R.id.messages_view);
        if (null != view) {
            try {
                FileOutputStream f = this.openFileOutput(
                        this.getString(R.string.save_file_name),
                        MODE_PRIVATE);
                ObjectOutputStream o;
                try {
                    o = new ObjectOutputStream(f);
                    try {
                        o.writeObject(view.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        o.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                this.startActivity(new Intent(this, DemoPreferenceActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
