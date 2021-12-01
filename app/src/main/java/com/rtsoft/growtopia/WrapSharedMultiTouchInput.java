package com.rtsoft.growtopia;

import android.view.MotionEvent;

class WrapSharedMultiTouchInput {
    private SharedMultiTouchInput mInstance;

    static {
        try {
            Class.forName("com.rtsoft.growtopia.SharedMultiTouchInput");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean OnInput(MotionEvent motionEvent) {
        return SharedMultiTouchInput.OnInput(motionEvent);
    }

    /* Calling here forces class initialization */
    public static void checkAvailable(SharedActivity sharedActivity) {
        SharedMultiTouchInput.init(sharedActivity);
    }
}
