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
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Cedexis class to initialize SDK objects
 */
public final class Cedexis {

    private final Radar radar;

    private Cedexis(Activity activity) {
        radar = new RadarWebView(activity);
    }

    private Cedexis(WebView webView) {
        radar = new RadarWebView(webView);
    }

    private Cedexis(ViewGroup viewGroup) {
        radar = new RadarWebView(viewGroup);
    }

    /**
     * Inject a {@link Cedexis} {@link WebView} in the {@link Activity} root view.
     * Using `android.R.id.content` as the root view.
     * @param activity Activity to attach WebView on root
     * @return {@link Cedexis}
     */
    public static Cedexis init(Activity activity) {
        return new Cedexis(activity);
    }

    /**
     * Inject a {@link Cedexis} in the provided {@link WebView} and make visibility View.GONE
     * @param webView WebView to update
     * @return {@link Cedexis}
     */
    public static Cedexis init(WebView webView) {
        return new Cedexis(webView);
    }

    /**
     * Inject a {@link Cedexis} {@link WebView} in any {@link ViewGroup}
     * @param viewGroup viewGroup to attach a WebView
     * @return {@link Cedexis}
     */
    public static Cedexis init(ViewGroup viewGroup) {
        return new Cedexis(viewGroup);
    }

    public void start(int zoneId, int customerId) {
        // HTTP is the historical default, but users are encouraged to call the version that accepts
        // a RadarScheme object and specify RadarScheme.HTTPS in order to generate measurements for
        // HTTPS platforms.
        this.start(zoneId, customerId, RadarScheme.HTTP);
    }

    public void start(int zoneId, int customerId, RadarScheme scheme) {
        radar.start(zoneId, customerId, scheme);
    }
}
