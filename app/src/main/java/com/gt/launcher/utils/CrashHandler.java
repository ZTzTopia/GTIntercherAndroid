// Credit: Raunak Mods - https://t.me/raunakmods786
// Copy from https://github.com/LGLTeam/Android-Mod-Menu/blob/master/app/src/main/java/com/android/support/CrashHandler.java

package com.gt.launcher.utils;

import static java.util.Locale.ENGLISH;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gt.launcher.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashHandler {
    public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();

    public static void init(final Context app, final boolean overlayRequired) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                try {
                    tryUncaughtException(thread, throwable);
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) {
                        DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                    }
                    else {
                        System.exit(1);
                    }
                }
            }

            private void tryUncaughtException(Thread thread, Throwable throwable) {
                final String time = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", ENGLISH).format(new Date());
                String fileName = "mod_menu_crash_" + time + ".txt";
                String dirName = String.valueOf(app.getExternalFilesDir(null));
                File crashFile = new File(dirName, fileName);

                String errorLog =
                        "Crash time: " + time + "\n" +
                        "Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n" +
                        "Brand: " + Build.BRAND + "\n" +
                        "Device: " + Build.DEVICE + "\n" +
                        "Hardware: " + Build.HARDWARE + "\n" +
                        "ID: " + Build.ID + "\n" +
                        "Manufacturer: " + Build.MANUFACTURER + "\n" +
                        "Model: " + Build.MODEL + "\n" +
                        "Product: " + Build.PRODUCT + "\n" +
                        "Type: " + Build.TYPE + "\n" +
                        "Version.CodeName: " + Build.VERSION.CODENAME + "\n" +
                        "Version.Incremental: " + Build.VERSION.INCREMENTAL + "\n" +
                        "Version.Release: " + Build.VERSION.RELEASE + "\n" +
                        "Version.SDK: " + Build.VERSION.SDK_INT + "\n" +
                        Log.getStackTraceString(throwable);

                try {
                    writeFile(crashFile, errorLog);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(app, "Game has crashed unexpectedly", Toast.LENGTH_LONG).show();
                Toast.makeText(app, "Log saved to: " + String.valueOf(crashFile).replace("/storage/emulated/0/", ""), Toast.LENGTH_LONG).show();
                System.exit(1);
            }

            private void writeFile(File file, String content) throws IOException {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs();
                }

                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(content.getBytes());
                try {
                    fos.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
