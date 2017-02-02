package com.cedexis.androidradar;

import android.app.IntentService;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for executing a RadarSession in
 * a service on a separate handler thread.
 *
 * Use {@link Cedexis#initRadar(int zoneId, int customerId)} for future updates.
 */
@Deprecated
public class RadarService extends IntentService {
    public static final String EXTRA_SESSION_PROPERTIES = "com.cedexis.androidradar.extra.SESSION_PROPERTIES";
    private RadarSession _session;

    public RadarService() {
        super("RadarService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final RadarSessionProperties sessionProperties = intent.getParcelableExtra(EXTRA_SESSION_PROPERTIES);
            _session = RadarSession.initializeRadarSession(sessionProperties, this);
            if (null != _session) {
                //Log.d(TAG, session.toString());
                _session.run();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (_session != null) {
            _session.stop();
        }
        super.onDestroy();
    }
}
