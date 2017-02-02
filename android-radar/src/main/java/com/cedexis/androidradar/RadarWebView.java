package com.cedexis.androidradar;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Locale;

/**
 * A WebView {@link Radar} implementation, send radar events, executed on UI thread
 * as it is a WebView.
 */
@UiThread
final class RadarWebView implements Radar {

    private static final String WEBVIEW_TAG = "CEDEXIS_WEBVIEW";
    static final String RADAR_HOST = "radar.cedexis.com";

    private final int zoneId;
    private final int customerId;

    private CedexisRadarWebClient webViewClient;

    RadarWebView(int zoneId, int customerId) {
        this.zoneId = zoneId;
        this.customerId = customerId;
    }

    @Override
    public void sendRadarEvent(final Activity activity) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        WebView webView = createOrFindWebView(activity, viewGroup);
        configureWebView(webView, createOrGetWebClient());
        webView.loadUrl(getRadarUrl());
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
     * @param webView
     * @param client
     */
    private void configureWebView(WebView webView, CedexisRadarWebClient client) {
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
        webView.setWebViewClient(client);
    }

    @NonNull
    private WebView createOrFindWebView(Activity activity, ViewGroup viewGroup) {
        WebView webView = (WebView) viewGroup.findViewWithTag(WEBVIEW_TAG);
        if (webView == null) {
            webView = new WebView(activity);
            webView.setTag(WEBVIEW_TAG);
            viewGroup.addView(webView);
        }
        return webView;
    }

}
