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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.cedexis.mobileradarlib.IPostReportHandler;
import com.cedexis.mobileradarlib.ReportData;
import com.cedexis.mobileradarlib.rum.RadarRUMSession;

public class MobileRadarDemo extends Activity {
    
    private final static String TAG = "MobileRadarDemo";
    private final static String OUTER_SLICE_NAME = "Main Page Outer";
    private final static String INNER_SLICE_NAME = "Main Page Inner";
    private final static String SAVE_FILE_NAME = "mobileradardemo.txt";
    
    private boolean _resumed = false;
    IPostReportHandler _postReportHandler;
    
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
                        else {
                            Log.w(TAG, "Couldn't get the text view");
                        }
                    }
                });
            }
        };
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // Begin a new Radar RUM slice
        RadarRUMSession rumSession = ((MobileRadarDemoApplication)this.getApplication()).getRadarRUM();
        rumSession.reportSliceStart(MobileRadarDemo.INNER_SLICE_NAME);
        
        // Report when the method fired
        rumSession.reportEvent(
            "onStart",
            RadarRUMTags.MainPage.getValue() |
            RadarRUMTags.Miscellaneous.getValue());
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        // End Radar RUM slice
        RadarRUMSession rumSession = ((MobileRadarDemoApplication)this.getApplication()).getRadarRUM();
        rumSession.reportSliceEnd(MobileRadarDemo.INNER_SLICE_NAME);
        
        // Report when the method fired
        rumSession.reportEvent(
            "onStop",
            RadarRUMTags.MainPage.getValue() |
            RadarRUMTags.Miscellaneous.getValue());
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        
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
        60000); // repeat every minute or so
        
        RadarRUMSession session =
            ((MobileRadarDemoApplication)this.getApplication())
            .getRadarRUM();
        
        if (1 < session.getCurrentReportId()) {
            // This isn't a new session
            this.deserializeText();
        }
        
        // Register a handler to fire whenever a RUM report is sent
        session.addPostReportHandler(this._postReportHandler);
        
        // Start Create/Destroy slice
        session.reportSliceStart(MobileRadarDemo.OUTER_SLICE_NAME);
        
        // Report when the method fired
        session.reportEvent(
            "onCreate",
            RadarRUMTags.MainPage.getValue() |
            RadarRUMTags.Miscellaneous.getValue());
        
        // Attach metadata to the RUM session
        // In this example, we attach a fictitious username 
        session.reportSetProperty("user name", "some user name");
        
        // Register a handler to fire whenever a remote probing report is sent
        ((MobileRadarDemoApplication)this.getApplication())
            .getRadarHttp()
            .addPostReportHandler(this._postReportHandler);
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "Inside onDestroy");
        
        // End outer slice
        ((MobileRadarDemoApplication)this.getApplication())
            .getRadarRUM().reportSliceEnd(MobileRadarDemo.OUTER_SLICE_NAME);
        
        // Remove callbacks
        ((MobileRadarDemoApplication)this.getApplication())
            .getRadarRUM().removePostReportHandler(this._postReportHandler);
        
        ((MobileRadarDemoApplication)this.getApplication())
            .getRadarHttp()
            .removePostReportHandler(this._postReportHandler);
        
        // Serialize the textview text to file
        this.serializeText();
        
        super.onDestroy();
    }
    
    private void deserializeText() {
        if (Arrays.asList(this.fileList()).contains(SAVE_FILE_NAME)) {
            TextView view = (TextView)this.findViewById(R.id.messages_view);
            if (null != view) {
                try {
                    FileInputStream f = this.openFileInput(SAVE_FILE_NAME);
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
                this.deleteFile(SAVE_FILE_NAME);
            }
        }
    }
    
    private void serializeText() {
        TextView view = (TextView)this.findViewById(R.id.messages_view);
        if (null != view) {
            try {
                FileOutputStream f = this.openFileOutput(SAVE_FILE_NAME, MODE_PRIVATE);
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
    protected void onPause() {
        super.onPause();
        
        // Mark the activity as NOT resumed
        this._resumed = false;
        
        // Report when the method fired
        ((MobileRadarDemoApplication)this.getApplication())
            .getRadarRUM().reportEvent(
                "onPause",
                RadarRUMTags.MainPage.getValue() |
                RadarRUMTags.Miscellaneous.getValue());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Mark the activity as resumed
        this._resumed = true;
        
        // Report when the method fired
        RadarRUMSession session = ((MobileRadarDemoApplication)this.getApplication())
                .getRadarRUM(); 
        int reportId = session.reportEvent(
                "onResume",
                RadarRUMTags.MainPage.getValue() |
                RadarRUMTags.Miscellaneous.getValue());
        
        // Attach metadata to the report
        String[] colors = new String[] { "red", "blue", "green", "orange", "purple" };
        int randomColorIndex = new Random().nextInt(colors.length);
        session.reportSetProperty(reportId, "color", colors[randomColorIndex]);
    }
}
