package com.rtsoft.growtopia;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

public class Main extends SharedActivity {
    private final AppsFlyerManager appsflyerManager = new AppsFlyerManager(this);
    private HeightProvider heightProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArv12FD/xxuAJ3/B8Jgx78985UN/FitcQD5C21eIS5D+98yr7dy9sw8R2fSTFZKExBZVAfatgDH7s6fb9vfHi43szfpdXs3ZL2hsa7DeCWRyVSTD6o/i14vgwInv1S/dgLAwQth3PDXWF+zYXOlL+umOt9K9eqQo5CZhkwl9JAmMHlazvbhSGAldV5QsdY3pK5wmg/w2873abgYsGdI3B9wL75kgZW9tV2O6efiIbXlevktGOMup3Ql2H4Rcpa3ZeDtGl+YTQbEUQTYiYBDtFGCyqksXeM6+kCnaF97Ss5wA0w5ID9WJLkziXI4iGBMRd0a7s+vVniwpx771oGcJxewIDAQAB";
        dllname = "growtopia";
        securityEnabled = false;
        IAPEnabled = true;
        HookedEnabled = false;
        PackageName = "com.rtsoft.growtopia";

        // Uh this block BlueStack emulator?
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory().toString());
        stringBuilder.append(File.separatorChar);
        stringBuilder.append("windows");
        stringBuilder.append(File.separatorChar);
        stringBuilder.append("BstSharedFolder");
        if (new File(stringBuilder.toString()).exists()) {
            return;
        }

        System.loadLibrary(dllname);

        super.onCreate(savedInstanceState);

        appsflyerManager.Init();
        heightProvider = new HeightProvider(this).setHeightListener(this::OnKeyboardHeightChanged);
    }

    void OnKeyboardHeightChanged(int heightOfKeyboard) {
        m_KeyBoardHeight = heightOfKeyboard;
        Log.d("NIRMAN", "Keyboard height = " + m_KeyBoardHeight);

        if (m_KeyBoardHeight > 0 && !m_editText.isFocused()) {
            Log.d("NIRMAN", "KeyboardX opening...");
            UpdateEditBoxInView(true, false);
        } else if (m_KeyBoardHeight == 0 && m_editText.isFocused()) {
            Log.d("NIRMAN", "KeyboardX closing...");
            if (!SharedActivity.passwordField) {
                SharedActivity.nativeOnKey(1, 500000, 0);
            }

            nativeCancelBtnPressed();
            UpdateEditBoxInView(false, false);

            if (Looper.myLooper() != Looper.getMainLooper()) {
                nativeUpdateConsoleLogPos(m_KeyBoardHeight);
            }
        }

        if (m_editText.isFocused()) {
            UpdateEditBoxRootViewPosition();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        appsflyerManager.Start();
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        heightProvider.OnResume();
    }

    @Override
    protected synchronized void onPause() {
        super.onPause();
        heightProvider.OnPause();
    }

    @Override
    public void onApplsFlyerLogPurchase(String item, String currency, String price) {
        appsflyerManager.LogPurchase(item, currency, price);
    }

    @Override
    public void onApplsFlyerLogEvent(String eventName, String data) {
        appsflyerManager.LogEvent(eventName, data);
    }

    @Override
    public String GetAppsflyerUID() {
        return appsflyerManager.GetAppsFlyerId();
    }

    @Override
    public AssetManager getAssets() {
        return getApplicationContext().getAssets();
    }
}
