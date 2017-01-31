package com.cedexis.androidradar;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class Radar {

    private Radar instance;
    private final int zoneId;
    private final int customerId;

    private Radar(int zoneId, int customerId) {
        instance = this;
        this.zoneId = zoneId;
        this.customerId = customerId;
    }

    public static Radar init(int zoneId, int customerId) {
        Radar radar = new Radar(zoneId, customerId);
        return radar;
    }

    public void sendRadarEvent(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(android.R.id.content);
        WebView webView;
        webView = (WebView) viewGroup.findViewWithTag("CEDEXIS_WEBVIEW");
        if (webView == null) {
            webView = new WebView(activity);
            webView.setTag("CEDEXIS_WEBVIEW");
            viewGroup.addView(webView);
        }
        webView.setVisibility(View.INVISIBLE);
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <script src=\"http://radar.cedexis.com/" + zoneId + "/" + customerId + "/radar.js\"></script>\n" +
                "  </body>\n" +
                "</html>\n";
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadData(html, "text/html; charset=utf-8;", "UTF-8");
    }

}
