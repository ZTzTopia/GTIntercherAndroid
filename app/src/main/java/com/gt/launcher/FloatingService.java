package com.gt.launcher;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.rtsoft.growtopia.SharedActivity;

public class FloatingService extends Service {
    public static FrameLayout       mFrameLayout;
    public static View              mFloatingWidget;
    public static WindowManager     mWindowManager;
    public static RelativeLayout    mFloatingWidgetContent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*
         * Yes, I know, this is not readable code.
         * TODO: Fix onTouch and keyboard.
         */

        mFrameLayout = new FrameLayout(this);

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        mFloatingWidget.setVisibility(View.INVISIBLE);
        mFloatingWidget.setAlpha(0.75f);

        // Button to start floating window.
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        imageView.setVisibility(View.VISIBLE);
        imageView.setAlpha(0.75f);

        int applyDimension = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                getResources().getDisplayMetrics());
        imageView.getLayoutParams().height = applyDimension;
        imageView.getLayoutParams().width = applyDimension;

        imageView.setImageResource(R.drawable.ic_baseline_arrow_back_24);
        imageView.setBackgroundResource(R.drawable.round_border_black);

        imageView.setOnClickListener(v -> {
            if (mFloatingWidget.getVisibility() == View.VISIBLE) {
                imageView.setBackgroundResource(R.drawable.round_border_black);
                mFloatingWidget.setVisibility(View.INVISIBLE);
                mFloatingWidgetContent.removeView(SharedActivity.app.mGLView);
                SharedActivity.app.mViewGroup.addView(SharedActivity.app.mGLView);
                SharedActivity.app.mGLView.onPause();
                new android.os.Handler().postDelayed(() -> SharedActivity.app.mGLView.onResume(), 500);
                SharedActivity.app.isInFloatingMode = false;
            }
            else {
                imageView.setBackgroundResource(R.drawable.round_border_transparent);
                mFloatingWidget.setVisibility(View.VISIBLE);
                SharedActivity.app.mViewGroup.removeView(SharedActivity.app.mGLView);
                mFloatingWidgetContent.addView(SharedActivity.app.mGLView);
                SharedActivity.app.mGLView.onPause();
                new android.os.Handler().postDelayed(() -> SharedActivity.app.mGLView.onResume(), 500);
                SharedActivity.app.isInFloatingMode = true;
            }
        });
        // EOF

        mFrameLayout.addView(mFloatingWidget);
        mFrameLayout.addView(imageView);

        int layoutType;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
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
        params.y = 16; // Initial Position of window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFrameLayout, params);

        mFloatingWidgetContent = mFloatingWidget.findViewById(R.id.id_floating_widget_content);

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
        if (mFrameLayout == null) {
            return;
        }

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFrameLayout.getLayoutParams();

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
                    mWindowManager.updateViewLayout(mFrameLayout, params);
                }
                break;
        }
    }
}
