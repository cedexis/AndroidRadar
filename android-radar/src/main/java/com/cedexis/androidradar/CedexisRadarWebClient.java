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

import android.net.Uri;
import android.os.Build;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

class CedexisRadarWebClient extends WebViewClient {

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

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String startCommand = String.format(Locale.getDefault(), "cedexis.start(%d,%d);", zoneId, customerId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript("console.log('sending cedexis commands');", null);
            view.evaluateJavascript(startCommand, null);
        } else {
            view.loadUrl("javascript:console.log('sending cedexis commands');");
            view.loadUrl("javascript:" + startCommand);
        }
    }
}
