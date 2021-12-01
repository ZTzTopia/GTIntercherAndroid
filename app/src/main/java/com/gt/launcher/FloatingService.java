package com.gt.launcher;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import androidx.annotation.Nullable;

import com.rtsoft.growtopia.SharedActivity;

public class FloatingService extends Service {
    public static View              mFloatingWidget;
    public static WindowManager     mWindowManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        int layoutType;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            layoutType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        else {
            layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels < heightPixels) {
            widthPixels = displayMetrics.heightPixels;
            heightPixels = displayMetrics.widthPixels;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                (int) (((float) widthPixels) / 2.5f),
                (int) (((float) heightPixels) / 2.0f),
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 16; // Initial Position of window
        params.y = 16;  // Initial Position of window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);

        RelativeLayout relativeLayout = mFloatingWidget.findViewById(R.id.id_floating_widget_content);
        ((ViewGroup) SharedActivity.app.mViewGroup.getParent()).removeAllViews();
        relativeLayout.addView(SharedActivity.app.mViewGroup, params);
        new RelativeLayout(this).setLayoutParams(new TableLayout.LayoutParams(
                relativeLayout.getLayoutParams().width,
                relativeLayout.getLayoutParams().height));

        // Need to set OnTouchListener to move if user touch and drag floating widget title.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        if (mFloatingWidget != null) {
            mWindowManager.removeView(mFloatingWidget);
        }
    }

    private static int initialX = 0;
    private static int initialY = 0;
    private static float initialTouchX = 0;
    private static float initialTouchY = 0;

    public static void updateViewLayout(MotionEvent motionEvent, boolean isTitle) {
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFloatingWidget.getLayoutParams();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                params.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                params.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);
                if (isTitle) {
                    /* ~ */
                }
                else {
                    mWindowManager.updateViewLayout(mFloatingWidget, params);
                }
                break;
        }
    }
}
