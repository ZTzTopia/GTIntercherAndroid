package com.gt.launcher;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.rtsoft.growtopia.SharedActivity;

import java.io.File;

public class GrowtopiaActivity extends SharedActivity {
    private static final String TAG = "GTLauncherAndroid";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Launching growtopia..");

        BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArv12FD/xxuAJ3/B8Jgx78985UN/FitcQD5C21eIS5D+98yr7dy9sw8R2fSTFZKExBZVAfatgDH7s6fb9vfHi43szfpdXs3ZL2hsa7DeCWRyVSTD6o/i14vgwInv1S/dgLAwQth3PDXWF+zYXOlL+umOt9K9eqQo5CZhkwl9JAmMHlazvbhSGAldV5QsdY3pK5wmg/w2873abgYsGdI3B9wL75kgZW9tV2O6efiIbXlevktGOMup3Ql2H4Rcpa3ZeDtGl+YTQbEUQTYiYBDtFGCyqksXeM6+kCnaF97Ss5wA0w5ID9WJLkziXI4iGBMRd0a7s+vVniwpx771oGcJxewIDAQAB";
        dllname = "growtopia";
        securityEnabled = false;
        IAPEnabled = true;
        HookedEnabled = false;
        PackageName = "com.rtsoft.growtopia";
        if (!new File(Environment.getExternalStorageDirectory().toString() + File.separatorChar + "windows" + File.separatorChar + "BstSharedFolder").exists()) {
            try {
                System.loadLibrary("anzu");
                System.loadLibrary(dllname);
            }
            catch (UnsatisfiedLinkError e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load native library", Toast.LENGTH_SHORT).show();
            }

            super.onCreate(savedInstanceState);
        }
    }

    public static boolean toggleKeyboard(boolean show, int maxLength, String defaultText, boolean isPassword) {
        if (show && !m_canShowCustomKeyboard) {
            app.makeToastUI("Can't show keyboard while another keyboard is showed.");
            return false;
        }
        else {
            if (show) {
                SharedActivity.passwordField = isPassword;
                SharedActivity.m_text_max_length = maxLength;
                SharedActivity.m_text_default = defaultText;
                SharedActivity.m_before = defaultText;
                SharedActivity.updateText = true;

                app.clearIngameInputBox();
                app.ChangeEditBoxProperty();

                SharedActivity.updateText = false;

                app.toggle_keyboard(true);
                m_canShowCustomKeyboard = true;
            }
            else {
                app.toggle_keyboard(false);
            }

            app.mMainThreadHandler.post(app.mUpdateMainThread);
            return true;
        }
    }
}
