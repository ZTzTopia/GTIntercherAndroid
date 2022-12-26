package com.gt.launcher;

import android.annotation.SuppressLint;
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

import com.rtsoft.growtopia.SharedActivity;

public class Floating {
    private static int initialX = 0;
    private static int initialY = 0;
    private static float initialTouchX = 0;
    private static float initialTouchY = 0;
    private final Context mContext;
    private final CustomTouchView frameLayout;
    private final View floatingWindow;
    private final RelativeLayout mFloatingWindowContent;
    private WindowManager mWindowManager;

    class CustomTouchView extends FrameLayout implements View.OnTouchListener {
        public CustomTouchView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Check if we are touching outside the floating window.
            if (!SharedActivity.app.inFloatingMode) {
                return false;
            }

            SharedActivity.app.showEditTextBox(false);
            updateWindowManagerParams(false, false, true);
            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }
    }

    public Floating(Context context) {
        mContext = context;

        // The root frame layout of floating window and start floating window button.
        frameLayout = new CustomTouchView(context);

        floatingWindow = LayoutInflater.from(context)
            .inflate(R.layout.layout_floating_widget, null);
        floatingWindow.setVisibility(View.GONE);

        // Button to start floating window.
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ));
        imageView.setVisibility(View.VISIBLE);
        imageView.setAlpha(0.75f);

        imageView.setImageResource(R.drawable.baseline_navigate_before_24);
        imageView.setBackgroundResource(R.drawable.round_back_background);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(floatingWindow.getVisibility() != View.VISIBLE);
            }
        });

        int applyDimension = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            32,
            mContext.getResources().getDisplayMetrics()
        );
        imageView.getLayoutParams().height = applyDimension;
        imageView.getLayoutParams().width = applyDimension;

        // Button to close floating window.
        ImageView floatingCloseButton = floatingWindow.findViewById(R.id.floating_close_button);
        floatingCloseButton.setAlpha(0.75f);
        floatingCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroy();
                // AppRenderer.finishApp();
            }
        });

        frameLayout.addView(floatingWindow);
        frameLayout.addView(imageView);

        // Floating window content.
        mFloatingWindowContent = floatingWindow.findViewById(R.id.floating_widget_content);

        // Move floating window while drag at title bar.
        RelativeLayout floatingWidgetTitleBar = floatingWindow.findViewById(R.id.floating_widget_title_bar);
        floatingWidgetTitleBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                update(event);
                return true;
            }
        });
    }

    public void onDestroy() {
        if (floatingWindow != null) {
            mWindowManager.removeView(frameLayout);
        }
    }

    public void setWindowManagerWindowService() {
        int applyDimension = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            32,
            mContext.getResources().getDisplayMetrics()
        );

        // Set floating window params.
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            applyDimension,
            applyDimension,
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_PHONE :
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 16; // Initial Position of window
        params.y = 16; // Initial Position of window

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(frameLayout, params);
    }

    public void setVisibility(boolean visibility) {
        if (visibility) {
            // Show the floating window.
            updateWindowManagerParams(false, false, false);
            floatingWindow.setVisibility(View.VISIBLE);

            // Content stuff.
            SharedActivity.app.mViewGroup.removeView(SharedActivity.app.mGLView);
            SharedActivity.app.mViewGroup.removeView(SharedActivity.m_editTextRoot);
            mFloatingWindowContent.addView(SharedActivity.app.mGLView);
            mFloatingWindowContent.addView(SharedActivity.m_editTextRoot);

            SharedActivity.app.inFloatingMode = true;

            if (Main.isInApp()) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        } else {
            // Hide the floating window.
            updateWindowManagerParams(true, false, false);
            floatingWindow.setVisibility(View.GONE);

            // Content stuff.
            mFloatingWindowContent.removeView(SharedActivity.app.mGLView);
            mFloatingWindowContent.removeView(SharedActivity.m_editTextRoot);
            SharedActivity.app.mViewGroup.addView(SharedActivity.app.mGLView);
            SharedActivity.app.mViewGroup.addView(SharedActivity.m_editTextRoot);

            SharedActivity.app.inFloatingMode = false;
        }

        // Reload the surface.
        SharedActivity.app.mGLView.onPause();
        new android.os.Handler().postDelayed(() -> SharedActivity.app.mGLView.onResume(), 1000);
    }

    public void updateWindowManagerParams(boolean show, boolean canShowKeyboard, boolean z) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) frameLayout.getLayoutParams();

        if (!z) {
            if (show) {
                int applyDimension = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    32,
                    mContext.getResources().getDisplayMetrics()
                );

                params.x = 16; // Reset Position of window
                params.y = 16; // Reset Position of window
                params.width = applyDimension;
                params.height = applyDimension;
            } else {
                // Calculate the size of floating window.
                DisplayMetrics displayMetrics = mContext.getApplicationContext().getResources()
                    .getDisplayMetrics();
                int weirdPixels = displayMetrics.widthPixels;
                int hookPixels = displayMetrics.heightPixels;
                if (weirdPixels < hookPixels) {
                    weirdPixels = displayMetrics.heightPixels;
                    hookPixels = displayMetrics.widthPixels;
                }

                weirdPixels = (int) (((float) weirdPixels) / 2.5f);
                hookPixels = (int) (((float) hookPixels) / 2.3f);

                params.x = 16; // Reset Position of window
                params.y = 16; // Reset Position of window
                params.width = weirdPixels;
                params.height = hookPixels;
            }
        }

        if (canShowKeyboard) {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
        } else {
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        }

        mWindowManager.updateViewLayout(frameLayout, params);
    }

    public void update(MotionEvent event) {
        if (frameLayout == null || floatingWindow.getVisibility() != View.VISIBLE) {
            return;
        }

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) frameLayout.getLayoutParams();
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
                mWindowManager.updateViewLayout(frameLayout, params);
                break;
            default:
                break;
        }

        // We need to close the keyboard when the floating window is clicked.
        updateWindowManagerParams(false, true, true);
    }
}
