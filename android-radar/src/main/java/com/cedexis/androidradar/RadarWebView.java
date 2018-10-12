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
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Locale;

/**
 * A WebView {@link Radar} implementation, send radar events.
 */
final class RadarWebView implements Radar {

    static final String RADAR_HOST = "radar.cedexis.com";
    private static final String TAG = RadarWebView.class.getSimpleName();
    private WebView webView;

    private CedexisRadarWebClient webViewClient;

    RadarWebView(final Activity activity) {
        if (validateRuntimeConditions()) {
            final ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
            createWebView(activity, viewGroup);
        }
    }

    RadarWebView(ViewGroup viewGroup) {
        if (validateRuntimeConditions()) {
            createWebView(viewGroup.getContext(), viewGroup);
        }
    }

    RadarWebView(final WebView webView) {
        if (validateRuntimeConditions()) {
            this.webView = webView;
            this.webView.setVisibility(View.GONE);
        }
    }

    private void createWebView(Context context, ViewGroup viewGroup) {
        try {
            webView = new WebView(context);
            webView.setTag(TAG);
            webView.setVisibility(View.GONE);
            viewGroup.addView(webView);
        } catch (AndroidRuntimeException e) {
            // Swallow this exception
            Log.d(TAG, "AndroidRadar swallowing AndroidRuntimeException");
            webView = null;
        }
    }

    @Override
    public void start(final int zoneId, final int customerId) {
        this.start(zoneId, customerId, RadarScheme.HTTP);
    }

    @TargetApi(24)
    @Override
    public void start(final int zoneId, final int customerId, final RadarScheme scheme) {
        if (webView != null) {
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
            webView.setWebViewClient(createOrGetWebClient(zoneId, customerId));
            String url = String.format(Locale.getDefault(),
                    "%s://%s/0/0/radar.html", scheme.toString(), RADAR_HOST);
            Log.d(TAG, String.format("Radar URL: %s", url));
            webView.loadUrl(url);
        } else {
            Log.d(TAG, "AndroidRadar is missing a WebView to work with. This may be due to runtime requirements not being met or an exception occurring during WebView creation.");
        }
    }

    private CedexisRadarWebClient createOrGetWebClient(int zoneId, int customerId) {
        if (webViewClient == null) {
            webViewClient = new CedexisRadarWebClient(zoneId, customerId);
        }
        return webViewClient;
    }

    /**
     * @return true if the runtime conditions are okay for AndroidRadar to run
     */
    private boolean validateRuntimeConditions() {
        if (Build.VERSION.SDK_INT < 24) {
            // Various issues arise when we run on API levels lower than 24:
            // * Resource Timing is probably not available
            // * TLS might be a problem (<APIv19)
            Log.d(TAG, String.format("Detected API level less than 24 (%d). AndroidRadar going dormant.", Build.VERSION.SDK_INT));
            return false;
        }
        return true;
    }
}
