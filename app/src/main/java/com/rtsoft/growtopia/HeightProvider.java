package com.rtsoft.growtopia;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

public class HeightProvider extends PopupWindow implements ViewTreeObserver.OnGlobalLayoutListener {
    private final Activity mActivity;
    private final View rootView;
    int lastKeyboardHeight;
    private HeightListener listener;
    private View parentView;

    public HeightProvider(Activity activity) {
        super(activity);
        lastKeyboardHeight = -1;
        mActivity = activity;
        rootView = new FrameLayout(activity);
        setContentView(rootView);
        setBackgroundDrawable(new ColorDrawable(0));
        setWidth(0);
        setHeight(-1);
        setSoftInputMode(21);
        setInputMethodMode(1);
    }

    private ViewTreeObserver.OnGlobalLayoutListener getGlobalLayoutListener() {
        return this;
    }

    public void OnResume() {
        View findViewById = this.mActivity.findViewById(16908290);
        this.parentView = findViewById;
        findViewById.post(new Runnable() { // from class: com.rtsoft.growtopia.HeightProvider.1
            @Override // java.lang.Runnable
            public void run() {
                HeightProvider.this.rootView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(HeightProvider.this.getGlobalLayoutListener());
                if (HeightProvider.this.isShowing() || HeightProvider.this.parentView.getWindowToken() == null) {
                    return;
                }
                HeightProvider heightProvider = HeightProvider.this;
                heightProvider.showAtLocation(heightProvider.parentView, 0, 0, 0);
            }
        });
    }

    public void OnPause() {
        this.rootView.getViewTreeObserver().addOnGlobalLayoutListener(getGlobalLayoutListener());
        dismiss();
    }

    public HeightProvider setHeightListener(HeightListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onGlobalLayout() {
        HeightListener heightListener;
        Point point = new Point();
        this.mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        Rect rect = new Rect();
        this.rootView.getWindowVisibleDisplayFrame(rect);
        if (this.mActivity.getResources().getConfiguration().orientation == 1) {
            return;
        }
        int topCutoutHeight = (point.y + getTopCutoutHeight()) - rect.bottom;
        Log.d("HeightProvider", "Keyboard height: " + topCutoutHeight);
        if (topCutoutHeight != this.lastKeyboardHeight && (heightListener = this.listener) != null) {
            heightListener.onHeightChanged(topCutoutHeight);
        }
        this.lastKeyboardHeight = topCutoutHeight;
    }

    private int getTopCutoutHeight() {
        DisplayCutout displayCutout;
        View decorView = this.mActivity.getWindow().getDecorView();
        int i = 0;
        if (decorView == null) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            WindowInsets rootWindowInsets = decorView.getRootWindowInsets();
            if (Build.VERSION.SDK_INT >= 28 && (displayCutout = rootWindowInsets.getDisplayCutout()) != null) {
                for (Rect rect : displayCutout.getBoundingRects()) {
                    if (rect.top == 0) {
                        i += rect.bottom - rect.top;
                    }
                }
            }
        }
        return i;
    }

    public interface HeightListener {
        void onHeightChanged(int height);
    }
}
