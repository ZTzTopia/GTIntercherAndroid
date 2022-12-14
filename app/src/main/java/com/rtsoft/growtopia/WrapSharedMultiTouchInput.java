package com.rtsoft.growtopia;

import android.view.MotionEvent;

class WrapSharedMultiTouchInput {
    static {
        try {
            Class.forName("com.rtsoft.growtopia.SharedMultiTouchInput");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SharedMultiTouchInput mInstance;

    public static boolean OnInput(MotionEvent motionEvent) {
        return SharedMultiTouchInput.OnInput(motionEvent);
    }

    /* calling here forces class initialization */
    public static void checkAvailable(SharedActivity sharedActivity) {
        SharedMultiTouchInput.init(sharedActivity);
    }
}
