package com.anzu.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AnzuWebView extends WebView {
    public AnzuWebView(Context context) {
        super(context);
        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        Context context = getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true);
        }

        getSettings().setJavaScriptEnabled(true);
        getSettings().setAppCachePath(context.getCacheDir().getPath());
        getSettings().setAppCacheEnabled(true);
        getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        getSettings().setGeolocationEnabled(false);
        getSettings().setSaveFormData(false);

        setBackgroundColor(0);

        setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.println(Log.INFO, "ANZU", consoleMessage.message());
                return true;
            }
        });

        Display defaultDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        resize(point.x, point.y);

        setWebViewClient(new WebViewClient() {
            private int running = 0;

            public void onPageFinished(WebView webView, String str) {
                int i = running - 1;
                running = i;
                if (i == 0) {
                    Anzu.logicCallback("load_finish");
                }
            }

            public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
                running = Math.max(running, 1);
            }

            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                Anzu.logicCallback("load_fail");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String str) {
                running++;
                webView.loadUrl(str);
                return true;
            }
        });
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    void eval(String str) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            loadUrl("javascript:" + str);
            return;
        }

        evaluateJavascript(str, str1 -> {
            if (str1.compareTo("null") != 0) {
                Log.println(Log.WARN, "ANZU", "JS CALL RETURNED: " + str1);
            }
        });
    }

    public void resize(int i, int i2) {
        layout(0, 0, i, i2);
    }
}
