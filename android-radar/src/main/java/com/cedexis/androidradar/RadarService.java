package com.cedexis.androidradar;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for executing a RadarSession in
 * a service on a separate handler thread.
 */
public class RadarService extends IntentService {
    public static final String EXTRA_SESSION_PROPERTIES = "com.cedexis.androidradar.extra.SESSION_PROPERTIES";

    public RadarService() {
        super("RadarService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final RadarSessionProperties sessionProperties = intent.getParcelableExtra(EXTRA_SESSION_PROPERTIES);
            RadarSession session = RadarSession.initializeRadarSession(sessionProperties, this);
            if (null != session) {
                //Log.d(TAG, session.toString());
                session.run();
            }
        }
    }
}
