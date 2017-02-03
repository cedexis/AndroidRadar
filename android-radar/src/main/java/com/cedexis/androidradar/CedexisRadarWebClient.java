package com.cedexis.androidradar;

import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class CedexisRadarWebClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return !Uri.parse(url).getHost().equals(RadarWebView.RADAR_HOST);
    }
}
