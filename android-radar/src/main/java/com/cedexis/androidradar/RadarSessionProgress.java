package com.cedexis.androidradar;

import android.util.Pair;

import java.util.List;

/**
 * Created by jacob on 10/06/15.
 */
public class RadarSessionProgress {

    private String _step;
    private List<Pair<String, String>> _progressData;

    public RadarSessionProgress(String step, List<Pair<String, String>> progressData) {
        _step = step;
        _progressData = progressData;
    }

    public RadarSessionProgress(String step) {
        this(step, null);
    }

    @Override
    public String toString() {
        return "RadarSessionProgress{" +
                "_step='" + _step + '\'' +
                ", _progressData=" + _progressData +
                '}';
    }

    public String get_step() {
        return _step;
    }

    public List<Pair<String, String>> get_progressData() {
        return _progressData;
    }
}
