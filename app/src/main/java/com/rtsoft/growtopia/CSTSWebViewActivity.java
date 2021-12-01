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

    public String getDeviceInfos() {
        return ((("android version:" + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")") + ";android API Level:" + Build.VERSION.SDK_INT) + ";device:" + Build.DEVICE) + ";model:" + Build.MODEL;
    }

    public void onBackPressed() {
        if (_webView.canGoBack()) {
            _webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCSExit() {
        finish();
    }

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
            String stringExtra = intent.getStringExtra("cstsuid");
            String stringExtra2 = intent.getStringExtra("country");
            String stringExtra3 = intent.getStringExtra("language");
            boolean booleanExtra = intent.getBooleanExtra("payer", false);
            String stringExtra4 = intent.getStringExtra("ingameplayerid");
            String stringExtra5 = intent.getStringExtra("environment");
            String stringExtra6 = intent.getStringExtra("misc");
            String str = (stringExtra5.equals("PROD") ? "https://csts-mob.ubi.com/index.php" : "https://dev-csts-mob.ubi.com/index.php") + "?cstsuid=" + stringExtra + "&platform=android&language=" + stringExtra3 + "&country=" + stringExtra2 + "&iap=" + booleanExtra + "&igpid=" + stringExtra4 + "&device=" + urlencode(getDeviceInfos());
            String str2 = str;
            if (stringExtra6 != null) {
                str2 = str;
                if (!stringExtra6.equals("")) {
                    str2 = str + "&misc=" + urlencode(stringExtra6);
                }
            }

            String str3 = str2 + "&dnaid=" + stringExtra4;
            Log.v("cstslog", "connecting to CSTS  : " + str3);
            _initialURL = str3;
            _webView.loadUrl(str3);
        }
    }

    protected void onPause() {
        super.onPause();
        onCSExit();
    }

    protected void onPostCreate(Bundle bundle) {
        super.onPostCreate(bundle);
    }

    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        this._webView.restoreState(bundle);
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        this._webView.saveState(bundle);
    }

    public String urlencode(String str) {
        String str2 = str;
        try {
            str2 = URLEncoder.encode(str2, "utf-8");
        } catch (Exception e) {
            Log.e("cstslog", "CSTS_urlencode" + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
        return str2;
    }
}
