package com.rtsoft.growtopia;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.net.URLEncoder;
import java.util.Arrays;

public class CSTSWebViewActivity extends Activity implements CSTSWebViewClient.CSTSWebViewClientCallback {
    private String _initialURL;
    private CSTSWebView _webView;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        requestWindowFeature(1);

        _webView = new CSTSWebView(this);
        _webView.getWebClient().setCSTSWebViewActivityCallback(this);

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(_webView);

        setContentView(frameLayout);

        if (bundle == null) {
            Intent intent = getIntent();
            String cstsuid = intent.getStringExtra("cstsuid");
            String language = intent.getStringExtra("language");
            String country = intent.getStringExtra("country");
            boolean payer = intent.getBooleanExtra("payer", false);
            String ingameplayerid = intent.getStringExtra("ingameplayerid");
            String environment = intent.getStringExtra("environment");
            String misc = intent.getStringExtra("misc");
            String str = (environment.equals("PROD") ? "https://csts-mob.ubi.com/index.php" : "https://dev-csts-mob.ubi.com/index.php") +
                    "?cstsuid=" + cstsuid +
                    "&platform=android&language=" + language +
                    "&country=" + country +
                    "&iap=" + payer +
                    "&igpid=" + ingameplayerid +
                    "&device=" + urlencode(getDeviceInfos());
            if (misc != null) {
                if (!misc.equals("")) {
                    str += "&misc=" + urlencode(misc);
                }
            }

            str += "&dnaid=" + ingameplayerid;
            Log.v("cstslog", "connecting to CSTS  : " + str);
            _initialURL = str;
            _webView.loadUrl(str);
        }
    }

    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
    }

    protected void onPause() {
        super.onPause();
        onCSExit();
    }

    public void onBackPressed() {
        if (_webView.canGoBack()) {
            _webView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        _webView.restoreState(bundle);
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        _webView.saveState(bundle);
    }

    @Override
    public void onCSExit() {
        finish();
    }

    public String getDeviceInfos() {
        String deviceInfos = "android version:" + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")";
        deviceInfos += ";android API Level:" + Build.VERSION.SDK_INT;
        deviceInfos += ";device:" + Build.DEVICE;
        deviceInfos += ";model:" + Build.MODEL;
        return deviceInfos;
    }

    public String urlencode(String str) {
        String str2 = str;
        try {
            str2 = URLEncoder.encode(str2, "utf-8");
        }
        catch (Exception e) {
            Log.e("cstslog", "CSTS_urlencode" + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return str2;
    }
}
