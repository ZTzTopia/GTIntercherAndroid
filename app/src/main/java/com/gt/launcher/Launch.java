package com.gt.launcher;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.rtsoft.growtopia.SharedActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Launch extends SharedActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("GTLauncher", "Launching growtopia..");

        BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArv12FD/xxuAJ3/B8Jgx78985UN/FitcQD5C21eIS5D+98yr7dy9sw8R2fSTFZKExBZVAfatgDH7s6fb9vfHi43szfpdXs3ZL2hsa7DeCWRyVSTD6o/i14vgwInv1S/dgLAwQth3PDXWF+zYXOlL+umOt9K9eqQo5CZhkwl9JAmMHlazvbhSGAldV5QsdY3pK5wmg/w2873abgYsGdI3B9wL75kgZW9tV2O6efiIbXlevktGOMup3Ql2H4Rcpa3ZeDtGl+YTQbEUQTYiYBDtFGCyqksXeM6+kCnaF97Ss5wA0w5ID9WJLkziXI4iGBMRd0a7s+vVniwpx771oGcJxewIDAQAB";

        dllname = "growtopia";
        securityEnabled = false;
        IAPEnabled = true;
        HookedEnabled = false;
        PackageName = "com.rtsoft.growtopia";
        if (!new File(Environment.getExternalStorageDirectory().toString() + File.separatorChar + "windows" + File.separatorChar + "BstSharedFolder").exists()) {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(PackageName, 0);
                String libraryPath = packageInfo.applicationInfo.nativeLibraryDir;
                System.load(libraryPath + "/lib" + dllname + ".so");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            super.onCreate(savedInstanceState);
        }
    }

    public static boolean toggleKeyboard(boolean show, int max, String text, boolean isPassword) {
        if (show && !m_canShowCustomKeyboard) {
            app.makeToastUI("Can't show keyboard while another keyboard is showed.");
            return false;
        }
        else {
            if (show) {
                SharedActivity.passwordField = isPassword;
                SharedActivity.m_text_max_length = max;
                SharedActivity.m_text_default = text;
                SharedActivity.m_before = text;
                SharedActivity.updateText = true;
                app.clearIngameInputBox();
                app.ChangeEditBoxProperty();
                SharedActivity.updateText = false;
                app.toggle_keyboard(true);
            }
            else {
                app.toggle_keyboard(false);
            }

            app.mMainThreadHandler.post(app.mUpdateMainThread);
            return true;
        }
    }
}
