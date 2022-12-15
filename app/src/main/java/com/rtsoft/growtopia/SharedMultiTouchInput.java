package com.rtsoft.growtopia;

import android.view.MotionEvent;

import java.util.LinkedList;
import java.util.ListIterator;

// Tricks for being compatible with Android 1.5 but still being able to use new features of 2.2
public class SharedMultiTouchInput {
    public static SharedActivity app;
    static LinkedList<TouchInfo> listTouches;

    public static void init(SharedActivity sharedActivity) {
        app = sharedActivity;
        listTouches = new LinkedList<>();
    }

    public static int GetNextAvailableFingerID() {
        int fingerID = 0;
        while (fingerID < 12) {
            boolean bOk = true;
            for (TouchInfo touchInfo : listTouches) {
                if (fingerID == touchInfo.fingerID) {
                    bOk = false;
                    break;
                }
            }

            if (bOk) {
                return fingerID;
            }

            // Guess we failed, try again
            fingerID++;
        }

        return fingerID;
    }

    public static int GetFingerByPointerID(int pointerID) {
        for (TouchInfo touchInfo : listTouches) {
            if (pointerID == touchInfo.pointerID) {
                return touchInfo.fingerID;
            }
        }

        TouchInfo touchInfo = new TouchInfo();
        touchInfo.pointerID = pointerID;
        touchInfo.fingerID = GetNextAvailableFingerID();

        listTouches.add(touchInfo);
        return touchInfo.fingerID;
    }

    public static void RemoveFinger(int pointerID) {
        ListIterator<TouchInfo> iterator = listTouches.listIterator();
        while (iterator.hasNext()) {
            TouchInfo touchInfo = iterator.next();
            if (pointerID == touchInfo.pointerID) {
                iterator.remove();
                return;
            }
        }
    }

    public static void processMouse(int msg, float x, float y, int id) {
        // We can't just send the id, it cannot be used as a "fingerID" as it could be 100 or more in certain circumstances on an
        // xperia.  We'll do our own finger track abstraction here before we send it to Proton
        int fingerID = GetFingerByPointerID(id);
        if (msg == MotionEvent.ACTION_UP) {
            RemoveFinger(id);
        }

        AppGLSurfaceView.nativeOnTouch(msg, x, y, fingerID);
    }

    // Based on code from http://stackoverflow.com/questions/5860879/android-motionevent-getactionindex-and-multitouch
    public static boolean OnInput(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        int actionMasked = motionEvent.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                SharedMultiTouchInput.processMouse(
                    actionMasked,
                    motionEvent.getX(actionIndex),
                    motionEvent.getY(actionIndex),
                    motionEvent.getPointerId(actionIndex)
                );
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                SharedMultiTouchInput.processMouse(
                    MotionEvent.ACTION_DOWN,
                    motionEvent.getX(actionIndex),
                    motionEvent.getY(actionIndex),
                    motionEvent.getPointerId(actionIndex)
                );
                break;
            case MotionEvent.ACTION_POINTER_UP:
                SharedMultiTouchInput.processMouse(
                    MotionEvent.ACTION_UP,
                    motionEvent.getX(actionIndex),
                    motionEvent.getY(actionIndex),
                    motionEvent.getPointerId(actionIndex)
                );
                break;
            case MotionEvent.ACTION_MOVE: {
                int pointerCount = 0;
                while (pointerCount < motionEvent.getPointerCount()) {
                    SharedMultiTouchInput.processMouse(
                        actionMasked,
                        motionEvent.getX(pointerCount),
                        motionEvent.getY(pointerCount),
                        motionEvent.getPointerId(pointerCount)
                    );
                    pointerCount++;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                // This almost never happens... but really, I guess we should look at our active touch list and send button ups fr
                // each one before destroying the list
                listTouches.clear();
                break;
            default:
                return false;
        }
        return true;
    }

    static class TouchInfo {
        public int pointerID;
        int fingerID;
    }
}
