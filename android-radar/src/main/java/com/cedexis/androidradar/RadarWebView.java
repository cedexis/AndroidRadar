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

import android.app.Activity;
import android.content.Context;
import android.os.Build;
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
        final ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        createWebView(activity, viewGroup);
    }

    RadarWebView(ViewGroup viewGroup) {
        createWebView(viewGroup.getContext(), viewGroup);
    }

    RadarWebView(final WebView webView) {
        this.webView = webView;
        this.webView.setVisibility(View.GONE);
    }

    private void createWebView(Context context, ViewGroup viewGroup) {
        webView = new WebView(context);
        webView.setTag(TAG);
        webView.setVisibility(View.GONE);
        viewGroup.addView(webView);
    }

    @Override
    public void start(final int zoneId, final int customerId) {
        this.start(zoneId, customerId, RadarScheme.HTTP);
    }

    @Override
    public void start(final int zoneId, final int customerId, final RadarScheme scheme) {
        if (Build.VERSION.SDK_INT < 24) {
            // Resource Timing is probably not available, so let's skip.
            // Also skipping HTTPS on versions of Android where TLS might be a problem (<APIv19)
            Log.d(TAG, String.format("Skipping on API version %d", Build.VERSION.SDK_INT));
        } else if (webView == null) {
            throw new IllegalAccessError("Call Radar#init method before sending Radar events");
        } else {
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
        }
    }

    private CedexisRadarWebClient createOrGetWebClient(int zoneId, int customerId) {
        if (webViewClient == null) {
            webViewClient = new CedexisRadarWebClient(zoneId, customerId);
        }
        return webViewClient;
    }
}
