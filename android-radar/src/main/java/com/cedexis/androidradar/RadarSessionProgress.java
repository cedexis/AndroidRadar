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

import android.util.Pair;

import java.util.List;

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
