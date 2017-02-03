package com.cedexis.androidradar;

import android.app.Activity;

public interface Radar {

    void sendRadarEvent();

    void init(final Activity activity);

    void stop();

}
