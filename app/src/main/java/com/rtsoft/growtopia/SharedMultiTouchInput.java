package com.rtsoft.growtopia;

import android.view.MotionEvent;

import java.util.LinkedList;
import java.util.ListIterator;

// Tricks for being compatible with Android 1.5 but still being able to use new features of 2.2
public class SharedMultiTouchInput {
    public static SharedActivity app;
    static LinkedList<TouchInfo> listTouches;

    static class TouchInfo {
        int fingerID;
        public int pointerID;
    }

    public static int GetFingerByPointerID(int pointerID) {
        for (TouchInfo next : listTouches) {
            if (pointerID == next.pointerID) {
                return next.fingerID;
            }
        }

        TouchInfo touchInfo = new TouchInfo();
        touchInfo.pointerID = pointerID;
        touchInfo.fingerID = GetNextAvailableFingerID();
        listTouches.add(touchInfo);
        return touchInfo.fingerID;
    }

    public static int GetNextAvailableFingerID() {
        int fingerID = 0;
        while (fingerID < 12) {
            ListIterator<TouchInfo> listIterator = listTouches.listIterator();
            while (true) {
                if (listIterator.hasNext()) {
                    if (fingerID == listIterator.next().fingerID) {
                        break;
                    }
                } else {
                    return fingerID;
                }
            }

            // Guess we failed, try again
            fingerID++;
        }
        return fingerID;
    }

    // Based on code from http://stackoverflow.com/questions/5860879/android-motionevent-getactionindex-and-multitouch
    public static boolean OnInput(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        int actionMasked = motionEvent.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                SharedMultiTouchInput.processMouse(actionMasked, motionEvent.getX(actionIndex), motionEvent.getY(actionIndex), motionEvent.getPointerId(actionIndex));
                break;
            case MotionEvent.ACTION_MOVE: {
                int pointerCount = 0;
                while (pointerCount < motionEvent.getPointerCount()) {
                    SharedMultiTouchInput.processMouse(MotionEvent.ACTION_MOVE, motionEvent.getX(pointerCount), motionEvent.getY(pointerCount), motionEvent.getPointerId(pointerCount));
                    pointerCount++;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                listTouches.clear();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                SharedMultiTouchInput.processMouse(MotionEvent.ACTION_DOWN, motionEvent.getX(actionIndex), motionEvent.getY(actionIndex), motionEvent.getPointerId(actionIndex));
                break;
            case MotionEvent.ACTION_POINTER_UP:
                SharedMultiTouchInput.processMouse(MotionEvent.ACTION_UP, motionEvent.getX(actionIndex), motionEvent.getY(actionIndex), motionEvent.getPointerId(actionIndex));
                break;
            default:
                return false;
        }
        return true;
    }

    public static void RemoveFinger(int pointerID) {
        ListIterator<TouchInfo> listIterator = listTouches.listIterator();
        while (listIterator.hasNext()) {
            if (pointerID == listIterator.next().pointerID) {
                listIterator.remove();
            }
        }
    }

    public static void init(SharedActivity sharedActivity) {
        app = sharedActivity;
        listTouches = new LinkedList<>();
    }

    // We can't just send the id, it cannot be used as a "fingerID" as it could be 100 or more in certain circumstances on an
    // xperia.  We'll do our own finger track abstraction here before we send it to Proton
    public static void processMouse(int i, float f, float f2, int pointerID) {
        int GetFingerByPointerID = GetFingerByPointerID(pointerID);
        if (i == 1) {
            RemoveFinger(pointerID);
        }

        AppGLSurfaceView.nativeOnTouch(i, f, f2, GetFingerByPointerID);
    }
}
