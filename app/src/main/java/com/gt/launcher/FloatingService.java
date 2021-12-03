package com.gt.launcher;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
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

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    public void onCreate() {
        super.onCreate();

        // The root frame layout of floating window and start floating window button.
        mFrameLayout = new FrameLayout(this);
        mFrameLayout.setOnTouchListener((v, motionEvent) -> {
            SharedActivity.app.aww(false);
            return true;
        });

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        mFloatingWidget.setVisibility(View.GONE);
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
                // Hide the floating window.
                setWindowManagerParams(false, true);
                mFloatingWidget.setVisibility(View.GONE);

                // Set the button border to visible.
                imageView.setBackgroundResource(R.drawable.round_border_black);

                // Content stuff.
                mFloatingWidgetContent.removeView(SharedActivity.app.mGLView);
                mFloatingWidgetContent.removeView(SharedActivity.m_editTextRoot);
                SharedActivity.app.mViewGroup.addView(SharedActivity.app.mGLView);
                SharedActivity.app.mViewGroup.addView(SharedActivity.m_editTextRoot);

                // Reload the surface.
                SharedActivity.app.mGLView.onPause();
                new android.os.Handler().postDelayed(() -> SharedActivity.app.mGLView.onResume(), 1000);

                SharedActivity.app.isInFloatingMode = false;
            }
            else {
                // Show the floating window.
                setWindowManagerParams(false, false);
                mFloatingWidget.setVisibility(View.VISIBLE);

                // Set the button border to transparent.
                imageView.setBackgroundResource(R.drawable.round_border_transparent);

                // Content stuff.
                SharedActivity.app.mViewGroup.removeView(SharedActivity.app.mGLView);
                SharedActivity.app.mViewGroup.removeView(SharedActivity.m_editTextRoot);
                mFloatingWidgetContent.addView(SharedActivity.app.mGLView);
                mFloatingWidgetContent.addView(SharedActivity.m_editTextRoot);

                // Reload the surface.
                SharedActivity.app.mGLView.onPause();
                new android.os.Handler().postDelayed(() -> SharedActivity.app.mGLView.onResume(), 1000);

                SharedActivity.app.isInFloatingMode = true;
            }
        });
        // EOF

        mFrameLayout.addView(mFloatingWidget);
        mFrameLayout.addView(imageView);

        // Set window manager params.
        setWindowManagerParams(true, true);

        // Floating window content.
        mFloatingWidgetContent = mFloatingWidget.findViewById(R.id.id_floating_widget_content);

        // Move floating window while drag at title bar.
        RelativeLayout floatingWidgetTitleBar = mFloatingWidget.findViewById(R.id.id_floating_widget_title_bar);
        floatingWidgetTitleBar.setOnTouchListener((v, motionEvent) -> {
            updateViewLayout(motionEvent);
            return true;
        });

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

    private void setWindowManagerParams(boolean isFirstTime, boolean isHideMode) {
        // Calculate the size of floating window.
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        if (widthPixels < heightPixels) {
            widthPixels = displayMetrics.heightPixels;
            heightPixels = displayMetrics.widthPixels;
        }

        widthPixels = (int) (((float) widthPixels) / 2.5f);
        heightPixels = (int) (((float) heightPixels) / 2.0f);

        // Set the params for the floating window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                isHideMode ? 32 * 2 : widthPixels,
                isHideMode ? 32 * 2 : heightPixels,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_PHONE :
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                isHideMode ? WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE :
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 16; // Initial Position of window
        params.y = 16; // Initial Position of window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (isFirstTime) {
            mWindowManager.addView(mFrameLayout, params);
            return;
        }

        mWindowManager.updateViewLayout(mFrameLayout, params);
    }

    private static int initialX = 0;
    private static int initialY = 0;
    private static float initialTouchX = 0;
    private static float initialTouchY = 0;

    public static void updateViewLayout(MotionEvent motionEvent) {
        if (mFrameLayout == null || mFloatingWidget.getVisibility() != View.VISIBLE) {
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
                mWindowManager.updateViewLayout(mFrameLayout, params);
                break;
        }
    }
}
