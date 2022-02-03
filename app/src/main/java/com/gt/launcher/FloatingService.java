package com.gt.launcher;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.rtsoft.growtopia.SharedActivity;

// https://github.com/LGLTeam/Android-Mod-Menu/blob/master/app/src/main/java/com/android/support/Launcher.java
public class FloatingService extends Service {
    public static Floating mFloating; // memory leak? what? how to i can call floating.updateViewLayout without static lol.

    @Override
    public void onCreate() {
        super.onCreate();

        mFloating = new Floating(this);
        mFloating.setWindowManagerWindowService();

        boolean canCloseFloatingWindow = false;
        final Handler handler = new Handler();
        handler.post((new Runnable() {
            boolean canCloseFloatingWindow;

            public Runnable init(boolean canCloseFloatingWindow) {
                this.canCloseFloatingWindow = canCloseFloatingWindow;
                return this;
            }

            public void run() {
                if (SharedActivity.app.inFloatingMode && Main.isNotInApp()) {
                    canCloseFloatingWindow = true;
                }

                if (canCloseFloatingWindow) {
                    if (SharedActivity.app.inFloatingMode && !Main.isNotInApp()) {
                        mFloating.setVisibility(false);
                        canCloseFloatingWindow = false;
                    }
                }

                handler.postDelayed(this, 1000);
            }
        }).init(canCloseFloatingWindow));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        mFloating.onDestroy();
    }

    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopSelf();
    }
}
