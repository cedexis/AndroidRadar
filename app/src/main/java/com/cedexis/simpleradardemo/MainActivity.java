package com.cedexis.simpleradardemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.cedexis.androidradar.RadarImpactProperties;
import com.cedexis.androidradar.RadarService;
import com.cedexis.androidradar.RadarSessionProgress;
import com.cedexis.androidradar.RadarSessionProperties;
import com.cedexis.androidradar.RadarSessionTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        RadarSessionTask.RadarSessionTaskCaller,
        DownloadProviderNamesTask.Caller {

    private static final String TAG = "MainActivity";
    private static final String TAG_PROGRESS = "MainActivity.progress";

    Button radarButton;
    ListView radarSessionProgressListView;
    ProgressBar _radarSessionProgressBar;
    String _impactSessionId;
    JSONObject _providerNames = null;
    private int _requestorZoneId = 1;
    private int _requestorCustomerId = 18980;
    private String _impactPerformanceTestUrl = "http://www.cedexis.com/images/homepage/portal-bg-1.jpg";
    private Intent _radarService;

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

        radarButton = (Button) findViewById(R.id.radar_button);
        radarButton.setOnClickListener(this);

        AppProviderProgressAdapter adapter = new AppProviderProgressAdapter(this, R.layout.radar_session_progress_view);

        radarSessionProgressListView = (ListView) findViewById(R.id.radar_session_progress_listview);
        radarSessionProgressListView.setAdapter(adapter);

        _radarSessionProgressBar = (ProgressBar) findViewById(R.id.radar_session_progress_bar);

        new DownloadProviderNamesTask(this).execute(Pair.create(_requestorZoneId, _requestorCustomerId));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        _impactSessionId = UUID.randomUUID().toString();
        Log.d(TAG, String.format("Impact session id: %s", _impactSessionId));
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Button pressed");
        //radarButton.setEnabled(false);
        //AppProviderProgressAdapter adapter = (AppProviderProgressAdapter) radarSessionProgressListView.getAdapter();
        //adapter.clear();
        //_radarSessionProgressBar.setProgress(0);

        RadarImpactProperties impactProperties = new RadarImpactProperties(_impactSessionId);
        impactProperties.setPerformanceTestUrl(_impactPerformanceTestUrl);
        impactProperties.setCategory("cart");
        impactProperties.addKpi("value", 12.34);
        impactProperties.addKpi("first kpi", 1);
        impactProperties.addKpi("second kpi", "abc");
        impactProperties.addKpi("third kpi", true);

        RadarSessionProperties radarSessionProperties = new RadarSessionProperties(
                _requestorZoneId,
                _requestorCustomerId,
                impactProperties
                , 1
                , 0.5
        );

        _radarService = new Intent(this, RadarService.class);
        _radarService.putExtra(RadarService.EXTRA_SESSION_PROPERTIES, radarSessionProperties);
        startService(_radarService);


//        RadarSessionTask task = new RadarSessionTask(this);
//        task.set_caller(this);
//        task.execute(radarSessionProperties);
    }

    @Override
    public void onProgress(RadarSessionProgress sessionProgress) {
        Log.d(TAG, sessionProgress.toString());
        TupleSearcher<String, String> search = new TupleSearcher<String, String>(sessionProgress.get_progressData());
        switch (sessionProgress.get_step()) {
            case "sessionComplete":
                radarButton.setEnabled(true);
                break;
            case "gotProviders":
                Pair<String, String> tuple = search.search("providerCount");
                if (null != tuple) {
                    _radarSessionProgressBar.setMax(Integer.parseInt(tuple.second));
                }
                break;
            case "finishedProvider":
                Log.d(TAG_PROGRESS, sessionProgress.toString());
                updateListProgress(sessionProgress);
                _radarSessionProgressBar.incrementProgressBy(1);
                break;
        }
    }

    private void updateListProgress(RadarSessionProgress sessionProgress) {
        AppProviderProgressAdapter adapter = (AppProviderProgressAdapter) radarSessionProgressListView.getAdapter();
        TupleSearcher<String, String> search = new TupleSearcher<String, String>(sessionProgress.get_progressData());
        String providerId = search.search("providerId").second;
        Log.d(TAG_PROGRESS, String.format("providerId: %s", providerId));
        try {
            AppProvider provider = new AppProvider(providerId, _providerNames.getString(providerId));
            Pair<String, String> temp = search.search("measurement.connect");
            if (null != temp) {
                provider.set_connectTimeText(temp.second);
            }
            temp = search.search("measurement.rtt");
            if (null != temp) {
                provider.set_responseTimeText(temp.second);
            }
            temp = search.search("measurement.throughput");
            if (null != temp) {
                provider.set_throughputText(temp.second);
            }
            adapter.add(provider);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostExecute(JSONObject providerNames) {
        _providerNames = providerNames;
        radarButton.setEnabled(true);
    }

    public void onStopClicked(View view) {
        Log.d(TAG, "Stop clicked");
        this.stopService(_radarService);
    }
}