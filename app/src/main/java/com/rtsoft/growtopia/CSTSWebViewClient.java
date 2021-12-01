package com.rtsoft.growtopia;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class CSTSWebViewClient extends WebViewClient {
    private CSTSWebViewClientCallback _callback;
    private boolean _isInCreateAccount;

    interface CSTSWebViewClientCallback {
        void onCSExit();
    }

    public boolean isInCreateAccount() {
        return _isInCreateAccount;
    }

    public void onLoadResource(WebView webView, String str) {
        if (str.contains("Default/CreateAccount?appId")) {
            _isInCreateAccount = true;
        }
    }

    public void onPageFinished(WebView webView, String str) {
        Log.v("cstslog", "adding javascript callback");
        webView.loadUrl("javascript:function csts_onTicketCreationResult(wasTicketCreated, message) {window.location.href = 'ticket://'+(wasTicketCreated?1:0)+'/'+message; };");
    }

    public void onReceivedError(WebView webView, int i, String str, String str2) {
        Log.e("csts", "onReceivedError [" + str + "] : " + str2);
    }

    public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("csts", "onReceivedError [" + ((Object) webResourceError.getDescription()) + "] : " + webResourceRequest.getUrl());
        }
    }

    public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("csts", "onReceivedHttpError [" + webResourceResponse.getStatusCode() + "] : " + webResourceRequest.getUrl());
        }
    }

    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        Log.v("cstslog", "onReceivedSslError : " + sslError.toString());
        Log.v("cstslog", "the URL : " + sslError.getUrl());
        Log.v("cstslog", "CANCEL");
        sslErrorHandler.cancel();
    }

    public void setCSTSWebViewActivityCallback(CSTSWebViewClientCallback cSTSWebViewClientCallback) {
        _callback = cSTSWebViewClientCallback;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        Log.v("cstslog", "shouldOverrideUrlLoading [" + str + "]");
        if (str.equals("exit://")) {
            CSTSWebViewClientCallback cSTSWebViewClientCallback = _callback;
            if (cSTSWebViewClientCallback != null) {
                cSTSWebViewClientCallback.onCSExit();
            }
            return true;
        } else if (str.contains("legal.ubi.com")) {
            webView.getContext().startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
            return true;
        } else {
            boolean z = false;
            if (str.startsWith("ticket://")) {
                Log.v("cstslog", "Ticket detected");
                if (str.charAt(9) == '1') {
                    z = true;
                }
                Log.v("cstslog", "Ticket creation status: " + z + " detail: " + str.substring(11));
                return true;
            }
            _isInCreateAccount = false;
            return false;
        }
    }
}
