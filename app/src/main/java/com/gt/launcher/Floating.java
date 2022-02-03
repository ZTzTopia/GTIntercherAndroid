package com.gt.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
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

import androidx.annotation.RequiresApi;

import com.rtsoft.growtopia.AppRenderer;
import com.rtsoft.growtopia.SharedActivity;

public class Floating {
    private final Context mContext;
    private WindowManager mWindowManager;
    private final FrameLayout mFrameLayout;
    private final View mFloatingWindow;
    private final RelativeLayout mFloatingWindowContent;

    @SuppressLint("ClickableViewAccessibility")
    public Floating(Context context) {
        mContext = context;

        // The root frame layout of floating window and start floating window button.
        mFrameLayout = new FrameLayout(context);
        mFrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Check if we are touching outsied the floating window.
                if (!SharedActivity.app.inFloatingMode) {
                    return false;
                }

                SharedActivity.app.showEditTextBox(false);
                updateWindowManagerParams(false, false, true);
                return true;
            }
        });

        mFloatingWindow = LayoutInflater.from(context).inflate(R.layout.layout_floating_widget, null);
        mFloatingWindow.setVisibility(View.GONE);

        // Button to start floating window.
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        imageView.setVisibility(View.VISIBLE);
        imageView.setAlpha(0.75f);

        imageView.setImageResource(R.drawable.ic_baseline_arrow_back_24);
        imageView.setBackgroundResource(R.drawable.round_border_purple);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(mFloatingWindow.getVisibility() != View.VISIBLE);
            }
        });

        int applyDimension = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                mContext.getResources().getDisplayMetrics());
        imageView.getLayoutParams().height = applyDimension;
        imageView.getLayoutParams().width = applyDimension;

        // Button to close floating window.
        ImageView floatingCloseButton = mFloatingWindow.findViewById(R.id.id_floating_close_button);
        floatingCloseButton.setAlpha(0.75f);
        floatingCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroy();
                AppRenderer.finishApp();
            }
        });

        mFrameLayout.addView(mFloatingWindow);
        mFrameLayout.addView(imageView);

        // Floating window content.
        mFloatingWindowContent = mFloatingWindow.findViewById(R.id.id_floating_widget_content);

        // Move floating window while drag at title bar.
        RelativeLayout floatingWidgetTitleBar = mFloatingWindow.findViewById(R.id.id_floating_widget_title_bar);
        floatingWidgetTitleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                update(event);
                return true;
            }
        });
    }

    public void onDestroy() {
        if (mFloatingWindow != null) {
            mWindowManager.removeView(mFrameLayout);
        }
    }

    public void setWindowManagerWindowService() {
        int applyDimension = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32,
                mContext.getResources().getDisplayMetrics());

        // Set floating window params.
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                applyDimension,
                applyDimension,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_PHONE :
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 16; // Initial Position of window
        params.y = 16; // Initial Position of window

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mFrameLayout, params);
    }

    public void setVisibility(boolean visibility) {
        if (visibility) {
            // Show the floating window.
            updateWindowManagerParams(false, false, false);
            mFloatingWindow.setVisibility(View.VISIBLE);

            // Content stuff.
            SharedActivity.app.mViewGroup.removeView(SharedActivity.app.mGLView);
            SharedActivity.app.mViewGroup.removeView(SharedActivity.m_editTextRoot);
            mFloatingWindowContent.addView(SharedActivity.app.mGLView);
            mFloatingWindowContent.addView(SharedActivity.m_editTextRoot);

            SharedActivity.app.inFloatingMode = true;

            if (!Main.isNotInApp()) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }
        else {
            // Hide the floating window.
            updateWindowManagerParams(true, false, false);
            mFloatingWindow.setVisibility(View.GONE);

            // Content stuff.
            mFloatingWindowContent.removeView(SharedActivity.app.mGLView);
            mFloatingWindowContent.removeView(SharedActivity.m_editTextRoot);
            SharedActivity.app.mViewGroup.addView(SharedActivity.app.mGLView);
            SharedActivity.app.mViewGroup.addView(SharedActivity.m_editTextRoot);

            SharedActivity.app.inFloatingMode = false;
        }

        // Reload the surface.
        SharedActivity.app.mGLView.onPause();
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedActivity.app.mGLView.onResume();
            }
        }, 1000);
    }

    public void updateWindowManagerParams(boolean show, boolean canShowKeyboard, boolean z) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFrameLayout.getLayoutParams();

        if (!z) {
            if (show) {
                int applyDimension = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        32,
                        mContext.getResources().getDisplayMetrics());

                params.x = 16; // Reset Position of window
                params.y = 16; // Reset Position of window
                params.width = applyDimension;
                params.height = applyDimension;
            }
            else {
                // Calculate the size of floating window.
                DisplayMetrics displayMetrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
                int weirdPixels = displayMetrics.widthPixels;
                int hookPixels = displayMetrics.heightPixels;
                if (weirdPixels < hookPixels) {
                    weirdPixels = displayMetrics.heightPixels;
                    hookPixels = displayMetrics.widthPixels;
                }

                weirdPixels = (int) (((float) weirdPixels) / 2.5f);
                hookPixels = (int) (((float) hookPixels) / 2.0f);

                params.x = 16; // Reset Position of window
                params.y = 16; // Reset Position of window
                params.width = weirdPixels;
                params.height = hookPixels;
            }
        }

        if (canShowKeyboard) {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
        }
        else {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }

        mWindowManager.updateViewLayout(mFrameLayout, params);
    }

    private static int initialX = 0;
    private static int initialY = 0;
    private static float initialTouchX = 0;
    private static float initialTouchY = 0;

    public void update(MotionEvent event) {
        if (mFrameLayout == null || mFloatingWindow.getVisibility() != View.VISIBLE) {
            return;
        }

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) mFrameLayout.getLayoutParams();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                mWindowManager.updateViewLayout(mFrameLayout, params);
                break;
            default:
                break;
        }

        // We need to close the keyboard when the floating window is clicked.
        updateWindowManagerParams(false, true, true);
    }
}
