package com.gt.launcher;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.rtsoft.growtopia.SharedActivity;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Main extends Activity {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We can load other library here.
        System.loadLibrary("GrowtopiaFix");

        try {
            getPackageManager().getPackageInfo("com.rtsoft.growtopia", 0);

            // Extract the library .apk file, i tested on my android 11 device the library is not in data/app/<path>/lib/<abi> anymore.
            // So we need to extract it.
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("com.rtsoft.growtopia", 0);
            String libraryPath = applicationInfo.nativeLibraryDir;
            File libraryFile = new File(libraryPath + "/libgrowtopia.so");
            if (!libraryFile.exists()) {
                String[] apkPath = applicationInfo.splitSourceDirs;
                for (String file : apkPath) {
                    if (file.contains("split_config.armeabi_v7a.apk") || file.contains("split_config.arm64_v8a.apk")) {
                        try {
                            ZipFile zipFile = new ZipFile(file);
                            zipFile.extractAll(getExternalFilesDir(null).getAbsolutePath() + "/extracted/");
                        }
                        catch (ZipException e) {
                            e.printStackTrace();
                            finish();
                            System.exit(1);
                            return;
                        }
                    }
                }
            }

            startActivity(new Intent(this, Launch.class));
            finish();
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("GTLauncherAndroid", "Growtopia application not found.");
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    AlertDialog alertDialog = new AlertDialog.Builder(Main.this).create();
                    alertDialog.setTitle("Growtopia application not found");
                    alertDialog.setMessage("This launcher need original Growtopia application from playstore to run, please donwload it from playstore and re-open the launcher.");
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(1);
                        }
                    });
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            });
        }
    }
}
