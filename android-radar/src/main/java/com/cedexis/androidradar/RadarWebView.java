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

    private final int zoneId;
    private final int customerId;

    private WebView webView;

    private CedexisRadarWebClient webViewClient;

    RadarWebView(int zoneId, int customerId) {
        this.zoneId = zoneId;
        this.customerId = customerId;
    }

    @Override
    public void sendRadarEvent() {
        if (webView == null) {
            throw new IllegalAccessError("Call Radar#init method before sending Radar events");
        }

        String startCommand = String.format(Locale.getDefault(), "cedexis.start(%d,%d);", zoneId, customerId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("console.log('sending cedexis commands');", null);
            webView.evaluateJavascript(startCommand, null);
        } else {
            webView.loadUrl("javascript:console.log('sending cedexis commands');");
            webView.loadUrl("javascript:" + startCommand);
        }
    }

    @Override
    public void init(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        webView = new WebView(activity);
        webView.setTag(WEBVIEW_TAG);
        configureWebView(webView);
        webView.setWebViewClient(createOrGetWebClient());
        webView.loadUrl(getRadarUrl());
        viewGroup.addView(webView);
    }

    @NonNull
    private String getRadarUrl() {
        return String.format(Locale.getDefault(),
                "http://" + RADAR_HOST + "/%d/%d/radar.html", zoneId, customerId);
    }

    @NonNull
    private CedexisRadarWebClient createOrGetWebClient() {
        if (webViewClient == null) {
            webViewClient = new CedexisRadarWebClient();
        }
        return webViewClient;
    }

    /**
     * Give all access to the webview to use JavaScript and allow universal access,
     * only send {@link CedexisRadarWebClient} to only allow redirects on the same
     * Radar host.
     *
     * @param webView
     * @param client
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
