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

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

class CedexisRadarWebClient extends WebViewClient {

    // 2 is the client profile code for AndroidRadar
    final int CLIENT_PROFILE = 2;
    // Client profile version conveys the version of the WebView wrapper code.
    final int CLIENT_PROFILE_VERSION = 3;
    final String TAG = CedexisRadarWebClient.class.getSimpleName();
    private final int zoneId;
    private final int customerId;

    CedexisRadarWebClient(int zoneId, int customerId) {
        this.zoneId = zoneId;
        this.customerId = customerId;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return !Uri.parse(url).getHost().equals(RadarWebView.RADAR_HOST);
    }

    @TargetApi(24)
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String startCommand = String.format(
                Locale.getDefault(),
                "cedexis.start(%d,%d,%d,%d);",
                zoneId,
                customerId,
                CLIENT_PROFILE,
                CLIENT_PROFILE_VERSION);
        Log.d(TAG, String.format("Detected version: %d", Build.VERSION.SDK_INT));
        try {
            // `evaluateJavascript` is safe. Gated on API level 24+ upstream.
            Log.d(TAG, String.format("Using evaluateJavascript; start command=%s", startCommand));
            view.evaluateJavascript("console.log('sending cedexis commands');", null);
            view.evaluateJavascript(startCommand, null);
        } catch (java.lang.IllegalStateException e) {
            // Just swallow for now. We believe this only emerges from a custom ROM bug, so it's
            // probably okay to surrender this data.
        }
    }
}
