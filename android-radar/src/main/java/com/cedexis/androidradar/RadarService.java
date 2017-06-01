// Copyright 2016 Cedexis
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

package com.cedexis.androidradar;

import android.app.IntentService;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for executing a RadarSession in
 * a service on a separate handler thread.
 *
 * Use {@link Cedexis} for future updates.
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
            if (sessionProperties == null) {
                throw new  AssertionError("Please add Radar session properties to intent before starting service");
            }
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
