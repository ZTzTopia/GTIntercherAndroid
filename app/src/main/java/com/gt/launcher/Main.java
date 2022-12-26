package com.gt.launcher;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.gt.launcher.utils.NativeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main extends com.rtsoft.growtopia.Main {
    static final String[] NATIVE_LIBRARIES = {
        "anzu", // We need anzu because we are using NativeUtils.installNativeLibraryPath
        "GrowtopiaFix"
    };
    private static final String TAG = "GTL.Main";

    public static boolean isInApp() {
        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance == 100;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            // Checks if the Growtopia app is installed on the device.
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.rtsoft.growtopia",
                0
            );

            // Checks if native library is exported to folder or
            // we have to extract app to get native library.

            var nativeLib = new File(applicationInfo.nativeLibraryDir + "/libgrowtopia.so");
            if (!nativeLib.exists()) {
                Log.d(TAG, "Native library not exist in " + applicationInfo.nativeLibraryDir);

                var extractedApk = new File(getCacheDir(), "extracted_apk");
                if (!extractedApk.exists()) {
                    Log.d(TAG, "Extracting app to " + extractedApk.getAbsolutePath());
                    if (!extractApplication(applicationInfo)) {
                        Log.e(TAG, "Failed extracting app to " + extractedApk.getAbsolutePath());

                        finish();
                        System.exit(1);
                    }
                } else {
                    Log.i(TAG, "Extracted app found!");
                }
            } else {
                Log.i(TAG, "Native library found!");
            }

            String nativeLibraryDir = applicationInfo.nativeLibraryDir;

            var extractedApk = new File(getCacheDir(), "extracted_apk");
            if (extractedApk.exists()) {
                String supportedAbi = Arrays.toString(Build.SUPPORTED_ABIS);
                if (supportedAbi.contains("arm64")) {
                    if (!new File(extractedApk + "/lib/arm64-v8a").exists()) {
                        Log.e(TAG, "LMAO");

                        finish();
                        System.exit(1);
                    }

                    nativeLibraryDir = extractedApk + "/lib/arm64-v8a";
                } else if (supportedAbi.contains("armeabi")) {
                    if (!new File(extractedApk + "/lib/armeabi-v7a").exists()) {
                        Log.e(TAG, "LMAO");

                        finish();
                        System.exit(1);
                    }

                    nativeLibraryDir = new File(extractedApk + "/lib/armeabi-v7a").getAbsolutePath();
                }
            }

            NativeUtils.installNativeLibraryPath(getClassLoader(), new File(nativeLibraryDir));
        } catch (Throwable e) {
            Toast.makeText(this, "Failed to start app? is Growtopia installed?", Toast.LENGTH_LONG)
                .show();

            e.printStackTrace();

            finish();
            System.exit(1);
        }

        try {
            for (String nativeLibrary : NATIVE_LIBRARIES) {
                System.loadLibrary(nativeLibrary);
            }
        } catch (ExceptionInInitializerError | UnsatisfiedLinkError e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            makeToastUI("Overlay permission is required in order to show mod menu. " + "Restart the game after you allow permission");
            startActivity(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION",
                Uri.parse("package:" + getPackageName())
            ));
            new Handler().postDelayed(this::finish, 5000);
        } else {
            new Handler().postDelayed(() -> {
                startService(new Intent(Main.this, FloatingService.class));
            }, 700);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, FloatingService.class));
    }

    private boolean extractApplication(ApplicationInfo applicationInfo) {
        String[] apkPath = applicationInfo.splitSourceDirs;
        for (String apkFile : apkPath) {
            String supportedAbi = Arrays.toString(Build.SUPPORTED_ABIS);
            if (supportedAbi.contains("arm64")) {
                if (!apkFile.contains("split_config.arm64_v8a.apk")) {
                    continue;
                }
            } else if (supportedAbi.contains("armeabi")) {
                if (!apkFile.contains("split_config.armeabi_v7a.apk")) {
                    continue;
                }
            }

            try {
                FileInputStream fileInputStream = new FileInputStream(apkFile);
                var zipInputStream = new ZipInputStream(fileInputStream);

                var buffer = new byte[1024];

                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    Log.v(TAG, "Unzipping " + zipEntry.getName());

                    var file_ = new File(getCacheDir(), "extracted_apk/" + zipEntry.getName());

                    File dir = zipEntry.isDirectory() ? file_ : file_.getParentFile();

                    if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
                        Log.e(TAG, "Failed to create directory. wtf?");
                    }

                    if (zipEntry.isDirectory()) {
                        continue;
                    }

                    var fileOutputStream = new FileOutputStream(file_);

                    int len;
                    while ((len = zipInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }

                    fileOutputStream.flush();
                    fileOutputStream.close();
                    zipInputStream.closeEntry();
                }

                zipInputStream.close();

                Log.d(TAG, "Unzipping complete! Path: " + new File(getCacheDir(), "extracted_apk"));
                return true;
            } catch (IOException e) {
                Toast.makeText(this, "Failed to extract APK.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to extract APK.", e);
            }
        }

        return false;
    }
}
