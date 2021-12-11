package com.gt.launcher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gt.launcher.utils.NativeUtils;
import com.rtsoft.growtopia.SharedActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Launch extends SharedActivity {
    private static final String TAG = "GTLauncherAndroid";

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("GTLauncherAndroid", "Launching growtopia..");

        BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArv12FD/xxuAJ3/B8Jgx78985UN/FitcQD5C21eIS5D+98yr7dy9sw8R2fSTFZKExBZVAfatgDH7s6fb9vfHi43szfpdXs3ZL2hsa7DeCWRyVSTD6o/i14vgwInv1S/dgLAwQth3PDXWF+zYXOlL+umOt9K9eqQo5CZhkwl9JAmMHlazvbhSGAldV5QsdY3pK5wmg/w2873abgYsGdI3B9wL75kgZW9tV2O6efiIbXlevktGOMup3Ql2H4Rcpa3ZeDtGl+YTQbEUQTYiYBDtFGCyqksXeM6+kCnaF97Ss5wA0w5ID9WJLkziXI4iGBMRd0a7s+vVniwpx771oGcJxewIDAQAB";
        dllname = "growtopia";
        securityEnabled = false;
        IAPEnabled = true;
        HookedEnabled = false;
        PackageName = "com.rtsoft.growtopia";
        if (!new File(Environment.getExternalStorageDirectory().toString() + File.separatorChar + "windows" + File.separatorChar + "BstSharedFolder").exists()) {
            try {
                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.rtsoft.growtopia", 0);
                String libraryPath = applicationInfo.nativeLibraryDir;
                File libraryFile = new File(libraryPath + "/libgrowtopia.so");
                if (libraryFile.exists()) {
                    copyLibraryToDex(libraryPath + "/libgrowtopia.so", "libgrowtopia.so");
                    copyLibraryToDex(libraryPath + "/libanzu.so", "libanzu.so");
                }
                else {
                    copyLibraryToDex(getExtractedLibraryPath() + "/libgrowtopia.so", "libgrowtopia.so");
                    copyLibraryToDex(getExtractedLibraryPath() + "/libanzu.so", "libanzu.so");
                }

                NativeUtils.installNativeLibraryPath(getClassLoader(),
                        new File(getDir("dex", 0).getAbsolutePath()), false);
            }
            catch (Exception e) {
                Toast.makeText(this, "Failed to install native libraries", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            catch (Throwable throwable) {
                Toast.makeText(this, "Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                throwable.printStackTrace();
            }

            try {
                System.loadLibrary("anzu");
                System.loadLibrary(dllname);
            }
            catch (UnsatisfiedLinkError e) {
                Toast.makeText(this, "Failed to load native library", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            super.onCreate(savedInstanceState);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                makeToastUI("Overlay permission is required in order to show mod menu. Restart the game after you allow permission");
                makeToastUI("Overlay permission is required in order to show mod menu. Restart the game after you allow permission");
                startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        System.exit(1);
                    }
                }, 5000);
            }
            else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startService(new Intent(Launch.this, FloatingService.class));
                    }
                }, 700);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, FloatingService.class));
    }

    public void copyLibraryToDex(String pathWithFileName, String fileName) throws Exception {
        FileInputStream open = new FileInputStream(pathWithFileName);
        FileOutputStream fileOutputStream = new FileOutputStream(getDir("dex", 0).getAbsolutePath() + "/" + fileName);
        byte[] bArr = new byte[1024];
        int bytesRead = 0;
        while (true) {
            int read = open.read(bArr);
            if (read <= 0) {
                fileOutputStream.flush();
                fileOutputStream.close();
                open.close();
                System.out.println("Writing " + bytesRead + " bytes");
                return;
            }

            bytesRead += read;
            fileOutputStream.write(bArr, 0, read);
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
            }
            else {
                app.toggle_keyboard(false);
            }

            app.mMainThreadHandler.post(app.mUpdateMainThread);
            return true;
        }
    }
}
