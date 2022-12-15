package com.rtsoft.growtopia;

import android.content.Context;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.tapjoy.TJSetUserIDListener;
import com.tapjoy.Tapjoy;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AppRenderer implements GLSurfaceView.Renderer {
    // Don't change the order of these defines, they match the ones in Proton!

    // Messages that might be sent to us from Proton's C++ side
    final static int MESSAGE_NONE = 0;
    final static int MESSAGE_OPEN_TEXT_BOX = 1;
    final static int MESSAGE_CLOSE_TEXT_BOX = 2;
    final static int MESSAGE_CHECK_CONNECTION = 3;
    final static int MESSAGE_SET_FPS_LIMIT = 4;
    final static int MESSAGE_SET_ACCELEROMETER_UPDATE_HZ = 5;
    final static int MESSAGE_FINISH_APP = 6; // Only respected by windows and android right now.  webos and iphone don't really need it
    final static int MESSAGE_SET_VIDEO_MODE = 7;

    final static int MESSAGE_TAPJOY_GET_FEATURED_APP = 8;
    final static int MESSAGE_TAPJOY_GET_AD = 9;
    final static int MESSAGE_TAPJOY_GET_MOVIE = 10;

    final static int MESSAGE_TAPJOY_SHOW_FEATURED_APP = 11;
    final static int MESSAGE_TAPJOY_SHOW_AD = 12;
    final static int MESSAGE_TAPJOY_SHOW_MOVIE_AD = 13;

    final static int MESSAGE_IAP_PURCHASE = 14;
    final static int MESSAGE_IAP_GET_PURCHASED_LIST = 15;
    final static int MESSAGE_IAP_ITEM_DETAILS = 39;

    final static int MESSAGE_TAPJOY_GET_TAP_POINTS = 16;
    final static int MESSAGE_TAPJOY_SPEND_TAP_POINTS = 17;
    final static int MESSAGE_TAPJOY_AWARD_TAP_POINTS = 18;
    final static int MESSAGE_TAPJOY_SHOW_OFFERS = 19;
    final static int MESSAGE_HOOKED_SHOW_RATE_DIALOG = 20;
    final static int MESSAGE_ALLOW_SCREEN_DIMMING = 21;
    final static int MESSAGE_REQUEST_AD_SIZE = 22;

    // CHARTBOOST STUFF
    final static int MESSAGE_CHARTBOOST_CACHE_INTERSTITIAL = 23;
    final static int MESSAGE_CHARTBOOST_SHOW_INTERSTITIAL = 24;
    final static int MESSAGE_CHARTBOOST_CACHE_MORE_APPS = 25;
    final static int MESSAGE_CHARTBOOST_SHOW_MORE_APPS = 26;
    final static int MESSAGE_CHARTBOOST_SETUP = 27;
    final static int MESSAGE_CHARTBOOST_NOTIFY_INSTALL = 28;
    final static int MESSAGE_CHARTBOOST_RESERVED1 = 29;
    final static int MESSAGE_CHARTBOOST_RESERVED2 = 30;

    // FLURRY
    final static int MESSAGE_FLURRY_SETUP = 31;
    final static int MESSAGE_FLURRY_ON_PAGE_VIEW = 32;
    final static int MESSAGE_FLURRY_LOG_EVENT = 33;

    final static int MESSAGE_SUSPEND_TO_HOME_SCREEN = 34;

    // TJ AGAIN
    final static int MESSAGE_TAPJOY_INIT_MAIN = 35;
    final static int MESSAGE_TAPJOY_INIT_PAID_APP_WITH_ACTIONID = 36;
    final static int MESSAGE_TAPJOY_SET_USERID = 37;

    // IAP again
    final static int MESSAGE_IAP_CONSUME_ITEM = 38;

    // Appsflyer logging purchase
    final static int MESSAGE_APPSFLYER_LOG_PURCHASE = 40;

    final static int MESSAGE_OPEN_TEXTBOX_SECRET = 41;

    final static int MESSAGE_FLURRY_START_TIMED_EVENT = 1001;
    final static int MESSAGE_FLURRY_STOP_TIMED_EVENT = 1002;

    // Appsflyer logging event
    static final int MESSAGE_APPSFLYER_EVENT = 1004;

    static final int MESSAGE_GETSOCIAL_EVENT = 1005;
    static final int MESSAGE_GETSOCIAL_LOGIN = 1006;
    static final int MESSAGE_GETSOCIAL_OPEN_UI = 1007;
    static final int MESSAGE_GETSOCIAL_ADD_FRIEND = 1008;
    static final int MESSAGE_GETSOCIAL_LOGOUT = 1009;

    final static int MESSAGE_TAPJOY_LOGOUT = 1010;

    final static int MESSAGE_SET_IAP_FLAG = 1011;

    static long m_gameTimer;
    static int m_timerLoopMS;
    public SharedActivity app;

    public AppRenderer(SharedActivity sharedActivity) {
        app = sharedActivity;
    }

    private static native void nativeInit();

    private static native void nativeResize(int w, int h);

    private static native void nativeSetWindow(Surface surface);

    private static native void nativeUpdate();

    private static native void nativeRender();

    private static native void nativeDone();

    private static native void nativeEmergencyMessageClear();

    private static native int nativeOSMessageGet();

    private static native int nativeGetLastOSMessageParm1();

    // Yes, I probably should do this as a Java class and init it from C++ and send that over but..

    private static native float nativeGetLastOSMessageX();

    private static native float nativeGetLastOSMessageY();

    private static native String nativeGetLastOSMessageString();

    private static native String nativeGetLastOSMessageString2();

    private static native String nativeGetLastOSMessageString3();

    public void onSurfaceCreated(final GL10 gl10, final EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        nativeSetWindow(app.mGLView.getHolder().getSurface());

        if (!SharedActivity.m_advertiserID.isEmpty()) {
            return;
        }

        Thread thread = new Thread(() -> {
            AdvertisingIdClient.Info advertisingIdInfo = null;

            try {
                advertisingIdInfo =
                    AdvertisingIdClient.getAdvertisingIdInfo((Context) SharedActivity.app);
            } catch (IOException ex) {
                Log.d(SharedActivity.PackageName, "Getting AID: IOException");
            } catch (GooglePlayServicesRepairableException ex2) {
                Log.d(SharedActivity.PackageName, "GooglePlayServicesRepairableException");
            } catch (GooglePlayServicesNotAvailableException ex3) {
                Log.d(
                    SharedActivity.PackageName,
                    "Google Play services is not available entirely."
                );
            } catch (IllegalStateException ex4) {
                Log.d(
                    SharedActivity.PackageName,
                    "IllegalStateException: Unrecoverable error connecting to Google Play services"
                );
            }

            if (advertisingIdInfo != null) {
                SharedActivity.m_advertiserID = advertisingIdInfo.getId();
                SharedActivity.m_limitAdTracking = advertisingIdInfo.isLimitAdTrackingEnabled();

                String stringBuilder = "------------ Got A-ID: " +
                    SharedActivity.m_advertiserID +
                    " Tracking: " +
                    SharedActivity.m_limitAdTracking;

                Log.d(SharedActivity.PackageName, stringBuilder);
            } else {
                SharedActivity.m_advertiserID = "";
                Log.d(SharedActivity.PackageName, "---------- Unable to get A-ID info");
            }
        });

        thread.start();
    }

    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        GLES20.glViewport(0, 0, w, h);
        nativeResize(w, h);
        nativeSetWindow(app.mGLView.getHolder().getSurface());
    }

    public synchronized void onDrawFrame(final GL10 gl10) {
        if (app == null) {
            return;
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (m_timerLoopMS != 0) {
            while (m_gameTimer > SystemClock.uptimeMillis()) {
                // Wait a bit - no exception catch needed for the SystemClock version of sleep
                SystemClock.sleep(1L);
            }

            while (m_gameTimer > SystemClock.uptimeMillis() + m_timerLoopMS + 1L) {
                // Wait a bit - no exception catch needed for the SystemClock version of sleep
                SystemClock.sleep(1L);
            }

            m_gameTimer = SystemClock.uptimeMillis() + m_timerLoopMS;
        }

        if (!SharedActivity.bIsShuttingDown && Looper.myLooper() != Looper.getMainLooper()) {
            nativeUpdate(); // Maybe later we'll want to adjust this for performance reasons..
            nativeRender();
        }

        // Let's process OS messages sent from the app if any exist
        int type = MESSAGE_NONE;

        while (!SharedActivity.bIsShuttingDown) { // It returns 0 if none is available
            type = nativeOSMessageGet();
            if (type == 0) {
                break;
            }

            switch (type) {
                case MESSAGE_OPEN_TEXTBOX_SECRET:
                case MESSAGE_OPEN_TEXT_BOX: { // Open text box
                    Log.d(
                        SharedActivity.PackageName,
                        "keyboard " +
                            (
                                type == MESSAGE_OPEN_TEXTBOX_SECRET
                                ? "MESSAGE_OPEN_TEXTBOX_SECRET"
                                : "MESSAGE_OPEN_TEXT_BOX"
                            )
                    );

                    SharedActivity.passwordField = type == MESSAGE_OPEN_TEXTBOX_SECRET;
                    SharedActivity.m_text_max_length = nativeGetLastOSMessageParm1();
                    SharedActivity.m_text_default = nativeGetLastOSMessageString();
                    SharedActivity.m_before = nativeGetLastOSMessageString();
                    SharedActivity.updateText = true;

                    app.clearIngameInputBox();
                    app.ChangeEditBoxProperty();

                    SharedActivity.updateText = false;

                    app.toggle_keyboard(true);
                    app.mMainThreadHandler.post(app.mUpdateMainThread);
                    break;
                }
                case MESSAGE_CLOSE_TEXT_BOX: { // Close text box
                    Log.d(SharedActivity.PackageName, "keyboard MESSAGE_CLOSE_TEXT_BOX");
                    app.toggle_keyboard(false);
                    app.mMainThreadHandler.post(app.mUpdateMainThread);
                    break;
                }
                case MESSAGE_SET_ACCELEROMETER_UPDATE_HZ:
                    app.setup_accel(nativeGetLastOSMessageX());
                    break;
                case MESSAGE_ALLOW_SCREEN_DIMMING: {
                    if (nativeGetLastOSMessageX() == 0.0f) {
                        // Disable screen dimming
                        SharedActivity.set_disallow_dimming_asap = true; // Must do it in the UI thread
                        app.mMainThreadHandler.post(app.mUpdateMainThread);
                        break;
                    }

                    Log.v(SharedActivity.PackageName, "Allowing screen dimming.");
                    SharedActivity.set_allow_dimming_asap = true; // Must do it in the UI thread
                    app.mMainThreadHandler.post(app.mUpdateMainThread);
                    break;
                }
                case MESSAGE_SET_FPS_LIMIT: {
                    if (nativeGetLastOSMessageX() == 0.0f) {
                        // Disable it, and avoid a div by 0
                        m_timerLoopMS = 0;
                        break;
                    }

                    m_timerLoopMS = (int) (1000.0f / nativeGetLastOSMessageX());
                    break;
                }
                case MESSAGE_FINISH_APP: {
                    if (SharedActivity.m_canShowCustomKeyboard) {
                        break;
                    }

                    Log.v(SharedActivity.PackageName, "Finishing app from java side");
                    SharedActivity.bIsShuttingDown = true;
                    nativeDone();
                    Log.v(SharedActivity.PackageName, "Native shutdown");

                    // app.finish() will get called in the update handler called below, don't need to do it now
                    app.mMainThreadHandler.post(app.mUpdateMainThread);
                    break;
                }
                case MESSAGE_SUSPEND_TO_HOME_SCREEN: {
                    Log.v(SharedActivity.PackageName, "Suspending to home screen");

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);

                    app.startActivity(intent);
                    break;
                }
                case MESSAGE_TAPJOY_GET_AD:
                    Log.v(SharedActivity.PackageName, "banner ads no longer supported in TJ 10");
                    break;
                case MESSAGE_FLURRY_SETUP:
                    Log.v(
                        SharedActivity.PackageName,
                        "ERROR: RT_FLURRY_SUPPORT isn't defined in Main.java, you can't use it!"
                    );
                    break;
                case MESSAGE_CHARTBOOST_SETUP:
                    Log.v(
                        SharedActivity.PackageName,
                        "ERROR: RT_CHARTBOOST_SUPPORT isn't defined in Main.java, you can't use it!"
                    );
                    break;
                case MESSAGE_TAPJOY_INIT_MAIN: {
                    try {
                        if (!Tapjoy.isConnected()) {
                            Log.d(
                                "TAPJOY. ",
                                "MESSAGE_TAPJOY_INIT_MAIN, Tapjoy has not been initialized."
                            );

                            app.onConnectToTapjoy(nativeGetLastOSMessageString());
                        } else {
                            Log.d(
                                "TAPJOY. ",
                                "MESSAGE_TAPJOY_INIT_MAIN, Tapjoy has been initialized."
                            );

                            Tapjoy.startSession();
                        }
                    } catch (Exception ex) {
                        Log.e("TAPJOY. ", "MESSAGE_TAPJOY_INIT_MAIN failed: " + ex.getMessage());
                    }

                    break;
                }
                case MESSAGE_TAPJOY_SET_USERID: {
                    Log.v(SharedActivity.PackageName, "Setting userID: " + nativeGetLastOSMessageString());

                    Tapjoy.setUserID(nativeGetLastOSMessageString(), new TJSetUserIDListener() {
                        @Override
                        public void onSetUserIDSuccess() {
                            Log.v(SharedActivity.PackageName, "Setting userID success");
                        }

                        @Override
                        public void onSetUserIDFailure(String s) {
                            Log.v(SharedActivity.PackageName, "Setting userID failed");
                        }
                    });

                    app.requestPlacement("Sub_01");
                    app.requestPlacement("GROW_GGP_V4VC_TV");
                    app.requestOfferwall("Grow_Store_Placement_01");
                    break;
                }
                case MESSAGE_TAPJOY_GET_FEATURED_APP: {
                    // Re-purposing to show videos
                    final String lastOSMessageString = nativeGetLastOSMessageString();

                    Log.v(SharedActivity.PackageName, "Asking tj for fullscreen ad");
                    Log.v(
                        SharedActivity.PackageName,
                        "MESSAGE_TAPJOY_GET_FEATURED_APP: " + lastOSMessageString
                    );

                    if (lastOSMessageString.length() > 0 && app.tapjoyAdPlacementForSub01 != null && lastOSMessageString.equals("Sub_01")) {
                        if (app.tapjoyAdPlacementForSub01.isContentReady()) {
                            app.tapjoyAdPlacementForSub01.showContent();
                            break;
                        }

                        app.requestPlacementAndShow("Sub_01");
                    } else {
                        if (lastOSMessageString.length() == 0 || app.tapjoyAdPlacementForTV == null || !lastOSMessageString.equals("GROW_GGP_V4VC_TV")) {
                            Log.e(SharedActivity.PackageName, "Tapjoy Plancement name not passed");
                            break;
                        }

                        if (app.tapjoyAdPlacementForTV.isContentReady()) {
                            app.tapjoyAdPlacementForTV.showContent();
                            break;
                        }

                        app.requestPlacementAndShow("GROW_GGP_V4VC_TV");
                    }
                    break;
                }
                case MESSAGE_TAPJOY_SHOW_OFFERS: {
                    if (app.offerwallPlacement == null) {
                        break;
                    }

                    if (app.offerwallPlacement.isContentReady()) {
                        app.offerwallPlacement.showContent();
                        app.requestOfferwall(nativeGetLastOSMessageString());
                        break;
                    }

                    app.requestOfferwallAndShow(nativeGetLastOSMessageString());
                    break;
                }
                case MESSAGE_TAPJOY_SHOW_AD: {
                    SharedActivity.tapjoy_ad_show = (int) nativeGetLastOSMessageX();
                    Log.v(
                        SharedActivity.PackageName,
                        "Tapjoy banner ads no longer supported in SDK 10, parm is: " + SharedActivity.tapjoy_ad_show
                    );
                    if (app.tapjoyAdPlacementForSub01 != null) {
                        app.tapjoyAdPlacementForSub01.showContent();
                    }

                    app.mMainThreadHandler.post(app.mUpdateMainThread);
                    break;
                }
                case MESSAGE_REQUEST_AD_SIZE: {
                    SharedActivity.adBannerWidth = (int) nativeGetLastOSMessageX();
                    SharedActivity.adBannerHeight = (int) nativeGetLastOSMessageY();

                    SharedActivity.adBannerWidth = 480;
                    SharedActivity.adBannerHeight = 72;

                    SharedActivity.tapBannerSize = SharedActivity.adBannerWidth + "x" + SharedActivity.adBannerHeight;
                    Log.v(
                        SharedActivity.PackageName,
                        "Setting tapjoy banner size to " + SharedActivity.tapBannerSize
                    );
                    break;
                }
                case MESSAGE_IAP_PURCHASE: {
                    if (app.iapManager == null) {
                        Log.d(
                            SharedActivity.PackageName,
                            "requestPurchase>> Um, you'll need to change IAPEnabled to true in Main.java!"
                        );
                    }

                    app.iapManager.IAPPurchase(nativeGetLastOSMessageString());
                    break;
                }
                case MESSAGE_IAP_GET_PURCHASED_LIST: {
                    if (app.iapManager == null) {
                        Log.d(
                            SharedActivity.PackageName,
                            "requestPurchase>> Um, you'll need to change IAPEnabled to true in Main.java!"
                        );
                    }

                    app.iapManager.RequestAIPPurchasedList();
                    break;
                }
                case MESSAGE_IAP_CONSUME_ITEM: {
                    Log.d(SharedActivity.PackageName, "Consume");
                    if (app.iapManager == null) {
                        Log.d(
                            SharedActivity.PackageName,
                            "requestPurchase>> Um, you'll need to change IAPEnabled to true in Main.java!"
                        );
                    }

                    app.iapManager.ConsumeItem(nativeGetLastOSMessageString());
                    break;
                }
                case MESSAGE_IAP_ITEM_DETAILS: {
                    if (app.iapManager == null) {
                        Log.d(
                            SharedActivity.PackageName,
                            "requestPurchase>> Um, you'll need to change IAPEnabled to true in Main.java!"
                        );
                    }

                    app.iapManager.RequestItemDetails(nativeGetLastOSMessageString());
                    break;
                }
                case MESSAGE_HOOKED_SHOW_RATE_DIALOG: {
                    Log.v(SharedActivity.PackageName, "Launching hooked");
                    SharedActivity.run_hooked = true;
                    app.mMainThreadHandler.post(app.mUpdateMainThread);
                    break;
                }
                case MESSAGE_APPSFLYER_LOG_PURCHASE: {
                    try {
                        app.onApplsFlyerLogPurchase(
                            nativeGetLastOSMessageString(),
                            nativeGetLastOSMessageString2(),
                            nativeGetLastOSMessageString3()
                        );
                    } catch (Exception ex) {
                        Log.d("Appsflyer", "Tracking failed : " + ex.getMessage());
                    }

                    break;
                }
                case MESSAGE_APPSFLYER_EVENT: {
                    try {
                        app.onApplsFlyerLogEvent(
                            nativeGetLastOSMessageString(),
                            nativeGetLastOSMessageString2()
                        );
                    } catch (Exception ex) {
                        Log.d("Appsflyer", "Tracking failed : " + ex.getMessage());
                    }

                    break;
                }
                case MESSAGE_TAPJOY_LOGOUT: {
                    Log.d("TAPJOY. ", "MESSAGE_TAPJOY_LOGOUT, Do endSession.");
                    Tapjoy.endSession();
                    break;
                }
                default: {
                    Log.v("Unhandled", "Unhandled OS message");
                    nativeEmergencyMessageClear();
                    break;
                }
            }
        }
    }
}
