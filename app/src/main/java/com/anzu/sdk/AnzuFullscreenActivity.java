package com.anzu.sdk;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.tapjoy.TJAdUnitConstants;

public class AnzuFullscreenActivity extends Activity {
    private FrameLayout frame;
    private View viewToAdd;

    private void addCloseButton(int i, final byte[] bArr) {
        new Handler(Looper.getMainLooper()).post(() -> {
            ImageButton imageButton = new ImageButton(viewToAdd.getContext());
            imageButton.setImageBitmap(BitmapFactory.decodeByteArray(bArr, 0, bArr.length));
        });
    }

    private void closeActivity() {
        new Handler(Looper.getMainLooper()).post(this::finish);
    }

    private int getScreenOrientation() {
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        int rotation = defaultDisplay.getRotation();
        int[] iArr = {1, 0, 9, 8};
        int i = 0;
        if (rotation < 0 || rotation > 3) {
            return iArr[0];
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        if (displayMetrics.heightPixels < displayMetrics.widthPixels) {
            i = 1;
        }
        return iArr[(rotation ^ i) ^ (rotation & 1)];
    }

    private int orientationToRequest(boolean z, boolean z2, int i) {
        int i2 = new int[]{0, 2, 0, 2, 2, 1, 2, 2}[(((z ? 1 : 0) | (z2 ? 2 : 0)) << 1) | (i & 1)];
        int[] iArr = new int[3];
        iArr[0] = 1;
        iArr[1] = 0;
        iArr[2] = i;
        return iArr[i2];
    }

    public void onBackPressed() {
        interstitialCallback(TJAdUnitConstants.String.CLOSE);
        super.onBackPressed();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        requestWindowFeature(1);

        frame = new FrameLayout(this);
        frame.setBackgroundColor(0);

        viewToAdd = setInterstitialActivity();
        if (viewToAdd.getParent() != null) {
            ((ViewGroup) viewToAdd.getParent()).removeView(viewToAdd);
        }

        frame.addView(viewToAdd, -1, -1);
        getWindow().getDecorView().setSystemUiVisibility(4);

        try {
            setRequestedOrientation(orientationToRequest(true, true, getScreenOrientation()));
        } catch (IllegalStateException e) {
            /* ~ */
        }

        setContentView(frame);
    }

    public void onDestroy() {
        super.onDestroy();
        unsetInterstitialActivity();
    }

    public void onStop() {
        super.onStop();
        frame.removeView(viewToAdd);
        viewToAdd = null;
    }

    private static native void interstitialCallback(String str);
    private native View setInterstitialActivity();
    private native void unsetInterstitialActivity();
}
