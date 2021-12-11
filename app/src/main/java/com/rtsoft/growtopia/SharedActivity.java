package com.rtsoft.growtopia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.anzu.sdk.Anzu;
import com.gt.launcher.FloatingService;
import com.gt.launcher.R;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJError;
import com.tapjoy.TJGetCurrencyBalanceListener;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementVideoListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class SharedActivity extends Activity implements SensorEventListener, TJGetCurrencyBalanceListener, TJPlacementVideoListener {
    //********** THESE WILL BE OVERRIDDEN IN YOUR Main.java file **************
    public static String PackageName = "com.rtsoft.something";
    public static String dllname = "rtsomething";
    public static boolean securityEnabled = false; // If false, it won't try to use the online license stuff
    public static boolean bIsShuttingDown = false;
    public static boolean IAPEnabled = false; // If false, IAB won't be initted.  I call it IAP because I'm used to it from iOS land

    public static String tapBannerSize = ""; // Tapjoy banner size text, set in Main.cpp, or by AdManager calls
    public static int adBannerWidth = 0;
    public static int adBannerHeight = 0;

    public static String m_advertiserID = ""; // Only will be set it Google Services is added to the project.   This is a big hassle for Proton projects,
    // I ended up just copying over the res and jar manually because android.library.reference.1= in project.properties didn't seem to let the manifest access the .res and.. argh.
    public static boolean m_limitAdTracking = false;

    public static boolean m_focusOnKeyboard = false;
    public static boolean m_focusOffKeyboard = false;
    public static boolean m_canShowCustomKeyboard = true;

    public static boolean HookedEnabled = false;
    //************************************************************************
    static final int RC_REQUEST = 10001;

    public static SharedActivity app = null; // A global to use in static functions with JNI

    // For the accelerometer
    private static float accelHzSave = 0;
    private static Sensor sensor;
    private static SensorManager sensorManager;
    private static float m_lastMusicVol = 1;
    public static int apiVersion;

    // TAPJOY
    public static View adView;
    public static RelativeLayout adLinearLayout;
    public static EditText m_editText;
    public static String m_before = "";
    public static int m_text_max_length = 20;
    public static String m_text_default = "";
    public static boolean update_display_ad;
    public static boolean run_hooked;
    public static int tapjoy_ad_show; // 0 for don't shot, 1 for show
    public TJPlacement offerwallPlacement;
    public PurchasesUpdatedListener purchaseUpdateListener;
    public ConcurrentHashMap<String, com.android.billingclient.api.Purchase> purchasedList = new ConcurrentHashMap<>();
    public TJPlacement tapjoyAdPlacementForSub01;
    public TJPlacement tapjoyAdPlacementForTV;
    private ProgressDialog nDialog;
    private ProgressDialog oDialog;

    public static boolean set_allow_dimming_asap = false;
    public static boolean set_disallow_dimming_asap = false;

    // GOOGLE IAB
    public BillingClient billingClient;

    ////////////////////////////////////////////////////////////////////////////
    // Licensing Server code
    ////////////////////////////////////////////////////////////////////////////
    public boolean is_demo = false;
    public String BASE64_PUBLIC_KEY = "this will be set in your app's Main.java";

    // 20 random bytes. You can override these in your own Main.java
    public byte[] SALT = new byte[]{
            24, -96, 16, 91, 65, -86, -54, -73, -101, 12, -84, -90, -53, -68, 20, -67, 45, 35, 85, 17
    };

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        private MyLicenseCheckerCallback() {
            super();
        }

        @Override
        public void allow(int reason) {
            Log.v("allow()", "Allow the user access");
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            // Should allow user access.
            // displayResult(getString(R.string.allow));
        }

        @Override
        public void dontAllow(int reason) {
            Log.v("dontAllow()", "Don't allow the user access");
            is_demo = true;
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }

            // In this example, we show a dialog that takes the user to Market.
            showDialog(0);
        }

        @Override
        public void applicationError(int applicationErrorCode) {
            Log.v("applicationError", String.format("Application error: %1$s", applicationErrorCode));
            dontAllow(applicationErrorCode);
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        // We have only one dialog.
        return new AlertDialog.Builder(this)
                .setTitle("Application not licensed")
                .setMessage("This application is not licensed.  Please purchase it from Android Market.\n\nTip: if you have purchased this application, press Retry a few times.  It may take a minute to connect to the licensing server.  If that does not work, try rebooting your phone.")
                .setPositiveButton("Buy app", (dialogInterface, i13) -> {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://market.android.com/details?id=" + this.getPackageName())));
                    finish();
                    Process.killProcess(Process.myPid());
                }).setNegativeButton("Exit", (dialogInterface, i1) -> {
                    finish();
                    Process.killProcess(Process.myPid());
                }).setNeutralButton("Retry", (dialogInterface, i12) -> {
                    is_demo = false;
                    doCheck();
                }).create();
    }

    private void doCheck() {
        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    @SuppressLint("HardwareIds")
    private void license_init() {
        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
                this,
                new ServerManagedPolicy(this, new AESObfuscator(this.SALT, getPackageName(), deviceId)),
                // new StrictPolicy(),
                BASE64_PUBLIC_KEY);
        doCheck();
    }

    ////////////////////////////////////////////////////////////////////////////

    public final Handler mMainThreadHandler = new Handler();

    protected void onDestroy() {
        Log.d(PackageName, "Destroying...");
        super.onDestroy();
        Log.d(PackageName, "Destroying helper.");
    }

    protected void onStart() {
        super.onStart();
        Tapjoy.onActivityStart(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionActivity.mainActivity = this;
            startActivity(new Intent(getApplicationContext(), PermissionActivity.class));
        }
    }

    protected void onStop() {
        super.onStop();
        Tapjoy.onActivityStop(this);
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    void alert(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(str);
        builder.setNeutralButton("OK", null);
        Log.d(PackageName, "Showing alert dialog: " + str);
        builder.create().show();
    }

    void complain(String str) {
        Log.e(PackageName, "Initialization error: " + str);
        alert("Error: " + str);
    }

    protected void onCreate(Bundle savedInstanceState) {
        app = this;
        nativeInitActivity(app);

        apiVersion = Build.VERSION.SDK_INT;
        Log.d(PackageName, "***********************************************************************");
        Log.d(PackageName, "API Level: " + apiVersion);

        super.onCreate(savedInstanceState);

        mGLView = new AppGLSurfaceView(this, this);
        mViewGroup = new RelativeLayout(this);
        mViewGroup.setLayoutParams(new ViewGroup.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mGLView.setLayoutParams(layoutParams);

        mViewGroup.addView(mGLView);
        setContentView(mViewGroup);

        CreateEditBox();
        AddEditBoxListeners();
        mGLView.requestFocus();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (securityEnabled) {
            license_init();
        }

        // Create dummy tapjoy view overlay we'll show ads on
        adLinearLayout = new RelativeLayout(this);
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        Log.d(PackageName, "Tapjoy enabled - setting up adview overlay");
        addContentView(adLinearLayout, layoutParams);

        Log.d(PackageName, "Setting IAB...");

        update_display_ad = false;
        run_hooked = false;
        tapjoy_ad_show = 0;
        if (IAPEnabled) {
            purchaseUpdateListener = (billingResult, list) -> {
                int responseCode = billingResult.getResponseCode();
                if (responseCode == 0 && list != null) {
                    for (Purchase purchase : list) {
                        if (purchase.getPurchaseState() == 1) {
                            nativeSendGUIStringEx(
                                    MESSAGE_TYPE_IAP_RESULT,
                                    responseCode,
                                    0,
                                    0,
                                    purchase.getOriginalJson() + "|" + purchase.getSignature());
                        } else if (purchase.getPurchaseState() == 2) {
                            nativeSendGUIStringEx(
                                    MESSAGE_TYPE_IAP_RESULT,
                                    responseCode,
                                    5,
                                    0,
                                    purchase.getOriginalJson() + "|" + purchase.getSignature());
                        }
                    }
                } else {
                    if (responseCode == 7) {
                        Purchase.PurchasesResult queryPurchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                        if (queryPurchases == null) {
                            nativeSendGUIEx(
                                    MESSAGE_TYPE_IAP_PURCHASED_LIST_STATE,
                                    -1,
                                    0,
                                    0);
                            return;
                        }

                        if (queryPurchases.getPurchasesList() == null || queryPurchases.getPurchasesList().size() == 0) {
                            nativeSendGUIEx(
                                    MESSAGE_TYPE_IAP_PURCHASED_LIST_STATE,
                                    -1,
                                    0,
                                    0);
                        } else {
                            for (com.android.billingclient.api.Purchase purchase : queryPurchases.getPurchasesList()) {
                                if (purchase.getPurchaseState() == 1) {
                                    nativeSendGUIStringEx(
                                            MESSAGE_TYPE_IAP_RESULT,
                                            responseCode,
                                            0,
                                            0,
                                            purchase.getOriginalJson() + "|" + purchase.getSignature());
                                }
                            }
                        }
                    } else {
                        nativeSendGUIEx(MESSAGE_TYPE_IAP_RESULT, responseCode, 0, 0);
                    }
                }
            };

            billingClient = BillingClient.newBuilder(this)
                    .setListener(purchaseUpdateListener)
                    .enablePendingPurchases()
                    .build();

            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {
                    /* ~ */
                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    billingResult.getResponseCode();
                }
            });
        }

        Anzu.SetContext(this);
        sendVersionDetails();
    }

    public boolean isInFloatingMode = false;

    protected synchronized void onPause() {
        Log.d(PackageName, "onPause...");
        if (!isInFloatingMode) {
            InputMethodManager inputMethodManager = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mGLView != null) {
                inputMethodManager.hideSoftInputFromWindow(mGLView.getWindowToken(), 0);
            }

            if (m_editText != null) {
                m_editText.setText("");
                inputMethodManager.hideSoftInputFromWindow(m_editText.getWindowToken(), 0);
            }

            UpdateEditBoxInView(false, false);
        }

        float hzTemp = accelHzSave;
        setup_accel(0.0f);
        accelHzSave = hzTemp;
        if (!isInFloatingMode) {
            mGLView.onPause();
        }
        super.onPause();
    }

    protected synchronized void onResume() {
        music_set_volume(m_lastMusicVol);
        if (!isInFloatingMode) {
            mGLView.onResume();
        }
        setup_accel(accelHzSave);
        super.onResume();
    }

    public final Runnable mUpdateMainThread = () -> {
        if (bIsShuttingDown) {
            finish();
            Process.killProcess(Process.myPid());
            return;
        }

        updateResultsInUi();
    };

    private void updateResultsInUi() {
        if (set_allow_dimming_asap) {
            set_allow_dimming_asap = false;
            Log.d(PackageName, "Allowing screen dimming.");
            mGLView.setKeepScreenOn(false);
        }

        if (set_disallow_dimming_asap) {
            set_allow_dimming_asap = false;
            Log.d(PackageName, "Disabling screen dimming.");
            mGLView.setKeepScreenOn(true);
        }

        if (m_focusOnKeyboard) {
            m_focusOnKeyboard = false;
            if (m_editText != null) {
                m_editText.setText(m_text_default);
                m_editText.setSelection(m_editText.getText().length());
                m_editText.requestFocus();
            }
        }

        if (m_focusOffKeyboard) {
            Log.d(PackageName, "Removing edittextView m_focusOffKeyboard");

            m_canShowCustomKeyboard = false;
            m_focusOffKeyboard = false;
            if (m_editText != null) {
                mGLView.requestFocus();
            }

            Log.d(PackageName, "Removing edittextView m_focusOffKeyboard");
        }

        if (run_hooked && HookedEnabled) {
            Log.d(PackageName, "Lauching Hooked (wasabi) dialog");
            run_hooked = false;
        }

        if (update_display_ad) {
            Log.d(PackageName, "Updating view in main  thread");
            update_display_ad = false;
            adLinearLayout.removeAllViews();
            if (tapjoy_ad_show == 1) {
                adLinearLayout.addView(adView);
            }
        }
    }

    public void makeToastUI(String text) {
        runOnUiThread(() -> {
            Toast makeText = Toast.makeText(app.getApplicationContext(), text, Toast.LENGTH_LONG);
            makeText.setGravity(Gravity.CENTER, 0, 0);
            makeText.show();
        });
    }

    // JNI used to get Save data dir
    public static String get_docdir() {
        return app.getExternalFilesDir(null).getAbsolutePath();
    }

    public static String get_externaldir() {
        // Directory of external storage
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
        else if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        }
        else {
            // mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (!mExternalStorageWriteable) {
            return "";
        }

        // Alternatave GetBundlePrefix & GetBundleName.
        return "";
        // return Environment.getExternalStorageDirectory().toString();
    }

    // JNI used to get Save data dir
    public static String get_apkFileName() {
        try {
            return app.getPackageManager().getApplicationInfo(PackageName, 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to locate assets, aborting...");
        }
    }

    public static String get_region() {
        // Will return region in the format "en_us"
        Locale locale = Locale.getDefault();
        return (locale.getLanguage() + "_" + locale.getCountry()).toLowerCase();
    }

    public static String get_clipboard() {
        // Note: On Honeycomb this appears to cause a crash because it has to be done in the main thread, which isn't active when
        // JNI invokes this.  So we have to do a callback and send back the answer later? Argh.  For now, I'll try/catch the crash, it
        // will just be a no-op.

        try {
            ClipboardManager clipboardManager = (ClipboardManager) app.getSystemService(CLIPBOARD_SERVICE);
            return clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
        } catch (Exception e) {
            Log.d(PackageName, "get_clipboard> Avoided crash. " + e);
            return "Thread error, sorry, paste can't be used here.";
        }
    }

    @SuppressLint({"WrongConstant", "MissingPermission", "HardwareIds"})
    public static String get_deviceID() {
        String m_szDevIDShort = "35" + // We make this look like a valid IMEI
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; // 13 digits

        if (app.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
            final String DeviceId, SerialNum;
            DeviceId = tm.getDeviceId();
            SerialNum = tm.getSimSerialNumber();
            return m_szDevIDShort + DeviceId + SerialNum;
        } else {
            return m_szDevIDShort;
        }
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String get_macAddress() {
        WifiManager wimanager = (WifiManager) app.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        return macAddress == null ? "" : macAddress;
    }

    public static String get_language() {
        return Locale.getDefault().getLanguage().toLowerCase();
    }

    public static String get_device_model() {
        String str = Build.MODEL;
        Log.d("get_device_model", str);
        return str;
    }

    public static String get_device_os() {
        String str = Build.VERSION.RELEASE;
        Log.d("get_device_os", str);
        return str;
    }

    public static int is_app_installed(String str) {
        try {
            app.getPackageManager().getApplicationInfo(str, 0);
            return 1;
        }
        catch (PackageManager.NameNotFoundException unused) {
            return 0;
        }
    }

    private static boolean hasSuperuserApk() {
        return new File("/system/app/Superuser.apk").exists();
    }

    private static int isTestKeyBuild() {
        String str = Build.TAGS;
        if ((str != null) && (str.contains("test-keys")));
        for (int i = 1; ; i = 0) {
            return i;
        }
    }

    public static String get_advertisingIdentifier() {
        return m_advertiserID;
    }

    public static String get_cantSupportTrees() {
        return (
                hasSuperuserApk() ||
                is_app_installed("com.noshufou.android.su") == 1 ||
                is_app_installed("com.thirdparty.superuser") == 1 ||
                is_app_installed("eu.chainfire.supersu") == 1 ||
                is_app_installed("com.koushikdutta.superuser") == 1 ||
                is_app_installed("com.zachspong.temprootremovejb") == 1 ||
                is_app_installed("com.ramdroid.appquarantine") == 1 ||
                is_app_installed("cyanogenmod.superuser") == 1 ||
                is_app_installed("com.devadvance.rootcloakplus") == 1
        ) ? "0" : "4322";
    }

    public static String get_getNetworkType() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            try {
                NetworkInfo activeNetworkInfo = ((ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if (activeNetworkInfo != null) {
                    if (activeNetworkInfo.getType() == 1) {
                        // Wifi is connected
                        return "wifi";
                    }
                    if (activeNetworkInfo.getType() == 0) {
                        // Mobile connection available
                        return "mobile";
                    }
                }

                // No connection available
                return "none";
            }
            catch (Exception e) {
                // No connection available
                Log.d("DeviceNetwork", e.getMessage());
                return "none";
            }
        }
        else {
            ConnectivityManager connManager = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                if (connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                    // Wifi is connected
                    return "wifi";
                }
                else if(connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
                    // Mobile connection available
                    return "mobile";
                }
                else {
                    // No connection available
                    return "none";
                }
            }
            catch (Exception e2) {
                // No connection available
                Log.d("DeviceNetwork", e2.getMessage());
                return "none";
            }
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (event.values.length < 3) {
                return; // Who knows what this is
            }

            nativeOnAccelerometerUpdate(event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* ~ */
    }

    public void setup_accel(float hz) { // 0 to disable
        accelHzSave = hz;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
        if (hz > 0.0f) {
            sensorManager.registerListener(app, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    // Achievement handler (called from C++ class, and the actual handler is overriden in Main.java to do the actual app specific API call
    public void FireAchievement(String achievement) {
        Log.v("Achievement", "Firing in Wrong instance");
    }

    // JNI to talk to Kiip
    public static void HandleAchievement(String achievement) {
        Log.v("Achievement", "Unlocked value: " + achievement);
        app.FireAchievement(achievement);
    }

    /**
     * The listener that listen to events from the accelerometer listener
     */

    // JNI to open_url
    public static void LaunchURL(String url) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(url));
        try {
            app.startActivity(intent);
        }
        catch (ActivityNotFoundException unused) {
            Log.v("LaunchURL", "Couldn't find activity to launch URL!");
        }
    }

    public static void create_dir_recursively(String basepath, String path) {
        new File(basepath + path).mkdirs();
    }

    public static boolean isKeyboardExist = false;
    public static int m_KeyBoardHeight = 0;
    public static Button m_CancelButton = null;
    public static Button m_DoneButton = null;
    public static RelativeLayout m_editTextRoot = null;
    public static int maxLength = -1;
    public static int tempNum = 0;
    public static boolean updateText = false;
    public static boolean usingGoogleBilling = false;
    public static boolean passwordField = false;

    private void CreateEditBox() {
        m_editText = new EditText(this);
        m_editText.setText("");
        m_editText.setSelection(m_editText.getText().length());
        m_editText.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_FORCE_ASCII);
        m_editText.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE);
        m_editText.setInputType(524433); // TODO: Change this.
        m_editText.setGravity(Gravity.BOTTOM);
        m_editText.setMaxLines(3);
        m_editText.setBackgroundColor(-1);
        m_editText.setTextColor(ViewCompat.MEASURED_STATE_MASK);
        m_editText.setTextIsSelectable(true);

        RegisterLayoutChangeCallback();
        CreateEditBoxBG();
        UpdateEditBoxInView(false, true);
    }

    private void CreateEditBoxBG() {
        m_editTextRoot = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                m_editText.getMeasuredHeight());
        layoutParams.addRule(12);
        layoutParams.setMargins(0, 0, 0, m_KeyBoardHeight);
        m_editTextRoot.setLayoutParams(layoutParams);
        m_editTextRoot.setBackgroundColor(Color.parseColor("#e5e5e7"));

        m_DoneButton = new Button(this);
        m_DoneButton.setOnClickListener(view -> {
            ((InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mGLView.getWindowToken(), 0);
            nativeOnKey(1, 13, 13);
            Log.d(PackageName, "Done button pressed.");
            mGLView.requestFocus();
        });

        m_CancelButton = new Button(this);
        m_CancelButton.setOnClickListener(view -> {
            nativeCancelBtnPressed();
            toggle_keyboard(false);
        });

        mViewGroup.addView(m_editTextRoot);

        m_editTextRoot.addView(m_editText);
        m_editTextRoot.addView(m_DoneButton);
        m_editTextRoot.addView(m_CancelButton);

        m_editText.measure(0, 0);
    }

    private void RemoveEditBoxBG() {
        if (m_editTextRoot != null && m_editTextRoot.getParent() != null) {
            ((ViewGroup) m_editTextRoot.getParent()).removeView(m_editTextRoot);
        }

        Button doneButton = m_DoneButton;
        if (doneButton != null && doneButton.getParent() != null) {
            ((ViewGroup) doneButton.getParent()).removeView(m_DoneButton);
        }

        Button cancelButton = m_CancelButton;
        if (cancelButton != null && cancelButton.getParent() != null) {
            ((ViewGroup) cancelButton.getParent()).removeView(m_DoneButton);
        }
    }

    public void ChangeEditBoxProperty() {
        runOnUiThread(() -> {
            if (passwordField) {
                m_editText.setInputType(524417); // TODO: Change this.
                m_editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
                return;
            }

            m_editText.setInputType(524433); // TODO: Change this.
            m_editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10000000)});
        });
    }

    private void UpdateEditBoxInView(boolean showEditTextBox, boolean unk) {
        setViewVisibility(m_editTextRoot, showEditTextBox);

        if (showEditTextBox) {
            m_editText.setText(m_text_default);
            m_editText.setSelection(m_editText.getText().length());
            Log.d("NIRMAN", "UpdateEditBoxInView Enabling EditBox. ");
            maxLength = -1;
            UpdateRelativeElementsPosition();
            m_editText.setFocusableInTouchMode(true);
            m_editText.requestFocus();
            return;
        }

        if (unk) {
            m_editText.setText("");
            m_editText.setSelection(m_editText.getText().length());
            Log.d("NIRMAN", "UpdateEditBoxInView Disabling EditBox. ");
        }

        m_editText.setFocusable(false);
    }

    private void AddEditBoxListeners() {
        m_editText.setOnFocusChangeListener((view, z) -> {
            /* ~ */
        });

        try {
            m_editText.setOnKeyListener((view, keyCode, keyEvent) -> {
                if (keyEvent.getAction() != 0 || keyCode != KeyEvent.KEYCODE_ENTER) {
                    return false;
                }

                isKeyboardExist = false;
                Log.d(PackageName, "Removing edittextView  setOnKeyListener ");
                nativeOnKey(1, 0, 13);
                nativeOnKey(0, 0, 13);
                m_editText.setText("");
                m_editText.setSelection(m_editText.getText().length());
                return true;
            });
        }
        catch (NoClassDefFoundError e) {
            Log.d(PackageName, "setOnEditorActionListener(> Avoided crash. " + e);
        }

        try {
            m_editText.setOnEditorActionListener((textView, keyCode, keyEvent) -> {
                if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_ENDCALL) {
                    ((InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mGLView.getWindowToken(), 0);
                    Log.d(PackageName, "editor action says we're done editing text");
                    nativeOnKey(1, 13, 13);
                    Log.d(PackageName, "Removing edittextView setOnEditorActionListener");
                    mGLView.requestFocus();
                    return true;
                }

                Log.d(PackageName, "Removing edittextView setOnEditorActionListener2");
                return false;
            });
        }
        catch (NoClassDefFoundError e) {
            Log.d(PackageName, "setOnEditorActionListener(> Avoided crash. " + e);
        }

        m_editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* ~ */
            }

            public void afterTextChanged(Editable editable) {
                Log.d(PackageName, "afterTextChanged: onTextChanged  String: " + editable);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!updateText) {
                    maxLength = nativeGetChatString();
                    if (maxLength != -1) {
                        if (s.length() - m_before.length() < 0 && maxLength == 120) {
                            maxLength--;
                        }

                        if (!isAcceptableTextLength(s.length())) {
                            return;
                        }
                    }

                    for (int i = 0; i < m_before.length(); i++) {
                        nativeOnKey(1, 67, 0);
                    }

                    for (int i = 0; i < s.length(); i++) {
                        char charAt = s.charAt(i);
                        nativeOnKey(1, 0, charAt);
                        nativeOnKey(0, 0, charAt);
                    }

                    m_before = s.toString();
                }
            }
        });
    }

    private void UpdateEditBoxRootViewPosition() {
        m_editText.measure(0, 0);

        int measuredHeight = m_editText.getMeasuredHeight();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                measuredHeight);
        layoutParams.addRule(12);
        layoutParams.setMargins(0, 0, 0, m_KeyBoardHeight);
        m_editTextRoot.setLayoutParams(layoutParams);

        if (Looper.myLooper() != Looper.getMainLooper()) {
            nativeUpdateConsoleLogPos((float) (m_KeyBoardHeight + measuredHeight));
        }
    }

    private void UpdateRelativeElementsPosition() {
        m_editText.measure(0, 0);

        float nativeGetScreenWidth = (float) ((int) nativeGetScreenWidth());
        int measuredHeight = m_editText.getMeasuredHeight();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) (0.7f * nativeGetScreenWidth),
                m_editText.getMeasuredHeight());
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.setMargins((int) nativeGetEditBoxOffset(), 0, 0, 0);
        m_editText.setLayoutParams(layoutParams);
        m_editText.setSelection(m_editText.getText().length());

       layoutParams = new RelativeLayout.LayoutParams(
                (int) (nativeGetScreenWidth * 0.12f),
                measuredHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(0, 0, (int) (nativeGetScreenWidth * 0.12f), 0);
        m_DoneButton.setLayoutParams(layoutParams);
        m_DoneButton.setBackgroundColor(0);
        m_DoneButton.setTextColor(Color.parseColor("#5c5ac7"));
        m_DoneButton.setText(R.string.textedit_done);

        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(
                (int) (nativeGetScreenWidth * 0.12f),
                measuredHeight);
        layoutParams3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams3.setMargins(0, 0, 0, 0);
        m_CancelButton.setLayoutParams(layoutParams3);
        m_CancelButton.setBackgroundColor(0);
        m_CancelButton.setTextColor(Color.parseColor("#5c5ac7"));
        m_CancelButton.setText(R.string.textedit_cancel);
    }

    private void RegisterLayoutChangeCallback() {
        mViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!isInFloatingMode) {
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                m_KeyBoardHeight = mViewGroup.getRootView().getHeight() - rect.bottom;
                if (m_KeyBoardHeight > 0 && !m_editText.isFocused()) {
                    aww(true);
                }
                else if (m_KeyBoardHeight == 0 && m_editText.isFocused()) {
                    aww(false);
                }
            }
        });
    }

    public boolean isAcceptableTextLength(int length) {
        Log.d("NIRMAN", "isAcceptableTextLength: maxlength = " + maxLength + " length= " + length);
        if (maxLength >= 120) {
            return false;
        }

        if (maxLength == 119) {
            m_editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength + 1)});
        }
        else {
            m_editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10000)});
        }
        return true;
    }

    public void clearIngameInputBox() {
        runOnUiThread(() -> {
            m_before = m_text_default;
            m_editText.setText(m_text_default);
            m_editText.setSelection(m_editText.getText().length());
        });
    }

    private void isKeyboardShown(View view) {
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        boolean isKeyboardShown = ((float) (view.getBottom() - rect.bottom)) > view.getResources().getDisplayMetrics().density * 128.0f;
        if (isKeyboardShown) {
            isKeyboardExist = true;
        }

        Log.d("KEYBOARD", "isKeyboardShown = " + isKeyboardShown + " and isKeyboardExist = " + isKeyboardExist);
        if (!isKeyboardShown && isKeyboardExist && m_editText.isFocused()) {
            UpdateEditBoxInView(false, false);
            nativeOnKey(1, 13, 13);
            Log.d(PackageName, "Removing focus from input box");
            mGLView.requestFocus();
            isKeyboardExist = false;
        }
    }

    public void aww(boolean show) {
        runOnUiThread(() -> {
            if (show && !m_editText.isFocused()) {
                Log.d("NIRMAN", "KeyboardX opening...");
                UpdateEditBoxInView(true, false);
            }
            else if (!show && m_editText.isFocused()) {
                Log.d("NIRMAN", "KeyboardX closing...");
                if (!passwordField && !m_canShowCustomKeyboard) {
                    nativeOnKey(1, VIRTUAL_KEY_BACK, 0);
                }

                nativeCancelBtnPressed();
                UpdateEditBoxInView(false, false);
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    nativeUpdateConsoleLogPos((float) m_KeyBoardHeight);
                }
            }

            if (m_editText.isFocused()) {
                UpdateEditBoxRootViewPosition();
            }
        });
    }

    public void toggle_keyboard(boolean show) {
        if (isInFloatingMode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Fix for the other application keyboard not showing up in the floating mode.
                    FloatingService.mFloatingService.updateWindowManagerParams(true, show, true);
                }
            });

            aww(show);
        }

        InputMethodManager inputMethodManager = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mGLView.getWindowToken(), 0);
        if (show) {
            Log.d("Msg", "Enabling keyboard");
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            clearIngameInputBox();

            m_canShowCustomKeyboard = false;
            m_focusOnKeyboard = true;

            // On the Nexus One, SHOW_FORCED makes it impossible
            // to manually dismiss the keyboard.
            // On the Droid SHOW_IMPLICIT doesn't bring up the keyboard.
            return;
        }

        m_canShowCustomKeyboard = true;
        m_focusOnKeyboard = false;
        Log.d("Msg", "Disabling keyboard");
    }

    public static void setViewVisibility(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.INVISIBLE);
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setViewVisibility(viewGroup.getChildAt(i), visible);
            }
        }
    }

    // From MessageManager.h
    final static int VIRTUAL_KEY_BACK = 500000;
    final static int VIRTUAL_KEY_PROPERTIES = 500001;
    final static int VIRTUAL_KEY_HOME = 500002;
    final static int VIRTUAL_KEY_SEARCH = 500003;
    final static int VIRTUAL_KEY_DIR_UP = 500004;
    final static int VIRTUAL_KEY_DIR_DOWN = 500005;
    final static int VIRTUAL_KEY_DIR_LEFT = 500006;
    final static int VIRTUAL_KEY_DIR_RIGHT = 500007;
    final static int VIRTUAL_KEY_DIR_CENTER = 500008;
    final static int VIRTUAL_KEY_VOLUME_UP = 500009;
    final static int VIRTUAL_KEY_VOLUME_DOWN = 500010;
    final static int VIRTUAL_KEY_SHIFT = 500011;
    final static int VIRTUAL_KEY_TRACKBALL_DOWN = 500035;
    final static int VIRTUAL_DPAD_BUTTON_LEFT = 500036; //square on xperia
    final static int VIRTUAL_DPAD_BUTTON_UP = 500037; //triangle on xperia
    final static int VIRTUAL_DPAD_BUTTON_RIGHT = 500038; //O
    final static int VIRTUAL_DPAD_BUTTON_DOWN = 500039; //X
    final static int VIRTUAL_DPAD_SELECT = 500040;
    final static int VIRTUAL_DPAD_START = 500041;
    final static int VIRTUAL_DPAD_LBUTTON = 500042;
    final static int VIRTUAL_DPAD_RBUTTON = 500043;

    // Messages we could call on Proton using nativeSendGUIEx:
    final static int MESSAGE_TYPE_GUI_CLICK_START = 0;
    final static int MESSAGE_TYPE_GUI_CLICK_END = 1;
    final static int MESSAGE_TYPE_GUI_CLICK_MOVE = 2; // Only send when button/finger is held down
    final static int MESSAGE_TYPE_GUI_CLICK_MOVE_RAW = 3; // Win only, the raw mouse move messages
    final static int MESSAGE_TYPE_GUI_ACCELEROMETER = 4;
    final static int MESSAGE_TYPE_GUI_TRACKBALL = 5;
    final static int MESSAGE_TYPE_GUI_CHAR = 6; // The input box uses it on windows since we don't have a virtual keyboard
    static final int MESSAGE_TYPE_GUI_KEYBWD_CURSORPOS = 8;
    static final int MESSAGE_TYPE_GUI_KEYBWD_STRING = 7;
    final static int MESSAGE_TYPE_GUI_COPY = 9;
    final static int MESSAGE_TYPE_GUI_PASTE = 10;
    final static int MESSAGE_TYPE_GUI_TOGGLE_FULLSCREEN = 11;

    final static int MESSAGE_TYPE_SET_ENTITY_VARIANT = 12;
    final static int MESSAGE_TYPE_CALL_ENTITY_FUNCTION = 13;
    final static int MESSAGE_TYPE_CALL_COMPONENT_FUNCTION_BY_NAME = 14;
    final static int MESSAGE_TYPE_PLAY_SOUND = 15;
    final static int MESSAGE_TYPE_VIBRATE = 16;
    final static int MESSAGE_TYPE_REMOVE_COMPONENT = 17;
    final static int MESSAGE_TYPE_ADD_COMPONENT = 18;
    final static int MESSAGE_TYPE_OS_CONNECTION_CHECKED = 19; // Sent by macOS, will send an eOSSTreamEvent as parm1
    final static int MESSAGE_TYPE_PLAY_MUSIC = 20;
    final static int MESSAGE_TYPE_UNKNOWN = 21;
    final static int MESSAGE_TYPE_PRELOAD_SOUND = 22;
    final static int MESSAGE_TYPE_GUI_CHAR_RAW = 23;
    final static int MESSAGE_TYPE_SET_SOUND_ENABLED = 24;

    // Some tapjoy stuff
    final static int MESSAGE_TYPE_TAPJOY_AD_READY = 25;
    final static int MESSAGE_TYPE_TAPJOY_FEATURED_APP_READY = 26;
    final static int MESSAGE_TYPE_TAPJOY_MOVIE_AD_READY = 27;

    // GOOGLE BILLING
    final static int MESSAGE_TYPE_IAP_RESULT = 28;
    final static int MESSAGE_TYPE_IAP_ITEM_STATE = 29;
    final static int MESSAGE_TYPE_IAP_ITEM_INFO_RESULT = 54;

    // More tapjoy stuff
    final static int MESSAGE_TYPE_TAPJOY_TAP_POINTS_RETURN = 30;
    final static int MESSAGE_TYPE_TAPJOY_TAP_POINTS_RETURN_ERROR = 31;
    final static int MESSAGE_TYPE_TAPJOY_SPEND_TAP_POINTS_RETURN = 32;
    final static int MESSAGE_TYPE_TAPJOY_SPEND_TAP_POINTS_RETURN_ERROR = 33;
    final static int MESSAGE_TYPE_TAPJOY_AWARD_TAP_POINTS_RETURN = 34;
    final static int MESSAGE_TYPE_TAPJOY_AWARD_TAP_POINTS_RETURN_ERROR = 35;
    final static int MESSAGE_TYPE_TAPJOY_EARNED_TAP_POINTS = 36;

    final static int MESSAGE_TYPE_GUI_JOYPAD_BUTTONS = 37; // For Jake's android gamepad input
    final static int MESSAGE_TYPE_GUI_JOYPAD = 38; // For Jake's android gamepad input
    final static int MESSAGE_TYPE_GUI_JOYPAD_CONNECT = 39; // For Jakes android gamepad input
    final static int MESSAGE_TYPE_CALL_ENTITY_FUNCTION_RECURSIVELY = 40; // Used to schedule fake clicks, helps me with debugging

    final static int MESSAGE_TYPE_HW_TOUCH_KEYBOARD_WILL_SHOW = 41; // Ios only, when not using external keyboard
    final static int MESSAGE_TYPE_HW_TOUCH_KEYBOARD_WILL_HIDE = 42; // Ios only, when not using external keyboard
    final static int MESSAGE_TYPE_HW_KEYBOARD_INPUT_ENDING = 43; // Proton is done with input and requesting that the keyboard hid
    final static int MESSAGE_TYPE_HW_KEYBOARD_INPUT_STARTING = 44; // Proton is asking for the keyboard to open

    // GOOGLE BILLING again
    final static int MESSAGE_TYPE_IAP_PURCHASED_LIST_STATE = 45; // For sending back lists of items we've already purchased

    final static int MESSAGE_TYPE_CALL_STATIC_FUNCTION = 46; // Use by other platforms, but this value needs to be reserved by those platforms.

    // for sending through version values
    final static int MESSAGE_TYPE_APP_VERSION = 47;

    final static int MESSAGE_USER = 1000; // Send your own messages after this #

    // IAP RESPONSE CODES for Proton
    final static int RESULT_OK = 0;
    final static int RESULT_USER_CANCELED = 1;
    final static int RESULT_SERVICE_UNAVAILABLE = 2;
    final static int RESULT_BILLING_UNAVAILABLE = 3;
    final static int RESULT_ITEM_UNAVAILABLE = 4;
    final static int RESULT_DEVELOPER_ERROR = 5;
    final static int RESULT_ERROR = 6;
    final static int RESULT_OK_ALREADY_PURCHASED = 7;

    public int TranslateKeycodeToProtonVirtualKey(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                keyCode = VIRTUAL_KEY_BACK;
                break;
            case KeyEvent.KEYCODE_MENU:
                keyCode = VIRTUAL_KEY_PROPERTIES;
                break;
            case KeyEvent.KEYCODE_SEARCH:
                keyCode = VIRTUAL_KEY_SEARCH;
                break;
            case KeyEvent.KEYCODE_S:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                keyCode = VIRTUAL_KEY_DIR_DOWN;
                break;
            case KeyEvent.KEYCODE_W:
            case KeyEvent.KEYCODE_DPAD_UP:
                keyCode = VIRTUAL_KEY_DIR_UP;
                break;
            case KeyEvent.KEYCODE_A:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                keyCode = VIRTUAL_KEY_DIR_LEFT;
                break;
            case KeyEvent.KEYCODE_D:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                keyCode = VIRTUAL_KEY_DIR_RIGHT;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                keyCode = VIRTUAL_KEY_DIR_CENTER;
                break;
            case 0:
                keyCode = VIRTUAL_KEY_SHIFT;
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                keyCode = VIRTUAL_KEY_VOLUME_UP;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                keyCode = VIRTUAL_KEY_VOLUME_DOWN;
                break;
        }
        return keyCode;
    }

    // Single touch version, works starting API4/??
    public boolean onTrackballEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            nativeOnTrackball(motionEvent.getX(), motionEvent.getY());
            return true; // Signal that we handled it, so its messages don't show up as normal directional presses
        }
        else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            // They pushed the button
            nativeOnKey(1, VIRTUAL_KEY_TRACKBALL_DOWN, VIRTUAL_KEY_TRACKBALL_DOWN);
            return false;
        }
        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent keyEvent) {
        return super.onKeyMultiple(keyCode, count, keyEvent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        Log.v("onKeyDown", "onKeyDown Keydown Got " + keyCode + " " + Character.toChars(keyEvent.getUnicodeChar())[0]);
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            return true;
        }

        if (keyEvent.getRepeatCount() > 0) {
            return super.onKeyDown(keyCode, keyEvent);
        }

        if (keyEvent.isAltPressed() && keyCode == KeyEvent.KEYCODE_BACK) {
            // XPeria's O button, not the back button!
            nativeOnKey(1, VIRTUAL_DPAD_BUTTON_RIGHT, keyEvent.getUnicodeChar());
            return true; // Signal that we handled it
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK) {
            nativeOnKey(1, VIRTUAL_KEY_BACK, keyEvent.getUnicodeChar()); // 1 means keydown
            return true; // Signal that we handled it
        }

        int vKey = TranslateKeycodeToProtonVirtualKey(keyCode);
        nativeOnKey(1, vKey, (char) keyEvent.getUnicodeChar()); // 1 means keydown
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
        Log.v("onKeyUp", "Keyup Got " + keyCode + " " + Character.toChars(keyEvent.getUnicodeChar())[0]);
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            return true;
        }

        if (keyEvent.isAltPressed() && keyCode == KeyEvent.KEYCODE_BACK) {
            // XPeria's O button, not the back button!
            nativeOnKey(0, VIRTUAL_DPAD_BUTTON_RIGHT, keyEvent.getUnicodeChar());
            return true; // Signal that we handled it
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK) {
            nativeOnKey(0, VIRTUAL_KEY_BACK, keyEvent.getUnicodeChar()); // 0 is type keyup
            return true; // Signal that we handled it
        }

        int vKey = TranslateKeycodeToProtonVirtualKey(keyCode);
        nativeOnKey(0, vKey, (char) keyEvent.getUnicodeChar()); // 0 is type keyup
        return super.onKeyUp(keyCode, keyEvent);
    }

    // Straight version
    public void sendVersionDetails() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            nativeSendGUIStringEx(MESSAGE_TYPE_APP_VERSION, 0, 0, 0, packageInfo.versionName);
        }
        catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(PackageName, "Cannot load App Version!");
        }
    }

    // TAPJOY
    /*public void getFullScreenAdResponse() {
        Log.i(PackageName, "Displaying Full Screen Ad..");
    }

    public void getFullScreenAdResponseFailed(int error) {
        Log.i(PackageName, "No Full Screen Ad to display: " + error);
    }

    public void getDisplayAdResponse(View view) {
        adView = view;

        int ad_width = adBannerWidth;
        int ad_height = adBannerHeight;

        if (ad_width == 0) {
            ad_width = view.getLayoutParams().width;
        }

        if (ad_height == 0) {
            ad_height = adView.getLayoutParams().height;
        }

        Log.d(PackageName, "adView dimensions: " + ad_width + "x" + ad_height);

        int measuredWidth = app.mGLView.getMeasuredWidth();
        Log.d(PackageName, "mGLView width is " + measuredWidth);

        if (measuredWidth > ad_width) {
            measuredWidth = ad_width;
        }

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(measuredWidth, (ad_height * measuredWidth) / ad_width);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        adView.setLayoutParams(layoutParams);
        Log.v(PackageName, "adLinearLayout dimensions: " + mGLView.getMeasuredWidth() + "x" + mGLView.getMeasuredHeight());
        nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 1, 0, 0);
    }

    public void getDisplayAdResponseFailed(String error) {
        Log.d(PackageName, "getDisplayAd error: " + error);
        nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 0, 0, 0);

        // We must use a handler since we cannot update UI elements from a different thread.
        // mMainThreadHandler.post(mUpdateResults);
    }

    // This method must be implemented if using the TapjoyConnect.getTapPoints() method.
    // It is the callback method which contains the currency and points data.
    public void getUpdatePoints(String currencyName, int pointTotal) {
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_TAP_POINTS_RETURN, pointTotal, 0, 0, currencyName);
    }

    // This method must be implemented if using the TapjoyConnect.getTapPoints() method.
    // It is the callback method which contains the currency and points data.
    public void getUpdatePointsFailed(String error) {
        Log.i("growtopia", "getTapPoints error: " + error);
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_TAP_POINTS_RETURN_ERROR, 0, 0, 0, error);
    }

    // Notifier for when spending virtual currency succeeds.
    public void getSpendPointsResponse(String currencyName, int pointTotal) {
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_SPEND_TAP_POINTS_RETURN, pointTotal, 0, 0, currencyName);
    }

    // Notifier for when spending virtual currency fails.
    public void getSpendPointsResponseFailed(String error) {
        Log.i("growtopia", "spendTapPoints error: " + error);
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_SPEND_TAP_POINTS_RETURN_ERROR, 0, 0, 0, error);
    }

    public void getAwardPointsResponse(String currencyName, int pointTotal) {
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_AWARD_TAP_POINTS_RETURN, pointTotal, 0, 0, currencyName);
    }

    public void getAwardPointsResponseFailed(String error) {
        Log.i("growtopia", "getAwardPointsResponseFailed: " + error);
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_AWARD_TAP_POINTS_RETURN_ERROR, 0, 0, 0, error);
    }

    public void earnedTapPoints(int amount) {
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_EARNED_TAP_POINTS, amount, 0, 0, "");
    }

    public void videoReady() {
        Log.i("growtopia", "VIDEO READY");
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_MOVIE_AD_READY, 1, 0, 0, "");
    }

    // Notifier when a video ad starts.
    public void videoStart() {
        Log.i("growtopia", "VIDEO START");
    }

    public void videoComplete() {
        Log.i("growtopia", "VIDEO COMPLETE");
        nativeSendGUIStringEx(MESSAGE_TYPE_TAPJOY_MOVIE_AD_READY, 2, 0, 0, "");
    }*/

    public void requestPlacementAndShow(String placementName) {
        Tapjoy.setActivity(app);

        TJPlacement tJPlacement = Tapjoy.getPlacement(placementName, new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onRequestSuccess for placement " + tJPlacement.getName());
                if (!tJPlacement.isContentAvailable()) {
                    Log.d(getPackageName(), "No content available for placement " + tJPlacement.getName());
                    nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 0, 0, 0);
                    makeToastUI("No video content is available for your device.");
                    return;
                }

                runOnUiThread(() -> {
                    nDialog = new ProgressDialog(app);
                    nDialog.setTitle("Loading");
                    nDialog.setMessage("Wait while loading...");
                    nDialog.setCancelable(true);
                    nDialog.show();
                });
            }

            @Override
            public void onRequestFailure(TJPlacement tJPlacement, TJError tJError) {
                Log.d(getPackageName(), "onRequestFailure for placement " + tJPlacement.getName() + " -- error: " + tJError.message);
                if (nDialog != null && nDialog.isShowing()) {
                    nDialog.dismiss();
                }

                nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 0, 0, 0);
            }

            @Override
            public void onContentReady(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onContentReady for placement " + tJPlacement.getName());
                if (nDialog != null && nDialog.isShowing()) {
                    nDialog.dismiss();
                }

                nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 1, 0, 0);
                if (tJPlacement.isContentReady()) {
                    tJPlacement.showContent();
                }
            }

            @Override
            public void onContentShow(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentShow for placement " + tJPlacement.getName());
                if (nDialog != null && nDialog.isShowing()) {
                    nDialog.dismiss();
                }
            }

            @Override
            public void onContentDismiss(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentDismiss for placement " + tJPlacement.getName());
                if (nDialog != null && nDialog.isShowing()) {
                    nDialog.dismiss();
                }

                Tapjoy.getCurrencyBalance(app);
            }

            @Override
            public void onPurchaseRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str) {
                TapjoyLog.i(getPackageName(), "onPurchaseRequest " + tJPlacement.getName());
            }

            @Override
            public void onRewardRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str, int i) {
                TapjoyLog.i(getPackageName(), "onRewardRequest " + tJPlacement.getName());
            }

            @Override
            public void onClick(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onClick " + tJPlacement.getName());
            }
        });

        if (Tapjoy.isConnected()) {
            tJPlacement.setVideoListener(app);
            Log.d(getPackageName(), "requestPlacementAndShow:Requesting placement content");
            tJPlacement.requestContent();
        }
        else {
            Log.e(getPackageName(), "Tapjoy SDK must finish connecting before requesting content.");
        }

        if (placementName.equals("Sub_01")) {
            tapjoyAdPlacementForSub01 = tJPlacement;
        }
        else if (placementName.equals("GROW_GGP_V4VC_TV")) {
            tapjoyAdPlacementForTV = tJPlacement;
        }
    }

    public void requestPlacement(String placementName) {
        Tapjoy.setActivity(app);

        TJPlacement tJPlacement = Tapjoy.getPlacement(placementName, new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onRequestSuccess for placement " + tJPlacement.getName());
                if (!tJPlacement.isContentAvailable()) {
                    Log.d(getPackageName(), "No content available for placement " + tJPlacement.getName());
                    nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 0, 0, 0);
                }
            }

            @Override
            public void onRequestFailure(TJPlacement tJPlacement, TJError tJError) {
                Log.d(getPackageName(), "onRequestFailure for placement " + tJPlacement.getName() + " -- error: " + tJError.message);
            }

            @Override
            public void onContentReady(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onContentReady for placement " + tJPlacement.getName());
            }

            @Override
            public void onContentShow(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentShow for placement " + tJPlacement.getName());
            }

            @Override
            public void onContentDismiss(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentDismiss for placement " + tJPlacement.getName());
                Tapjoy.getCurrencyBalance(app);
            }

            @Override
            public void onPurchaseRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str) {
                TapjoyLog.i(getPackageName(), "onPurchaseRequest " + tJPlacement.getName());
            }

            @Override
            public void onRewardRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str, int i) {
                TapjoyLog.i(getPackageName(), "onRewardRequest " + tJPlacement.getName());
            }

            @Override
            public void onClick(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onClick " + tJPlacement.getName());
            }
        });

        if (Tapjoy.isConnected()) {
            tJPlacement.setVideoListener(app);
            Log.d(getPackageName(), "requestPlacementAndShow:Requesting placement content");
        }
        else {
            Log.e(getPackageName(), "Tapjoy SDK must finish connecting before requesting content.");
        }

        if (placementName.equals("Sub_01")) {
            tapjoyAdPlacementForSub01 = tJPlacement;
        }
        else if (placementName.equals("GROW_GGP_V4VC_TV")) {
            tapjoyAdPlacementForTV = tJPlacement;
        }
    }

    public void requestOfferwallAndShow(String placementName) {
        Tapjoy.setActivity(app);

        offerwallPlacement = Tapjoy.getPlacement(placementName, new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onRequestSuccess for placement " + tJPlacement.getName());
                if (!tJPlacement.isContentAvailable()) {
                    Log.d(getPackageName(), "No content available for placement " + tJPlacement.getName());
                    nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_MOVIE_AD_READY, 0, 0, 0);
                    return;
                }

                runOnUiThread(() -> {
                    oDialog = new ProgressDialog(app);
                    oDialog.setTitle("Loading");
                    oDialog.setMessage("Wait while loading...");
                    oDialog.setCancelable(true);
                    oDialog.show();
                });
            }

            @Override
            public void onRequestFailure(TJPlacement tJPlacement, TJError tJError) {
                Log.d(getPackageName(), "onRequestFailure for placement " + tJPlacement.getName() + " -- error: " + tJError.message);
                if (oDialog != null && oDialog.isShowing()) {
                    oDialog.dismiss();
                }

                nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 0, 0, 0);
            }

            @Override
            public void onContentReady(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onContentReady for placement " + tJPlacement.getName());
                if (oDialog != null && oDialog.isShowing()) {
                    oDialog.dismiss();
                }

                nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_AD_READY, 1, 0, 0);
                if (tJPlacement.isContentReady()) {
                    tJPlacement.showContent();
                }
            }

            @Override
            public void onContentShow(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentShow for placement " + tJPlacement.getName());
                if (oDialog != null && oDialog.isShowing()) {
                    oDialog.dismiss();
                }
            }

            @Override
            public void onContentDismiss(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentDismiss for placement " + tJPlacement.getName());
                if (oDialog != null && oDialog.isShowing()) {
                    oDialog.dismiss();
                }

                Tapjoy.getCurrencyBalance(app);
            }

            @Override
            public void onPurchaseRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str) {
                TapjoyLog.i(getPackageName(), "onPurchaseRequest " + tJPlacement.getName());
            }

            @Override
            public void onRewardRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str, int i) {
                TapjoyLog.i(getPackageName(), "onRewardRequest " + tJPlacement.getName());
            }

            @Override
            public void onClick(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onClick " + tJPlacement.getName());
            }
        });

        if (Tapjoy.isConnected()) {
            offerwallPlacement.setVideoListener(app);
            Log.d(getPackageName(), "requestOfferwallAndShow:Requesting placement content");
            offerwallPlacement.requestContent();
        }
        else {
            Log.e(getPackageName(), "Tapjoy SDK must finish connecting before requesting content.");
        }
    }

    public void requestOfferwall(String placementName) {
        Tapjoy.setActivity(app);

        offerwallPlacement = Tapjoy.getPlacement(placementName, new TJPlacementListener() {
            @Override
            public void onRequestSuccess(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onRequestSuccess for placement " + tJPlacement.getName());
                if (!tJPlacement.isContentAvailable()) {
                    Log.d(getPackageName(), "No content available for placement " + tJPlacement.getName());
                    nativeSendGUIEx(MESSAGE_TYPE_TAPJOY_MOVIE_AD_READY, 0, 0, 0);
                }
            }

            @Override
            public void onRequestFailure(TJPlacement tJPlacement, TJError tJError) {
                Log.d(getPackageName(), "onRequestFailure for placement " + tJPlacement.getName() + " -- error: " + tJError.message);
            }

            @Override
            public void onContentReady(TJPlacement tJPlacement) {
                Log.d(getPackageName(), "onContentReady for placement " + tJPlacement.getName());
            }

            @Override
            public void onContentShow(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentShow for placement " + tJPlacement.getName());
            }

            @Override
            public void onContentDismiss(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onContentDismiss for placement " + tJPlacement.getName());
                Tapjoy.getCurrencyBalance(app);
            }

            @Override
            public void onPurchaseRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str) {
                TapjoyLog.i(getPackageName(), "onPurchaseRequest " + tJPlacement.getName());
            }

            @Override
            public void onRewardRequest(TJPlacement tJPlacement, TJActionRequest tJActionRequest, String str, int i) {
                TapjoyLog.i(getPackageName(), "onRewardRequest " + tJPlacement.getName());
            }

            @Override
            public void onClick(TJPlacement tJPlacement) {
                TapjoyLog.i(getPackageName(), "onClick " + tJPlacement.getName());
            }
        });

        if (Tapjoy.isConnected()) {
            offerwallPlacement.setVideoListener(app);
            Log.d(getPackageName(), "requestOfferwall:Requesting placement content");
        }
        else {
            Log.e(getPackageName(), "Tapjoy SDK must finish connecting before requesting content.");
        }
    }

    @Override
    public void onGetCurrencyBalanceResponse(String currencyName, int balance) {
        Log.d(getPackageName(), "onGetCurrencyBalanceResponse currencyName: " + currencyName + ", balance: " + balance);
    }

    @Override
    public void onGetCurrencyBalanceResponseFailure(String error) {
        Log.e(getPackageName(), "onGetCurrencyBalanceResponseFailure error: " + error);
    }

    @Override
    public void onVideoStart(TJPlacement tJPlacement) {
        Log.d(getPackageName(), "onVideoStart TJPlacement: " + tJPlacement);
    }

    @Override
    public void onVideoError(TJPlacement tJPlacement, String statusCode) {
        Log.e(getPackageName(), "onVideoError TJPlacement: " + tJPlacement + ", statusCode: " + statusCode);
    }

    @Override
    public void onVideoComplete(TJPlacement tJPlacement) {
        Log.d(getPackageName(), "onVideoComplete TJPlacement: " + tJPlacement);
    }

    public void onConnectToTapjoy(String sdkKey) {
        Hashtable<String, Object> hashtable = new Hashtable<>();
        hashtable.put(TapjoyConnectFlag.ENABLE_LOGGING, "false");
        hashtable.put(TapjoyConnectFlag.DISABLE_ANDROID_ID_AS_ANALYTICS_ID, "true");
        Tapjoy.connect(getApplicationContext(), sdkKey, hashtable, new TJConnectListener() {
            @Override
            public void onConnectSuccess() {
                TapjoyLog.i("onConnectToTapjoy", "Tapjoy connect success");
            }

            @Override
            public void onConnectFailure() {
                TapjoyLog.i("onConnectToTapjoy", "Tapjoy connect failed");
            }
        });
    }
    //***************************

    //************** SOUND STUFF *************

    // JNI to play music, etc
    public MediaPlayer _music = null;

    private static class MusicFadeOutThread extends Thread {
        private final int m_duration;

        public MusicFadeOutThread(int duration) {
            super();
            m_duration = duration;
        }

        public void run() {
            final int volumeChangeInterval = 100;  // Change volume every this amount of ms
            final int totalSteps = m_duration / volumeChangeInterval;
            int remainingSteps = totalSteps;

            while (remainingSteps  > 0) {
                synchronized (app._music) {
                    float phase  = (float) remainingSteps  / (float) totalSteps;
                    app._music.setVolume(phase  * m_lastMusicVol, phase  * m_lastMusicVol);
                    remainingSteps--;
                }

                try {
                    Thread.sleep(volumeChangeInterval);
                }
                catch (InterruptedException unused) {
                    return;
                }
            }

            synchronized (app._music) {
                app._music.stop();
                app._music.setVolume(m_lastMusicVol, m_lastMusicVol);
            }
        }
    }

    private MusicFadeOutThread musicFadeOutThread = null;

    public static synchronized void music_play(String fname, boolean looping) {
        if (app._music != null) {
            app._music.reset();
        }
        else {
            app._music = new MediaPlayer();
        }

        if (fname.charAt(0) == '/') {
            // Load as raw, not an asset
            try {
                FileInputStream fileInputStream = new FileInputStream(fname);
                app._music.setDataSource(fileInputStream.getFD());
                fileInputStream.close();
                app._music.setLooping(looping);
                app._music.prepare();
                music_set_volume(m_lastMusicVol);
                app._music.start();
            }
            catch (IOException unused) {
                String packageName = app.getPackageName();
                Log.d(packageName, "Can't load music (raw) filename: " + fname);
            }
            catch (IllegalStateException unused2) {
                String packageName2 = app.getPackageName();
                Log.d(packageName2, "Can't load music (raw), illegal state filename: " + fname);
                app._music.reset();
            }
            return;
        }

        try {
            Context growtopiaContext = app.createPackageContext("com.rtsoft.growtopia", 0);
            AssetFileDescriptor openFd = growtopiaContext.getAssets().openFd(fname);
            app._music.setDataSource(openFd.getFileDescriptor(), openFd.getStartOffset(), openFd.getLength());
            openFd.close();
            app._music.setLooping(looping);
            app._music.prepare();
            music_set_volume(m_lastMusicVol);
            app._music.start();
        }
        catch (IOException unused3) {
            Log.d(app.getPackageName(), "Can't load music. filename: " + fname);
        }
        catch (IllegalStateException unused4) {
            Log.d(app.getPackageName(), "Can't load music, illegal state. filename: " + fname);
            app._music.reset();
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.d(app.getPackageName(), "Can't load music, Growtopia assets not found? filename: " + fname);
        }
    }

    public static synchronized void music_stop() {
        if (app._music != null) {
            if (app.musicFadeOutThread != null && app.musicFadeOutThread.isAlive()) {
                try {
                    app.musicFadeOutThread.interrupt();
                    app.musicFadeOutThread.join();
                }
                catch (InterruptedException unused) {
                    /* ~ */
                }
            }

            app._music.stop();
        }
    }

    public static synchronized void music_fadeout(int duration) {
        if (app._music != null && app._music.isPlaying()) {
            if (duration <= 0) {
                music_stop();
            }
            else if (app.musicFadeOutThread == null || !app.musicFadeOutThread.isAlive()) {
                app.musicFadeOutThread = new MusicFadeOutThread(duration);
                app.musicFadeOutThread.start();
            }
        }
    }

    public static synchronized void music_set_volume(float v) {
        if (app._music != null) {
            m_lastMusicVol = v;
            app._music.setVolume(v, v);
        }
    }

    @SuppressLint("MissingPermission")
    public static synchronized void vibrate(int vibMS) {
        Vibrator vibrator = ((Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE));
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibMS, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // Deprecated in API 26
                vibrator.vibrate(vibMS);
            }
        }
    }

    public static synchronized int music_get_pos() {
        if (app._music == null) {
            return 0;
        }
        return app._music.getCurrentPosition();
    }

    public static synchronized boolean music_is_playing() {
        if (app._music == null) {
            return false;
        }
        return app._music.isPlaying();
    }

    public static synchronized void music_set_pos(int positionMS) {
        if (app._music == null) {
            Log.d(app.getPackageName(), "warning: music_set_position: no music playing, can't set position");
            return;
        }

        app._music.seekTo(positionMS);
    }

    // JNI to play sounds
    public SoundPool _sounds = null;

    public static synchronized void sound_init() {
        if (app._sounds == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();

                app._sounds = new SoundPool.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setMaxStreams(8)
                        .build();

                return;
            }

            app._sounds = new SoundPool(8, AudioManager.STREAM_MUSIC, 1);
        }
    }

    public static synchronized void sound_destroy() {
        if (app._sounds != null) {
            app._sounds.release();
            app._sounds = null;
        }
    }

    public static synchronized int sound_load(String sound) {
        if (app._sounds == null) {
            sound_init();
        }

        if (sound.charAt(0) == '/') {
            // Must be a raw file on the disc, not in the assets.  load differently
            return app._sounds.load(sound, 1);
        }

        try {
            Context growtopiaContext = app.createPackageContext("com.rtsoft.growtopia", 0);
            AssetFileDescriptor openFd = growtopiaContext.getAssets().openFd(sound);
            return app._sounds.load(openFd.getFileDescriptor(), openFd.getStartOffset(), openFd.getLength(), 1);
        }
        catch (IOException unused) {
            Log.d("Can't load sound", sound);
        }
        catch (PackageManager.NameNotFoundException unused2) {
            Log.d("Can't load sound", "Growtopia assets not found? sound: " + sound);
        }

        return 0;
    }

    public static synchronized int sound_play(int soundID, float leftVol, float rightVol, int priority, int loop, float speedMod) {
        if (app._sounds == null) {
            sound_init();
        }

        return app._sounds.play(soundID, leftVol, rightVol, priority, loop, speedMod);
    }

    public static void sound_kill(int streamID) {
        if (app._sounds == null) {
            sound_init();
        }

        app._sounds.unload(streamID);
    }

    public static void sound_stop(int streamID) {
        if (app._sounds == null) {
            sound_init();
        }

        app._sounds.stop(streamID);
    }

    public static void sound_set_vol(int streamID, float left, float right) {
        if (app._sounds == null) {
            sound_init();
        }

        app._sounds.setVolume(streamID, left, right);
    }

    public static void sound_set_rate(int streamID, float rate) {
        if (app._sounds == null) {
            sound_init();
        }

        app._sounds.setRate(streamID, rate);
    }

    public static void _OpenCSTS(String cstsuid, String country, String language, boolean payer,
                                 String ingameplayerid, String environment, String misc) {
        Intent intent = new Intent(app.getApplicationContext(), CSTSWebViewActivity.class);
        intent.putExtra("cstsuid", cstsuid);
        intent.putExtra("country", country);
        intent.putExtra("language", language);
        intent.putExtra("payer", payer);
        intent.putExtra("ingameplayerid", ingameplayerid);
        intent.putExtra("environment", environment);
        intent.putExtra("misc", misc);
        app.startActivity(intent);
    }

    public String getExtractedLibraryPath() {
        String extractedPath = getExternalFilesDir(null).getAbsolutePath() + "/extracted";
        String libPath = extractedPath + "/lib";
        String[] libAbi = { "armeabi-v7a", "arm64-v8a" };
        for (String abi : libAbi) {
            File libAbiPath = new File(libPath + "/" + abi);
            if (libAbiPath.exists()) {
                return libAbiPath.getAbsolutePath();
            }
        }
        return "";
    }

    public GLSurfaceView mGLView;
    public RelativeLayout mViewGroup;
    public static native void nativeOnKey(int type, int keycode, int c);
    public static native void nativeOnTrackball(float x, float y);
    public static native void nativeOnAccelerometerUpdate(float x, float y, float z);
    public static native void nativeSendGUIEx(int messageType, int parm1, int parm2, int finger);
    public static native void nativeSendGUIStringEx(int messageType, int parm1, int parm2, int finger, String s);
    public static native void nativeCancelBtnPressed();
    public static native int nativeGetChatString();
    public static native float nativeGetEditBoxOffset();
    public static native float nativeGetScreenHeight();
    public static native float nativeGetScreenWidth();
    public static native void nativeInitActivity(Activity activity);
    public static native void nativeUpdateConsoleLogPos(float f);
}