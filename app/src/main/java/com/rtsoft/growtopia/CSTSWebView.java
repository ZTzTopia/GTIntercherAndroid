package com.rtsoft.growtopia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.WebView;

class CSTSWebView extends WebView {
    private CSTSWebViewClient _webClient = null;

    public CSTSWebView(Context context) {
        super(context);
        setupWebView();
    }

    public CSTSWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupWebView();
    }

    public CSTSWebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setupWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        if (_webClient == null) {
            _webClient = new CSTSWebViewClient();
            setWebViewClient(_webClient);

            getSettings().setJavaScriptEnabled(true);
            getSettings().setDomStorageEnabled(true);
            clearCache(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
            }
        }
    }

    public CSTSWebViewClient getWebClient() {
        return _webClient;
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public boolean shouldGoBackToFirstURL() {
        if (getUrl().contains("facebook")) {
            return true;
        }

        return _webClient.isInCreateAccount();
    }
}
