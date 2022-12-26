package com.gt.launcher;

import static com.gt.launcher.Main.isInApp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.rtsoft.growtopia.SharedActivity;

public class FloatingService extends Service {
    public static Floating floating;

    @Override
    public void onCreate() {
        super.onCreate();

        floating = new Floating(this);
        floating.setWindowManagerWindowService();

        var handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                if (SharedActivity.app.inFloatingMode && isInApp()) {
                    floating.setVisibility(false);
                }

                handler.postDelayed(this, 1000);
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        floating.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        new Handler().postDelayed(this::stopSelf, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }
}
