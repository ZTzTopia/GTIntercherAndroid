package com.anzu.sdk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.rtsoft.growtopia.SharedActivity;
import com.tapjoy.TapjoyConstants;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Anzu {
    private static final int BUFFER_SIZE = 16384;
    private static final boolean SUPPORTS_GOOGLE_ADVERTISIG_ID = true;
    private static String advertisingId = null;
    private static Context appContext = null;
    private static boolean interstitialIsVisible = false;
    private static AnzuWebView interstitialRunner = null;
    private static Bitmap interstitialRunnerBitmap = null;
    private static Canvas interstitialRunnerCanvas = null;
    private static boolean interstitialRunnerRenderToBuffer = false;
    private static AnzuWebView logicRunner = null;
    private static volatile boolean shouldCancelWebTasks = false;
    private static SharedPreferences sp = null;
    private static int udidSource = -1;

    private static class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            int type = -1;
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null) {
                        type = activeNetworkInfo.getType();
                    }
                }
            } catch (Exception e) {
                /* ~ */
            }

            Anzu.OnReachabilityChanged(type);
        }
    }

    private static class HttpResponse_t {
        public String error;
        public String text;

        public HttpResponse_t(String text, String error) {
            this.text = text;
            this.error = error;
        }
    }

    public static String Anzu_SetUpUserAgent() {
        String defaultUserAgent = "";
        try {
            defaultUserAgent = WebSettings.getDefaultUserAgent(appContext);
        } catch (Exception e) {
            Log.w("ANZU", "Could not get userAgent, " + e.getMessage());
        }

        if (defaultUserAgent.indexOf(32) != -1) {
            if (defaultUserAgent.indexOf(47) == -1) {
                return System.getProperty("http.agent");
            }
        }

        return defaultUserAgent;
    }

    private static String Anzu_setUpCacheFolder() {
        // TODO: Fix the code.
        final String absolutePath = Anzu.appContext.getFilesDir().getAbsolutePath();
        final int n = 1;
        Serializable s = absolutePath;
        while (true) {
            try {
                int n2 = 0;
                Label_0174: {
                    Label_0029: {
                        if (CheckIfValidCacheFolder(absolutePath, "anzu")) {
                            s = absolutePath;
                        }
                        else {
                            s = absolutePath;
                            final String s2 = (String)(s = Anzu.appContext.getCacheDir().getAbsolutePath());
                            if (!CheckIfValidCacheFolder(s2, "anzu")) {
                                s = s2;
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                                    final String s3 = s2;
                                    s = s2;
                                    if (ContextCompat.checkSelfPermission(Anzu.appContext, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                                        break Label_0029;
                                    }
                                }
                                s = s2;
                                final String s4 = (String)(s = Anzu.appContext.getExternalFilesDir((String)null).getAbsolutePath());
                                if (CheckIfValidCacheFolder(s4, "anzu")) {
                                    s = s4;
                                    break Label_0029;
                                }
                                s = s4;
                                final String s5 = (String)(s = Anzu.appContext.getExternalCacheDir().getAbsolutePath());
                                final boolean checkIfValidCacheFolder = CheckIfValidCacheFolder(s5, "anzu");
                                final String s3 = s5;
                                if (checkIfValidCacheFolder) {
                                    s = s5;
                                    break Label_0029;
                                }
                                n2 = 0;
                                s = s3;
                                break Label_0174;
                            }
                            s = s2;
                        }
                    }
                    n2 = 1;
                }
                if (n2 == 0) {
                    final String s6 = (String)(s = Anzu.appContext.getCacheDir().getAbsolutePath());
                    try {
                        Label_0357: {
                            if (CheckIfValidCacheFolder(s6, "")) {
                                s = s6;
                                n2 = n;
                            }
                            else {
                                s = s6;
                                final String s7 = (String)(s = Anzu.appContext.getFilesDir().getAbsolutePath());
                                if (CheckIfValidCacheFolder(s7, "")) {
                                    s = s7;
                                    n2 = n;
                                }
                                else {
                                    s = s7;
                                    while (true) {
                                        Label_0283: {
                                            if (Build.VERSION.SDK_INT >= 19) {
                                                break Label_0283;
                                            }
                                            final String s8 = s7;
                                            s = s7;
                                            if (ContextCompat.checkSelfPermission(Anzu.appContext, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                                                break Label_0283;
                                            }
                                            s = s8;
                                            break Label_0357;
                                        }
                                        s = s7;
                                        final String s9 = (String)(s = Anzu.appContext.getExternalFilesDir((String)null).getAbsolutePath());
                                        if (CheckIfValidCacheFolder(s9, "")) {
                                            s = s9;
                                            n2 = n;
                                        }
                                        else {
                                            s = s9;
                                            final String s10 = (String)(s = Anzu.appContext.getExternalCacheDir().getAbsolutePath());
                                            final boolean checkIfValidCacheFolder2 = CheckIfValidCacheFolder(s10, "");
                                            final String s8 = s10;
                                            if (!checkIfValidCacheFolder2) {
                                                continue;
                                            }
                                            n2 = n;
                                            s = s10;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception ex) {}
                    if (n2 == 0) {
                        s = Anzu.appContext.getCacheDir().getAbsolutePath();
                        final StringBuilder sb = new StringBuilder();
                        sb.append("Will Use problematic cache folder: ");
                        sb.append((String)s);
                        sb.append("anzu");
                        Log.println(6, "ANZU", sb.toString());
                    }
                }
                else {
                    String string = (String)s;
                    if (!((String)s).endsWith(File.separator)) {
                        final StringBuilder sb2 = new StringBuilder();
                        sb2.append((String)s);
                        sb2.append(File.separator);
                        string = sb2.toString();
                    }
                    s = new StringBuilder();
                    ((StringBuilder)s).append(string);
                    ((StringBuilder)s).append("anzu");
                    s = ((StringBuilder)s).toString();
                    if (((String)s).endsWith(File.separator)) {
                        s = ((String)s).substring(0, ((String)s).length() - 1);
                    }
                }
                return (String)s;
            }
            catch (Exception ex2) {
                final String s3 = (String)s;
                continue;
            }
            // break;
        }
    }

    static boolean CheckIfValidCacheFolder(String str, String str2) {
        String str3 = str;
        try {
            if (!str.endsWith(File.separator)) {
                str3 = str + File.separator;
            }

            File file = new File(str3 + str2);
            if (!str2.isEmpty()) {
                if (file.exists()) {
                    Log.println(Log.VERBOSE, "ANZU", "Will Use existing cache folder: " + str3 + str2);
                    return true;
                } else if (file.mkdirs() && file.exists()) {
                    Log.println(Log.VERBOSE, "ANZU", "Will Use created cache folder: " + str3 + str2);
                    return true;
                }
            } else if (file.exists()) {
                new ObjectOutputStream(appContext.openFileOutput("_anzu_test_write", 0)).close();
                Log.println(Log.VERBOSE, "ANZU", "Will Use writable cache folder: " + str3);
                return true;
            } else if (file.mkdirs() && file.exists()) {
                Log.println(Log.VERBOSE, "ANZU", "Will Use created cache folder: " + str3);
                return true;
            }
        } catch (Exception e) {
            /* ~ */
        }
        return false;
    }

    public static boolean ClassCheck() {
        return true;
    }

    public static native void Error(String str);

    protected static Context GetContext() {
        return appContext;
    }

    public static native void Log(String str);

    public static native float MetricGet(String str);

    public static native String MetricGetS(String str);

    public static native void OnGotLocation(int i, float f, float f2);

    public static native void OnReachabilityChanged(int i);

    public static void SetContext(final Context context) {
        appContext = context;
        Thread thread = new Thread() {
            public void run() {
                String str;
                if (Anzu.appContext != null) {
                    try {
                        try {
                            PackageInfo packageInfo = SharedActivity.app.getPackageManager().getPackageInfo(SharedActivity.PackageName, 0);
                            String libraryPath = packageInfo.applicationInfo.nativeLibraryDir;
                            System.load(libraryPath + "/libanzu.so");
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    } catch (UnsatisfiedLinkError e) {
                        Log.println(Log.WARN, "ANZU", "failed loading anzu shared library, this is ok if using static libs");
                    }

                    String Anzu_setUpCacheFolder = Anzu.Anzu_setUpCacheFolder();
                    String Anzu_SetUpUserAgent = Anzu.Anzu_SetUpUserAgent();
                    String packageName = Anzu.appContext.getPackageName();

                    try {
                        str = Anzu.appContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
                    } catch (Exception e2) {
                        str = "";
                    }

                    String userId = Anzu.getUserId();
                    Log.d("ANZU", "udid - " + userId);
                    String networkOperatorName = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
                    SharedPreferences sharedPreferences = Anzu.sp = context.getSharedPreferences("Anzu_keystore", 0);
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    int i = -1;
                    if (connectivityManager != null) {
                        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        i = -1;
                        if (activeNetworkInfo != null) {
                            i = activeNetworkInfo.getType();
                        }
                    }
                    Anzu.OnReachabilityChanged(i);
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                    context.registerReceiver(new ConnectivityBroadcastReceiver(), intentFilter);
                    Display defaultDisplay = ((WindowManager) Anzu.appContext.getSystemService("window")).getDefaultDisplay();
                    Point point = new Point();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        defaultDisplay.getRealSize(point);
                    } else {
                        defaultDisplay.getSize(point);
                    }
                    int i2 = point.x;
                    int i3 = point.y;
                    Anzu.shouldCancelWebTasks = false;
                    Anzu.sdkAndroidInit(packageName, userId, Anzu.udidSource, Anzu_setUpCacheFolder, str, networkOperatorName, Build.MANUFACTURER, Build.MODEL, Anzu_SetUpUserAgent, i2, i3, Anzu.class, AnzuVideoDecoder.class);
                    Log.println(Log.VERBOSE, "ANZU", "Done initializing native...");
                    return;
                }
                Log.e("ANZU", "No context received when calling SetContext()!");
            }
        };

        thread.start();
        try {
            thread.join(10);
        } catch (Exception e) {
            Log.e("ANZU", "Error - " + e.getLocalizedMessage());
        }
    }

    public static native float SystemMetricGet(String str);

    public static native String SystemMetricGetS(String str);

    public static native long TextureNativeRendererGetRenderCallback(String str);

    private static Bitmap captureInterstitial() {
        if (interstitialRunnerCanvas == null) {
            interstitialRunnerBitmap = Bitmap.createBitmap(interstitialRunner.getWidth(), interstitialRunner.getHeight(), Bitmap.Config.ARGB_8888);
            interstitialRunnerCanvas = new Canvas(interstitialRunnerBitmap);
        }

        interstitialRunner.draw(interstitialRunnerCanvas);
        return interstitialRunnerBitmap;
    }

    private static void cleanLogic() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Anzu.initLogicIfNeeded();
            Anzu.logicRunner.loadUrl("");
        });
    }

    private static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[0x4000];
        while (true) {
            int read = inputStream.read(bArr);
            if (read != -1) {
                outputStream.write(bArr, 0, read);
            } else {
                return;
            }
        }
    }

    private static void evalInterstitial(final String str) {
        if (interstitialRunner != null) {
            new Handler(Looper.getMainLooper()).post(() -> Anzu.interstitialRunner.eval(str));
        }
    }

    private static void evalLogic(byte[] bArr) {
        try {
            final String str = new String(bArr, StandardCharsets.UTF_8);
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    Anzu.initLogicIfNeeded();
                    Anzu.logicRunner.eval(str);
                } catch (Exception e) {
                    Log.println(Log.ERROR, "ANZU", "exception evaluating javascript (1): " + e.getLocalizedMessage());
                }
            });
        } catch (Exception e) {
            Log.println(Log.ERROR, "ANZU", "exception evaluating javascript (2): " + e.getLocalizedMessage());
        }
    }

    private static String getAdvertisingId() {
        if (advertisingId == null) {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        Anzu.advertisingId = AdvertisingIdClientInfo.getAdvertisingIdInfo(Anzu.appContext).getId();
                        if (Anzu.advertisingId != null && Anzu.advertisingId.length() > 0) {
                            Anzu.udidSource = 0;
                        }
                    } catch (Exception e) {
                        Log.println(Log.WARN, "ANZU", "Exception trying to get advertiser ID... " + e.getMessage() + ", will not use advertising ID");
                    }
                }
            };

            thread.start();
            try {
                thread.join();
            } catch (Exception e) {
                /* ~ */
            }
        }

        if (advertisingId == null) {
            advertisingId = "";
        }
        return advertisingId;
    }

    private static String getAndroidID() {
        udidSource = 2;
        return Settings.Secure.getString(appContext.getContentResolver(), TapjoyConstants.TJC_ANDROID_ID);
    }

    private static int getInterstitialHeight() {
        try {
            return interstitialRunner.getHeight();
        } catch (NullPointerException e) {
            Log.d("ANZU", "interstitialRunner was called while it's null");
            return -1;
        } catch (Exception e) {
            Log.d("ANZU", "Exception in getInterstitialHeight - " + e.getLocalizedMessage());
            return -1;
        }
    }

    private static int getInterstitialWidth() {
        try {
            return interstitialRunner.getWidth();
        } catch (NullPointerException e) {
            Log.d("ANZU", "interstitialRunner was called while it's null");
            return -1;
        } catch (Exception e) {
            Log.e("ANZU", "Exception in getInterstitialWidth - " + e.getLocalizedMessage());
            return -1;
        }
    }

    private static boolean getLocation() {
        boolean bl = false;
        try {
            boolean bl2;
            int n = appContext.checkCallingOrSelfPermission(String.valueOf(PackageManager.PERMISSION_DENIED)) == 0 ? 1 : 0;
            boolean bl3 = appContext.checkCallingOrSelfPermission(String.valueOf(PackageManager.PERMISSION_DENIED)) == 0;
            if (n == 0) {
                bl2 = bl;
                if (!bl3) return bl2;
            }
            LocationManager locationManager = (LocationManager)appContext.getSystemService("location");
            bl2 = bl;
            if (locationManager == null) return bl2;
            n = -1;
            bl2 = locationManager.isProviderEnabled("gps");
            Location location = null;
            Location location2 = bl2 ? locationManager.getLastKnownLocation("gps") : null;
            if (locationManager.isProviderEnabled("network")) {
                location = locationManager.getLastKnownLocation("network");
            }
            if (location2 != null && location != null) {
                if (location2.getAccuracy() < location.getAccuracy()) {
                    n = 0;
                } else {
                    n = 1;
                    location2 = location;
                }
            } else if (location2 == null) {
                location2 = location;
            }
            bl2 = bl;
            if (location2 == null) return bl2;
            Anzu.OnGotLocation(n, (float)location2.getLatitude(), (float)location2.getLongitude());
            return true;
        }
        catch (Exception exception) {
            Log.println((int)5, (String)"ANZU", (String)"Handled exception reading location services...");
            return bl;
        }
    }

    private static String getPreferredLanguage() {
        return Locale.getDefault().toString();
    }

    private static String getUserId() {
        String advertisingId2 = getAdvertisingId();
        if (advertisingId2.length() == 0) {
            udidSource = 3;
        }
        return advertisingId2;
    }

    private static void hideInterstitial() {
        interstitialRunner = null;
        interstitialRunnerCanvas = null;
        interstitialRunnerBitmap = null;
        interstitialIsVisible = false;
    }

    private static void httpDownload(long j, long j2, String str, String str2, String str3) {
        boolean z;
        String str4 = str;
        do {
            try {
                URLConnection openConnection = new URL(str4).openConnection();
                HttpURLConnection.setFollowRedirects(true);
                if (!str3.isEmpty()) {
                    openConnection.setRequestProperty("User-Agent" /* HTTP.USER_AGENT */, str3);
                }
                int responseCode = ((HttpURLConnection) openConnection).getResponseCode();
                if (!shouldCancelWebTasks) {
                    if (responseCode >= 200 && responseCode < 300) {
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(openConnection.getInputStream(), 16384);
                        try {
                            File file = new File(str2);
                            String absolutePath = file.getAbsolutePath();
                            String substring = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
                            File file2 = new File(substring);
                            boolean z2 = true;
                            if (!file2.exists()) {
                                z2 = true;
                                if (!file2.mkdir()) {
                                    z2 = false;
                                }
                            }
                            if (!z2) {
                                Log.println(6, "ANZU", "Can't Create Folder: " + substring);
                                httpDownloadCallback(j, j2, responseCode, "Cannot create folder: " + substring);
                            } else if (!shouldCancelWebTasks) {
                                boolean z3 = z2;
                                if (!file.exists()) {
                                    z3 = z2;
                                    if (!file.createNewFile()) {
                                        z3 = false;
                                    }
                                }
                                if (z3) {
                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                    copyStream(bufferedInputStream, fileOutputStream);
                                    fileOutputStream.close();
                                    httpDownloadCallback(j, j2, responseCode, "");
                                } else {
                                    Log.println(6, "ANZU", "Can't Create File: " + str2);
                                    httpDownloadCallback(j, j2, responseCode, "Cannot create file: " + str2);
                                }
                            } else {
                                httpDownloadCallback(j, j2, 400 /* HttpStatus.SC_BAD_REQUEST */, "operation cancelled");
                            }
                        } catch (IOException e) {
                            httpDownloadCallback(j, j2, responseCode, e.getLocalizedMessage());
                        }
                        bufferedInputStream.close();
                        z = false;
                    } else if (responseCode == 302) {
                        String headerField = openConnection.getHeaderField("Location");
                        if (!headerField.isEmpty()) {
                            str4 = headerField;
                            z = true;
                        } else {
                            httpDownloadCallback(j, j2, responseCode, "Bad http download redirect (empty address) for " + str4);
                            z = false;
                        }
                    } else {
                        httpDownloadCallback(j, j2, responseCode, "error downloading (" + responseCode + "): " + str4);
                        z = false;
                    }
                } else {
                    httpDownloadCallback(j, j2, 400 /* HttpStatus.SC_BAD_REQUEST */, "operation cancelled");
                    z = false;
                }
            } catch (Exception e2) {
                httpDownloadCallback(j, j2, 0, e2.getLocalizedMessage());
                return;
            }
        } while (z);
    }

    private static native void httpDownloadCallback(long j, long j2, int i, String str);

    private static void httpRequest(long j, long j2, String str, boolean z, String str2, String str3, String str4) {
        String str5;
        boolean z2;
        try {
            HttpResponse_t simpleHttpRequest = simpleHttpRequest(str.replace("\\/", "/"), z, str2, str3, str4);
            if (simpleHttpRequest.error != null) {
                str5 = simpleHttpRequest.error;
                z2 = false;
            } else {
                str5 = simpleHttpRequest.text;
                z2 = true;
            }
        } catch (Exception e) {
            str5 = "";
            z2 = false;
        }
        try {
            httpRequestCallback(j, j2, z2, str5);
        } catch (Exception e2) {
            httpRequestCallback(j, j2, false, e2.getLocalizedMessage());
        }
    }

    private static native void httpRequestCallback(long j, long j2, boolean z, String str);

    private static void initLogicIfNeeded() {
        if (logicRunner == null) {
            logicRunner = new AnzuWebView(appContext);
            AnzuScriptableWebInterface anzuScriptableWebInterface = new AnzuScriptableWebInterface();
            anzuScriptableWebInterface.setOnCommandListener(Anzu::logicCallback);
            Log.println(Log.WARN, "ANZU", "Initializing logic native interface...");
            logicRunner.addJavascriptInterface(anzuScriptableWebInterface, "ScriptableSDKObj");
            logicRunner.loadUrl("");
        }
    }

    private static native void interstitialCallback(String str);

    private static boolean isAppInstalled(String packageName) {
        try {
            appContext.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static boolean isConnected() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static boolean isNotificationsSupported() {
        if (appContext != null && Build.VERSION.SDK_INT >= 24) {
            try {
                Class.forName("androidx.core.app.NotificationManagerCompat");
                NotificationManagerCompat from = NotificationManagerCompat.from(appContext);
                try {
                    // TODO: Fix the warning :(
                    Method method = NotificationManagerCompat.class.getMethod("areNotificationsEnabled", null);
                    return Boolean.FALSE.equals(method.invoke(from, null));
                } catch (NoSuchMethodException | SecurityException e2) {
                    return false;
                }
            } catch (Exception | NoSuchMethodError e3) {
                return false;
            }
        }
        return false;
    }

    private static void loadInterstitial(final int i, final String str, final String str2, final int width, final int height) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (interstitialRunner == null) {
                interstitialRunner = new AnzuWebView(appContext);
                if (width > 0 && height > 0) {
                    interstitialRunner.resize(width, height);
                }

                if (i == 0) {
                    interstitialRunnerRenderToBuffer = true;
                } else {
                    interstitialRunnerRenderToBuffer = false;
                }

                setInterstitialView(interstitialRunner);
                AnzuScriptableWebInterface anzuScriptableWebInterface = new AnzuScriptableWebInterface();
                anzuScriptableWebInterface.setOnCommandListener(Anzu::interstitialCallback);
                interstitialRunner.addJavascriptInterface(anzuScriptableWebInterface, "ScriptableSDKObj");
            } else if (width > 0 && height > 0) {
                interstitialRunner.resize(width, height);
            }

            if (!str.isEmpty()) {
                interstitialRunner.loadUrl(str);
            } else {
                interstitialRunner.loadData(str2, "text/html", null);
            }

            interstitialCallback("init");
        });
    }

    public static native void logicCallback(String str);

    private static void openUrl(String str) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(parseUri(str));
            intent.setFlags(276824064); // TODO: Change the flags.
            appContext.startActivity(intent);
        } catch (Exception e) {
        }
    }

    private static Uri parseUri(String str) {
        String str2 = str;
        if (str.indexOf(58) == -1) {
            str2 = "http://" + str;
        }
        return Uri.parse(str2);
    }

    private static boolean postNotification(String title, String text) {
        NotificationCompat.Builder builder;
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(notificationChannel);
                    Log.println(Log.WARN, "ANZU", "pre notification builder(1)");
                    builder = new NotificationCompat.Builder(appContext, notificationChannel.getId());
                    Log.println(Log.WARN, "ANZU", "post notification builder (1)");
                } else {
                    Log.println(Log.WARN, "ANZU", "pre notification builder(0)");
                    builder = new NotificationCompat.Builder(appContext);
                    Log.println(Log.WARN, "ANZU", "post notification builder (0)");
                }

                builder.setContentTitle(title)
                        .setContentText(text)
                        .setDefaults(-1)
                        .setAutoCancel(true);
                notificationManager.notify(-1059169538, builder.build());
                return true;
            } catch (Exception e) {
                Log.println(Log.WARN, "ANZU", "Exception in Notification:" + e.getLocalizedMessage());
            }
        }
        return false;
    }

    private static String registryGet(String registry) {
        try {
            return sp.getString(registry, "");
        } catch (Exception e) {
            Object[] objArr = new Object[2];
            objArr[0] = registry;
            objArr[1] = e.getMessage();
            Error(String.format("Exception: Could not get key %s. Reason - %s", objArr));
        }
        return "";
    }

    private static void registrySet(String registry, String value) {
        try {
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(registry, value);
            edit.apply();
        } catch (Exception e) {
            Object[] objArr = new Object[2];
            objArr[0] = registry;
            objArr[1] = e.getMessage();
            Error(String.format("Exception: Could not set key %s. Reason - %s", objArr));
        }
    }

    private static native void sdkAndroidInit(String str, String str2, int i, String str3, String str4, String str5, String str6, String str7, String str8, int i2, int i3, Class cls, Class cls2);

    private static void setCancelWebTasks(boolean z) {
        shouldCancelWebTasks = z;
    }

    private static native void setInterstitialView(View view);

    private static void showInterstitial() {
        if (!interstitialIsVisible) {
            if (!interstitialRunnerRenderToBuffer) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent intent = new Intent(Anzu.appContext, AnzuFullscreenActivity.class);
                    intent.setFlags(276824064); // TODO: Fix the flags.
                    Anzu.appContext.startActivity(intent);
                });
            }

            interstitialIsVisible = true;
        }
    }

    private static HttpResponse_t simpleHttpRequest(String str, boolean z, String str2, String str3, String str4) throws Exception {
        String str5;
        boolean z2;
        String str6 = "";
        if (isConnected()) {
            String str7 = null;
            String str8 = "";
            String str9 = str;
            do {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str9).openConnection();
                z2 = true;
                HttpURLConnection.setFollowRedirects(true);
                if (!str4.isEmpty()) {
                    httpURLConnection.setRequestProperty("User-Agent" /* HTTP.USER_AGENT */, str4);
                }
                httpURLConnection.setUseCaches(false);
                if (z) {
                    httpURLConnection.setRequestMethod("POST" /* HttpPost.METHOD_NAME */);
                    httpURLConnection.setDoOutput(true);
                    if (str2 != null && str2.length() > 0) {
                        byte[] bytes = str2.getBytes(StandardCharsets.UTF_8);
                        if (!str3.isEmpty()) {
                            httpURLConnection.setRequestProperty("Content-Type" /* HTTP.CONTENT_TYPE */, str3);
                        }
                        httpURLConnection.setRequestProperty("Content-Length" /* HTTP.CONTENT_LEN */, "" + Integer.toString(bytes.length));
                        OutputStream outputStream = httpURLConnection.getOutputStream();
                        outputStream.write(bytes);
                        outputStream.flush();
                        outputStream.close();
                    }
                }
                try {
                    if (!shouldCancelWebTasks) {
                        int responseCode = httpURLConnection.getResponseCode();
                        if (responseCode < 200 || responseCode >= 300) {
                            String str10 = "post";
                            if (responseCode == 302) {
                                String headerField = httpURLConnection.getHeaderField("Location");
                                if (!headerField.isEmpty()) {
                                    str9 = headerField;
                                    str5 = str7;
                                } else {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("Bad http ");
                                    if (!z) {
                                        str10 = "get";
                                    }
                                    sb.append(str10);
                                    sb.append(" redirect (empty address)");
                                    str5 = sb.toString();
                                    z2 = false;
                                }
                                str6 = str8;
                            } else {
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("Bad http ");
                                if (!z) {
                                    str10 = "get";
                                }
                                sb2.append(str10);
                                sb2.append(" request: ");
                                sb2.append(Integer.toString(responseCode));
                                str5 = sb2.toString();
                                str6 = str8;
                                z2 = false;
                            }
                            httpURLConnection.disconnect();
                            str7 = str5;
                            str8 = str6;
                        } else {
                            str6 = slurp(httpURLConnection.getInputStream());
                            str5 = str7;
                            z2 = false;
                            httpURLConnection.disconnect();
                            str7 = str5;
                            str8 = str6;
                        }
                    } else {
                        throw new RuntimeException("operation cancelled...");
                    }
                } catch (Exception e) {
                    str5 = e.getLocalizedMessage();
                    z2 = false;
                    str6 = str8;
                } catch (Throwable th) {
                    httpURLConnection.disconnect();
                    throw th;
                }
            } while (z2);
        } else {
            str5 = "No Network";
        }
        return new HttpResponse_t(str6, str5);
    }

    private static String slurp(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[0x4000];
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                return byteArrayOutputStream.toString(String.valueOf(StandardCharsets.UTF_8));
            }

            byteArrayOutputStream.write(bArr, 0, read);
        }
    }
}
