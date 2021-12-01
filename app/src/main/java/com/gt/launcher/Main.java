/*
 *
 * TODO:
 * Fix Couldn't load surface (from assets), ok it looks like growtopia original too.
 * Fix save data cache path to this application data not growtopia data. -> Done
 * Fix growtopia and anzu java folder code, lazy to fix :D (but it works)
 * Fix tapjoy. -> Done
 * Fix IAP purchase . -> I think we need to change package name to com.rtsoft.growtopia?
 * Fix crash after resume the game. -> I think it's fixed
 *
 */

package com.gt.launcher;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.PathClassLoader;

public class Main extends Activity {
    private final String TAG = "GTLauncher";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We can load other library here.
        System.loadLibrary("GrowtopiaFix");

        try {
            getPackageManager().getPackageInfo("com.rtsoft.growtopia", 0);

            /*String baseApkDir = packageInfo.applicationInfo.publicSourceDir.replace("base.apk", "");
            File[] filesList = new File(baseApkDir).listFiles();
            if (filesList != null) {
                for (File file : filesList) {
                    if (file.getName().endsWith(".apk") && (file.getName().contains("armeabi_v7a") || file.getName().contains("arm64_v8a"))) {
                        Log.d(TAG, "Found apk: " + file.getName());
                        zipExtract(file.getAbsolutePath(), getExternalFilesDir(null).getAbsolutePath() + "/extracted/");
                    }
                }
            }*/
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Growtopia application not found.");

            runOnUiThread(() -> {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this);
                dialog.setTitle("Growtopia application not found.")
                        .setMessage("This launcher need original Growtopia application from playstore to run.")
                        .setNegativeButton("Close", (dialoginterface, which) -> {
                            Process.killProcess(Process.myPid());
                            System.exit(2);
                        }).create();

                dialog.show();
            });
            return;
        }

        new Launch(this).run();
    }

    private void zipExtract(String publicSourceDir, String destinationPath) {
        try {
            Log.d(TAG, "Extracting: " + publicSourceDir + " Destination: " + destinationPath);
            ZipFile zipFile = new ZipFile(publicSourceDir);
            zipFile.extractAll(destinationPath);
        }
        catch (ZipException e) {
            e.printStackTrace();
        }
    }

    static class Launch implements Runnable {
        private final Main main;

        public Launch(Main main) {
            this.main = main;
        }

        @Override
        public void run() {
            try {
                main.startActivity(new Intent(main, Class.forName("com.gt.launcher.Launch")));
                main.finish();
            }
            catch (ClassNotFoundException e) {
                throw new NoClassDefFoundError(e.getMessage());
            }
        }
    }
}
