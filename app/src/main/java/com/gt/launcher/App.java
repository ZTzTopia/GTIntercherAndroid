package com.gt.launcher;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;

import java.util.Arrays;

public class App extends Application {
    final static String[] CHANGE_PACKAGE_NAMES = {
        "com.appsflyer.internal",
        "com.gt.launcher.App.getAssets"
    };

    final static String[] CHANGE_ASSETS = {
        "com.rtsoft.growtopia.SharedActivity.music_play",
        "com.rtsoft.growtopia.SharedActivity.sound_load"
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public String getPackageName() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String stackTraceElement = Arrays.toString(stackTraceElements);

        Log.v("getPackageName", stackTraceElement);

        for (String changePackageName : CHANGE_PACKAGE_NAMES) {
            if (stackTraceElement.contains(changePackageName)) {
                return "com.rtsoft.growtopia";
            }
        }

        return super.getPackageName();
    }

    @Override
    public AssetManager getAssets() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String stackTraceElement = Arrays.toString(stackTraceElements);

        Log.v("getAssets", stackTraceElement);

        for (String changeAsset : CHANGE_ASSETS) {
            if (stackTraceElement.contains(changeAsset)) {
                try {
                    Context context = createPackageContext(getPackageName(), 0);
                    return context.getAssets();
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return super.getAssets();
    }
}