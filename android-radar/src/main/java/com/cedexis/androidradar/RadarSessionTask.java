package com.cedexis.androidradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Use this class to execute a RadarSession in a separate thread.
 *
 * @deprecated use {@link RadarService} instead. See README.
 */
@Deprecated()
public class RadarSessionTask extends AsyncTask<RadarSessionProperties, RadarSessionProgress, Void> {

    public static final String TAG = RadarSessionTask.class.getSimpleName();
    //private String _providersDomain = "radar.test.wildlemur.com";
    private RadarSessionTaskCaller _caller = null;
    private Context _context;

    /**
     * The constructor for the RadarSessionTask class.
     *
     * @param context The Context in which the task runs.  Generally the Activity where the task
     *                 is created.
     */
    public RadarSessionTask(Context context) {
        this._context = context;
    }

    public interface RadarSessionTaskCaller {
        public void onProgress(RadarSessionProgress sessionProgress);
    }

    public void set_caller(RadarSessionTaskCaller _caller) {
        this._caller = _caller;
    }

    @Override
    protected Void doInBackground(RadarSessionProperties... params) {
        int countOfParams = params.length;
        for (int i = 0; i < countOfParams; i++) {
            RadarSession session = RadarSession.initializeRadarSession(params[i], _context);
            if (null != session) {
                //Log.d(TAG, session.toString());
                session.run();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(RadarSessionProgress... values) {
        super.onProgressUpdate(values);
        if (null != _caller) {
            int countOfValues = values.length;
            for (int i = 0; i < countOfValues; i++) {
                _caller.onProgress(values[i]);
            }
        }
    }
}
