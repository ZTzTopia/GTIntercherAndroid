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

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                running++;
                webView.loadUrl(url);
                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                running = Math.max(running, 1);
            }

            public void onPageFinished(WebView webView, String url) {
                running--;
                if (running == 0) {
                    Anzu.logicCallback("load_finish");
                }
            }

            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                Anzu.logicCallback("load_fail");
            }
        });
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    void eval(String javascript) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            loadUrl("javascript:" + javascript);
            return;
        }

        evaluateJavascript(javascript, javascriptEvaulated -> {
            if (javascriptEvaulated.compareTo("null") != 0) {
                Log.println(Log.WARN, "ANZU", "JS CALL RETURNED: " + javascriptEvaulated);
            }
        });
    }

    public void resize(int width, int height) {
        layout(0, 0, width, height);
    }
}
