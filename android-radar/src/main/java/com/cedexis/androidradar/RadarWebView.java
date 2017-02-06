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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Locale;

/**
 * A WebView {@link Radar} implementation, send radar events.
 */
final class RadarWebView implements Radar {

    private static final String WEBVIEW_TAG = "CEDEXIS_WEBVIEW";
    static final String RADAR_HOST = "radar.cedexis.com";

    private WebView webView;

    private CedexisRadarWebClient webViewClient;

    RadarWebView(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        webView = new WebView(activity);
        webView.setTag(WEBVIEW_TAG);
        configureWebView(webView);
        viewGroup.addView(webView);
    }

    @Override
    public void start(int zoneId, int customerId) {
        webView.setWebViewClient(createOrGetWebClient(zoneId, customerId));
        webView.loadUrl(getRadarUrl(zoneId, customerId));
        if (webView == null) {
            throw new IllegalAccessError("Call Radar#init method before sending Radar events");
        }
    }

    @NonNull
    private String getRadarUrl(int zoneId, int customerId) {
        return String.format(Locale.getDefault(),
                "http://%s/%d/%d/radar.html", RADAR_HOST, zoneId, customerId);
    }

    @NonNull
    private CedexisRadarWebClient createOrGetWebClient(int zoneId, int customerId) {
        if (webViewClient == null) {
            webViewClient = new CedexisRadarWebClient(zoneId, customerId);
        }
        return webViewClient;
    }

    /**
     * Give all access to the webview to use JavaScript and allow universal access,
     * only send {@link CedexisRadarWebClient} to only allow redirects on the same
     * Radar host.
     *
     * @param webView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(WebView webView) {
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowFileAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webView.getSettings().setAllowContentAccess(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
    }

}
